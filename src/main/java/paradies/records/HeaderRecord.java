package paradies.records;

import java.util.ArrayList;

public class HeaderRecord extends Record {
	
	static final int TOTAL_FIELDS = 13;
	static final int SIZE = 48;

	public static final String FIELD_TRANSACTION_TYPE = "Transaction Type";
	public static final String FIELD_TRANSACTION_MODIFIER = "Transaction Modifier";
	public static final String FIELD_STORE_ID = "Store ID";
	public static final String FIELD_REGISTER_ID = "Register ID";
	public static final String FIELD_CASHIER_ID = "Cashier ID";
	public static final String FIELD_SALESPERSON_ID = "Salesperson ID";
	public static final String FIELD_DATETIME = "Date/Time";
	public static final String FIELD_TRANSACTION_NUMBER = "Transaction Number";
	public static final String FIELD_TRANSACTION_VOID_FLAG = "Transaction Void Flag";
	public static final String FIELD_RECORD_SEQUENCE = "Record # Sequence";
	public static final String FIELD_SALE_TYPE = "Sale Type";
	public static final String FIELD_BUSINESS_DATE = "Business Date";
	public static final String FIELD_CENTURY_INDICATOR = "Century Indicator";

	public HeaderRecord(String recordType) {
		super(TOTAL_FIELDS, SIZE);
		
		Field[] createFields = {
			new Field(1, FIELD_TRANSACTION_TYPE, 1, "2", Field.Type.ALPHANUMERIC, recordType),
			new Field(2, FIELD_TRANSACTION_MODIFIER, 3, "1", Field.Type.NUMERIC, ""),
			new Field(3, FIELD_STORE_ID, 4, "4", Field.Type.NUMERIC),	
			new Field(4, FIELD_REGISTER_ID, 8, "2", Field.Type.NUMERIC),
			new Field(5, FIELD_CASHIER_ID, 10, "6", Field.Type.NUMERIC),
			new Field(6, FIELD_SALESPERSON_ID, 16, "6", Field.Type.NUMERIC),
			new Field(7, FIELD_DATETIME, 22, "12", Field.Type.NUMERIC),
			new Field(8, FIELD_TRANSACTION_NUMBER, 34, "4", Field.Type.NUMERIC),
			new Field(9, FIELD_TRANSACTION_VOID_FLAG, 38, "1", Field.Type.NUMERIC, ""),
			new Field(10, FIELD_RECORD_SEQUENCE, 39, "3", Field.Type.NUMERIC),
			new Field(11, FIELD_SALE_TYPE, 42, "2", Field.Type.NUMERIC, ""),
			new Field(12, FIELD_BUSINESS_DATE, 44, "4", Field.Type.NUMERIC),
			new Field(13, FIELD_CENTURY_INDICATOR, 48, "1", Field.Type.NUMERIC, "1")
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
	
	public String toString() {
		String output = "";
		for (Field f : fields) {
			output += f;
		}
		return output;
	}
}