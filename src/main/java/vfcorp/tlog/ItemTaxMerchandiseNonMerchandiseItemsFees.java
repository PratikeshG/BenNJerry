package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;

import vfcorp.FieldDetails;
import vfcorp.Record;
import vfcorp.Util;

public class ItemTaxMerchandiseNonMerchandiseItemsFees extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 84;
        id = "025";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Tax Type", new FieldDetails(2, 4, ""));
        fields.put("Tax Method", new FieldDetails(2, 6, ""));
        fields.put("Tax Rate", new FieldDetails(7, 8, "9(2)v99999, zero filled"));
        fields.put("Tax Amount", new FieldDetails(10, 15, "zero filled"));
        fields.put("Tax Override Code", new FieldDetails(25, 25, ""));
        fields.put("Taxable Amount", new FieldDetails(10, 50, "zero filled"));
        fields.put("Tax Code", new FieldDetails(25, 60, "left justified"));
    }

    public ItemTaxMerchandiseNonMerchandiseItemsFees() {
        super();
    }

    public ItemTaxMerchandiseNonMerchandiseItemsFees(String record) {
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

    public ItemTaxMerchandiseNonMerchandiseItemsFees parse(PaymentTax tax, PaymentItemization itemization)
            throws Exception {
        String taxType = "01";
        String taxMethod = "01";
        String taxCode = "1";
        switch (tax.getName()) {
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
            case "Sales Tax":
            default:
                taxType = "01";
                break;
        }
        long taxRate = Math.round(Double.parseDouble(tax.getRate()) * 10000000);

        // for special taxes
        String taxDetails = Util.getValueInParenthesis(tax.getName());
        if (taxDetails.length() > 2) {
            taxMethod = taxDetails.substring(0, 2);
            taxCode = taxDetails.substring(2);
        }

        putValue("Tax Type", taxType);
        putValue("Tax Method", taxMethod);
        putValue("Tax Rate", "" + taxRate);
        putValue("Tax Amount", ""); // not supported
        putValue("Tax Override Code", ""); // not supported
        putValue("Taxable Amount", "" + itemization.getNetSalesMoney().getAmount());
        putValue("Tax Code", taxCode);

        return this;
    }
}
