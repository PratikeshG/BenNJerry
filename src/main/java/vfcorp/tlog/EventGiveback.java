package vfcorp.tlog;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemAppliedDiscount;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class EventGiveback extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 73;
        id = "071";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Item Number", new FieldDetails(24, 4, "left justified, space filled"));
        fields.put("Amount", new FieldDetails(10, 28, "zero filled"));
        fields.put("Event Number", new FieldDetails(5, 38, "zero filled"));
        fields.put("Deal Number", new FieldDetails(5, 43, "zero filled"));
        fields.put("Coupon Number", new FieldDetails(24, 48, "left justified, space filled"));
        fields.put("Transaction Discount", new FieldDetails(1, 72, ""));
        fields.put("Component Type", new FieldDetails(1, 73, ""));
    }

    public EventGiveback() {
        super();
    }

    public EventGiveback(String record) {
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

    public EventGiveback parse(PaymentItemization itemization, PaymentDiscount discount, int itemNumberLookupLength,
            String promoDetails, String discountAppyType) throws Exception {
        String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
        if (sku.matches("[0-9]+")) {
            sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
        }

        int discountTotal = discount.getAppliedMoney().getAmount(); // negative value

        String eventNumber = "";
        String dealNumber = "";
        String couponNumber = "";
        if (promoDetails.length() > 6) {
            eventNumber = promoDetails.substring(0, 3);
            dealNumber = promoDetails.substring(3, 6);
            couponNumber = promoDetails.substring(6);
        }

        putValue("Item Number", sku);
        putValue("Amount", "" + -discountTotal);
        putValue("Event Number", eventNumber);
        putValue("Deal Number", dealNumber);
        putValue("Coupon Number", couponNumber);
        putValue("Transaction Discount", discountAppyType);
        putValue("Component Type", "2"); // Square doesn't offer "qualifying" actions like BOGO

        return this;
    }

    public EventGiveback parse(OrderLineItem lineItem, OrderLineItemAppliedDiscount discount, int itemNumberLookupLength,
            String promoDetails, String discountAppyType, Map<String, CatalogObject> catalog) throws Exception {
    	CatalogObject catalogObject = catalog.get(lineItem.getCatalogObjectId());
    	String sku = catalogObject != null && catalogObject.getItemVariationData() != null ?
    			catalogObject.getItemVariationData().getSku() : "";
        if (!sku.isEmpty() && sku.matches("[0-9]+")) {
            sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
        }

        int discountTotal = discount.getAppliedMoney().getAmount();

        String eventNumber = "";
        String dealNumber = "";
        String couponNumber = "";
        if (promoDetails.length() > 6) {
            eventNumber = promoDetails.substring(0, 3);
            dealNumber = promoDetails.substring(3, 6);
            couponNumber = promoDetails.substring(6);
        }

        putValue("Item Number", sku);
        putValue("Amount", "" + discountTotal);
        putValue("Event Number", eventNumber);
        putValue("Deal Number", dealNumber);
        putValue("Coupon Number", couponNumber);
        putValue("Transaction Discount", discountAppyType);
        putValue("Component Type", "2"); // Square doesn't offer "qualifying" actions like BOGO

        return this;
    }
}
