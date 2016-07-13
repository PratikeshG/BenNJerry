package paradies.records;

import java.util.ArrayList;

public class AuthorizationResponseRecord extends Record {
	
	static final int TOTAL_FIELDS = 2;
	static final int SIZE = 100;

	public static final String ID = "A2";
	public static final String FIELD_CREDIT_SERVICE_RESPONSE_1 = "Credit Service Resp Part 1";
	public static final String FIELD_CREDIT_SERVICE_RESPONSE_2 = "Credit Service Resp Part 2";

	public AuthorizationResponseRecord() {
		super(TOTAL_FIELDS, SIZE);

		Field[] createFields = {
			new Field(14, FIELD_CREDIT_SERVICE_RESPONSE_1, 49, "60", Field.Type.ALPHANUMERIC),
			new Field(15, FIELD_CREDIT_SERVICE_RESPONSE_2, 109, "40", Field.Type.ALPHANUMERIC)
		};

		fields = new ArrayList<Field>();
		for (Field f : createFields) {
			fields.add(f);
		}
	}
}