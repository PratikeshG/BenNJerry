package paradies.records;

import java.util.ArrayList;

/*
 * The Payment or Tender record types are used to denote a variety of tender options,
 * including not only cash and checks, but also gift cards, stored value cards, and 
 * so forth. All tender types are user defined.
 */
public class MethodOfPaymentRecord extends Record {

	static final int TOTAL_FIELDS = 12;
	static final int SIZE = 72;

	public static final String ID = "P";
	public static final String FIELD_TENDER_ID = "Tender Id";
	public static final String FIELD_TENDER_AMOUNT = "Tender Amount";
	public static final String FIELD_POSITIVE_FLAG = "Positive Flag";
	public static final String FIELD_EXCHANGE_AMOUNT = "Exchange Amount";
	public static final String FIELD_CHANGE_DUE = "Change Due";
	public static final String FIELD_REISSUE_FLAG = "Re-issue Flag";
	public static final String FIELD_REFERENCE_NUMBER_1 = "Reference Number 1";
	public static final String FIELD_FUTURE = "Future";
	public static final String FIELD_DECLINE_OVERRIDE = "Decline Override";
	public static final String FIELD_PROFILE_PROMPT_FLAG = "Profile Prompt Flag";


	public MethodOfPaymentRecord() {
		super(TOTAL_FIELDS, SIZE);
		
		Field[] createFields = {
			new Field(14, FIELD_TENDER_ID, 49, "2", Field.Type.NUMERIC),
			new Field(15, FIELD_TENDER_AMOUNT, 51, "6.2", Field.Type.NUMERIC),
			new Field(16, FIELD_POSITIVE_FLAG, 59, "1", Field.Type.ALPHANUMERIC),
			new Field(17, FIELD_EXCHANGE_AMOUNT, 60, "6.2", Field.Type.NUMERIC),
			new Field(18, FIELD_POSITIVE_FLAG, 68, "1", Field.Type.ALPHANUMERIC),
			new Field(19, FIELD_CHANGE_DUE, 69, "6.2", Field.Type.NUMERIC),
			new Field(20, FIELD_POSITIVE_FLAG, 77, "1", Field.Type.ALPHANUMERIC),
			new Field(21, FIELD_REISSUE_FLAG, 78, "1", Field.Type.NUMERIC),
			new Field(22, FIELD_REFERENCE_NUMBER_1, 79, "30", Field.Type.ALPHANUMERIC),
			new Field(23, FIELD_FUTURE, 109, "10", Field.Type.ALPHANUMERIC),
			new Field(24, FIELD_DECLINE_OVERRIDE, 119, "1", Field.Type.NUMERIC),
			new Field(25, FIELD_PROFILE_PROMPT_FLAG, 120, "1", Field.Type.NUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}