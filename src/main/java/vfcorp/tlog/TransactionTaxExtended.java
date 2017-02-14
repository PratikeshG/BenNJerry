package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentTax;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class TransactionTaxExtended extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 60;
        id = "054";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Tax Type", new FieldDetails(2, 4, ""));
        fields.put("Tax Method", new FieldDetails(2, 6, ""));
        fields.put("Tax Code", new FieldDetails(25, 8, "Left justified, space filled"));
        fields.put("Tax Rate", new FieldDetails(7, 33, "zero filled"));
        fields.put("Taxable Amount", new FieldDetails(10, 40, "zero filled"));
        fields.put("Tax", new FieldDetails(10, 50, "zero filled"));
        fields.put("Sign Indicator", new FieldDetails(1, 60, ""));
    }

    public TransactionTaxExtended() {
        super();
    }

    public TransactionTaxExtended(String record) {
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

    public TransactionTaxExtended parse(Payment payment, PaymentTax tax) throws Exception {
        String taxType = "01";
        String taxCode = "";
        switch (tax.getName()) {
            case "Sales Tax":
                taxType = "01";
                break;
            case "VAT":
                taxType = "02";
                break;
            case "GST":
                taxType = "03";
                break;
            case "GST/PST":
                taxType = "04";
                taxCode = "PST";
                break;
            case "PST ON GST":
                taxType = "05";
                break;
            case "No tax":
                taxType = "07";
                break;
            case "HST":
                taxType = "08";
                break;
        }

        long taxRate = Math.round(Double.parseDouble(tax.getRate()) * 10000000);

        int taxableAmount = payment.getTotalCollectedMoney().getAmount() - payment.getTaxMoney().getAmount();

        putValue("Tax Type", taxType);
        putValue("Tax Method", "01"); // not supported
        putValue("Tax Rate", "" + taxRate);
        putValue("Tax Code", taxCode);
        putValue("Taxable Amount", "" + taxableAmount); // TODO(colinlam): this doesn't take into account what kind of tax it is
        putValue("Tax", "" + tax.getAppliedMoney().getAmount());
        // TODO(): needs to be refactored for refunds
        putValue("Sign Indicator", "0");

        return this;
    }
}
