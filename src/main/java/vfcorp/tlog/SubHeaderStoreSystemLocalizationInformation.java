package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class SubHeaderStoreSystemLocalizationInformation extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 133;
		id = "086";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Country/Language Indicator", new RecordDetails(20, 4, "Left justified, space filled"));
		fields.put("Currency Decimal Indicator", new RecordDetails(10, 24, "Right justified, zero filled"));
		fields.put("CRM Currency Code", new RecordDetails(3, 34, ""));
		fields.put("Reserved", new RecordDetails(97, 37, "Space filled, reserved"));
	}

	public SubHeaderStoreSystemLocalizationInformation() {
		super();
	}

	public SubHeaderStoreSystemLocalizationInformation(String record) {
		super(record);
	}

	@Override
	public Map<String, RecordDetails> getFields() {
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
	
	public SubHeaderStoreSystemLocalizationInformation parse() {
		values.put("Country/Language Indicator", "00000409"); // taken from the example file
		values.put("Currency Decimal Indicator", "2"); // taken from the example file
		
		return this;
	}
}
