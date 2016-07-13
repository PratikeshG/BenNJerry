package paradies.records;

import java.util.ArrayList;

public class MerchandiseSalePartFourRecord extends Record {
	
	static final int TOTAL_FIELDS = 21;
	static final int SIZE = 93;

	public static final String ID = "I6";
	public static final String FIELD_PRICE_ORIGINAL_CURRENCY = "Price in Original Currency";
	public static final String FIELD_ORIGINAL_CURRENCY_CODE = "Original Currency Code";
	public static final String FIELD_STANDARD_UNIT_PRICE = "Standard Unit Price";
	public static final String FIELD_EXTENDED_NET_PRICE = "Extended Net Price";
	public static final String FIELD_DISCOUNT_MODIFIED_FLAG = "Discount Modified Flag";
	public static final String FIELD_DESCRIPTION_ENTERED_FLAG = "Description Entered Flag";
	public static final String FIELD_COUPON_ITEM_FLAG = "Coupon Item Flag";
	public static final String FIELD_SCALE_ITEM_FLAG= "Scale Item Flag";
	public static final String FIELD_RANDOM_WEIGHT_FLAG = "Random Weight Flag";
	public static final String FIELD_ALLOW_DECIMAL_QTY_FLAG = "Allow Decimal Qty Flag";
	public static final String FIELD_EXTENDED_COST_AMOUNT = "Extended Cost Amount";
	public static final String FIELD_COST_CODE = "Cost Code";
	public static final String FIELD_PROMO_APPLY_PRICE = "Promo Apply Price";
	public static final String FIELD_PROMO_PRICE_METHOD = "Promo Price Method";
	public static final String FIELD_PROMO_PRICE_ADJUST_AMT = "Promo Price Adjust Amount";
	public static final String FIELD_PROMO_PERCENT_ADJUST_AMT = "Promo Percent Adjust Amt";
	public static final String FIELD_CROSS_REFERENCE_FLAG = "Cross Reference Flag";
	public static final String FIELD_RETURN_ORIGINAL_SEQ_NUMBER = "Return Original records sequence number";
	public static final String FIELD_SCALE_TRANSACTION_ITEM_FLAG = "Scale Transaction Item Flag";
	public static final String FIELD_TAX_OVERRIDE_FLAG = "Tax Override Flag";
	public static final String FIELD_TAX_OVERRIDE_PERCENTAGE = "Tax Override Percentage";
	public static final String FIELD_SUGGESTED_RETAIL_PRICE = "Suggested Retail Price";

	public MerchandiseSalePartFourRecord() {
		super(TOTAL_FIELDS, SIZE);
		
		Field[] createFields = {
			new Field(14, FIELD_PRICE_ORIGINAL_CURRENCY, 49, "6.2", Field.Type.NUMERIC),
			new Field(15, FIELD_ORIGINAL_CURRENCY_CODE, 57, "3", Field.Type.ALPHANUMERIC),
			new Field(16, FIELD_STANDARD_UNIT_PRICE, 60, "6.2", Field.Type.NUMERIC),
			new Field(17, FIELD_EXTENDED_NET_PRICE, 68, "6.2", Field.Type.NUMERIC),
			new Field(18, FIELD_DISCOUNT_MODIFIED_FLAG, 76, "1", Field.Type.NUMERIC),
			new Field(19, FIELD_DESCRIPTION_ENTERED_FLAG, 77, "1", Field.Type.NUMERIC),
			new Field(20, FIELD_COUPON_ITEM_FLAG, 78, "1", Field.Type.NUMERIC),
			new Field(21, FIELD_SCALE_ITEM_FLAG, 79, "1", Field.Type.NUMERIC),
			new Field(22, FIELD_RANDOM_WEIGHT_FLAG, 80, "1", Field.Type.NUMERIC),
			new Field(23, FIELD_ALLOW_DECIMAL_QTY_FLAG, 81, "1", Field.Type.NUMERIC),
			new Field(24, FIELD_EXTENDED_COST_AMOUNT, 82, "6.2", Field.Type.NUMERIC),
			new Field(25, FIELD_COST_CODE, 90, "6", Field.Type.ALPHANUMERIC),
			new Field(26, FIELD_PROMO_APPLY_PRICE, 96, "5", Field.Type.NUMERIC),
			new Field(27, FIELD_PROMO_PRICE_METHOD, 101, "3", Field.Type.NUMERIC),
			new Field(28, FIELD_PROMO_PRICE_ADJUST_AMT, 104, "6.2", Field.Type.NUMERIC),
			new Field(29, FIELD_PROMO_PERCENT_ADJUST_AMT, 112, "6.2", Field.Type.NUMERIC),
			new Field(30, FIELD_CROSS_REFERENCE_FLAG, 120, "1", Field.Type.NUMERIC),
			new Field(31, FIELD_RETURN_ORIGINAL_SEQ_NUMBER, 121, "3", Field.Type.NUMERIC),
			new Field(32, FIELD_SCALE_TRANSACTION_ITEM_FLAG, 124, "1", Field.Type.NUMERIC),
			new Field(33, FIELD_TAX_OVERRIDE_FLAG, 125, "3", Field.Type.NUMERIC),
			new Field(34, FIELD_TAX_OVERRIDE_PERCENTAGE, 128, "7", Field.Type.NUMERIC),
			new Field(35, FIELD_SUGGESTED_RETAIL_PRICE, 134, "6.2", Field.Type.NUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}