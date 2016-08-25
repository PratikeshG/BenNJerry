package paradies.records;

import java.util.ArrayList;

public class DiscountRecord extends Record {

	static final int TOTAL_FIELDS = 32;
	static final int SIZE = 105;

	public static final String ID = "D";
	public static final String FIELD_DISCOUNT_TYPE = "Discount Type";
	public static final String FIELD_REASON = "Reason";
	public static final String FIELD_DISCOUNT_PERCENTAGE = "Discount Percentage";
	public static final String FIELD_EXTENDED_DISCOUNT_AMT = "Extended Discount Amt";
	public static final String FIELD_POSITIVE_FLAG = "Positive Flag";
	public static final String FIELD_REFERENCE_NUMBER = "Reference number";
	public static final String FIELD_ORIGINAL_PRICE = "Original Price";
	public static final String FIELD_DISCOUNT_ID = "Discount ID";
	public static final String FIELD_TAX_TABLE_1 = "Tax Table 1";
	public static final String FIELD_TAX_TABLE_2 = "Tax Table 2";
	public static final String FIELD_TAX_TABLE_3 = "Tax Table 3";
	public static final String FIELD_TAX_TABLE_4 = "Tax Table 4";
	public static final String FIELD_AUTO_PRESET_DISCOUNT = "Auto Preset Discount";
	public static final String FIELD_AFFECT_NET_SALES = "Affect Net Sales";
	public static final String FIELD_PROFILE_PROMPT_FLAG = "Profile Prompt Flag";
	public static final String FIELD_SCANNED_REFERENCE_NUM = "Scanned Reference Num";
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
	public static final String FIELD_PROMO_AMOUNT = "Promo Amount";
	public static final String FIELD_ALLEGIANCE_ID = "Allegiance ID";
	public static final String FIELD_IEC_CODE = "IEC Code";
	public static final String FIELD_CHOICE_TYPE = "Choice Type";
	public static final String FIELD_REASON_CODE = "Reason Code";
	public static final String FIELD_PROFILE_ID = "Profile ID";
	public static final String FIELD_DISCOUNT_QUANTITY = "Discount Quantity";
	public static final String FIELD_USING_PRESET_FLAG = "Using Preset Flag";
	public static final String FIELD_EMPLOYEE_SALE_FLAG = "Employee Sale Flag";
	public static final String FIELD_TXN_DISCOUNT_ON_ITEM_FLAG = "Txn. Discount on Item Discount Flag";
	public static final String FIELD_TXN_DISCOUNT_ON_TXN_FLAG = "Txn. Discount on Txn. Discount Flag";
	public static final String FIELD_CODED_COUPON_FLAG = "Coded Coupon Flag";
	public static final String FIELD_AUTO_DISCOUNT_FLAG = "Auto Discount Flag";	

	public DiscountRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_DISCOUNT_TYPE, 49, "2", Field.Type.NUMERIC),
			new Field(15, FIELD_REASON, 51, "2", Field.Type.NUMERIC),
			new Field(16, FIELD_DISCOUNT_PERCENTAGE, 53, "3.3", Field.Type.NUMERIC),
			new Field(17, FIELD_EXTENDED_DISCOUNT_AMT, 59, "5.2", Field.Type.NUMERIC),
			new Field(18, FIELD_POSITIVE_FLAG, 66, "1", Field.Type.ALPHANUMERIC),
			new Field(19, FIELD_REFERENCE_NUMBER, 67, "20", Field.Type.ALPHANUMERIC),
			new Field(20, FIELD_ORIGINAL_PRICE, 87, "6.2", Field.Type.NUMERIC),
			new Field(21, FIELD_POSITIVE_FLAG, 95, "1", Field.Type.ALPHANUMERIC),
			new Field(22, FIELD_DISCOUNT_ID, 96, "2", Field.Type.NUMERIC),
			new Field(23, FIELD_TAX_TABLE_1, 98, "1", Field.Type.NUMERIC),
			new Field(24, FIELD_TAX_TABLE_2, 99, "1", Field.Type.NUMERIC),
			new Field(25, FIELD_TAX_TABLE_3, 100, "1", Field.Type.NUMERIC),
			new Field(26, FIELD_TAX_TABLE_4, 101, "1", Field.Type.NUMERIC),
			new Field(27, FIELD_AUTO_PRESET_DISCOUNT, 102, "1", Field.Type.NUMERIC),
			new Field(28, FIELD_AFFECT_NET_SALES, 103, "1", Field.Type.NUMERIC),
			new Field(29, FIELD_PROFILE_PROMPT_FLAG, 104, "1", Field.Type.NUMERIC),
			new Field(30, FIELD_SCANNED_REFERENCE_NUM, 105, "1", Field.Type.NUMERIC),
			new Field(31, FIELD_TAX_5, 106, "1", Field.Type.NUMERIC),
			new Field(32, FIELD_TAX_6, 107, "1", Field.Type.NUMERIC),
			new Field(33, FIELD_TAX_7, 108, "1", Field.Type.NUMERIC),
			new Field(34, FIELD_TAX_8, 109, "1", Field.Type.NUMERIC),
			new Field(35, FIELD_TAX_9, 110, "1", Field.Type.NUMERIC),
			new Field(36, FIELD_TAX_10, 111, "1", Field.Type.NUMERIC),
			new Field(37, FIELD_TAX_11, 112, "1", Field.Type.NUMERIC),
			new Field(38, FIELD_TAX_12, 113, "1", Field.Type.NUMERIC),
			new Field(39, FIELD_TAX_13, 114, "1", Field.Type.NUMERIC),
			new Field(40, FIELD_TAX_14, 115, "1", Field.Type.NUMERIC),
			new Field(41, FIELD_TAX_15, 116, "1", Field.Type.NUMERIC),
			new Field(42, FIELD_TAX_16, 117, "1", Field.Type.NUMERIC),
			new Field(43, FIELD_PROMO_AMOUNT, 118, "4.2", Field.Type.NUMERIC),
			new Field(44, FIELD_ALLEGIANCE_ID, 124, "10", Field.Type.NUMERIC),
			new Field(45, FIELD_IEC_CODE, 134, "20", Field.Type.ALPHANUMERIC),
			new Field(46, FIELD_CHOICE_TYPE, 154, "3", Field.Type.NUMERIC),
			new Field(47, FIELD_REASON_CODE, 157, "3", Field.Type.NUMERIC),
			new Field(48, FIELD_PROFILE_ID, 160, "3", Field.Type.NUMERIC),
			new Field(49, FIELD_DISCOUNT_QUANTITY, 163, "7", Field.Type.NUMERIC),
			new Field(50, FIELD_USING_PRESET_FLAG, 170, "1", Field.Type.NUMERIC),
			new Field(51, FIELD_EMPLOYEE_SALE_FLAG, 171, "1", Field.Type.NUMERIC),
			new Field(52, FIELD_TXN_DISCOUNT_ON_ITEM_FLAG, 172, "1", Field.Type.NUMERIC),
			new Field(53, FIELD_TXN_DISCOUNT_ON_TXN_FLAG, 173, "1", Field.Type.NUMERIC),
			new Field(54, FIELD_CODED_COUPON_FLAG, 174, "1", Field.Type.NUMERIC),
			new Field(55, FIELD_AUTO_DISCOUNT_FLAG, 175, "1", Field.Type.NUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}