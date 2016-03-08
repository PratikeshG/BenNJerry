package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;

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
		fields.put("Tax Rate", new RecordDetails(7, 8, "9(2)v99999, zero filled"));
		fields.put("Tax Amount", new RecordDetails(10, 15, "zero filled"));
		fields.put("Tax Override Code", new RecordDetails(25, 25, ""));
		fields.put("Taxable Amount", new RecordDetails(10, 50, "zero filled"));
		fields.put("Tax Code", new RecordDetails(25, 60, "left justified"));
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
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees parse(PaymentTax tax, PaymentItemization itemization) {
		String taxType = "01";
		switch (tax.getName()) {
			case "Sales Tax": taxType = "01";
				break;
			case "VAT": taxType = "02";
				break;
			case "GST": taxType = "03";
				break;
			case "GST/PST": taxType = "04";
				break;
			case "PST ON GST": taxType = "05";
				break;
			case "No tax": taxType = "07";
				break;
			case "HST": taxType = "08";
				break;
		}
		long taxRate = Math.round(Double.parseDouble(tax.getRate()) * 10000000);
		
		values.put("Tax Type", taxType);
		values.put("Tax Method", "01"); // not supported
		values.put("Tax Rate", "" + taxRate);
		values.put("Tax Amount", ""); // not supported
		values.put("Tax Override Code", ""); // not supported
		values.put("Taxable Amount", "" + itemization.getGrossSalesMoney().getAmount());
		values.put("Tax Code", "1"); // not supported, but follows the pattern in the file given
		
		return this;
	}
}
