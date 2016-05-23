package paradies.records;

import java.util.ArrayList;

public class DiscountRecord extends Record {

	static final int TOTAL_FIELDS = 32;
	static final int SIZE = 105;
	public static final String ID = "D";

	public DiscountRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, "Discount Type", 49, "2", Field.Type.NUMERIC),
			new Field(15, "Reason", 51, "2", Field.Type.NUMERIC),
			new Field(16, "Discount Percentage", 53, "3.3", Field.Type.NUMERIC),
			new Field(17, "Extended Discount Amt", 59, "5.2", Field.Type.NUMERIC),
			new Field(18, "Positive Flag", 66, "1", Field.Type.ALPHANUMERIC),
			new Field(19, "Reference number", 67, "20", Field.Type.ALPHANUMERIC),
			new Field(20, "Original Price", 87, "6.2", Field.Type.NUMERIC),
			new Field(21, "Positive Flag", 95, "1", Field.Type.ALPHANUMERIC),
			new Field(22, "Discount ID", 96, "2", Field.Type.NUMERIC),
			new Field(23, "Tax Table 1", 98, "1", Field.Type.NUMERIC),
			new Field(24, "Tax Table 2", 99, "1", Field.Type.NUMERIC),
			new Field(25, "Tax Table 3", 100, "1", Field.Type.NUMERIC),
			new Field(26, "Tax Table 4", 101, "1", Field.Type.NUMERIC),
			new Field(27, "Auto Preset Discount", 102, "1", Field.Type.NUMERIC),
			new Field(28, "Affect Net Sales", 103, "1", Field.Type.NUMERIC),
			new Field(29, "Profile Prompt Flag", 104, "1", Field.Type.NUMERIC),
			new Field(30, "Scanned Reference Num", 105, "1", Field.Type.NUMERIC),
			new Field(31, "Tax 5", 106, "1", Field.Type.NUMERIC),
			new Field(32, "Tax 6", 107, "1", Field.Type.NUMERIC),
			new Field(33, "Tax 7", 108, "1", Field.Type.NUMERIC),
			new Field(34, "Tax 8", 109, "1", Field.Type.NUMERIC),
			new Field(35, "Tax 9", 110, "1", Field.Type.NUMERIC),
			new Field(36, "Tax 10", 111, "1", Field.Type.NUMERIC),
			new Field(37, "Tax 11", 112, "1", Field.Type.NUMERIC),
			new Field(38, "Tax 12", 113, "1", Field.Type.NUMERIC),
			new Field(39, "Tax 13", 114, "1", Field.Type.NUMERIC),
			new Field(40, "Tax 14", 115, "1", Field.Type.NUMERIC),
			new Field(41, "Tax 15", 116, "1", Field.Type.NUMERIC),
			new Field(42, "Tax 16", 117, "1", Field.Type.NUMERIC),
			new Field(43, "Promo Amount", 118, "4.2", Field.Type.NUMERIC),
			new Field(44, "Allegiance ID", 124, "10", Field.Type.NUMERIC),
			new Field(45, "IEC Code", 134, "20", Field.Type.ALPHANUMERIC),
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}