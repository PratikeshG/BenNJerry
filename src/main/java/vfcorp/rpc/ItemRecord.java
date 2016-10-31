package vfcorp.rpc;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class ItemRecord extends Record {

    public static final String ACTION_TYPE_ADD = "1";
    public static final String ACTION_TYPE_DELETE = "2";
    public static final String ACTION_TYPE_CHANGE_RECORD = "3";
    public static final String ACTION_TYPE_PLACE_ON_SALE = "4";
    public static final String ACTION_TYPE_CHANGE_FIELD = "5";

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 446;
        id = "01";

        fields.put("Record Type", new FieldDetails(2, 1, "zero filled, Always 01"));
        fields.put("Action Type", new FieldDetails(1, 3, "zero filled"));
        fields.put("Item Number", new FieldDetails(24, 4, "space filled"));
        fields.put("Department Number", new FieldDetails(4, 28, "space filled"));
        fields.put("Class Number", new FieldDetails(4, 32, "space filled"));
        fields.put("Luxury Tax Code", new FieldDetails(25, 36, "space filled, Left justified"));
        fields.put("Style Number", new FieldDetails(24, 61, "space filled, Left justified"));
        fields.put("Tax Rate Code (VAT Tax Code)", new FieldDetails(25, 85, "space filled"));
        fields.put("Item Type", new FieldDetails(5, 110, "zero filled"));
        fields.put("Item Cost", new FieldDetails(10, 115, "zero filled"));
        fields.put("Activate Date", new FieldDetails(8, 125, "space filled, MMDDYYYY"));
        fields.put("Deactivate Date", new FieldDetails(8, 133, "space filled, MMDDYYYY"));
        fields.put("Description", new FieldDetails(24, 141, "space filled"));
        fields.put("Taxable Indicator", new FieldDetails(1, 165, "zero filled"));
        fields.put("Retail Price", new FieldDetails(10, 166, "zero filled"));
        fields.put("Original Price", new FieldDetails(10, 176, "zero filled"));
        fields.put("Comparative Price", new FieldDetails(10, 186, "zero filled"));
        fields.put("Package Price", new FieldDetails(10, 196, "zero filled"));
        fields.put("Lowest Price Last X Days", new FieldDetails(10, 206, "zero filled"));
        fields.put("Sale Price", new FieldDetails(10, 216, "zero filled"));
        fields.put("Date Sale/Action Begins", new FieldDetails(8, 226, "space filled, MMDDYYYY"));
        fields.put("Date Sale/Action Ends", new FieldDetails(8, 234, "space filled, MMDDYYYY"));
        fields.put("Time Sale Begins", new FieldDetails(8, 242, "space filled, hh:mm:ss (Military time)"));
        fields.put("Time Sale Ends", new FieldDetails(8, 250, "space filled, hh:mm:ss (Military time)"));
        fields.put("Employee Discount Code", new FieldDetails(2, 258, "space filled"));
        fields.put("Max Discount Percent", new FieldDetails(5, 260, "zero filled"));
        fields.put("Sales Tax Override", new FieldDetails(1, 265, "zero filled"));
        fields.put("Incentive Indicator", new FieldDetails(1, 266, "zero filled"));
        fields.put("Suggested Sell Indicator", new FieldDetails(1, 267, "zero filled"));
        fields.put("Fee Indicator", new FieldDetails(1, 268, "zero filled"));
        fields.put("Not in Use", new FieldDetails(24, 269, "space filled"));
        fields.put("Not in Use", new FieldDetails(5, 293, "space filled"));
        fields.put("Validate When Sold Indicator", new FieldDetails(1, 298, "zero filled"));
        fields.put("Raincheck Available", new FieldDetails(1, 299, "zero filled"));
        fields.put("Not Discountable by Item", new FieldDetails(1, 300, "zero filled"));
        fields.put("Not Discountable by Transaction", new FieldDetails(1, 301, "zero filled"));
        fields.put("Available in Layaway Sale", new FieldDetails(1, 302, "zero filled"));
        fields.put("Available in Special Order Sale", new FieldDetails(1, 303, "zero filled"));
        fields.put("Available in Send Sale", new FieldDetails(1, 304, "zero filled"));
        fields.put("Additional Input Indicator", new FieldDetails(1, 305, "zero filled"));
        fields.put("Not in Use", new FieldDetails(10, 306, "space filled"));
        fields.put("Not in Use", new FieldDetails(5, 316, "space filled"));
        fields.put("Minimum Quantity Indicator", new FieldDetails(1, 321, "zero filled"));
        fields.put("Minimum Quantity", new FieldDetails(10, 322, "zero filled"));
        fields.put("Maximum Quantity Indicator", new FieldDetails(1, 332, "zero filled"));
        fields.put("Maximum Quantity", new FieldDetails(10, 333, "zero filled"));
        fields.put("Item Lock Quantity", new FieldDetails(10, 343, "zero filled"));
        fields.put("Quantity on Hand", new FieldDetails(10, 353, "zero filled"));
        fields.put("Quantity on Order", new FieldDetails(10, 363, "zero filled"));
        fields.put("Wardrobe Indicator", new FieldDetails(1, 373, "zero filled"));
        fields.put("Form Type", new FieldDetails(2, 374, "zero filled, 0 if not used"));
        fields.put("Not in Use", new FieldDetails(8, 376, "zero filled"));
        fields.put("Ship Fee Amount", new FieldDetails(10, 384, "zero filled"));
        fields.put("Pickup", new FieldDetails(1, 394, "zero filled"));
        fields.put("Available in Package", new FieldDetails(1, 395, "zero filled, 1 = Available"));
        fields.put("Send Carrier Code", new FieldDetails(8, 396, "zero filled"));
        fields.put("Not Currently Used", new FieldDetails(4, 404, "space filled"));
        fields.put("Ship From DC Only Indicator", new FieldDetails(1, 408, "zero filled, 1 = Yes"));
        fields.put("Weight of Item", new FieldDetails(10, 409, "zero filled"));
        fields.put("Tax Rate Code 2", new FieldDetails(25, 419, "space filled, Left justified"));
        fields.put("Taxable Indicator 2", new FieldDetails(1, 444, "zero filled"));
        fields.put("Return Indicator",
                new FieldDetails(1, 445, "zero filled, 1 = Can be returned, 0 = Cannot be returned"));
        fields.put("Fractional Quantity Indicator",
                new FieldDetails(1, 446, "zero filled, 1 = True, 0 = False (default)"));
    }

    public ItemRecord() {
        super();
    }

    public ItemRecord(String record) {
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
}
