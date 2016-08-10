package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;

public class ItemTaxMerchandiseNonMerchandiseItemsFees extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 84;
		id = "025";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Tax Type", new FieldDetails(2, 4, ""));
		fields.put("Tax Method", new FieldDetails(2, 6, ""));
		fields.put("Tax Rate", new FieldDetails(7, 8, "9(2)v99999, zero filled"));
		fields.put("Tax Amount", new FieldDetails(10, 15, "zero filled"));
		fields.put("Tax Override Code", new FieldDetails(25, 25, ""));
		fields.put("Taxable Amount", new FieldDetails(10, 50, "zero filled"));
		fields.put("Tax Code", new FieldDetails(25, 60, "left justified"));
	}
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees() {
		super();
	}
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees(String record) {
		super(record);
	}

	@Override
	public Map<String,FieldDetails> getFields() {
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
	
	public ItemTaxMerchandiseNonMerchandiseItemsFees parse(PaymentTax tax, PaymentItemization itemization) throws Exception {
		String taxType = "01";
		switch (tax.getName()) {
			case "VAT":
				taxType = "02";
				break;
			case "GST":
				taxType = "03";
				break;
			case "GST/PST":
				taxType = "04";
				break;
			case "PST ON GST":
				taxType = "05";
				break;
			case "No tax":
				taxType = "07";
				break;
			case "HST":
				taxType = "08";
				break;
			case "Sales Tax":
			default:
				taxType = "01";
				break;
		}
		long taxRate = Math.round(Double.parseDouble(tax.getRate()) * 10000000);
		
		putValue("Tax Type", taxType);
		putValue("Tax Method", "01"); // not supported
		putValue("Tax Rate", "" + taxRate);
		putValue("Tax Amount", ""); // not supported
		putValue("Tax Override Code", ""); // not supported
		putValue("Taxable Amount", "" + itemization.getGrossSalesMoney().getAmount()); // TODO(colinlam): this isn't true if this is an additive tax
		putValue("Tax Code", "1"); // not supported, but follows the pattern in the file given
		
		return this;
	}
}
