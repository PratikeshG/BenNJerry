package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class DiscountTypeIndicator extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 42;
		id = "021";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Discount Code", new FieldDetails(8, 4, "left justified, space filled"));
		fields.put("Amount Before Discount", new FieldDetails(10, 12, "zero filled"));
		fields.put("Amount Discount", new FieldDetails(10, 22, "zero filled"));
		fields.put("Amount After Discount", new FieldDetails(10, 32, "zero filled"));
		fields.put("Transaction Discount", new FieldDetails(1, 42, ""));
	}
	
	public DiscountTypeIndicator() {
		super();
	}

	public DiscountTypeIndicator(String record) {
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
	
	public DiscountTypeIndicator parse(PaymentItemization itemization, PaymentDiscount discount, String discountCode, String discountAppyType) throws Exception {
		// Need to subtract previously applied discounts on this item from beforeTotal
		int beforeTotal = itemization.getGrossSalesMoney().getAmount();
		for (PaymentDiscount prevDiscount : itemization.getDiscounts()) {
			if (prevDiscount.getDiscountId().equals(discount.getDiscountId())) {
				break;
			}
			beforeTotal += prevDiscount.getAppliedMoney().getAmount(); // negative value
		}
		int discountTotal = discount.getAppliedMoney().getAmount(); // negative value
		int finalTotal = beforeTotal + discountTotal;
		
		putValue("Discount Code", discountCode);
		putValue("Amount Before Discount", "" + beforeTotal);
		putValue("Amount Discount", "" + -discountTotal);
		putValue("Amount After Discount", "" + finalTotal);
		putValue("Transaction Discount", discountAppyType);

		return this;
	}
}
