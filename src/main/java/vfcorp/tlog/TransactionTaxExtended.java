package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItemTax;

import vfcorp.FieldDetails;
import vfcorp.Record;
import vfcorp.Util;

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
        fields.put("Tax Method", new FieldDetails(2, 6, "zero filled"));
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

    public TransactionTaxExtended parse(Order order, OrderLineItemTax tax) throws Exception {
        String taxType = "01";
        String taxMethod = "01";
        String taxCode = "";
        switch (tax.getName()) {
            case "Sales Tax":
                taxType = "01";
                break;
            case "VAT":
                taxType = "02";
                break;
            case "GST":
            case "Goods and Services Tax":
                taxType = "03";
                break;
            case "GST/PST":
            case "Provincial Sales Tax":
                taxType = "04";
                taxCode = "PST";
                break;
            case "PST ON GST":
                taxType = "05";
                break;
            case "No tax":
                taxType = "07";
                break;
            case "Youth Item Tax":
                taxCode = "2";
            case "HST":
            case "Harmonized Sales Tax":
                taxType = "08";
                break;
        }

        long taxRate = Math.round(Double.parseDouble(tax.getPercentage()) * 10000000);
        int netAmounts = order.getNetAmounts() != null && order.getNetAmounts().getTotalMoney() != null ? order.getNetAmounts().getTotalMoney().getAmount() : 0;
        int totalTaxMoney = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
        int taxableAmount = netAmounts - totalTaxMoney;

        // for special taxes
        String taxDetails = Util.getValueInParenthesis(tax.getName());
        if (taxDetails.length() > 2) {
            taxMethod = taxDetails.substring(0, 2);
            taxCode = taxDetails.substring(2);
        }

        putValue("Tax Type", taxType);
        putValue("Tax Method", taxMethod);
        putValue("Tax Rate", "" + taxRate);
        putValue("Tax Code", taxCode);
        putValue("Taxable Amount", "" + taxableAmount);
        putValue("Tax", "" + tax.getAppliedMoney().getAmount());
        // TODO(): needs to be refactored for refunds
        putValue("Sign Indicator", "0");

        return this;
    }
}
