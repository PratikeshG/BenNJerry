package paradies.records;

import java.util.ArrayList;

/*
 * The Payment or Tender record types are used to denote a variety of tender options,
 * including not only cash and checks, but also gift cards, stored value cards, and 
 * so forth. All tender types are user defined.
 */
public class TotalSaleRecord extends Record {
	
	static final int TOTAL_FIELDS = 23;
	static final int SIZE = 80;

	// TODO(bhartard): Need to update if they allow manual tax overrides
	public static final String ID = "T";
	public static final String FIELD_TOTAL_SALE_NO_TAX = "Total Sale w/o Tax";
	public static final String FIELD_POSITIVE_FLAG_TOTAL = "Positive flag total";
	public static final String FIELD_TAX_1 = "Tax 1";
	public static final String FIELD_POSITIVE_FLAG_TAX_1 = "Positive flag tax 1";
	public static final String FIELD_MANUAL_TAX_FLAG = "Manual Tax flag";
	public static final String FIELD_MANUAL_TAX_PERCENT = "Manual Tax %";
	public static final String FIELD_TAX_2 = "Tax 2";
	public static final String FIELD_POSITIVE_FLAG_TAX_2 = "Positive flag tax 2";
	public static final String FIELD_TAX_3 = "Tax 3";
	public static final String FIELD_POSITIVE_FLAG_TAX_3 = "Positive flag tax 3";
	public static final String FIELD_TAX_4 = "Tax 4";
	public static final String FIELD_POSITIVE_FLAG_TAX_4 = "Positive flag tax 4";
	public static final String FIELD_T2_FLAG = "T2 Record Follows";
	public static final String FIELD_MANUAL_ITEM_TAX_TOTALS = "Manual Item Tax Totals";
	public static final String FIELD_POSITIVE_FLAG_TAX_TOTALS = "Positive flag tax totals";
	public static final String FIELD_TOTAL_SAVED = "Total Saved";
	public static final String FIELD_POSITIVE_FLAG_TOTAL_SAVED = "Positive flag total saved";

	public TotalSaleRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_TOTAL_SALE_NO_TAX, 49, "6.2", Field.Type.NUMERIC),
			new Field(15, FIELD_POSITIVE_FLAG_TOTAL, 57, "1", Field.Type.ALPHANUMERIC),
			new Field(16, FIELD_TAX_1, 58, "4.2", Field.Type.NUMERIC),
			new Field(17, FIELD_POSITIVE_FLAG_TAX_1, 64, "1", Field.Type.ALPHANUMERIC),
			new Field(18, FIELD_MANUAL_TAX_FLAG, 65, "1", Field.Type.NUMERIC),
			new Field(19, FIELD_MANUAL_TAX_PERCENT, 66, "2.3", Field.Type.NUMERIC),
			new Field(20, FIELD_TAX_2, 71, "4.2", Field.Type.NUMERIC),
			new Field(21, FIELD_POSITIVE_FLAG_TAX_2, 77, "1", Field.Type.ALPHANUMERIC),
			new Field(22, FIELD_MANUAL_TAX_FLAG, 78, "1", Field.Type.NUMERIC),
			new Field(23, FIELD_MANUAL_TAX_PERCENT, 79, "2.3", Field.Type.NUMERIC),
			new Field(24, FIELD_TAX_3, 84, "4.2", Field.Type.NUMERIC),
			new Field(25, FIELD_POSITIVE_FLAG_TAX_3, 90, "1", Field.Type.ALPHANUMERIC),
			new Field(26, FIELD_MANUAL_TAX_FLAG, 91, "1", Field.Type.NUMERIC),
			new Field(27, FIELD_MANUAL_TAX_PERCENT, 92, "2.3", Field.Type.NUMERIC),
			new Field(28, FIELD_TAX_4, 97, "4.2", Field.Type.NUMERIC),
			new Field(29, FIELD_POSITIVE_FLAG_TAX_4, 103, "1", Field.Type.ALPHANUMERIC),
			new Field(30, FIELD_MANUAL_TAX_FLAG, 104, "1", Field.Type.NUMERIC),
			new Field(31, FIELD_MANUAL_TAX_PERCENT, 105, "2.3", Field.Type.NUMERIC),
			new Field(32, FIELD_T2_FLAG, 110, "1", Field.Type.NUMERIC),
			new Field(33, FIELD_MANUAL_ITEM_TAX_TOTALS, 111, "8", Field.Type.NUMERIC),
			new Field(34, FIELD_POSITIVE_FLAG_TAX_TOTALS, 119, "1", Field.Type.ALPHANUMERIC),
			new Field(35, FIELD_TOTAL_SAVED, 120, "6.2", Field.Type.NUMERIC),
			new Field(36, FIELD_POSITIVE_FLAG_TOTAL_SAVED, 128, "1", Field.Type.ALPHANUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}