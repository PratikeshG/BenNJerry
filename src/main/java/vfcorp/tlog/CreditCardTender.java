package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Tender;

public class CreditCardTender extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 662;
		id = "065";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Account Number", new FieldDetails(120, 4, "Left justified, space filled"));
		fields.put("Account Number Format", new FieldDetails(1, 124, ""));
		fields.put("Start Date", new FieldDetails(8, 125, "00MMYYYY, zero filled"));
		fields.put("Expiration Date", new FieldDetails(8, 133, "00MMYYYY, zero filled"));
		fields.put("Entry Method", new FieldDetails(1, 141, "zero filled"));
		fields.put("Authorization Method", new FieldDetails(1, 142, "zero filled"));
		fields.put("Authorization Code", new FieldDetails(8, 143, "Left justified"));
		fields.put("Settlement Data", new FieldDetails(512, 151, "Left justified"));
	}

	public CreditCardTender() {
		super();
	}

	public CreditCardTender(String record) {
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

	public CreditCardTender parse(Tender tender, Payment payment) throws Exception {
		String entryMethod = "";
		if (payment.getCardDetails() != null && payment.getCardDetails().getEntryMethod().equals("SWIPED"))
			entryMethod = "1";
		if (payment.getCardDetails() != null && payment.getCardDetails().getEntryMethod().equals("MANUAL"))
			entryMethod = "2";
		String last4 =  payment.getCardDetails() != null
				&& payment.getCardDetails().getCard() != null
				&& payment.getCardDetails().getCard().getLast4() != null ?
						payment.getCardDetails().getCard().getLast4() : "";
		putValue("Account Number", last4); // not supported
		putValue("Account Number Format", "0"); // not supported
		putValue("Start Date", ""); // not supported
		putValue("Expiration Date", ""); // not supported
		putValue("Entry Method", entryMethod);
		putValue("Authorization Method", "2"); // "electronic authorization"; only supported method
		putValue("Authorization Code", ""); // not supported
		putValue("Settlement Data", payment.getCardDetails().getCard().getCardBrand() != null ?  payment.getCardDetails().getCard().getCardBrand() : "");

		return this;
	}
}
