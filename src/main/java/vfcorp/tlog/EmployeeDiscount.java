package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class EmployeeDiscount extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 24;
		id = "009";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Amount of Discount", new FieldDetails(10, 4, "zero filled"));
		fields.put("Amount of Item", new FieldDetails(10, 14, "zero filled"));
		fields.put("Discount Type Indicator", new FieldDetails(1, 24, ""));
	}
	
	public EmployeeDiscount() {
		super();
	}

	public EmployeeDiscount(String record) {
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

	public EmployeeDiscount parse(PaymentItemization itemization, PaymentDiscount discount) throws Exception {		
		// Need to subtract previously applied discounts on this item from beforeTotal
		int beforeTotal = itemization.getGrossSalesMoney().getAmount();
		for (PaymentDiscount prevDiscount : itemization.getDiscounts()) {
			if (prevDiscount.getDiscountId().equals(discount.getDiscountId())) {
				break;
			}
			beforeTotal += prevDiscount.getAppliedMoney().getAmount(); // negative value
		}
		int discountTotal = discount.getAppliedMoney().getAmount(); // negative value
		
		putValue("Amount of Discount", "" + -discountTotal);
		putValue("Amount of Item", "" + beforeTotal);
		putValue("Discount Type Indicator", "0"); // 0 is "Normal Employee discount"

		return this;
	}
}