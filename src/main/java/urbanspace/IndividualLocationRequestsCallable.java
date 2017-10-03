package urbanspace;

import java.util.LinkedList;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Category;
import com.squareup.connect.Discount;
import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.SquareClient;

public class IndividualLocationRequestsCallable implements Callable {

    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        ReportGeneratorPayload sp = (ReportGeneratorPayload) message.getPayload();

        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);
        SquareClient client = new SquareClient(sp.getSquarePayload().getAccessToken(encryptionKey), apiUrl, apiVersion,
                sp.getSquarePayload().getMerchantId(), sp.getSquarePayload().getLocationId());

        // Payments
        Payment[] payments = client.payments().list(sp.getParams());
        sp.setPayments(payments);

        // Refunds
        Refund[] refunds = client.refunds().list(sp.getParams());
        sp.setRefunds(refunds);

        // Refund payment details
        if (refunds != null) {
            List<Payment> squareRefundPayments = new LinkedList<Payment>();
            for (Refund refund : refunds) {
                squareRefundPayments.add(client.payments().retrieve(refund.getPaymentId()));
            }
            sp.setRefundPayments(squareRefundPayments.toArray(new Payment[squareRefundPayments.size()]));
        }

        // Categories
        Category[] categories = client.categories().list();
        sp.setCategories(categories);

        // Discounts
        Discount[] discounts = client.discounts().list();
        sp.setDiscounts(discounts);

        return sp;
    }
}
