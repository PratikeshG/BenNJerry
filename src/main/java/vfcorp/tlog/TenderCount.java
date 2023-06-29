package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Order;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class TenderCount extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 41;
        id = "034";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Tender Code", new FieldDetails(8, 4, "From table, left justified"));
        fields.put("Number In Drawer", new FieldDetails(6, 12, "zero filled"));
        fields.put("Amount In Drawer", new FieldDetails(10, 18, "zero filled"));
        fields.put("Amount In Drawer Sign", new FieldDetails(1, 28, "1 = Negative, zero filled"));
        fields.put("Amount Counted", new FieldDetails(10, 29, "zero filled"));
        fields.put("Amount Counted Sign", new FieldDetails(1, 39, "1 = Negative, zero filled"));
        fields.put("Currency Indicator", new FieldDetails(1, 40, "1 = Alternate, 0 = Primary, zero filled"));
        fields.put("Counted Indicator", new FieldDetails(1, 41, "1 = Dollar, 0 = Quantity, zero filled"));
    }

    public TenderCount() {
        super();
    }

    public TenderCount(String record) {
        super(record);
    }

    @Override
    public Map<String, FieldDetails> getFields() {
        return fields;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getId() {
        return id;
    }

    public TenderCount parse(String tenderCode, List<com.squareup.connect.Payment> squarePaymentsList, String deployment) throws Exception {
        int number = 0;
        int amount = 0;

        for (com.squareup.connect.Payment squarePayment : squarePaymentsList) {
            for (com.squareup.connect.Tender tender : squarePayment.getTender()) {
                if (deployment.contains("vans") || deployment.contains("test")) {
                    if ((Tender.TENDER_CODE_CASH.equals(tenderCode) && tender.getType().equals("CASH"))
                            || (Tender.TENDER_CODE_VANS_CARD.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD"))
                            || (Tender.TENDER_CODE_GIFT_CERTIFICATE.equals(tenderCode)
                                    && tender.getType().equals("OTHER")
                                    && "MERCHANT_GIFT_CARD".equals(tender.getName()))
                            || (Tender.TENDER_CODE_98.equals(tenderCode) && tender.getType().equals("UNKNOWN"))) {

                        number += 1;
                        if (tender.getTenderedMoney() != null) {
                            amount += tender.getTenderedMoney().getAmount();
                        } else {
                            amount += tender.getTotalMoney().getAmount();
                        }
                    }
                } else {
                    if ((Tender.TENDER_CODE_CASH.equals(tenderCode) && tender.getType().equals("CASH"))
                            || (Tender.TENDER_CODE_AMEX.equals(tenderCode) && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("AMERICAN_EXPRESS"))
                            || (Tender.TENDER_CODE_AMEX_BETA.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("AMERICAN_EXPRESS"))
                            || (Tender.TENDER_CODE_DISCOVER.equals(tenderCode) && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("DISCOVER"))
                            || (Tender.TENDER_CODE_DISCOVER_BETA.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("DISCOVER"))
                            || (Tender.TENDER_CODE_VISA.equals(tenderCode) && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("VISA"))
                            || (Tender.TENDER_CODE_VISA_BETA.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD") && tender.getCardBrand().equals("VISA"))
                            || (Tender.TENDER_CODE_MASTERCARD.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("MASTER_CARD"))
                            || (Tender.TENDER_CODE_MASTERCARD_BETA.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("MASTER_CARD"))
                            || (Tender.TENDER_CODE_JCB.equals(tenderCode) && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("JCB"))
                            || (Tender.TENDER_CODE_JCB_BETA.equals(tenderCode) && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("JCB"))
                            || (Tender.TENDER_CODE_DEBIT_BETA.equals(tenderCode)
                                    && tender.getType().equals("CREDIT_CARD")
                                    && tender.getCardBrand().equals("OTHER_BRAND"))
                            || (Tender.TENDER_CODE_GIFT_CERTIFICATE.equals(tenderCode)
                                    && tender.getType().equals("OTHER")
                                    && "MERCHANT_GIFT_CARD".equals(tender.getName()))
                            || (Tender.TENDER_CODE_98.equals(tenderCode) && tender.getType().equals("UNKNOWN"))) {

                        number += 1;
                        if (tender.getTenderedMoney() != null) {
                            amount += tender.getTenderedMoney().getAmount();
                        } else {
                            amount += tender.getTotalMoney().getAmount();
                        }
                    }
                }
            }
        }

        putValue("Tender Code", tenderCode);
        putValue("Number In Drawer", "" + number);
        putValue("Amount In Drawer", "" + amount);
        putValue("Amount In Drawer Sign", amount >= 0 ? "0" : "1");
        putValue("Amount Counted", "" + amount);
        putValue("Amount Counted Sign", amount >= 0 ? "0" : "1");
        putValue("Currency Indicator", "0"); // not supported
        putValue("Counted Indicator", "1"); // 1 is "dollars"

        return this;
    }

    public TenderCount parse(String tenderCode, List<Order> squareOrdersList, Map<String, Payment> tenderToPayment, String deployment) throws Exception {
        int number = 0;
        int amount = 0;

        for (Order order : squareOrdersList) {
        	if(order.getTenders() != null) {
        		for (com.squareup.connect.v2.Tender tender : order.getTenders()) {
                	Payment payment = tenderToPayment.get(tender.getId());
                	if(payment != null) {
                		if (deployment.contains("vans") || deployment.contains("test")) {
                            if ((Tender.TENDER_CODE_CASH.equals(tenderCode) && tender.getType().equals(com.squareup.connect.v2.Tender.TENDER_TYPE_CASH))
                                    || (Tender.TENDER_CODE_VANS_CARD.equals(tenderCode)
                                            && tender.getType().equals(com.squareup.connect.v2.Tender.TENDER_TYPE_CARD))
                                    || (Tender.TENDER_CODE_GIFT_CERTIFICATE.equals(tenderCode)
                                            && tender.getType().equals("OTHER")
                                            && "MERCHANT_GIFT_CARD".equals(payment.getSourceType()))
                                    || (Tender.TENDER_CODE_98.equals(tenderCode) && tender.getType().equals("UNKNOWN"))) {

                                number += 1;
                                if (payment.getCashDetails() != null) {
                                    amount += payment.getCashDetails().getBuyerSuppliedMoney().getAmount();
                                } else {
                                    amount += payment.getTotalMoney().getAmount();
                                }
                            }
                        } else {
                        	String cardBrand = payment.getCardDetails() != null && payment.getCardDetails().getCard() != null ?
                        			payment.getCardDetails().getCard().getCardBrand() : "";
                            if ((Tender.TENDER_CODE_CASH.equals(tenderCode) && tender.getType().equals("CASH"))
                                    || (Tender.TENDER_CODE_AMEX.equals(tenderCode) && tender.getType().equals("CARD")
                                            && cardBrand.equals("AMERICAN_EXPRESS"))
                                    || (Tender.TENDER_CODE_AMEX_BETA.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("AMERICAN_EXPRESS"))
                                    || (Tender.TENDER_CODE_DISCOVER.equals(tenderCode) && tender.getType().equals("CARD")
                                            && cardBrand.equals("DISCOVER"))
                                    || (Tender.TENDER_CODE_DISCOVER_BETA.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("DISCOVER"))
                                    || (Tender.TENDER_CODE_VISA.equals(tenderCode) && tender.getType().equals("CARD")
                                            && cardBrand.equals("VISA"))
                                    || (Tender.TENDER_CODE_VISA_BETA.equals(tenderCode)
                                            && tender.getType().equals("CARD") && cardBrand.equals("VISA"))
                                    || (Tender.TENDER_CODE_MASTERCARD.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("MASTER_CARD"))
                                    || (Tender.TENDER_CODE_MASTERCARD_BETA.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("MASTER_CARD"))
                                    || (Tender.TENDER_CODE_MASTERCARD.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("MASTERCARD"))
                                    || (Tender.TENDER_CODE_MASTERCARD_BETA.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("MASTERCARD"))
                                    || (Tender.TENDER_CODE_JCB.equals(tenderCode) && tender.getType().equals("CARD")
                                            && cardBrand.equals("JCB"))
                                    || (Tender.TENDER_CODE_JCB_BETA.equals(tenderCode) && tender.getType().equals("CARD")
                                            && cardBrand.equals("JCB"))
                                    || (Tender.TENDER_CODE_DEBIT_BETA.equals(tenderCode)
                                            && tender.getType().equals("CARD")
                                            && cardBrand.equals("OTHER_BRAND"))
                                    || (Tender.TENDER_CODE_GIFT_CERTIFICATE.equals(tenderCode)
                                            && tender.getType().equals("OTHER")
                                            && "MERCHANT_GIFT_CARD".equals(payment.getSourceType()))
                                    || (Tender.TENDER_CODE_98.equals(tenderCode) && tender.getType().equals("UNKNOWN"))) {

                                number += 1;
                                if (payment.getCashDetails() != null) {
                                    amount += payment.getCashDetails().getBuyerSuppliedMoney().getAmount();
                                } else {
                                    amount += payment.getTotalMoney().getAmount();
                                }
                            }
                        }
                	}
                }
        	}
        }

        putValue("Tender Code", tenderCode);
        putValue("Number In Drawer", "" + number);
        putValue("Amount In Drawer", "" + amount);
        putValue("Amount In Drawer Sign", amount >= 0 ? "0" : "1");
        putValue("Amount Counted", "" + amount);
        putValue("Amount Counted Sign", amount >= 0 ? "0" : "1");
        putValue("Currency Indicator", "0"); // not supported
        putValue("Counted Indicator", "1"); // 1 is "dollars"

        return this;
    }
}
