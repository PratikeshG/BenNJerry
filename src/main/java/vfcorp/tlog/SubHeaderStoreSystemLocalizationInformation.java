package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class SubHeaderStoreSystemLocalizationInformation extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 133;
		id = "086";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Country/Language Indicator", new FieldDetails(20, 4, "Left justified, space filled"));
		fields.put("Currency Decimal Indicator", new FieldDetails(10, 24, "Right justified, zero filled"));
		fields.put("CRM Currency Code", new FieldDetails(3, 34, ""));
		fields.put("Reserved", new FieldDetails(97, 37, "Space filled, reserved"));
	}

	public SubHeaderStoreSystemLocalizationInformation() {
		super();
	}

	public SubHeaderStoreSystemLocalizationInformation(String record) {
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
	
	public SubHeaderStoreSystemLocalizationInformation parse() throws Exception {
		putValue("Country/Language Indicator", "00000409"); // taken from the example file
		putValue("Currency Decimal Indicator", "2"); // taken from the example file
		
		return this;
	}
}
