package paradies.records;

import java.util.ArrayList;

public class MerchandiseSalePartTwoRecord extends Record {
	
	static final int TOTAL_FIELDS = 39;
	static final int SIZE = 114;

	public static final String ID = "I2";
	public static final String FIELD_EXTENDED_REGULAR_PRICE = "Extended Regular Price";
	public static final String FIELD_EXTENDED_PREPROMO_PRICE = "Extended Pre-Promo Price";
	public static final String FIELD_EXTENDED_NET_PRICE = "Extended Net Price";
	public static final String FIELD_POSITIVE_FLAG = "Positive Flag";
	public static final String FIELD_TAX_5 = "Tax 5";
	public static final String FIELD_TAX_6 = "Tax 6";
	public static final String FIELD_TAX_7 = "Tax 7";
	public static final String FIELD_TAX_8 = "Tax 8";
	public static final String FIELD_TAX_9 = "Tax 9";
	public static final String FIELD_TAX_10 = "Tax 10";
	public static final String FIELD_TAX_11 = "Tax 11";
	public static final String FIELD_TAX_12 = "Tax 12";
	public static final String FIELD_TAX_13 = "Tax 13";
	public static final String FIELD_TAX_14 = "Tax 14";
	public static final String FIELD_TAX_15 = "Tax 15";
	public static final String FIELD_TAX_16 = "Tax 16";
	public static final String FIELD_TAX_MODIFY_5 = "Tax Modify 5";
	public static final String FIELD_TAX_MODIFY_6 = "Tax Modify 6";
	public static final String FIELD_TAX_MODIFY_7 = "Tax Modify 7";
	public static final String FIELD_TAX_MODIFY_8 = "Tax Modify 8";
	public static final String FIELD_TAX_MODIFY_9 = "Tax Modify 9";
	public static final String FIELD_TAX_MODIFY_10 = "Tax Modify 10";
	public static final String FIELD_TAX_MODIFY_11 = "Tax Modify 11";
	public static final String FIELD_TAX_MODIFY_12 = "Tax Modify 12";
	public static final String FIELD_TAX_MODIFY_13 = "Tax Modify 13";
	public static final String FIELD_TAX_MODIFY_14 = "Tax Modify 14";
	public static final String FIELD_TAX_MODIFY_15 = "Tax Modify 15";
	public static final String FIELD_TAX_MODIFY_16 = "Tax Modify 16";
	public static final String FIELD_LOYALTY_PROGRAM_1 = "Loyalty Program 1";
	public static final String FIELD_LOYALTY_PROGRAM_2 = "Loyalty Program 2";
	public static final String FIELD_LOYALTY_PROGRAM_3 = "Loyalty Program 3";
	public static final String FIELD_LOYALTY_PROGRAM_4 = "Loyalty Program 4";
	public static final String FIELD_PROMOTION_NUMBER =  "Promotion Number";
	public static final String FIELD_PROMO_DETAIL_ID =  "Promo Detail ID";
	public static final String FIELD_ORIGINAL_PRICE =  "Original Price";
	public static final String FIELD_PROMOTION_APPLY_PRICE_METHOD =  "Promotion Apply Price Method";
	public static final String FIELD_INVENTORY_GROUP_ID =  "Inventory Group ID";
	public static final String FIELD_UNIT_SIZE =  "Unit Size";
	public static final String FIELD_EXTENDED_TAXABLE_AMOUNT =  "Extended Taxable Amount";

