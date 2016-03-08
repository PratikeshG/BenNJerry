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
		fields.put("Tax Type", new RecordDetails(2, 4, ""));
		fields.put("Tax Method", new RecordDetails(2, 6, ""));
		fields.put("Tax Rate", new RecordDetails(7, 8, "9(2)v99999"));
		fields.put("Tax Amount", new RecordDetails(10, 15, ""));
		fields.put("Tax Override Code", new RecordDetails(25, 25, ""));
		fields.put("Taxable Amount", new RecordDetails(10, 50, ""));
		fields.put("Tax Code", new RecordDetails(25, 60, ""));
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
