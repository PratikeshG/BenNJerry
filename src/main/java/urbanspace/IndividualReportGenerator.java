package urbanspace;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.squareup.connect.Payment;
import com.squareup.connect.v2.CatalogObject;

import util.TimeManager;

public class IndividualReportGenerator {

    private String timeZone;
    private int offset;
    private int range;
    private ReportCalculator reportCalculator;

    public IndividualReportGenerator(String timeZone, int offset, int range) {
        this.timeZone = timeZone;
        this.offset = offset;
        this.range = range;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public String generate(ReportGeneratorPayload reportGeneratorPayload) throws ParseException {
        Payment[] payments = reportGeneratorPayload.getPayments();
        Payment[] refundPayments = reportGeneratorPayload.getRefundPayments();
        CatalogObject[] categories = reportGeneratorPayload.getCategories();
        CatalogObject[] discounts = reportGeneratorPayload.getDiscounts();

        reportCalculator = new ReportCalculator(range, offset, timeZone);

        IndividualLocationResult locationResult = new IndividualLocationResult();

        locationResult.setMerchantId(reportGeneratorPayload.getSquarePayload().getMerchantId());
        locationResult.setMerchantName(reportGeneratorPayload.getSquarePayload().getMerchantAlias());

        locationResult.setGiftCardSales(totalMoneyCollectedForGiftCardsByTime(payments));
        locationResult.setGiftCardSalesRefunds(totalMoneyCollectedForGiftCardsRefundsByTime(refundPayments));
        locationResult.setGiftCardSalesNet(
                addTimeMoneyMaps(locationResult.getGiftCardSales(), locationResult.getGiftCardSalesRefunds()));

        locationResult.setTotalTaxMoney(totalTaxMoneyByTime(payments));
        locationResult.setTotalTaxMoneyRefunds(totalTaxMoneyRefundsByTime(refundPayments));
        locationResult.setTotalTaxMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalTaxMoney(), locationResult.getTotalTaxMoneyRefunds()));

