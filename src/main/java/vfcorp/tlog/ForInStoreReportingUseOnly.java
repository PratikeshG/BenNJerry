package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.v2.Order;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class ForInStoreReportingUseOnly extends Record {

	public static final String TRANSACTION_IDENTIFIER_ITEM_VOIDS = "001";
	public static final String TRANSACTION_IDENTIFIER_MERCHANDISE_SALES = "002";
	public static final String TRANSACTION_IDENTIFIER_MERCHANDISE_RETURNS = "003";
	public static final String TRANSACTION_IDENTIFIER_NON_MERCHANDISE_SALES = "004";
	public static final String TRANSACTION_IDENTIFIER_GIFT_CERTIFICATE_SALES = "005";
	public static final String TRANSACTION_IDENTIFIER_FEES = "006";
	public static final String TRANSACTION_IDENTIFIER_ON_ACCOUNT_PAYMENTS = "007";
	public static final String TRANSACTION_IDENTIFIER_PAYOUTS = "008";
	public static final String TRANSACTION_IDENTIFIER_DISCOUNTS = "009";
	public static final String TRANSACTION_IDENTIFIER_EMPLOYEE_DISCOUNTS = "010";
	public static final String TRANSACTION_IDENTIFIER_CUSTOMER_DISCOUNTS = "011";
	public static final String TRANSACTION_IDENTIFIER_LAYAWAY_PAYMENTS = "012";
	public static final String TRANSACTION_IDENTIFIER_SALES_TAX = "013";
	public static final String TRANSACTION_IDENTIFIER_NET_SALES = "014";
	public static final String TRANSACTION_IDENTIFIER_RETURNS = "015";
	public static final String TRANSACTION_IDENTIFIER_EMPLOYEE_SALES = "016";
	public static final String TRANSACTION_IDENTIFIER_TAXABLE_SALES = "017";
	public static final String TRANSACTION_IDENTIFIER_NON_TAXABLE_SALES = "018";
	public static final String TRANSACTION_IDENTIFIER_LUXURY_TAX = "019";
	public static final String TRANSACTION_IDENTIFIER_CANCELS = "020";
	public static final String TRANSACTION_IDENTIFIER_POST_VOIDS = "021";
	public static final String TRANSACTION_IDENTIFIER_NO_SALES = "022";
	public static final String TRANSACTION_IDENTIFIER_NEW_LAYAWAYS = "023";
	public static final String TRANSACTION_IDENTIFIER_CANCELLED_LAYAWAYS = "024";
	public static final String TRANSACTION_IDENTIFIER_ADJUSTED_LAYAWAYS = "025";
	public static final String TRANSACTION_IDENTIFIER_NEW_LAYAWAY_DEPOSITS = "026";
	public static final String TRANSACTION_IDENTIFIER_LAYAWAY_FEES = "027";
	public static final String TRANSACTION_IDENTIFIER_CANCELLED_LAYAWAY_DEPOSIT = "028";
	public static final String TRANSACTION_IDENTIFIER_PAYINS = "029";
	public static final String TRANSACTION_IDENTIFIER_PICKUPS = "030";
	public static final String TRANSACTION_IDENTIFIER_LAYAWAY_PICKUPS = "031";
	public static final String TRANSACTION_IDENTIFIER_RESTOCK_LAYAWAY_DEPOSITS = "032";
	public static final String TRANSACTION_IDENTIFIER_LAYAWAY_APPLIED_DEPOSITS = "033";
	public static final String TRANSACTION_IDENTIFIER_LAYAWAY_BOOKED = "034";
	public static final String TRANSACTION_IDENTIFIER_LAYAWAY_FINAL = "035";
	public static final String TRANSACTION_IDENTIFIER_TRANSACTION_DISCOUNT = "036";
	public static final String TRANSACTION_IDENTIFIER_PICKUP_NO_REDUCE = "037";
	public static final String TRANSACTION_IDENTIFIER_HST_SALES_TAX = "038";
	public static final String TRANSACTION_IDENTIFIER_STORE_CREDIT_ISSUE = "039";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_PAYMENTS = "040";
	public static final String TRANSACTION_IDENTIFIER_NEW_SPECIAL_ORDER = "041";
	public static final String TRANSACTION_IDENTIFIER_CANCELLED_SPECIAL_ORDER = "042";
	public static final String TRANSACTION_IDENTIFIER_ADJUSTED_SPECIAL_ORDER = "043";
	public static final String TRANSACTION_IDENTIFIER_NEW_SPECIAL_ORDER_DEPOSIT = "044";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_FEES = "045";
	public static final String TRANSACTION_IDENTIFIER_CANCELLED_SPECIAL_ORDER_DEPOSITS = "046";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_PICKUPS = "047";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_RESTOCK = "048";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_APPLIED_DEPOSITS = "049";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_BOOKED = "050";
	public static final String TRANSACTION_IDENTIFIER_SPECIAL_ORDER_FINAL = "051";
	public static final String TRANSACTION_IDENTIFIER_CHARITY_DONATIONS = "052";
	public static final String TRANSACTION_IDENTIFIER_GIFT_CARD_SALES = "053";
	public static final String TRANSACTION_IDENTIFIER_ITEM_PROMO_DISCOUNT = "054";
	public static final String TRANSACTION_IDENTIFIER_TRANSACTION_PROMO_DISCOUNT = "055";
	public static final String TRANSACTION_IDENTIFIER_SALES_TAXABLE_AMOUNT_2_VALUE = "056";
	public static final String TRANSACTION_IDENTIFIER_SALES_NON_TAXABLE_AMOUNT_2_VALUE = "057";
	public static final String TRANSACTION_IDENTIFIER_PLU_SALE_PRICE_DISCOUNTS = "058";
	public static final String TRANSACTION_IDENTIFIER_SALES_REMOTE_TAX = "059";
	public static final String TRANSACTION_IDENTIFIER_ROUNDING_ADJUSTMENT = "060";

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 24;
		id = "037";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Transaction Identifier", new FieldDetails(3, 4, ""));
		fields.put("Count", new FieldDetails(6, 7, "zero filled"));
		fields.put("Amount", new FieldDetails(10, 13, "zero filled"));
		fields.put("Amount Sign", new FieldDetails(1, 23, "1 = Negative"));
		fields.put("Currency Indicator", new FieldDetails(1, 24, "1 = Alternate, 0 = Primary"));
	}

	public ForInStoreReportingUseOnly() {
		super();
	}

	public ForInStoreReportingUseOnly(String record) {
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

	public ForInStoreReportingUseOnly parse(String transactionIdentifier, List<Order> orders) throws Exception {
		int count = 0;
		int amount = 0;

		if (TRANSACTION_IDENTIFIER_MERCHANDISE_SALES.equals(transactionIdentifier)) {
			count = orders.size();
			for (Order order : orders) {
				// check values of the order payload and make sure I don't have to subtract processing fee
				amount += order.getNetAmounts() != null ? order.getNetAmounts().getTotalMoney().getAmount() : 0;
			}
		} else if (TRANSACTION_IDENTIFIER_DISCOUNTS.equals(transactionIdentifier)) {
			for (Order order : orders) {
				if (order.getTotalDiscountMoney() != null && order.getTotalDiscountMoney().getAmount() != 0) {
					count += 1;
					amount -= order.getTotalDiscountMoney().getAmount();
				}
			}
		} else if (TRANSACTION_IDENTIFIER_SALES_TAX.equals(transactionIdentifier)) {
			for (Order order : orders) {
				if (order.getTotalTaxMoney() != null && order.getTotalTaxMoney().getAmount() > 0) {
					count += 1;
					amount += order.getTotalTaxMoney().getAmount();
				}
			}
		}

		putValue("Transaction Identifier", transactionIdentifier);
		putValue("Count", "" + count);
		putValue("Amount", "" + Math.abs(amount));
		putValue("Amount Sign", amount >= 0 ? "0" : "1");
		putValue("Currency Indicator", "0"); // 0 is primary; other value not supported

		return this;
	}
}
