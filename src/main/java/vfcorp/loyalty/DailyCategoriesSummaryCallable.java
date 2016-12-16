package vfcorp.loyalty;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;

import util.TimeManager;

public class DailyCategoriesSummaryCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<LocationTransactionDetails> transactionDetailsByLocation = (List<LocationTransactionDetails>) message
                .getProperty("transactionDetailsByLocation", PropertyScope.INVOCATION);

        StringBuilder builder = new StringBuilder();
        builder.append("\"Store\",\"Category\",\"Items Sold\",\"Gross Sales\",\"Discounts\",\"Net Sales\"\n");

        for (LocationTransactionDetails locationTransactionDetails : transactionDetailsByLocation) {
            // Cache all PaymentItemizations for a given category
            HashMap<String, List<PaymentItemization>> categoryPaymentItemizationCache = new HashMap<String, List<PaymentItemization>>();

            for (Payment payment : locationTransactionDetails.getPayments()) {
                for (PaymentItemization paymentItemization : payment.getItemizations()) {
                    String categoryName = "Uncategorized";

                    if (paymentItemization.getItemDetail() != null
                            && paymentItemization.getItemDetail().getCategoryName() != null
                            && paymentItemization.getItemDetail().getCategoryName().length() > 0) {
                        categoryName = paymentItemization.getItemDetail().getCategoryName();
                    }

                    List<PaymentItemization> itemizationCache = categoryPaymentItemizationCache.get(categoryName);
                    if (itemizationCache == null) {
                        itemizationCache = new ArrayList<PaymentItemization>();
                        categoryPaymentItemizationCache.put(categoryName, itemizationCache);
                    }

                    itemizationCache.add(paymentItemization);
                }
            }

            for (String categoryName : categoryPaymentItemizationCache.keySet()) {
                List<PaymentItemization> paymentItemizations = categoryPaymentItemizationCache.get(categoryName);

                builder.append(String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"\n",
                        locationTransactionDetails.getLocation().getName(), categoryName,
                        totalItemsSoldInCategory(paymentItemizations), grossSalesInCategory(paymentItemizations),
                        discountsInCategory(paymentItemizations), netSalesInCategory(paymentItemizations)));
            }
        }

        // Calculate day of report generated
        String timezone = "America/Los_Angeles";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(cal, timezone), timezone,
                "yyyy-MM-dd");

        DataHandler dataHandler = new DataHandler(builder.toString(), "text/plain; charset=UTF-8");
        eventContext.getMessage().addOutboundAttachment(currentDate + "-category-summary.csv", dataHandler);

        // empty return
        return "See attachment.";

    }

    private int totalItemsSoldInCategory(List<PaymentItemization> paymentItemizations) {
        int total = 0;

        for (PaymentItemization paymentItemization : paymentItemizations) {
            total += paymentItemization.getQuantity().intValue();
        }

        return total;
    }

    private String grossSalesInCategory(List<PaymentItemization> paymentItemizations) {
        int gross = 0;

        for (PaymentItemization paymentItemization : paymentItemizations) {
            gross += paymentItemization.getGrossSalesMoney().getAmount();
        }

        return formatTotal(gross);
    }

    private String discountsInCategory(List<PaymentItemization> paymentItemizations) {
        int discounts = 0;

        for (PaymentItemization paymentItemization : paymentItemizations) {
            discounts += paymentItemization.getDiscountMoney().getAmount();
        }

        return formatTotal(discounts);
    }

    private String netSalesInCategory(List<PaymentItemization> paymentItemizations) {
        int net = 0;

        for (PaymentItemization paymentItemization : paymentItemizations) {
            net += paymentItemization.getNetSalesMoney().getAmount();
        }

        return formatTotal(net);
    }

    private String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0);
    }
}
