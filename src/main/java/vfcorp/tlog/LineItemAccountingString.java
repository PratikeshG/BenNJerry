package vfcorp.tlog;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.Util;
import vfcorp.FieldDetails;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemAppliedDiscount;

public class LineItemAccountingString extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 148;
		id = "055";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Item Number", new FieldDetails(24, 4, "Left justified, space filled"));
		fields.put("Non Merchandise Number", new FieldDetails(24, 28, ""));
		fields.put("EGC/Gift Certificate Number", new FieldDetails(20, 52, ""));
		fields.put("Item Value", new FieldDetails(10, 72, "zero filled"));
		fields.put("Type Indicator", new FieldDetails(2, 82, "See below"));
		fields.put("Adjust Line Item Quantity", new FieldDetails(1, 84, "0 = No, 1 = Yes"));
		fields.put("Sales Taxable Amount", new FieldDetails(10, 85, "Zero filled"));
		fields.put("Sales Not Taxable Amount", new FieldDetails(10, 95, "Zero filled"));
		fields.put("Sales Taxable Amount 2", new FieldDetails(10, 105, "Zero filled"));
		fields.put("Sales Not Taxable Amount 2", new FieldDetails(10, 115, "Zero filled"));
		fields.put("Return Quantity Indicator", new FieldDetails(2, 125, ""));
		fields.put("Productivity Quantity", new FieldDetails(9, 127, "9(6)V999, zero filled"));
		fields.put("Item Index", new FieldDetails(7, 136, "zero filled"));
		fields.put("Reserved for Future Use", new FieldDetails(6, 143, "Space filled"));
	}

	public LineItemAccountingString() {
		super();
	}

	public LineItemAccountingString(String record) {
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

	public LineItemAccountingString parse(OrderLineItem lineItem, int itemNumberLookupLength, int lineItemIndex, Map<String, CatalogObject> catalog) throws Exception {
		// TODO(bhartard): Create helper function to share this logic
    	CatalogObject catalogObject = catalog.get(lineItem.getCatalogObjectId());
    	String sku = catalogObject != null && catalogObject.getItemVariationData() != null ?
    			catalogObject.getItemVariationData().getSku() : "";
		if (!sku.isEmpty() && sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
		}

		// Square doesn't support partial quantities
		String productivityQuantity = String.format( "%.3f", 1.0).replace(".", "");

		// Get line item's applied amount from discounts applied to line item's index
		int totalLineItemQty =  Integer.parseInt(lineItem.getQuantity());
		int lineItemAmount = lineItem.getBasePriceMoney().getAmount();
		if(lineItem.getAppliedDiscounts() != null) {
			for (OrderLineItemAppliedDiscount discount : lineItem.getAppliedDiscounts()) {
				int[] discountAmounts = Util.divideIntegerEvenly(discount.getAppliedMoney().getAmount(), totalLineItemQty);
				lineItemAmount -= discountAmounts[lineItemIndex-1];
			}

		}

		// Taxable amounts
		boolean isTaxable = lineItem.getAppliedTaxes() != null && lineItem.getAppliedTaxes().length > 0;
		String salesTaxableAmount = isTaxable ? "" + lineItemAmount : "0";
		String salesNotTaxableAmount = isTaxable ? "0" : "" + lineItemAmount;

		putValue("Item Number", sku); // requires special formating, according to documentation
		putValue("Non Merchandise Number", ""); // no such thing as "non merchandise" in square
		putValue("EGC/Gift Certificate Number", "");
		putValue("Item Value", "" + lineItemAmount); // amount of total + discounts applied to this single quantity
		putValue("Type Indicator", "01"); // "merchandise sale"
		putValue("Adjust Line Item Quantity", "0"); // transactions can't be altered after completion
		putValue("Sales Taxable Amount", salesTaxableAmount);
		putValue("Sales Not Taxable Amount", salesNotTaxableAmount);
		putValue("Sales Not Taxable Amount 2", ""); // not supported
		putValue("Sales Taxable Amount 2", ""); // not supported
		putValue("Return Quantity Indicator", "00"); // rentals are not supported
		putValue("Productivity Quantity", productivityQuantity); // The value equals 1 except in the case of fractional quantities
		putValue("Item Index", "" + lineItemIndex);
		return this;
	}
}
