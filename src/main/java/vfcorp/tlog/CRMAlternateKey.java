package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class CRMAlternateKey extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 87;
		id = "110";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Alternate Key Type", new FieldDetails(4, 4, "zero filled"));
		fields.put("Alternate Key", new FieldDetails(80, 8, "Left justified, space filled"));
	}

	public CRMAlternateKey() {
		super();
	}

	public CRMAlternateKey(String record) {
		super(record);
	}

	@Override
	public Map<String, FieldDetails> getFields() {
		return fields;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String getId() {
		return id;
	}

	public CRMAlternateKey parse(String key) throws Exception {		
		putValue("Alternate Key Type", "LOYA");
		putValue("Alternate Key", key);

		return this;
	}
}