	public MerchandiseSalePartTwoRecord() {
		super(TOTAL_FIELDS, SIZE);
		
		Field[] createFields = {
			new Field(14, FIELD_EXTENDED_REGULAR_PRICE, 49, "6.2", Field.Type.NUMERIC), // Price after cluster/qty and price level pricing, and promos, but before price overrides and discounts)
			new Field(15, FIELD_EXTENDED_PREPROMO_PRICE, 57, "6.2", Field.Type.NUMERIC), // (Price after cluster/qty and price level pricing, but before promos, price overrides & discounts)
			new Field(16, FIELD_EXTENDED_NET_PRICE, 65, "6.2", Field.Type.NUMERIC), // (Price after cluster/qty and price level pricing, promos, price overrides and also after discounts which are configured to affect net sales only) [Not yet implemented - will be set to the value of ‘Extended Selling Price’ in the record type I]
			new Field(17, FIELD_POSITIVE_FLAG, 73, "1", Field.Type.ALPHANUMERIC),
			new Field(18, FIELD_TAX_5, 74, "1", Field.Type.NUMERIC),
			new Field(19, FIELD_TAX_6, 75, "1", Field.Type.NUMERIC),
			new Field(20, FIELD_TAX_7, 76, "1", Field.Type.NUMERIC),
			new Field(21, FIELD_TAX_8, 77, "1", Field.Type.NUMERIC),
			new Field(22, FIELD_TAX_9, 78, "1", Field.Type.NUMERIC),
			new Field(23, FIELD_TAX_10, 79, "1", Field.Type.NUMERIC),
			new Field(24, FIELD_TAX_11, 80, "1", Field.Type.NUMERIC),
			new Field(25, FIELD_TAX_12, 81, "1", Field.Type.NUMERIC),
			new Field(26, FIELD_TAX_13, 82, "1", Field.Type.NUMERIC),
			new Field(27, FIELD_TAX_14, 83, "1", Field.Type.NUMERIC),
			new Field(28, FIELD_TAX_15, 84, "1", Field.Type.NUMERIC),
			new Field(29, FIELD_TAX_16, 85, "1", Field.Type.NUMERIC),
			new Field(30, FIELD_TAX_MODIFY_5, 86, "1", Field.Type.NUMERIC),
			new Field(31, FIELD_TAX_MODIFY_6, 87, "1", Field.Type.NUMERIC),
			new Field(32, FIELD_TAX_MODIFY_7, 88, "1", Field.Type.NUMERIC),
			new Field(33, FIELD_TAX_MODIFY_8, 89, "1", Field.Type.NUMERIC),
			new Field(34, FIELD_TAX_MODIFY_9, 90, "1", Field.Type.NUMERIC),
			new Field(35, FIELD_TAX_MODIFY_10, 91, "1", Field.Type.NUMERIC),
			new Field(36, FIELD_TAX_MODIFY_11, 92, "1", Field.Type.NUMERIC),
			new Field(37, FIELD_TAX_MODIFY_12, 93, "1", Field.Type.NUMERIC),
			new Field(38, FIELD_TAX_MODIFY_13, 94, "1", Field.Type.NUMERIC),
			new Field(39, FIELD_TAX_MODIFY_14, 95, "1", Field.Type.NUMERIC),
			new Field(40, FIELD_TAX_MODIFY_15, 96, "1", Field.Type.NUMERIC),
			new Field(41, FIELD_TAX_MODIFY_16, 97, "1", Field.Type.NUMERIC),
			new Field(42, FIELD_LOYALTY_PROGRAM_1, 98, "2", Field.Type.NUMERIC),
			new Field(43, FIELD_LOYALTY_PROGRAM_2, 100, "2", Field.Type.NUMERIC),
			new Field(44, FIELD_LOYALTY_PROGRAM_3, 102, "2", Field.Type.NUMERIC),
			new Field(45, FIELD_LOYALTY_PROGRAM_4, 104, "2", Field.Type.NUMERIC),
			new Field(46, FIELD_PROMOTION_NUMBER, 106, "5", Field.Type.NUMERIC),
			new Field(47, FIELD_PROMO_DETAIL_ID, 111, "8", Field.Type.NUMERIC),
			new Field(48, FIELD_ORIGINAL_PRICE, 119, "6.2", Field.Type.NUMERIC),
			new Field(49, FIELD_PROMOTION_APPLY_PRICE_METHOD, 127, "4", Field.Type.NUMERIC),
			new Field(50, FIELD_INVENTORY_GROUP_ID, 131, "18", Field.Type.NUMERIC),
			new Field(51, FIELD_UNIT_SIZE, 149, "6", Field.Type.NUMERIC),
			new Field(52, FIELD_EXTENDED_TAXABLE_AMOUNT, 155, "6.2", Field.Type.NUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}