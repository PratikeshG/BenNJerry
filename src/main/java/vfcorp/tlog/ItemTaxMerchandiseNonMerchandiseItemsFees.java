package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class ItemTaxMerchandiseNonMerchandiseItemsFees extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,RecordDetails>();
		length = 84;
		id = "025";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
	}
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees() {
		super();
	}
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees(String record) {
		super(record);
	}

	@Override
	public Map<String,RecordDetails> getFields() {
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
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees parse(Payment squarePayment) {
		return this;
	}
}
