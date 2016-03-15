package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;

public class LineItemAssociateAndDiscountAccountingString extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,RecordDetails>();
		length = 213;
		id = "056";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Team Identifier", new RecordDetails(1, 4, "1 = In Team, 0 = Not in Team"));
		fields.put("Team Number", new RecordDetails(3, 5, "000 if Not in Team"));
		fields.put("Item Number", new RecordDetails(24, 8, "Left justified, space filled"));
		fields.put("Non Merchandise Number", new RecordDetails(24, 32, ""));
		fields.put("EGC/Gift Certificate Number", new RecordDetails(20, 56, ""));
		fields.put("Associate Number", new RecordDetails(11, 76, "zero filled"));
		fields.put("Value (per associate)", new RecordDetails(10, 87, "zero filled"));
		fields.put("Type Indicator", new RecordDetails(2, 97, ""));
		fields.put("Adjust Line Item Quantity", new RecordDetails(1, 99, ""));
		fields.put("Emp Discount Value", new RecordDetails(10, 100, "zero filled"));
		fields.put("PCM Discount Value", new RecordDetails(10, 110, "zero filled"));
		fields.put("Line Item Discount Value", new RecordDetails(10, 120, "zero filled"));
		fields.put("Line Item Promo Value", new RecordDetails(10, 130, "zero filled"));
		fields.put("Transaction Discount Value", new RecordDetails(10, 140, "zero filled"));
		fields.put("Transaction Promo Value", new RecordDetails(10, 150, "zero filled"));
		fields.put("Price Override Indicator", new RecordDetails(1, 160, ""));
		fields.put("Price Override Value", new RecordDetails(10, 161, "zero filled"));
		fields.put("Receipt Presentation Price", new RecordDetails(10, 171, "zero filled"));
		fields.put("Productivity Quantity", new RecordDetails(9, 181, "zero filled"));
		fields.put("Employee Number", new RecordDetails(11, 190, "zero filled"));
		fields.put("PLU Sale Price Discount Value", new RecordDetails(10, 201, "zero filled"));
		fields.put("Reserved for Future Use", new RecordDetails(3, 211, "Space filled"));
	}
	
	public LineItemAssociateAndDiscountAccountingString() {
		super();
	}

	public LineItemAssociateAndDiscountAccountingString(String record) {
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
	
	public LineItemAssociateAndDiscountAccountingString parse(Payment payment, PaymentItemization itemization, int itemNumberLookupLength, String employeeId, List<Employee> squareEmployees, double quantity) {
		String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
		if (sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", Integer.parseInt(sku));
		}
		
		String productivityQuantity = "";
		if (quantity > 1) {
			productivityQuantity = String.format( "%.3f", 1.0).replace(".", "");
		} else {
			productivityQuantity = String.format( "%.3f", quantity).replace(".", "");
		}
		
		for (Employee employee : squareEmployees) {
			if (employee.getId().equals(employeeId)) {
				putValue("Associate Number", employee.getExternalId());
				putValue("Employee Number", employee.getExternalId());
			}
		}
		
		if (values.get("Associate Number") == null) {
			putValue("Associate Number", "");
			putValue("Employee Number", "");
		}
		
		putValue("Team Identifier", "0"); // not supported
		putValue("Team Number", "000"); // not supported
		putValue("Item Number", sku);
		putValue("Non Merchandise Number", ""); // no such thing as "non merchandise" in square
		putValue("EGC/Gift Certificate Number", ""); // TODO(colinlam): gift card sales?
		putValue("Value (per associate)", "" + itemization.getNetSalesMoney().getAmount());
		putValue("Type Indicator", "01"); // "merchandise sale"; no other values supported
		putValue("Adjust Line Item Quantity", "0"); // transactions can't be altered after completion
		putValue("Emp Discount Value", "");
		putValue("PCM Discount Value", "");
		putValue("Line Item Discount Value", "" + -itemization.getDiscountMoney().getAmount());
		putValue("Line Item Promo Value", ""); // not supported
		putValue("Transaction Discount Value", "" + -payment.getDiscountMoney().getAmount());
		putValue("Transaction Promo Value", ""); // not supported
		putValue("Price Override Indicator", "0"); // not supported
		putValue("Price Override Value", ""); // not supported
		putValue("Receipt Presentation Price", "" + itemization.getGrossSalesMoney().getAmount());
		putValue("Productivity Quantity", productivityQuantity);
		putValue("PLU Sale Price Discount Value", "" + -itemization.getDiscountMoney().getAmount());
		
		return this;
	}
}
