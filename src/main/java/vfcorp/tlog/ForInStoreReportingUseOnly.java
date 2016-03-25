package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class ForInStoreReportingUseOnly extends Record {

	public static enum TransactionIdentifier {
		ITEM_VOIDS,
		MERCHANDISE_SALES,
		MERCHANDISE_RETURNS,
		NON_MERCHANDISE_SALES,
		GIFT_CERTIFICATE_SALES,
		FEES,
		ON_ACCOUNT_PAYMENTS,
		PAYOUTS,
		DISCOUNTS,
		EMPLOYEE_DISCOUNTS,
		CUSTOMER_DISCOUNTS,
		LAYAWAY_PAYMENTS,
		SALES_TAX,
		NET_SALES,
		RETURNS,
		EMPLOYEE_SALES,
		TAXABLE_SALES,
		NON_TAXABLE_SALES,
		LUXURY_TAX,
		CANCELS,
		POST_VOIDS,
		NO_SALES,
		NEW_LAYAWAYS,
		CANCELLED_LAYAWAYS,
		ADJUSTED_LAYAWAYS,
		NEW_LAYAWAY_DEPOSITS,
		LAYAWAY_FEES,
		CANCELLED_LAYAWAY_DEPOSIT,
		PAYINS,
		PICKUPS,
		LAYAWAY_PICKUPS,
		RESTOCK_LAYAWAY_DEPOSITS,
		LAYAWAY_APPLIED_DEPOSITS,
		LAYAWAY_BOOKED,
		LAYAWAY_FINAL,
		TRANSACTION_DISCOUNT,
		PICKUP_NO_REDUCE,
		HST_SALES_TAX,
		STORE_CREDIT_ISSUE,
		SPECIAL_ORDER_PAYMENTS,
		NEW_SPECIAL_ORDER,
		CANCELLED_SPECIAL_ORDER,
		ADJUSTED_SPECIAL_ORDER,
		NEW_SPECIAL_ORDER_DEPOSIT,
		SPECIAL_ORDER_FEES,
		CANCELLED_SPECIAL_ORDER_DEPOSITS,
		SPECIAL_ORDER_PICKUPS,
		SPECIAL_ORDER_RESTOCK,
		SPECIAL_ORDER_APPLIED_DEPOSITS,
		SPECIAL_ORDER_BOOKED,
		SPECIAL_ORDER_FINAL,
		CHARITY_DONATIONS,
		GIFT_CARD_SALES,
		ITEM_PROMO_DISCOUNT,
		TRANSACTION_PROMO_DISCOUNT,
		SALES_TAXABLE_AMOUNT_2_VALUE,
		SALES_NON_TAXABLE_AMOUNT_2_VALUE,
		PLU_SALE_PRICE_DISCOUNTS,
		SALES_REMOTE_TAX,
		ROUNDING_ADJUSTMENT
	}
	
	private static Map<String,FieldDetails> fields;
	private static Map<TransactionIdentifier,String> transactionIdentifiers;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		transactionIdentifiers = new HashMap<TransactionIdentifier,String>();
		length = 24;
		id = "037";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Transaction Identifier", new FieldDetails(3, 4, ""));
		fields.put("Count", new FieldDetails(6, 7, "zero filled"));
		fields.put("Amount", new FieldDetails(10, 13, "zero filled"));
		fields.put("Amount Sign", new FieldDetails(1, 23, "1 = Negative"));
		fields.put("Currency Indicator", new FieldDetails(1, 24, "1 = Alternate, 0 = Primary"));
		
		transactionIdentifiers.put(TransactionIdentifier.ITEM_VOIDS, "001");
		transactionIdentifiers.put(TransactionIdentifier.MERCHANDISE_SALES, "002");
		transactionIdentifiers.put(TransactionIdentifier.MERCHANDISE_RETURNS, "003");
		transactionIdentifiers.put(TransactionIdentifier.NON_MERCHANDISE_SALES, "004");
		transactionIdentifiers.put(TransactionIdentifier.GIFT_CERTIFICATE_SALES, "005");
		transactionIdentifiers.put(TransactionIdentifier.FEES, "006");
		transactionIdentifiers.put(TransactionIdentifier.ON_ACCOUNT_PAYMENTS, "007");
		transactionIdentifiers.put(TransactionIdentifier.PAYOUTS, "008");
		transactionIdentifiers.put(TransactionIdentifier.DISCOUNTS, "009");
		transactionIdentifiers.put(TransactionIdentifier.EMPLOYEE_DISCOUNTS, "010");
		transactionIdentifiers.put(TransactionIdentifier.CUSTOMER_DISCOUNTS, "011");
		transactionIdentifiers.put(TransactionIdentifier.LAYAWAY_PAYMENTS, "012");
		transactionIdentifiers.put(TransactionIdentifier.SALES_TAX, "013");
		transactionIdentifiers.put(TransactionIdentifier.NET_SALES, "014");
		transactionIdentifiers.put(TransactionIdentifier.RETURNS, "015");
		transactionIdentifiers.put(TransactionIdentifier.EMPLOYEE_SALES, "016");
		transactionIdentifiers.put(TransactionIdentifier.TAXABLE_SALES, "017");
		transactionIdentifiers.put(TransactionIdentifier.NON_TAXABLE_SALES, "018");
		transactionIdentifiers.put(TransactionIdentifier.LUXURY_TAX, "019");
		transactionIdentifiers.put(TransactionIdentifier.CANCELS, "020");
		transactionIdentifiers.put(TransactionIdentifier.POST_VOIDS, "021");
		transactionIdentifiers.put(TransactionIdentifier.NO_SALES, "022");
		transactionIdentifiers.put(TransactionIdentifier.NEW_LAYAWAYS, "023");
		transactionIdentifiers.put(TransactionIdentifier.CANCELLED_LAYAWAYS, "024");
		transactionIdentifiers.put(TransactionIdentifier.ADJUSTED_LAYAWAYS, "025");
		transactionIdentifiers.put(TransactionIdentifier.NEW_LAYAWAY_DEPOSITS, "026");
		transactionIdentifiers.put(TransactionIdentifier.LAYAWAY_FEES, "027");
		transactionIdentifiers.put(TransactionIdentifier.CANCELLED_LAYAWAY_DEPOSIT, "028");
		transactionIdentifiers.put(TransactionIdentifier.PAYINS, "029");
		transactionIdentifiers.put(TransactionIdentifier.PICKUPS, "030");
		transactionIdentifiers.put(TransactionIdentifier.LAYAWAY_PICKUPS, "031");
		transactionIdentifiers.put(TransactionIdentifier.RESTOCK_LAYAWAY_DEPOSITS, "032");
		transactionIdentifiers.put(TransactionIdentifier.LAYAWAY_APPLIED_DEPOSITS, "033");
		transactionIdentifiers.put(TransactionIdentifier.LAYAWAY_BOOKED, "034");
		transactionIdentifiers.put(TransactionIdentifier.LAYAWAY_FINAL, "035");
		transactionIdentifiers.put(TransactionIdentifier.TRANSACTION_DISCOUNT, "036");
		transactionIdentifiers.put(TransactionIdentifier.PICKUP_NO_REDUCE, "037");
		transactionIdentifiers.put(TransactionIdentifier.HST_SALES_TAX, "038");
		transactionIdentifiers.put(TransactionIdentifier.STORE_CREDIT_ISSUE, "039");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_PAYMENTS, "040");
		transactionIdentifiers.put(TransactionIdentifier.NEW_SPECIAL_ORDER, "041");
		transactionIdentifiers.put(TransactionIdentifier.CANCELLED_SPECIAL_ORDER, "042");
		transactionIdentifiers.put(TransactionIdentifier.ADJUSTED_SPECIAL_ORDER, "043");
		transactionIdentifiers.put(TransactionIdentifier.NEW_SPECIAL_ORDER_DEPOSIT, "044");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_FEES, "045");
		transactionIdentifiers.put(TransactionIdentifier.CANCELLED_SPECIAL_ORDER_DEPOSITS, "046");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_PICKUPS, "047");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_RESTOCK, "048");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_APPLIED_DEPOSITS, "049");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_BOOKED, "050");
		transactionIdentifiers.put(TransactionIdentifier.SPECIAL_ORDER_FINAL, "051");
		transactionIdentifiers.put(TransactionIdentifier.CHARITY_DONATIONS, "052");
		transactionIdentifiers.put(TransactionIdentifier.GIFT_CARD_SALES, "053");
		transactionIdentifiers.put(TransactionIdentifier.ITEM_PROMO_DISCOUNT, "054");
		transactionIdentifiers.put(TransactionIdentifier.TRANSACTION_PROMO_DISCOUNT, "055");
		transactionIdentifiers.put(TransactionIdentifier.SALES_TAXABLE_AMOUNT_2_VALUE, "056");
		transactionIdentifiers.put(TransactionIdentifier.SALES_NON_TAXABLE_AMOUNT_2_VALUE, "057");
		transactionIdentifiers.put(TransactionIdentifier.PLU_SALE_PRICE_DISCOUNTS, "058");
		transactionIdentifiers.put(TransactionIdentifier.SALES_REMOTE_TAX, "059");
		transactionIdentifiers.put(TransactionIdentifier.ROUNDING_ADJUSTMENT, "060");
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
	
	public ForInStoreReportingUseOnly parse(TransactionIdentifier transactionIdentifier, List<Payment> squarePayments) {
		int count = 0;
		int amount = 0;
		
		if (transactionIdentifier == TransactionIdentifier.MERCHANDISE_SALES) {
			count = squarePayments.size();
			for (Payment payment : squarePayments) {
				amount += payment.getNetTotalMoney().getAmount();
			}
		} else if (transactionIdentifier == TransactionIdentifier.MERCHANDISE_RETURNS) {
			// TODO(colinlam): do this
		} else if (transactionIdentifier == TransactionIdentifier.DISCOUNTS) {
			for (Payment payment : squarePayments) {
				if (payment.getDiscountMoney().getAmount() > 0) {
					count += 1;
					amount += payment.getDiscountMoney().getAmount();
				}
			}
		} else if (transactionIdentifier == TransactionIdentifier.SALES_TAX) {
			for (Payment payment : squarePayments) {
				if (payment.getTaxMoney().getAmount() > 0) {
					count += 1;
					amount += payment.getTaxMoney().getAmount();
				}
			}
		} else if (transactionIdentifier == TransactionIdentifier.NET_SALES) {
			// TODO(colinlam): what is the difference between net sales and merchandise sales?
		} else if (transactionIdentifier == TransactionIdentifier.RETURNS) {
			// TODO(colinlam): what is the difference between returns and merchandise returns?
		} else if (transactionIdentifier == TransactionIdentifier.TAXABLE_SALES) {
			// TODO(colinlam): what is taxable sales?
		} else if (transactionIdentifier == TransactionIdentifier.NON_TAXABLE_SALES) {
			// TODO(colinlam): what is non-taxable sales?
		} else if (transactionIdentifier == TransactionIdentifier.TRANSACTION_DISCOUNT) {
			// TODO(colinlam): what is a transaction discount?
		}
		
		putValue("Transaction Identifier", transactionIdentifiers.get(transactionIdentifier));
		putValue("Count", "" + count);
		putValue("Amount", "" + amount);
		putValue("Amount Sign", amount >= 0 ? "0" : "1");
		putValue("Currency Indicator", "0"); // 0 is primary; other value not supported
		
		return this;
	}
}
