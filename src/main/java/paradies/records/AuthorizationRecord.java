package paradies.records;

import java.util.ArrayList;

public class AuthorizationRecord extends Record {
	
	static final int TOTAL_FIELDS = 19;
	static final int SIZE = 99;

	public static final String ID = "A";
	public static final String FIELD_TENDER_ID = "Tender ID";
	public static final String FIELD_CREDIT_OR_DEBIT_TYPE = "Credit or debit type";
	public static final String FIELD_HOW_AUTHORIZED = "How Authorized";
	public static final String FIELD_CARD_ENTRY_TYPE = "Card Entry Type";
	public static final String FIELD_CARD_NUMBER = "Card Number";
	public static final String FIELD_EXPIRE_DATE = "Expire Date";
	public static final String FIELD_REQUEST_AMOUNT = "Request Amount";
	public static final String FIELD_AUTHORIZED_AMOUNT = "Authorized Amount";
	public static final String FIELD_AUTHORIZATION_NUMBER = "Authorization Number";
	public static final String FIELD_FUTURE = "Future";
	public static final String FIELD_POSITIVE_FLAG = "Positive Flag";
	public static final String FIELD_CREDIT_OR_DEBIT_TRANSACTION_TYPE = "Credit or debit transaction type";
	public static final String FIELD_EXT_CARD_NUMBER = "Ext Card number";
	public static final String FIELD_A2_PART1_LENGTH = "Length of Resp Part 1 field in A2";
	public static final String FIELD_A2_PART2_LENGTH = "Length of Resp Part 2 field in A2";
	public static final String FIELD_CASH_BACK_AMOUNT = "Cash Back Amount";
	public static final String FIELD_RETRIEVAL_NUMBER = "Retrieval Number";
	public static final String FIELD_ORIGINAL_CREDIT_TRANSACTION_TYPE = "Original Credit Transaction Type";
	public static final String FIELD_CORPORATE_CARD_TYPE = "Corporate Card Type";

	public AuthorizationRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_TENDER_ID, 49, "2", Field.Type.NUMERIC),
			new Field(15, FIELD_CREDIT_OR_DEBIT_TYPE, 51, "2", Field.Type.NUMERIC),
			new Field(16, FIELD_HOW_AUTHORIZED, 53, "1", Field.Type.NUMERIC),
			new Field(17, FIELD_CARD_ENTRY_TYPE, 54, "1", Field.Type.NUMERIC),
			new Field(18, FIELD_CARD_NUMBER, 55, "20", Field.Type.NUMERIC),
			new Field(19, FIELD_EXPIRE_DATE, 75, "6", Field.Type.NUMERIC),
			new Field(20, FIELD_REQUEST_AMOUNT, 81, "6.2", Field.Type.NUMERIC),
			new Field(21, FIELD_AUTHORIZED_AMOUNT, 89, "6.2", Field.Type.NUMERIC),
			new Field(22, FIELD_AUTHORIZATION_NUMBER, 97, "10", Field.Type.ALPHANUMERIC),
			new Field(23, FIELD_FUTURE, 107, "1", Field.Type.NUMERIC),
			new Field(24, FIELD_POSITIVE_FLAG, 108, "1", Field.Type.ALPHANUMERIC),
			new Field(25, FIELD_CREDIT_OR_DEBIT_TRANSACTION_TYPE, 109, "2", Field.Type.NUMERIC),
			new Field(26, FIELD_EXT_CARD_NUMBER, 111, "10", Field.Type.ALPHANUMERIC),
			new Field(27, FIELD_A2_PART1_LENGTH, 121, "2", Field.Type.NUMERIC),
			new Field(28, FIELD_A2_PART2_LENGTH, 123, "2", Field.Type.NUMERIC),
			new Field(29, FIELD_CASH_BACK_AMOUNT, 125, "6.2", Field.Type.NUMERIC),
			new Field(30, FIELD_RETRIEVAL_NUMBER, 133, "12", Field.Type.NUMERIC),
			new Field(31, FIELD_ORIGINAL_CREDIT_TRANSACTION_TYPE, 145, "2", Field.Type.NUMERIC),
			new Field(32, FIELD_CORPORATE_CARD_TYPE, 147, "1", Field.Type.NUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}