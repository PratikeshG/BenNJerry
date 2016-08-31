package paradies.records;

import java.util.ArrayList;

public class TotalSalePartTwoRecord extends Record {

	static final int TOTAL_FIELDS = 24;
	static final int SIZE = 84;

	// TODO(bhartard): Need to update if they allow manual tax overrides
	public static final String ID = "T2";
	public static final String FIELD_TAX_5 = "Tax 5";
	public static final String FIELD_POSITIVE_FLAG_TAX_5 = "Positive flag tax 5";
	public static final String FIELD_TAX_6 = "Tax 6";
	public static final String FIELD_POSITIVE_FLAG_TAX_6 = "Positive flag tax 6";
	public static final String FIELD_TAX_7 = "Tax 7";
	public static final String FIELD_POSITIVE_FLAG_TAX_7 = "Positive flag tax 7";
	public static final String FIELD_TAX_8 = "Tax 8";
	public static final String FIELD_POSITIVE_FLAG_TAX_8 = "Positive flag tax 8";
	public static final String FIELD_TAX_9 = "Tax 9";
	public static final String FIELD_POSITIVE_FLAG_TAX_9 = "Positive flag tax 9";
	public static final String FIELD_TAX_10 = "Tax 10";
	public static final String FIELD_POSITIVE_FLAG_TAX_10 = "Positive flag tax 10";
	public static final String FIELD_TAX_11 = "Tax 11";
	public static final String FIELD_POSITIVE_FLAG_TAX_11 = "Positive flag tax 11";
	public static final String FIELD_TAX_12 = "Tax 12";
	public static final String FIELD_POSITIVE_FLAG_TAX_12 = "Positive flag tax 12";
	public static final String FIELD_TAX_13 = "Tax 13";
	public static final String FIELD_POSITIVE_FLAG_TAX_13 = "Positive flag tax 13";
	public static final String FIELD_TAX_14 = "Tax 14";
	public static final String FIELD_POSITIVE_FLAG_TAX_14 = "Positive flag tax 14";
	public static final String FIELD_TAX_15 = "Tax 15";
	public static final String FIELD_POSITIVE_FLAG_TAX_15 = "Positive flag tax 15";
	public static final String FIELD_TAX_16 = "Tax 16";
	public static final String FIELD_POSITIVE_FLAG_TAX_16 = "Positive flag tax 16";

	public TotalSalePartTwoRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_TAX_5, 49, "4.2", Field.Type.NUMERIC),
			new Field(15, FIELD_POSITIVE_FLAG_TAX_5, 55, "1", Field.Type.ALPHANUMERIC),
			new Field(16, FIELD_TAX_6, 56, "4.2", Field.Type.NUMERIC),
			new Field(17, FIELD_POSITIVE_FLAG_TAX_6, 62, "1", Field.Type.ALPHANUMERIC),
			new Field(18, FIELD_TAX_7, 63, "4.2", Field.Type.NUMERIC),
			new Field(19, FIELD_POSITIVE_FLAG_TAX_7, 69, "1", Field.Type.ALPHANUMERIC),
			new Field(20, FIELD_TAX_8, 70, "4.2", Field.Type.NUMERIC),
			new Field(21, FIELD_POSITIVE_FLAG_TAX_8, 76, "1", Field.Type.ALPHANUMERIC),
			new Field(22, FIELD_TAX_9, 77, "4.2", Field.Type.NUMERIC),
			new Field(23, FIELD_POSITIVE_FLAG_TAX_9, 83, "1", Field.Type.ALPHANUMERIC),
			new Field(24, FIELD_TAX_10, 84, "4.2", Field.Type.NUMERIC),
			new Field(25, FIELD_POSITIVE_FLAG_TAX_10, 90, "1", Field.Type.ALPHANUMERIC),
			new Field(26, FIELD_TAX_11, 91, "4.2", Field.Type.NUMERIC),
			new Field(27, FIELD_POSITIVE_FLAG_TAX_11, 97, "1", Field.Type.ALPHANUMERIC),
			new Field(28, FIELD_TAX_12, 98, "4.2", Field.Type.NUMERIC),
			new Field(29, FIELD_POSITIVE_FLAG_TAX_12, 104, "1", Field.Type.ALPHANUMERIC),
			new Field(30, FIELD_TAX_13, 105, "4.2", Field.Type.NUMERIC),
			new Field(31, FIELD_POSITIVE_FLAG_TAX_13, 111, "1", Field.Type.ALPHANUMERIC),
			new Field(32, FIELD_TAX_14, 112, "4.2", Field.Type.NUMERIC),
			new Field(33, FIELD_POSITIVE_FLAG_TAX_14, 118, "1", Field.Type.ALPHANUMERIC),
			new Field(34, FIELD_TAX_15, 119, "4.2", Field.Type.NUMERIC),
			new Field(35, FIELD_POSITIVE_FLAG_TAX_15, 125, "1", Field.Type.ALPHANUMERIC),
			new Field(36, FIELD_TAX_16, 126, "4.2", Field.Type.NUMERIC),
			new Field(37, FIELD_POSITIVE_FLAG_TAX_16, 132, "1", Field.Type.ALPHANUMERIC),
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}