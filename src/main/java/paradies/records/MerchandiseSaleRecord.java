package paradies.records;

import java.util.ArrayList;

public class MerchandiseSaleRecord extends Record {

	static final int TOTAL_FIELDS = 32;
	static final int SIZE = 103;
	
	public static final String ID = "I";
	public static final String FIELD_SKU = "SKU/UPC";
	public static final String FIELD_DEPARTMENT = "Department Number";
	public static final String FIELD_TAX1 = "Tax1";
	public static final String FIELD_TAX2 = "Tax2";
	public static final String FIELD_TAX_MODIFY_1 = "Tax Modify 1";
	public static final String FIELD_TAX_MODIFY_2 = "Tax Modify 2";
	public static final String FIELD_QUANTITY = "Quantity";
	public static final String FIELD_EXTENDED_SELLING_PRICE = "Extended Selling Price";
	public static final String FIELD_POSITIVE_FLAG = "Positive Flag";
	public static final String FIELD_EXTENDED_ORIGINAL_PRICE = "Extended Original Price";
	public static final String FIELD_COST = "Cost";
	public static final String FIELD_PRICE_OVERRIDE = "Price Override";
	public static final String FIELD_EMPLOYEE_SALE_FLAG = "Employee Sale Flag";
	public static final String FIELD_ADDITIONAL_AMOUNT = "Additional Amount";
	public static final String FIELD_PLU_FLAG = "PLU Flag";
	public static final String FIELD_DISCOUNT_FLAG = "Discount Flag";
	public static final String FIELD_PROFILE_PROMPT_FLAG = "Profile Prompt Flag";
	public static final String FIELD_TAX_3 = "Tax 3";
	public static final String FIELD_TAX_4 = "Tax 4";
	public static final String FIELD_TAX_MODIFY_3 = "Tax Modify 3";
	public static final String FIELD_TAX_MODIFY_4 = "Tax Modify 4";
	public static final String FIELD_PRICE_LEVEL_CODE = "Price Level";
	public static final String FIELD_QUANTITY_CLUSTER = "Quantity Cluster";
	public static final String FIELD_LINKED_ITEM = "Linked Item";
	public static final String FIELD_SCANNED_ITEM = "Scanned Item";
	public static final String FIELD_PRICE_OVERRIDE_REASON = "Price Override Reason";
	public static final String FIELD_PROMOTION_NUMBER = "Promotion Number";
	public static final String FIELD_MIX_MATCH_NUMBER = "Mix Match Number";
	public static final String FIELD_EXT_ORIG_PRICE_OVR = "Ext. Orig. Price with Price Ovr.";
	public static final String FIELD_MANUAL_ITEM_TAX_SET = "Manual Item Tax Set";
	public static final String FIELD_PRICE_LEVEL = "Price Level";

	public MerchandiseSaleRecord() {
		super(TOTAL_FIELDS, SIZE);
		
		Field[] createFields = {
			new Field(14, FIELD_SKU, 49, "18", Field.Type.ALPHANUMERIC),
			new Field(15, FIELD_DEPARTMENT, 67, "4", Field.Type.NUMERIC),
			new Field(16, FIELD_TAX1, 71, "1", Field.Type.NUMERIC),
			new Field(17, FIELD_TAX2, 72, "1", Field.Type.NUMERIC),
			new Field(18, FIELD_TAX_MODIFY_1, 73, "1", Field.Type.NUMERIC),
			new Field(19, FIELD_TAX_MODIFY_2, 74, "1", Field.Type.NUMERIC),
			new Field(20, FIELD_QUANTITY, 75, "4.3", Field.Type.NUMERIC),
			new Field(21, FIELD_EXTENDED_SELLING_PRICE, 82, "6.2", Field.Type.NUMERIC), // (Price after cluster/qty and price level pricing, promos, price overrides & discounts)
			new Field(22, FIELD_POSITIVE_FLAG, 90, "1", Field.Type.ALPHANUMERIC),
			new Field(23, FIELD_EXTENDED_ORIGINAL_PRICE, 91, "6.2", Field.Type.NUMERIC), // (Price before cluster/qty and price level pricing, promos, price overrides & discounts)
			new Field(24, FIELD_COST, 99, "6", Field.Type.ALPHANUMERIC),
			new Field(25, FIELD_PRICE_OVERRIDE, 105, "1", Field.Type.NUMERIC),
			new Field(26, FIELD_EMPLOYEE_SALE_FLAG, 106, "1", Field.Type.NUMERIC),
			new Field(27, FIELD_EMPLOYEE_SALE_FLAG, 107, "3.2", Field.Type.NUMERIC),
			new Field(28, FIELD_POSITIVE_FLAG, 112, "1", Field.Type.ALPHANUMERIC),
			new Field(29, FIELD_PLU_FLAG, 113, "1", Field.Type.NUMERIC),
			new Field(30, FIELD_DISCOUNT_FLAG, 114, "1", Field.Type.NUMERIC),
			new Field(31, FIELD_PROFILE_PROMPT_FLAG, 115, "1", Field.Type.NUMERIC),
			new Field(32, FIELD_TAX_3, 116, "1", Field.Type.NUMERIC),
			new Field(33, FIELD_TAX_4, 117, "1", Field.Type.NUMERIC),
			new Field(34, FIELD_TAX_MODIFY_3, 118, "1", Field.Type.NUMERIC),
			new Field(35, FIELD_TAX_MODIFY_4, 119, "1", Field.Type.NUMERIC),
			new Field(36, FIELD_PRICE_LEVEL_CODE, 120, "2", Field.Type.NUMERIC),
			new Field(37, FIELD_QUANTITY_CLUSTER, 122, "3", Field.Type.NUMERIC),
			new Field(38, FIELD_LINKED_ITEM, 125, "1", Field.Type.NUMERIC),
			new Field(39, FIELD_SCANNED_ITEM, 126, "1", Field.Type.NUMERIC),
			new Field(40, FIELD_PRICE_OVERRIDE_REASON, 127, "2", Field.Type.NUMERIC),
			new Field(41, FIELD_PROMOTION_NUMBER, 129, "3", Field.Type.NUMERIC),
			new Field(42, FIELD_MIX_MATCH_NUMBER, 132, "3", Field.Type.NUMERIC),
			new Field(43, FIELD_EXT_ORIG_PRICE_OVR, 135, "6.2", Field.Type.NUMERIC),
			new Field(44, FIELD_MANUAL_ITEM_TAX_SET, 143, "1", Field.Type.NUMERIC),
			new Field(45, FIELD_PRICE_LEVEL, 144, "6.2", Field.Type.NUMERIC) // Price after cluster/qty and price level pricing, promos and price overrides, but before discounts)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}