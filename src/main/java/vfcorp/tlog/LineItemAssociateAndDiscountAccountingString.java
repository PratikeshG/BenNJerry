package vfcorp.tlog;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemAppliedDiscount;
import com.squareup.connect.v2.OrderLineItemDiscount;

import vfcorp.FieldDetails;
import vfcorp.Record;
import vfcorp.Util;

public class LineItemAssociateAndDiscountAccountingString extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 213;
        id = "056";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Team Identifier", new FieldDetails(1, 4, "1 = In Team, 0 = Not in Team"));
        fields.put("Team Number", new FieldDetails(3, 5, "000 if Not in Team"));
        fields.put("Item Number", new FieldDetails(24, 8, "Left justified, space filled"));
        fields.put("Non Merchandise Number", new FieldDetails(24, 32, ""));
        fields.put("EGC/Gift Certificate Number", new FieldDetails(20, 56, ""));
        fields.put("Associate Number", new FieldDetails(11, 76, "zero filled"));
        fields.put("Value (per associate)", new FieldDetails(10, 87, "zero filled"));
        fields.put("Type Indicator", new FieldDetails(2, 97, ""));
        fields.put("Adjust Line Item Quantity", new FieldDetails(1, 99, ""));
        fields.put("Emp Discount Value", new FieldDetails(10, 100, "zero filled"));
        fields.put("PCM Discount Value", new FieldDetails(10, 110, "zero filled"));
        fields.put("Line Item Discount Value", new FieldDetails(10, 120, "zero filled"));
        fields.put("Line Item Promo Value", new FieldDetails(10, 130, "zero filled"));
        fields.put("Transaction Discount Value", new FieldDetails(10, 140, "zero filled"));
        fields.put("Transaction Promo Value", new FieldDetails(10, 150, "zero filled"));
        fields.put("Price Override Indicator", new FieldDetails(1, 160, ""));
        fields.put("Price Override Value", new FieldDetails(10, 161, "zero filled"));
        fields.put("Receipt Presentation Price", new FieldDetails(10, 171, "zero filled"));
        fields.put("Productivity Quantity", new FieldDetails(9, 181, "zero filled"));
        fields.put("Employee Number", new FieldDetails(11, 190, "zero filled"));
        fields.put("PLU Sale Price Discount Value", new FieldDetails(10, 201, "zero filled"));
        fields.put("Reserved for Future Use", new FieldDetails(3, 211, "Space filled"));
    }

    public LineItemAssociateAndDiscountAccountingString() {
        super();
    }

    public LineItemAssociateAndDiscountAccountingString(String record) {
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

    public LineItemAssociateAndDiscountAccountingString parse(Payment payment, PaymentItemization itemization,
            int itemNumberLookupLength, int lineItemIndex, String employeeId) throws Exception {
        String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
        if (sku.matches("[0-9]+")) {
            sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
        }

        int lineItemDiscountValue = 0;
        int lineItemPromoValue = 0;
        int transactionDiscountValue = 0;
        int transactionPromoValue = 0;
        int employeeDiscountValue = 0;

        // Get line item's applied amount from discounts applied to line item's index
        int totalLineItemQty = itemization.getQuantity().intValue();
        int lineItemAmount = itemization.getSingleQuantityMoney().getAmount();

        for (PaymentDiscount discount : itemization.getDiscounts()) {
            String discountType = "";
            String discountAppyType = "";
            String discountDetails = Util.getValueInBrackets(discount.getName());

            if (discountDetails.length() == 5) {
                String firstChar = discountDetails.substring(0, 1);
                if (firstChar.equals("1") || firstChar.equals("2")) {
                    discountType = firstChar;
                } else {
                    discountType = "0";
                }
                discountAppyType = discountDetails.substring(1, 2).equals("1") ? "1" : "0";
            }

            int[] discountAmounts = Util.divideIntegerEvenly(-discount.getAppliedMoney().getAmount(), totalLineItemQty);
            int discountedAmount = discountAmounts[lineItemIndex - 1];

            lineItemAmount -= discountAmounts[lineItemIndex - 1];

            // Line Item Discount
            if (discountType.equals("0") && discountAppyType.equals("0")) {
                lineItemDiscountValue += discountedAmount;
            }
            // Line Item Promo
            if (discountType.equals("1") && discountAppyType.equals("0")) {
                lineItemPromoValue += discountedAmount;
            }
            // Transaction Discount
            if (discountType.equals("0") && discountAppyType.equals("1")) {
                transactionDiscountValue += discountedAmount;
            }
            // Transaction Promo
            if (discountType.equals("1") && discountAppyType.equals("1")) {
                transactionPromoValue += discountedAmount;
            }
            // Employee Discount
            if (discountType.equals("2")) {
                employeeDiscountValue += discountedAmount;
            }
        }

        // Receipt Presentation Price
        // NOTE: Can be the full, non-discounted value when there is a transaction-level discount(s)
        // but must be the discounted total when ONLY applying item-level discounts
        int rrp = itemization.getGrossSalesMoney().getAmount();
        if ((lineItemDiscountValue + lineItemPromoValue + employeeDiscountValue > 0)
                && (transactionDiscountValue + transactionPromoValue == 0)) {
            rrp = itemization.getNetSalesMoney().getAmount();
        }

        int priceOverrideIndicator = 0;
        int priceOverrideAmount = 0;
        if (Util.hasPriceOverride(itemization)) {
            int soldUnitPrice = itemization.getSingleQuantityMoney().getAmount();
            int originalUnitPrice = Util.getPriceBeforeOverride(itemization);
            if (originalUnitPrice < soldUnitPrice) {
                priceOverrideIndicator = 1;
            } else {
                priceOverrideIndicator = 2;
            }
            priceOverrideAmount = Math.abs(originalUnitPrice - soldUnitPrice);
        }

        putValue("Line Item Discount Value", "" + lineItemDiscountValue);
        putValue("Line Item Promo Value", "" + lineItemPromoValue);
        putValue("Transaction Discount Value", "" + transactionDiscountValue);
        putValue("Transaction Promo Value", "" + transactionPromoValue);
        putValue("Emp Discount Value", "" + employeeDiscountValue);
        putValue("PCM Discount Value", ""); // not supported

        putValue("Team Identifier", "0"); // not supported
        putValue("Team Number", "000"); // not supported
        putValue("Item Number", sku);
        putValue("Non Merchandise Number", ""); // no such thing as "non merchandise" in square
        putValue("EGC/Gift Certificate Number", "");
        putValue("Associate Number", employeeId);
        putValue("Value (per associate)", "" + lineItemAmount);
        putValue("Type Indicator", "01"); // "merchandise sale"; no other values supported
        putValue("Adjust Line Item Quantity", "0"); // transactions can't be altered after completion
        putValue("Price Override Indicator", "" + priceOverrideIndicator);
        putValue("Price Override Value", "" + priceOverrideAmount);
        putValue("Receipt Presentation Price", "" + rrp);
        putValue("Employee Number", employeeId);
        putValue("Productivity Quantity", String.format("%.3f", 1.0).replace(".", "")); // Always qty 1 for non team sales
        putValue("PLU Sale Price Discount Value", "");

        return this;
    }

    public LineItemAssociateAndDiscountAccountingString parse(Order order, OrderLineItem lineItem,
            int itemNumberLookupLength, int lineItemIndex, String employeeId, Map<String, CatalogObject> catalog) throws Exception {
    	CatalogObject catalogObject = catalog.get(lineItem.getCatalogObjectId());
    	String sku = catalogObject != null && catalogObject.getItemVariationData() != null ?
    			catalogObject.getItemVariationData().getSku() : "";
		if (!sku.isEmpty() && sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
		}

        int lineItemDiscountValue = 0;
        int lineItemPromoValue = 0;
        int transactionDiscountValue = 0;
        int transactionPromoValue = 0;
        int employeeDiscountValue = 0;

        // Get line item's applied amount from discounts applied to line item's index
        int totalLineItemQty = Integer.parseInt(lineItem.getQuantity());
        int lineItemAmount = lineItem.getBasePriceMoney().getAmount();

		Map<String, OrderLineItemDiscount> lineItemDiscountDetails = order.getDiscounts() != null ?
				Arrays.stream(order.getDiscounts()).collect(Collectors.toMap(OrderLineItemDiscount::getUid, Function.identity())) : new HashMap<>();

		if(lineItem.getAppliedDiscounts() != null) {
			for (OrderLineItemAppliedDiscount discount : lineItem.getAppliedDiscounts()) {
	            String discountType = "";
	            String discountAppyType = "";
				String name = lineItemDiscountDetails.containsKey(discount.getDiscountUid()) ? lineItemDiscountDetails.get(discount.getDiscountUid()).getName() : "";
	            String discountDetails = Util.getValueInBrackets(name);

	            if (discountDetails.length() == 5) {
	                String firstChar = discountDetails.substring(0, 1);
	                if (firstChar.equals("1") || firstChar.equals("2")) {
	                    discountType = firstChar;
	                } else {
	                    discountType = "0";
	                }
	                discountAppyType = discountDetails.substring(1, 2).equals("1") ? "1" : "0";
	            }

	            int[] discountAmounts = Util.divideIntegerEvenly(discount.getAppliedMoney().getAmount(), totalLineItemQty);
	            int discountedAmount = discountAmounts[lineItemIndex - 1];

	            lineItemAmount -= discountAmounts[lineItemIndex - 1];

	            // Line Item Discount
	            if (discountType.equals("0") && discountAppyType.equals("0")) {
	                lineItemDiscountValue += discountedAmount;
	            }
	            // Line Item Promo
	            if (discountType.equals("1") && discountAppyType.equals("0")) {
	                lineItemPromoValue += discountedAmount;
	            }
	            // Transaction Discount
	            if (discountType.equals("0") && discountAppyType.equals("1")) {
	                transactionDiscountValue += discountedAmount;
	            }
	            // Transaction Promo
	            if (discountType.equals("1") && discountAppyType.equals("1")) {
	                transactionPromoValue += discountedAmount;
	            }
	            // Employee Discount
	            if (discountType.equals("2")) {
	                employeeDiscountValue += discountedAmount;
	            }
	        }
		}

        // Receipt Presentation Price
        // NOTE: Can be the full, non-discounted value when there is a transaction-level discount(s)
        // but must be the discounted total when ONLY applying item-level discounts
        int rrp = lineItem.getGrossSalesMoney().getAmount();
        if ((lineItemDiscountValue + lineItemPromoValue + employeeDiscountValue > 0)
                && (transactionDiscountValue + transactionPromoValue == 0)) {
            rrp = lineItem.getGrossSalesMoney().getAmount() + lineItem.getTotalDiscountMoney().getAmount();
        }

        int priceOverrideIndicator = 0;
        int priceOverrideAmount = 0;
        if (Util.hasPriceOverride(lineItem)) {
            int soldUnitPrice = lineItem.getBasePriceMoney().getAmount();
            int originalUnitPrice = Util.getPriceBeforeOverride(lineItem);
            if (originalUnitPrice < soldUnitPrice) {
                priceOverrideIndicator = 1;
            } else {
                priceOverrideIndicator = 2;
            }
            priceOverrideAmount = Math.abs(originalUnitPrice - soldUnitPrice);
        }

        putValue("Line Item Discount Value", "" + lineItemDiscountValue);
        putValue("Line Item Promo Value", "" + lineItemPromoValue);
        putValue("Transaction Discount Value", "" + transactionDiscountValue);
        putValue("Transaction Promo Value", "" + transactionPromoValue);
        putValue("Emp Discount Value", "" + employeeDiscountValue);
        putValue("PCM Discount Value", ""); // not supported

        putValue("Team Identifier", "0"); // not supported
        putValue("Team Number", "000"); // not supported
        putValue("Item Number", sku);
        putValue("Non Merchandise Number", ""); // no such thing as "non merchandise" in square
        putValue("EGC/Gift Certificate Number", "");
        putValue("Associate Number", employeeId);
        putValue("Value (per associate)", "" + lineItemAmount);
        putValue("Type Indicator", "01"); // "merchandise sale"; no other values supported
        putValue("Adjust Line Item Quantity", "0"); // transactions can't be altered after completion
        putValue("Price Override Indicator", "" + priceOverrideIndicator);
        putValue("Price Override Value", "" + priceOverrideAmount);
        putValue("Receipt Presentation Price", "" + rrp);
        putValue("Employee Number", employeeId);
        putValue("Productivity Quantity", String.format("%.3f", 1.0).replace(".", "")); // Always qty 1 for non team sales
        putValue("PLU Sale Price Discount Value", "");

        return this;
    }
}
