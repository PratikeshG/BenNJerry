package paradies.records;

import java.util.ArrayList;

public class ExtendedAuthorizationRecord extends Record {
	
	static final int TOTAL_FIELDS = 11;
	static final int SIZE = 100;

	public static final String ID = "AE";
	public static final String FIELD_BANK_CARD_NAME = "Bank Card Name";
	public static final String FIELD_BALANCE_AMOUNT = "Balance Amount";
	public static final String FIELD_RESPONSE_CODE = "Response Code";
	public static final String FIELD_VOUCHER_ID = "Voucher ID";
	public static final String FIELD_VOUCHER_AGENT = "Voucher Agent";
	public static final String FIELD_TRANSACTION_DATE = "Transaction Date";
	public static final String FIELD_TRANSACTION_TIME = "Transaction Time";
	public static final String FIELD_REFERENCE_NUMBER = "Reference (Trace) Number";
	public static final String FIELD_ELAPSED_TIME = "Elapsed Time";
	public static final String FIELD_TAX_AMOUNT = "Tax Amount";
	public static final String FIELD_CUSTOMER_REFERENCE_NUMBER = "Customer Reference Number";

	// Documented as AE: Encrypted extended authorization record, but actually
	// Record type A5: Extended authorization record
	public ExtendedAuthorizationRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_BANK_CARD_NAME, 49, "25", Field.Type.ALPHANUMERIC),
			new Field(15, FIELD_BALANCE_AMOUNT, 74, "6.2", Field.Type.NUMERIC),
			new Field(16, FIELD_RESPONSE_CODE, 82, "4", Field.Type.ALPHANUMERIC),
			new Field(17, FIELD_VOUCHER_ID, 86, "2", Field.Type.NUMERIC),
			new Field(18, FIELD_VOUCHER_AGENT, 88, "2", Field.Type.NUMERIC),
			new Field(19, FIELD_TRANSACTION_DATE, 90, "8", Field.Type.NUMERIC),
			new Field(20, FIELD_TRANSACTION_TIME, 98, "6", Field.Type.NUMERIC),
			new Field(21, FIELD_REFERENCE_NUMBER, 104, "12", Field.Type.ALPHANUMERIC),
			new Field(22, FIELD_ELAPSED_TIME, 116, "3.3", Field.Type.NUMERIC),
			new Field(23, FIELD_TAX_AMOUNT, 122, "5.2", Field.Type.NUMERIC),
			new Field(24, FIELD_CUSTOMER_REFERENCE_NUMBER, 129, "20", Field.Type.ALPHANUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}