package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.Tender;

public class CreditCardTender extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 662;
		id = "065";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Account Number", new RecordDetails(120, 4, "Left justified, space filled"));
		fields.put("Account Number Format", new RecordDetails(1, 124, ""));
		fields.put("Start Date", new RecordDetails(8, 125, "00MMYYYY, zero filled"));
		fields.put("Expiration Date", new RecordDetails(8, 133, "00MMYYYY, zero filled"));
		fields.put("Entry Method", new RecordDetails(1, 141, "zero filled"));
		fields.put("Authorization Method", new RecordDetails(1, 142, "zero filled"));
		fields.put("Authorization Code", new RecordDetails(8, 143, "Left justified"));
		fields.put("Settlement Data", new RecordDetails(512, 151, "Left justified"));
	}

	public CreditCardTender() {
		super();
	}

	public CreditCardTender(String record) {
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
	
	public CreditCardTender parse(Tender tender) {
		String entryMethod = "";
		if (tender.getEntryMethod().equals("SWIPED"))
			entryMethod = "1";
		if (tender.getEntryMethod().equals("MANUAL"))
			entryMethod = "2";
		
		putValue("Account Number", tender.getPanSuffix()); // not supported
		putValue("Account Number Format", "0"); // not supported
		putValue("Start Date", ""); // not supported
		putValue("Expiration Date", ""); // not supported
		putValue("Entry Method", entryMethod);
		putValue("Authorization Method", "2"); // "electronic authorization"; only supported method
		putValue("Authorization Code", ""); // not supported
		putValue("Settlement Data", ""); // not supported
		
		return this;
	}
}