        locationResult.setTotalTipMoney(totalTipMoneyByTime(payments));
        locationResult.setTotalTipMoneyRefunds(totalTipMoneyRefundsByTime(refundPayments));
        locationResult.setTotalTipMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalTipMoney(), locationResult.getTotalTipMoneyRefunds()));

        // total collected + discounts - tax - tips = gross sales
        locationResult.setTotalCollectedMoney(totalTotalCollectedMoneyByTime(payments));
        locationResult.setTotalCollectedMoneyRefunds(totalTotalCollectedMoneyRefundsByTime(refundPayments));
        locationResult.setTotalCollectedMoneyNet(addTimeMoneyMaps(locationResult.getTotalCollectedMoney(),
                locationResult.getTotalCollectedMoneyRefunds()));

        locationResult.setTotalDiscountsMoney(totalDiscountMoneyByTime(payments));
        locationResult.setTotalDiscountsMoneyRefunds(totalDiscountMoneyRefundsByTime(refundPayments));
        locationResult.setTotalDiscountsMoneyNet(addTimeMoneyMaps(locationResult.getTotalDiscountsMoney(),
                locationResult.getTotalDiscountsMoneyRefunds()));

        // gross sales = net sales + discounts
        locationResult.setGrossSales(totalGrossSalesByTime(payments));
        locationResult.setGrossSalesRefunds(totalGrossSalesRefundsByTime(refundPayments));
        locationResult.setGrossSalesNet(
                addTimeMoneyMaps(locationResult.getGrossSales(), locationResult.getGrossSalesRefunds()));

        locationResult
                .setNetSales(addTimeMoneyMaps(locationResult.getGrossSales(), locationResult.getTotalDiscountsMoney()));
        locationResult.setNetSalesRefunds(addTimeMoneyMaps(locationResult.getGrossSalesRefunds(),
                locationResult.getTotalDiscountsMoneyRefunds()));
        locationResult
                .setNetSalesNet(addTimeMoneyMaps(locationResult.getNetSales(), locationResult.getNetSalesRefunds()));

        locationResult.setTotalCashMoney(totalMoneyCollectedForTenderByTime(payments, "CASH"));
        locationResult.setTotalCashMoneyRefunds(totalMoneyCollectedForTenderRefundsByTime(refundPayments, "CASH"));
        locationResult.setTotalCashMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalCashMoney(), locationResult.getTotalCashMoneyRefunds()));

        locationResult.setTotalCardMoney(totalMoneyCollectedForTenderByTime(payments, "CREDIT_CARD"));
        locationResult
                .setTotalCardMoneyRefunds(totalMoneyCollectedForTenderRefundsByTime(refundPayments, "CREDIT_CARD"));
        locationResult.setTotalCardMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalCardMoney(), locationResult.getTotalCardMoneyRefunds()));

        locationResult.setTotalGiftCardMoney(totalMoneyCollectedForTenderByTime(payments, "SQUARE_GIFT_CARD"));
        locationResult.setTotalGiftCardMoneyRefunds(
                totalMoneyCollectedForTenderRefundsByTime(refundPayments, "SQUARE_GIFT_CARD"));
        locationResult.setTotalGiftCardMoneyNet(addTimeMoneyMaps(locationResult.getTotalGiftCardMoney(),
                locationResult.getTotalGiftCardMoneyRefunds()));

        LinkedHashMap<String, Integer> intermediaryResult1 = addTimeMoneyMaps(
                totalMoneyCollectedForTenderByTime(payments, "OTHER"),
                totalMoneyCollectedForTenderByTime(payments, "THIRD_PARTY_CARD"));
        LinkedHashMap<String, Integer> intermediaryResult2 = addTimeMoneyMaps(
                totalMoneyCollectedForTenderByTime(payments, "UNKNOWN"), intermediaryResult1);
        LinkedHashMap<String, Integer> intermediaryResult3 = addTimeMoneyMaps(
                totalMoneyCollectedForTenderByTime(payments, "SQUARE_WALLET"), intermediaryResult2);
        LinkedHashMap<String, Integer> intermediaryResult4 = addTimeMoneyMaps(
                totalMoneyCollectedForTenderByTime(payments, "NO_SALE"), intermediaryResult3);
        locationResult.setTotalOtherMoney(intermediaryResult4);

        intermediaryResult1 = addTimeMoneyMaps(totalMoneyCollectedForTenderRefundsByTime(payments, "OTHER"),
                totalMoneyCollectedForTenderRefundsByTime(payments, "THIRD_PARTY_CARD"));
        intermediaryResult2 = addTimeMoneyMaps(totalMoneyCollectedForTenderRefundsByTime(payments, "UNKNOWN"),
                intermediaryResult1);
        intermediaryResult3 = addTimeMoneyMaps(totalMoneyCollectedForTenderRefundsByTime(payments, "SQUARE_WALLET"),
                intermediaryResult2);
        intermediaryResult4 = addTimeMoneyMaps(totalMoneyCollectedForTenderRefundsByTime(payments, "NO_SALE"),
                intermediaryResult3);
        locationResult.setTotalOtherMoneyRefunds(intermediaryResult4);

        locationResult.setTotalOtherMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalOtherMoney(), locationResult.getTotalOtherMoneyRefunds()));

        locationResult.setTotalPartialRefundsMoney(totalPartialRefundsByTime());
        locationResult.setTotalPartialRefundsMoneyRefunds(totalPartialRefundsRefundsByTime(refundPayments));
        locationResult.setTotalPartialRefundsMoneyNet(addTimeMoneyMaps(locationResult.getTotalPartialRefundsMoney(),
                locationResult.getTotalPartialRefundsMoneyRefunds()));

        locationResult.setTotalFeesMoney(totalProcessingFeeMoneyByTime(payments));
        locationResult.setTotalFeesMoneyRefunds(totalProcessingFeeMoneyRefundsByTime(refundPayments));
        locationResult.setTotalFeesMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalFeesMoney(), locationResult.getTotalFeesMoneyRefunds()));

        locationResult.setNetTotalMoney(
                addTimeMoneyMaps(locationResult.getTotalCollectedMoney(), locationResult.getTotalFeesMoney()));
        locationResult.setNetTotalMoneyRefunds(addTimeMoneyMaps(locationResult.getTotalCollectedMoneyRefunds(),
                locationResult.getTotalFeesMoneyRefunds()));
        locationResult.setNetTotalMoneyNet(
                addTimeMoneyMaps(locationResult.getNetTotalMoney(), locationResult.getNetTotalMoneyRefunds()));

        locationResult.setTotalNumberOfPayments(totalNumberOfTransactionsByTime(payments));
        locationResult.setTotalNumberOfRefunds(totalNumberOfTransactionsByTime(refundPayments));

        locationResult.setCategorySales(totalSalesForCategoriesByTime(payments, categories));
        locationResult.setCategorySalesRefunds(totalSalesForCategoriesRefundsByTime(payments, categories));
        locationResult.setCategorySalesNet(totalSalesForCategoriesNetByTime(locationResult.getCategorySales(),
                locationResult.getCategorySalesRefunds()));

        locationResult.setTotalsPerDiscount(totalSalesForDiscountsByTime(payments, discounts));
        locationResult.setTotalsPerDiscountRefunds(totalSalesForDiscountsRefundsByTime(payments, discounts));
        locationResult.setTotalsPerDiscountNet(totalSalesForDiscountsNetByTime(locationResult.getTotalsPerDiscount(),
                locationResult.getTotalsPerDiscountRefunds()));

        locationResult.setTotalCardSwipedMoney(totalMoneyCollectedForCardEntryMethodByTime(payments, "SWIPED"));
        locationResult
                .setTotalCardSwipedMoneyRefunds(totalMoneyCollectedForCardEntryMethodRefundsByTime(payments, "SWIPED"));
        locationResult.setTotalCardSwipedMoneyNet(addTimeMoneyMaps(locationResult.getTotalCardSwipedMoney(),
                locationResult.getTotalCardSwipedMoneyRefunds()));

        locationResult.setTotalCardKeyedMoney(totalMoneyCollectedForCardEntryMethodByTime(payments, "MANUAL"));
        locationResult
                .setTotalCardKeyedMoneyRefunds(totalMoneyCollectedForCardEntryMethodRefundsByTime(payments, "MANUAL"));
        locationResult.setTotalCardKeyedMoneyNet(addTimeMoneyMaps(locationResult.getTotalCardKeyedMoney(),
                locationResult.getTotalCardKeyedMoneyRefunds()));

        locationResult.setTotalVisaMoney(totalMoneyCollectedForCardBrandByTime(payments, "VISA"));
        locationResult.setTotalVisaMoneyRefunds(totalMoneyCollectedForCardBrandRefundsByTime(payments, "VISA"));
        locationResult.setTotalVisaMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalVisaMoney(), locationResult.getTotalVisaMoneyRefunds()));

        locationResult.setTotalMasterCardMoney(totalMoneyCollectedForCardBrandByTime(payments, "MASTER_CARD"));
        locationResult
                .setTotalMasterCardMoneyRefunds(totalMoneyCollectedForCardBrandRefundsByTime(payments, "MASTER_CARD"));
        locationResult.setTotalMasterCardMoneyNet(addTimeMoneyMaps(locationResult.getTotalMasterCardMoney(),
                locationResult.getTotalMasterCardMoneyRefunds()));

        locationResult.setTotalDiscoverMoney(totalMoneyCollectedForCardBrandByTime(payments, "DISCOVER"));
        locationResult.setTotalDiscoverMoneyRefunds(totalMoneyCollectedForCardBrandRefundsByTime(payments, "DISCOVER"));
        locationResult.setTotalDiscoverMoneyNet(addTimeMoneyMaps(locationResult.getTotalDiscoverMoney(),
                locationResult.getTotalDiscoverMoneyRefunds()));

        locationResult.setTotalAmexMoney(totalMoneyCollectedForCardBrandByTime(payments, "AMERICAN_EXPRESS"));
        locationResult
                .setTotalAmexMoneyRefunds(totalMoneyCollectedForCardBrandRefundsByTime(payments, "AMERICAN_EXPRESS"));
        locationResult.setTotalAmexMoneyNet(
                addTimeMoneyMaps(locationResult.getTotalAmexMoney(), locationResult.getTotalAmexMoneyRefunds()));

        locationResult.setTotalOtherCardMoney(
                addTimeMoneyMaps(totalMoneyCollectedForCardBrandByTime(payments, "DISCOVER_DINERS"),
                        addTimeMoneyMaps(totalMoneyCollectedForCardBrandByTime(payments, "JCB"),
                                totalMoneyCollectedForCardBrandByTime(payments, "UNKNOWN"))));
        locationResult.setTotalOtherCardMoneyRefunds(
                addTimeMoneyMaps(totalMoneyCollectedForCardBrandRefundsByTime(payments, "DISCOVER_DINERS"),
                        addTimeMoneyMaps(totalMoneyCollectedForCardBrandRefundsByTime(payments, "JCB"),
                                totalMoneyCollectedForCardBrandRefundsByTime(payments, "UNKNOWN"))));
        locationResult.setTotalOtherCardMoneyNet(addTimeMoneyMaps(locationResult.getTotalOtherCardMoney(),
                locationResult.getTotalOtherCardMoneyRefunds()));

        StringBuilder sb = new StringBuilder();
        sb.append("\"" + locationResult.getMerchantName()
                + "\",Daily Total,6am-7am,7am-8am,8am-9am,9am-10am,10am-11am,11am-12pm,12pm-1pm,1pm-2pm,2pm-3pm,3pm-4pm,4pm-5pm,5pm-6pm,6pm-7pm,7pm-8pm,8pm-9pm,9pm-10pm,10pm-11pm\n");
        sb.append("Gross Sales," + breakoutTimeTotalMoneyString(locationResult.getGrossSales()) + "\n");
        sb.append(
                "Gross Sales (Refunds)," + breakoutTimeTotalMoneyString(locationResult.getGrossSalesRefunds()) + "\n");
        sb.append("Gross Sales (Net)," + breakoutTimeTotalMoneyString(locationResult.getGrossSalesNet()) + "\n");
        sb.append("Total Discounts Money," + breakoutTimeTotalMoneyString(locationResult.getTotalDiscountsMoney())
                + "\n");
        sb.append("Total Discounts Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalDiscountsMoneyRefunds()) + "\n");
        sb.append("Total Discounts Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalDiscountsMoneyNet()) + "\n");
        sb.append("Net Sales," + breakoutTimeTotalMoneyString(locationResult.getNetSales()) + "\n");
        sb.append("Net Sales (Refunds)," + breakoutTimeTotalMoneyString(locationResult.getNetSalesRefunds()) + "\n");
        sb.append("Net Sales (Net)," + breakoutTimeTotalMoneyString(locationResult.getNetSalesNet()) + "\n");
        sb.append("Gift Card Sales," + breakoutTimeTotalMoneyString(locationResult.getGiftCardSales()) + "\n");
        sb.append("Gift Card Sales (Refunds)," + breakoutTimeTotalMoneyString(locationResult.getGiftCardSalesRefunds())
                + "\n");
        sb.append("Gift Card Sales (Net)," + breakoutTimeTotalMoneyString(locationResult.getGiftCardSalesNet()) + "\n");
        sb.append("Total Tax Money," + breakoutTimeTotalMoneyString(locationResult.getTotalTaxMoney()) + "\n");
        sb.append("Total Tax Money (Refunds)," + breakoutTimeTotalMoneyString(locationResult.getTotalTaxMoneyRefunds())
                + "\n");
        sb.append("Total Tax Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalTaxMoneyNet()) + "\n");
        sb.append("Total Tip Money," + breakoutTimeTotalMoneyString(locationResult.getTotalTipMoney()) + "\n");
        sb.append("Total Tip Money (Refunds)," + breakoutTimeTotalMoneyString(locationResult.getTotalTipMoneyRefunds())
                + "\n");
        sb.append("Total Tip Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalTipMoneyNet()) + "\n");
        sb.append("Total Partial Refunds Money,"
                + breakoutTimeTotalMoneyString(locationResult.getTotalPartialRefundsMoney()) + "\n");
        sb.append("Total Partial Refunds Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalPartialRefundsMoneyRefunds()) + "\n");
        sb.append("Total Partial Refunds Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalPartialRefundsMoneyNet()) + "\n");
        sb.append("Total Collected Money," + breakoutTimeTotalMoneyString(locationResult.getTotalCollectedMoney())
                + "\n");
        sb.append("Total Collected Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalCollectedMoneyRefunds()) + "\n");
        sb.append("Total Collected Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalCollectedMoneyNet()) + "\n");
        sb.append("Total Cash Money," + breakoutTimeTotalMoneyString(locationResult.getTotalCashMoney()) + "\n");
        sb.append("Total Cash Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalCashMoneyRefunds()) + "\n");
        sb.append(
                "Total Cash Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalCashMoneyNet()) + "\n");
        sb.append("Total Card Money," + breakoutTimeTotalMoneyString(locationResult.getTotalCardMoney()) + "\n");
        sb.append("Total Card Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalCardMoneyRefunds()) + "\n");
        sb.append(
                "Total Card Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalCardMoneyNet()) + "\n");
        sb.append(
                "Total Gift Card Money," + breakoutTimeTotalMoneyString(locationResult.getTotalGiftCardMoney()) + "\n");
        sb.append("Total Gift Card Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalGiftCardMoneyRefunds()) + "\n");
        sb.append("Total Gift Card Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalGiftCardMoneyNet()) + "\n");
        sb.append("Total Other Money," + breakoutTimeTotalMoneyString(locationResult.getTotalOtherMoney()) + "\n");
        sb.append("Total Other Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalOtherMoneyRefunds()) + "\n");
        sb.append("Total Other Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalOtherMoneyNet())
                + "\n");
        sb.append("Total Fees Money," + breakoutTimeTotalMoneyString(locationResult.getTotalFeesMoney()) + "\n");
        sb.append("Total Fees Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalFeesMoneyRefunds()) + "\n");
        sb.append(
                "Total Fees Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalFeesMoneyNet()) + "\n");
        sb.append("Net Total Money," + breakoutTimeTotalMoneyString(locationResult.getNetTotalMoney()) + "\n");
        sb.append("Net Total Money (Refunds)," + breakoutTimeTotalMoneyString(locationResult.getNetTotalMoneyRefunds())
                + "\n");
        sb.append("Net Total Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getNetTotalMoneyNet()) + "\n");

        sb.append("\nNumber of Payments," + breakoutTimeTotalString(locationResult.getTotalNumberOfPayments()) + "\n");
        sb.append("Number of Refunds," + breakoutTimeTotalString(locationResult.getTotalNumberOfRefunds()) + "\n");

        if (locationResult.getCategorySales() != null) {
            sb.append("\nCategory Sales\n");

            for (String category : locationResult.getCategorySales().keySet()) {
                LinkedHashMap<String, Integer> categorySales = locationResult.getCategorySales().get(category);
                sb.append("\"" + category + "\"," + breakoutTimeTotalMoneyString(categorySales) + "\n");

                LinkedHashMap<String, Integer> categorySalesRefunds = locationResult.getCategorySalesRefunds()
                        .get(category);
                sb.append(
                        "\"" + category + " (Refunds)\"," + breakoutTimeTotalMoneyString(categorySalesRefunds) + "\n");

                LinkedHashMap<String, Integer> categorySalesNet = locationResult.getCategorySalesNet().get(category);
                sb.append("\"" + category + " (Net)\"," + breakoutTimeTotalMoneyString(categorySalesNet) + "\n");
            }
        }

        if (locationResult.getTotalsPerDiscount() != null && locationResult.getTotalsPerDiscount().size() != 0) {
            sb.append("\nDiscounts\n");

            for (String discount : locationResult.getTotalsPerDiscount().keySet()) {
                LinkedHashMap<String, Integer> discountSales = locationResult.getTotalsPerDiscount().get(discount);
                sb.append("\"" + discount + "\"," + breakoutTimeTotalMoneyString(discountSales) + "\n");

                LinkedHashMap<String, Integer> discountSalesRefunds = locationResult.getTotalsPerDiscountRefunds()
                        .get(discount);
                sb.append(
                        "\"" + discount + " (Refunds)\"," + breakoutTimeTotalMoneyString(discountSalesRefunds) + "\n");

                LinkedHashMap<String, Integer> discountSalesNet = locationResult.getTotalsPerDiscountNet()
                        .get(discount);
                sb.append("\"" + discount + " (Net)\"," + breakoutTimeTotalMoneyString(discountSalesNet) + "\n");
            }
        }

        sb.append("\nTotal Card Swiped Money," + breakoutTimeTotalMoneyString(locationResult.getTotalCardSwipedMoney())
                + "\n");
        sb.append("Total Card Swiped Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalCardSwipedMoneyRefunds()) + "\n");
        sb.append("Total Card Swiped Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalCardSwipedMoneyNet()) + "\n");
        sb.append("\"Total Card Tapped, Dipped, or Keyed Money\","
                + breakoutTimeTotalMoneyString(locationResult.getTotalCardKeyedMoney()) + "\n");
        sb.append("\"Total Card Tapped, Dipped, or Keyed Money (Refunds)\","
                + breakoutTimeTotalMoneyString(locationResult.getTotalCardKeyedMoneyRefunds()) + "\n");
        sb.append("\"Total Card Tapped, Dipped, or Keyed Money (Net)\","
                + breakoutTimeTotalMoneyString(locationResult.getTotalCardKeyedMoneyNet()) + "\n");
        sb.append("Total Visa Money," + breakoutTimeTotalMoneyString(locationResult.getTotalVisaMoney()) + "\n");
        sb.append("Total Visa Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalVisaMoneyRefunds()) + "\n");
        sb.append(
                "Total Visa Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalVisaMoneyNet()) + "\n");
        sb.append("Total MasterCard Money," + breakoutTimeTotalMoneyString(locationResult.getTotalMasterCardMoney())
                + "\n");
        sb.append("Total MasterCard Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalMasterCardMoneyRefunds()) + "\n");
        sb.append("Total MasterCard Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalMasterCardMoneyNet()) + "\n");
        sb.append(
                "Total Discover Money," + breakoutTimeTotalMoneyString(locationResult.getTotalDiscoverMoney()) + "\n");
        sb.append("Total Discover Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalDiscoverMoneyRefunds()) + "\n");
        sb.append("Total Discover Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalDiscoverMoneyNet()) + "\n");
        sb.append("Total Amex Money," + breakoutTimeTotalMoneyString(locationResult.getTotalAmexMoney()) + "\n");
        sb.append("Total Amex Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalAmexMoneyRefunds()) + "\n");
        sb.append(
                "Total Amex Money (Net)," + breakoutTimeTotalMoneyString(locationResult.getTotalAmexMoneyNet()) + "\n");
        sb.append("Total Other Card Money," + breakoutTimeTotalMoneyString(locationResult.getTotalOtherCardMoney())
                + "\n");
        sb.append("Total Other Card Money (Refunds),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalOtherCardMoneyRefunds()) + "\n");
        sb.append("Total Other Card Money (Net),"
                + breakoutTimeTotalMoneyString(locationResult.getTotalOtherCardMoneyNet()) + "\n");

        return sb.toString();
    }

    private LinkedHashMap<String, Integer> addTimeMoneyMaps(LinkedHashMap<String, Integer> moneyMap1,
            LinkedHashMap<String, Integer> moneyMap2) {
        if (moneyMap1 == null) {
            return moneyMap2;
        } else if (moneyMap2 == null) {
            return moneyMap1;
        }

        LinkedHashMap<String, Integer> addedMap = new LinkedHashMap<String, Integer>();

        for (String time : moneyMap1.keySet()) {
            addedMap.put(time, moneyMap1.get(time) + moneyMap2.get(time));
        }

        return addedMap;
    }

    // This function ONLY MAKES SENSE FOR ONE DAY OF PAYMENTS.
    private LinkedHashMap<String, List<Payment>> splitPaymentsByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = new LinkedHashMap<String, List<Payment>>();

        paymentsSplitByTime.put("sixAm", new LinkedList<Payment>());
        paymentsSplitByTime.put("sevenAm", new LinkedList<Payment>());
        paymentsSplitByTime.put("eightAm", new LinkedList<Payment>());
        paymentsSplitByTime.put("nineAm", new LinkedList<Payment>());
        paymentsSplitByTime.put("tenAm", new LinkedList<Payment>());
        paymentsSplitByTime.put("elevenAm", new LinkedList<Payment>());
        paymentsSplitByTime.put("twelvePm", new LinkedList<Payment>());
        paymentsSplitByTime.put("onePm", new LinkedList<Payment>());
        paymentsSplitByTime.put("twoPm", new LinkedList<Payment>());
        paymentsSplitByTime.put("threePm", new LinkedList<Payment>());
        paymentsSplitByTime.put("fourPm", new LinkedList<Payment>());
        paymentsSplitByTime.put("fivePm", new LinkedList<Payment>());
        paymentsSplitByTime.put("sixPm", new LinkedList<Payment>());
        paymentsSplitByTime.put("sevenPm", new LinkedList<Payment>());
        paymentsSplitByTime.put("eightPm", new LinkedList<Payment>());
        paymentsSplitByTime.put("ninePm", new LinkedList<Payment>());
        paymentsSplitByTime.put("tenPm", new LinkedList<Payment>());

        // Starts at 6am-7am; goes to 10pm-11pm
        for (Payment payment : payments) {
            Calendar c = TimeManager.toCalendar(payment.getCreatedAt());
            c.setTimeZone(TimeZone.getTimeZone(timeZone));

            if (c.get(Calendar.HOUR_OF_DAY) == 6) {
                paymentsSplitByTime.get("sixAm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 7) {
                paymentsSplitByTime.get("sevenAm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 8) {
                paymentsSplitByTime.get("eightAm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 9) {
                paymentsSplitByTime.get("nineAm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 10) {
                paymentsSplitByTime.get("tenAm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 11) {
                paymentsSplitByTime.get("elevenAm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 12) {
                paymentsSplitByTime.get("twelvePm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 13) {
                paymentsSplitByTime.get("onePm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 14) {
                paymentsSplitByTime.get("twoPm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 15) {
                paymentsSplitByTime.get("threePm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 16) {
                paymentsSplitByTime.get("fourPm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 17) {
                paymentsSplitByTime.get("fivePm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 18) {
                paymentsSplitByTime.get("sixPm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 19) {
                paymentsSplitByTime.get("sevenPm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 20) {
                paymentsSplitByTime.get("eightPm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 21) {
                paymentsSplitByTime.get("ninePm").add(payment);
            } else if (c.get(Calendar.HOUR_OF_DAY) == 22) {
                paymentsSplitByTime.get("tenPm").add(payment);
            }
        }

        return paymentsSplitByTime;
    }

    private String breakoutTimeTotalMoneyString(LinkedHashMap<String, Integer> totalsByTime) {
        StringBuilder sb = new StringBuilder();

        if (totalsByTime != null) {
            int total = 0;
            for (Integer integer : totalsByTime.values()) {
                total += integer;
            }

            sb.append(centsToDollars(total) + ",");

            for (Integer integer : totalsByTime.values()) {
                sb.append(centsToDollars(integer) + ",");
            }

            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private String breakoutTimeTotalString(LinkedHashMap<String, Integer> totalsByTime) {
        StringBuilder sb = new StringBuilder();

        if (totalsByTime != null) {
            int total = 0;
            for (Integer integer : totalsByTime.values()) {
                total += integer;
            }

            sb.append(total + ",");

            for (Integer integer : totalsByTime.values()) {
                sb.append(integer + ",");
            }

            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private String centsToDollars(int cents) {
        // Why Canada? Because it displays negative currencies as "-$XX.YY".
        // US displays negative currencies as "($XX.YY)".
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.CANADA);
        return "\"" + n.format(cents / 100.0) + "\"";
    }

    private LinkedHashMap<String, Integer> totalGrossSalesByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalGrossSalesByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalGrossSalesByTime.put(key, reportCalculator
                    .totalGrossSales(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalGrossSalesByTime;
    }

    private LinkedHashMap<String, Integer> totalGrossSalesRefundsByTime(Payment[] refundPayments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalGrossSalesRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalGrossSalesRefundsByTime.put(key, reportCalculator.totalGrossSalesRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalGrossSalesRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForGiftCardsByTime(Payment[] payments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForGiftCardsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForGiftCardsByTime.put(key, reportCalculator.totalMoneyCollectedForGiftCards(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalMoneyCollectedForGiftCardsByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForGiftCardsRefundsByTime(Payment[] refundPayments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForGiftCardsRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForGiftCardsRefundsByTime.put(key,
                    reportCalculator.totalMoneyCollectedForGiftCardsRefunds(
                            paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalMoneyCollectedForGiftCardsRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalTaxMoneyByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalTaxMoneyByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalTaxMoneyByTime.put(key, reportCalculator
                    .totalTaxMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalTaxMoneyByTime;
    }

    private LinkedHashMap<String, Integer> totalTaxMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalTaxMoneyRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalTaxMoneyRefundsByTime.put(key, reportCalculator
                    .totalTaxMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalTaxMoneyRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalTipMoneyByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalTipMoneyByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalTipMoneyByTime.put(key, reportCalculator
                    .totalTipMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalTipMoneyByTime;
    }

    private LinkedHashMap<String, Integer> totalTipMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalTipMoneyRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalTipMoneyRefundsByTime.put(key, reportCalculator
                    .totalTipMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalTipMoneyRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalTotalCollectedMoneyByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalTotalCollectedMoneyByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalTotalCollectedMoneyByTime.put(key, reportCalculator.totalTotalCollectedMoney(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalTotalCollectedMoneyByTime;
    }

    private LinkedHashMap<String, Integer> totalTotalCollectedMoneyRefundsByTime(Payment[] refundPayments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalTotalCollectedMoneyRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalTotalCollectedMoneyRefundsByTime.put(key, reportCalculator.totalTotalCollectedMoneyRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalTotalCollectedMoneyRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalDiscountMoneyByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalDiscountedMoneyByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalDiscountedMoneyByTime.put(key, reportCalculator
                    .totalDiscountMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalDiscountedMoneyByTime;
    }

    private LinkedHashMap<String, Integer> totalDiscountMoneyRefundsByTime(Payment[] refundPayments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalDiscountMoneyRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalDiscountMoneyRefundsByTime.put(key, reportCalculator.totalDiscountMoneyRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalDiscountMoneyRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForTenderByTime(Payment[] payments, String tender)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForTenderByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForTenderByTime.put(key, reportCalculator.totalMoneyCollectedForTender(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), tender));
        }

        return totalMoneyCollectedForTenderByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForTenderRefundsByTime(Payment[] refundPayments,
            String tender) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForTenderRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForTenderRefundsByTime.put(key, reportCalculator.totalMoneyCollectedForTenderRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), tender));
        }

        return totalMoneyCollectedForTenderRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalPartialRefundsByTime() throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(new Payment[] {});

        LinkedHashMap<String, Integer> totalPartialRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            totalPartialRefundsByTime.put(key, 0);
        }

        return totalPartialRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalPartialRefundsRefundsByTime(Payment[] refundPayments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalPartialRefundsRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalPartialRefundsRefundsByTime.put(key, reportCalculator.totalPartialRefundsRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalPartialRefundsRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalProcessingFeeMoneyByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalProcessingFeeMoneyByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalProcessingFeeMoneyByTime.put(key, reportCalculator.totalProcessingFeeMoney(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalProcessingFeeMoneyByTime;
    }

    private LinkedHashMap<String, Integer> totalProcessingFeeMoneyRefundsByTime(Payment[] refundPayments)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalProcessingFeeMoneyByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalProcessingFeeMoneyByTime.put(key, reportCalculator.totalProcessingFeeMoneyRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
        }

        return totalProcessingFeeMoneyByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForCardEntryMethodByTime(Payment[] payments,
            String method) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForCardEntryMethodByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);

            totalMoneyCollectedForCardEntryMethodByTime.put(key, reportCalculator.totalMoneyCollectedForCardEntryMethod(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), method));
        }

        return totalMoneyCollectedForCardEntryMethodByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForCardEntryMethodRefundsByTime(Payment[] refundPayments,
            String method) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForCardEntryMethodRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForCardEntryMethodRefundsByTime.put(key,
                    reportCalculator.totalMoneyCollectedForCardEntryMethodRefunds(
                            paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), method));
        }

        return totalMoneyCollectedForCardEntryMethodRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForCardBrandByTime(Payment[] refundPayments, String brand)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForCardBrandByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForCardBrandByTime.put(key, reportCalculator.totalMoneyCollectedForCardBrand(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), brand));
        }

        return totalMoneyCollectedForCardBrandByTime;
    }

    private LinkedHashMap<String, Integer> totalMoneyCollectedForCardBrandRefundsByTime(Payment[] refundPayments,
            String brand) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);

        LinkedHashMap<String, Integer> totalMoneyCollectedForCardBrandRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalMoneyCollectedForCardBrandRefundsByTime.put(key,
                    reportCalculator.totalMoneyCollectedForCardBrandRefunds(
                            paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), brand));
        }

        return totalMoneyCollectedForCardBrandRefundsByTime;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForCategoriesByTime(Payment[] payments,
            CatalogObject[] categories) throws ParseException {
        LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForCategoriesByTime = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        for (CatalogObject category : categories) {
            totalSalesForCategoriesByTime.put(category.getCategoryData().getName(),
                    totalSalesForCategoryByTime(payments, category.getCategoryData().getName()));
        }

        totalSalesForCategoriesByTime.put("No category", totalSalesForCategoryByTime(payments, ""));

        return totalSalesForCategoriesByTime;
    }

    private LinkedHashMap<String, Integer> totalSalesForCategoryByTime(Payment[] payments, String category)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalSalesForCategoryByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalSalesForCategoryByTime.put(key, reportCalculator.totalMoneyCollectedForCategory(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), category));
        }

        return totalSalesForCategoryByTime;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForCategoriesRefundsByTime(
            Payment[] payments, CatalogObject[] categories) throws ParseException {
        LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForCategoriesRefundsByTime = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        for (CatalogObject category : categories) {
            totalSalesForCategoriesRefundsByTime.put(category.getCategoryData().getName(),
                    totalSalesForCategoryRefundsByTime(payments, category.getCategoryData().getName()));
        }

        totalSalesForCategoriesRefundsByTime.put("No category", totalSalesForCategoryRefundsByTime(payments, ""));

        return totalSalesForCategoriesRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalSalesForCategoryRefundsByTime(Payment[] payments, String category)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalSalesForCategoryRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalSalesForCategoryRefundsByTime.put(key, reportCalculator.totalMoneyCollectedForCategoryRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), category));
        }

        return totalSalesForCategoryRefundsByTime;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForCategoriesNetByTime(
            LinkedHashMap<String, LinkedHashMap<String, Integer>> categorySales,
            LinkedHashMap<String, LinkedHashMap<String, Integer>> categorySalesRefunds) throws ParseException {
        LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForCategoriesNetByTime = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        for (String category : categorySales.keySet()) {
            LinkedHashMap<String, Integer> categorySalesMap = categorySales.get(category);
            LinkedHashMap<String, Integer> categorySalesRefundsMap = categorySalesRefunds.get(category);
            totalSalesForCategoriesNetByTime.put(category, addTimeMoneyMaps(categorySalesMap, categorySalesRefundsMap));
        }

        LinkedHashMap<String, Integer> categorySalesNoCategory = categorySales.get("No category");
        LinkedHashMap<String, Integer> categorySalesRefundsNoCategory = categorySalesRefunds.get("No category");
        totalSalesForCategoriesNetByTime.put("No category",
                addTimeMoneyMaps(categorySalesNoCategory, categorySalesRefundsNoCategory));

        return totalSalesForCategoriesNetByTime;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForDiscountsByTime(Payment[] payments,
            CatalogObject[] discounts) throws ParseException {
        LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForDiscountByTime = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        for (CatalogObject discount : discounts) {
            totalSalesForDiscountByTime.put(discount.getDiscountData().getName(),
                    totalSalesForDiscountByTime(payments, discount.getDiscountData().getName()));
        }

        return totalSalesForDiscountByTime;
    }

    private LinkedHashMap<String, Integer> totalSalesForDiscountByTime(Payment[] payments, String discount)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalSalesForCategoryByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalSalesForCategoryByTime.put(key, reportCalculator.totalMoneyCollectedForDiscount(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), discount));
        }

        return totalSalesForCategoryByTime;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForDiscountsRefundsByTime(
            Payment[] payments, CatalogObject[] discounts) throws ParseException {
        LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForDiscountsRefundsByTime = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        for (CatalogObject discount : discounts) {
            totalSalesForDiscountsRefundsByTime.put(discount.getDiscountData().getName(),
                    totalSalesForDiscountRefundsByTime(payments, discount.getDiscountData().getName()));
        }

        return totalSalesForDiscountsRefundsByTime;
    }

    private LinkedHashMap<String, Integer> totalSalesForDiscountRefundsByTime(Payment[] payments, String discount)
            throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalSalesForDiscountRefundsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalSalesForDiscountRefundsByTime.put(key, reportCalculator.totalMoneyCollectedForDiscountRefunds(
                    paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), discount));
        }

        return totalSalesForDiscountRefundsByTime;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForDiscountsNetByTime(
            LinkedHashMap<String, LinkedHashMap<String, Integer>> discountSales,
            LinkedHashMap<String, LinkedHashMap<String, Integer>> discountSalesRefunds) throws ParseException {
        LinkedHashMap<String, LinkedHashMap<String, Integer>> totalSalesForDiscountsNetByTime = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

        for (String discount : discountSales.keySet()) {
            LinkedHashMap<String, Integer> discountSalesMap = discountSales.get(discount);
            LinkedHashMap<String, Integer> discountSalesRefundsMap = discountSalesRefunds.get(discount);
            totalSalesForDiscountsNetByTime.put(discount, addTimeMoneyMaps(discountSalesMap, discountSalesRefundsMap));
        }

        return totalSalesForDiscountsNetByTime;
    }

    private LinkedHashMap<String, Integer> totalNumberOfTransactionsByTime(Payment[] payments) throws ParseException {
        LinkedHashMap<String, List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);

        LinkedHashMap<String, Integer> totalNumberOfTransactionsByTime = new LinkedHashMap<String, Integer>();

        for (String key : paymentsSplitByTime.keySet()) {
            List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
            totalNumberOfTransactionsByTime.put(key, paymentsInTimeInterval.size());
        }

        return totalNumberOfTransactionsByTime;
    }
}
