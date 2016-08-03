package paradies.records;

import java.util.ArrayList;

public class TotalTaxRecord extends Record {
	
	static final int TOTAL_FIELDS = 6;
	static final int SIZE = 24;

	public static final String ID = "TX";
	public static final String FIELD_TAX_ID = "Tax ID";
	public static final String FIELD_JURISDICTION_ID = "Jurisdiction ID";
	public static final String FIELD_TAX_AMOUNT = "Tax Amount";
	public static final String FIELD_TAXABLE_AMOUNT = "Taxable Amount";
	public static final String FIELD_MANUAL_TAX = "Manual Tax";
	public static final String FIELD_MANUAL_TAX_ITEM_LEVEL = "Item Level Manual Tax";

	public TotalTaxRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_TAX_ID, 49, "2", Field.Type.NUMERIC),
			new Field(15, FIELD_JURISDICTION_ID, 51, "4", Field.Type.NUMERIC),
			new Field(16, FIELD_TAX_AMOUNT, 55, "6.2", Field.Type.NUMERIC),
			new Field(17, FIELD_TAXABLE_AMOUNT, 63, "6.2", Field.Type.NUMERIC),
			new Field(18, FIELD_MANUAL_TAX, 71, "1", Field.Type.NUMERIC), // incorrectly documented as ALPHANUMERIC
			new Field(18, FIELD_MANUAL_TAX_ITEM_LEVEL, 72, "1", Field.Type.NUMERIC) // incorrectly documented as ALPHANUMERIC
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}