package vfcorp.smartwool;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentModifier;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Refund;
import com.squareup.connect.v2.Transaction;

import util.Constants;
import util.LocationContext;
import util.TimeManager;

public class DashboardCsvRowFactory {
	public static final String PRODUCT_TYPE_REGISTER = "REGISTER";
	public static final String PRODUCT_TYPE_REGISTER_LABEL = "Point of Sale";

	private final String DETAILS_URL_ROUTE = "/dashboard/sales/transactions/";
	private final String DETAULS_URL_DELIMETER = "/by-unit/";

	public List<String> generateTransactionCsvRow(Payment payment, Transaction transaction, Customer customer,
			String locationName, String timeZoneId, String domainUrl) throws Exception {
		ArrayList<String> fields = new ArrayList<String>();
		DashboardCsvTenderSummary tenders = DashboardCsvTenderSummary.generateTenderSummary(transaction);
		fields.add(getDateWithFormat(payment.getCreatedAt(), timeZoneId, Constants.DATE_FORMAT));
		fields.add(getDateWithFormat(payment.getCreatedAt(), timeZoneId, Constants.TIME_FORMAT));
		fields.add(getTimeZoneLabel(timeZoneId));
		fields.add(getCurrencyString(payment.getGrossSalesMoney().getAmount()));
		fields.add(getCurrencyString(payment.getDiscountMoney().getAmount()));
		fields.add(getCurrencyString(payment.getNetSalesMoney().getAmount()));
		fields.add(getCurrencyString(getGiftCardSales(payment)));
		fields.add(getCurrencyString(payment.getTaxMoney().getAmount()));
		fields.add(getCurrencyString(payment.getTipMoney().getAmount()));
		fields.add(""); // Partial refunds
		fields.add(getCurrencyString(payment.getTotalCollectedMoney().getAmount()));
		fields.add(getProductTypeLabel(transaction.getProduct()));
		fields.add(getCurrencyString(tenders.getCard()));
		fields.add(tenders.getCardEntryMethods());
		fields.add(getCurrencyString(tenders.getCash()));
		fields.add(getCurrencyString(tenders.getGiftCard()));
		fields.add(getCurrencyString(0));
		fields.add("");
		fields.add("");
		fields.add(getCurrencyString(payment.getProcessingFeeMoney().getAmount()));
		fields.add(getCurrencyString(payment.getNetTotalMoney().getAmount()));
		fields.add(transaction.getId());
		fields.add(getTenderIds(payment));
		fields.add(emptyStringIfNull(tenders.getCardBrands()));
		fields.add(emptyStringIfNull(tenders.getPanSuffixes()));
		fields.add(emptyStringIfNull(payment.getDevice().getName()));
		fields.add("");
		fields.add("");
		fields.add(getDetailsUrl(transaction, domainUrl));
		fields.add(getItemizationSummary(payment));
		fields.add(Constants.EVENT_TYPE_PAYMENT);
		fields.add(locationName);
		fields.add(""); // Dining options
		fields.addAll(getCustomerInfo(customer));
		fields.add("");// Device nickname, can't get this
		return fields;
	}

	public List<String> generateItemCsvRow(Payment payment, PaymentItemization itemization, Transaction transaction,
			Customer customer, String locationName, String timeZoneId, String domainUrl) throws Exception {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(getDateWithFormat(payment.getCreatedAt(), timeZoneId, Constants.DATE_FORMAT));
		fields.add(getDateWithFormat(payment.getCreatedAt(), timeZoneId, Constants.TIME_FORMAT));
		fields.add(getTimeZoneLabel(timeZoneId));
		fields.add(itemization.getItemDetail().getCategoryName()); // Category
		fields.add(itemization.getName()); // Item name
		fields.add(itemization.getQuantity().toString()); // Qty
		fields.add(emptyStringIfNull(itemization.getItemVariationName())); // Price
																			// point
																			// name
		fields.add(getSku(itemization));// Sku
		fields.add(getModifiers(itemization));// Modifiers
		fields.add(getCurrencyString(itemization.getGrossSalesMoney().getAmount()));// Gross
																					// Sales
		fields.add(getCurrencyString(itemization.getDiscountMoney().getAmount()));// Discounts
		fields.add(getCurrencyString(itemization.getNetSalesMoney().getAmount()));// Net
																					// Sales
		fields.add(getTaxes(itemization));// Tax
		fields.add(transaction.getId());// Transaction ID
		fields.add(payment.getId());// Payment ID
		fields.add(getDeviceName(payment));// Device name
		fields.add(emptyStringIfNull(itemization.getNotes()));// Notes
		fields.add(getDetailsUrl(transaction, domainUrl));// Details
		fields.add(Constants.EVENT_TYPE_PAYMENT); // Event Type
		fields.add(locationName); // Location
		fields.add(""); // TODO: Dining Option
		fields.addAll(getCustomerInfo(customer)); // Customer ID, Name &
													// Reference ID
		fields.add(""); // Device nickname
		return fields;
	}

	public List<String> generateRefundCsvRow(Refund refund, LocationContext locationCtx, String timeZoneId,
			String domainUrl) throws Exception {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(getDateWithFormat(refund.getCreatedAt(), timeZoneId, Constants.DATE_FORMAT));
		fields.add(getDateWithFormat(refund.getCreatedAt(), timeZoneId, Constants.TIME_FORMAT));
		fields.add(getTimeZoneLabel(timeZoneId));
		fields.add(""); // gross sales
		fields.add(""); // discounts sales
		fields.add(""); // net sales
		fields.add(""); // gift card sales
		fields.add(""); // tax
		fields.add(""); // tip
		fields.add(""); // partial refunds
		fields.add(getCurrencyString(Math.negateExact(refund.getAmountMoney().getAmount())));
		fields.add(""); // source
		fields.add(""); // card
		fields.add(""); // card entry methods
		fields.add(""); // cash
		fields.add(""); // square gift card
		fields.add(""); // other tender
		fields.add(""); // other tender type
		fields.add(""); // other tender note
		fields.add(getCurrencyString(Math.negateExact(refund.getProcessingFeeMoney().getAmount())));
		fields.add(""); // net total
		fields.add(refund.getTransactionId());
		fields.add(refund.getTenderId());
		fields.add(""); // card brand
		fields.add(""); // pan suffix
		fields.add(""); // device name
		fields.add(""); // staff name
		fields.add(""); // staff id
		fields.add(getDetailsUrl(refund, domainUrl));
		fields.add(""); // description
		fields.add("Refund"); //
		fields.add(locationCtx.getName());
		fields.add(""); // dining option
		fields.add(""); // customer id
		fields.add(""); // customer name
		fields.add(""); // customer reference
		fields.add(""); // device nickname
		fields.add(""); // third party fees
		return fields;
	}

	private String getTenderIds(Payment payment) {
		if (payment.getTender().length > 1) {
			ArrayList<String> paymentIds = new ArrayList<String>();
			for (Tender tender : payment.getTender()) {
				paymentIds.add(tender.getId());
			}
			return String.join(", ", paymentIds);
		}
		;
		return payment.getTender()[0].getId();
	}

	private String emptyStringIfNull(String str) {
		if (str == null) {
			return "";
		} else {
			return str;
		}
	}

	private String getDateWithFormat(String iso8601string, String timeZoneId, String format) throws ParseException {
		return TimeManager.toSimpleDateTimeInTimeZone(iso8601string, timeZoneId, format);
	}

	private String getDetailsUrl(Transaction transaction, String domainUrl) {
		return domainUrl + DETAILS_URL_ROUTE + transaction.getId() + DETAULS_URL_DELIMETER
				+ transaction.getLocationId();
	}

	private String getDetailsUrl(Refund refund, String domainUrl) {
		return domainUrl + DETAILS_URL_ROUTE + refund.getTransactionId() + DETAULS_URL_DELIMETER
				+ refund.getLocationId();
	}

	private String getDeviceName(Payment payment) {
		if (payment.getDevice() != null && payment.getDevice().getName() != null) {
			return payment.getDevice().getName();
		}
		return "";
	}

	private int getGiftCardSales(Payment payment) {
		int giftCardSales = 0;
		for (PaymentItemization itemization : payment.getItemizations()) {
			String itemizationType = itemization.getItemizationType();
			if (itemizationType.equals("GIFT_CARD_ACTIVATION") || itemizationType.equals("GIFT_CARD_RELOAD")
					|| itemizationType.equals("GIFT_CARD_UNKNOWN")) {
				giftCardSales += itemization.getNetSalesMoney().getAmount();
			}
		}
		return giftCardSales;
	}

	private String getItemizationSummary(Payment payment) {
		List<String> itemizationSummary = new ArrayList<String>();
		for (PaymentItemization itemization : payment.getItemizations()) {
			StringBuilder buff = new StringBuilder();

			if (itemization.getQuantity().intValue() > 1) {
				buff.append(itemization.getQuantity().intValue());
				buff.append(" x ");
			}

			buff.append(itemization.getName());
			buff.append(" (");
			buff.append(itemization.getItemVariationName());
			buff.append(")");

			if (itemization.getNotes() != null && !itemization.getNotes().equals("")) {
				buff.append(" - ");
				buff.append(itemization.getNotes());
			}
			itemizationSummary.add(buff.toString());
		}
		return itemizationSummary.size() > 0 ? String.join(", ", itemizationSummary) : "";
	}

	private String getTimeZoneLabel(String timeZoneId) throws Exception {
		if (!timeZoneId.equals(Constants.PST_TIME_ZONE_ID)) {
			throw new Exception("Unknown timezone Id: " + timeZoneId);
		}
		return Constants.PST_TIME_ZONE_LABEL;
	}

	private List<String> getCustomerInfo(Customer customer) {
		if (customer != null) {
			String id = customer.getId();
			String name = emptyStringIfNull(customer.getName());
			String refId = emptyStringIfNull(customer.getReferenceId());
			return Arrays.asList(id, name, refId);
		} else {
			return Arrays.asList("", "", "");
		}
	}

	private String getCurrencyString(int num) {
		NumberFormat defaultFormat = NumberFormat.getCurrencyInstance(Locale.US);
		if (num < 0) {
			return "(" + defaultFormat.format(Math.abs(num) / 100.0) + ")";
		}
		return defaultFormat.format(num / 100.0);
	}

	private String getModifiers(PaymentItemization itemization) {
		if (itemization.getModifiers().length > 0) {
			ArrayList<String> modsApplied = new ArrayList<String>();
			for (PaymentModifier modifier : itemization.getModifiers()) {
				modsApplied.add(modifier.getName());
			}
			return String.join(", ", modsApplied);
		}
		return "";
	}

	private String getProductTypeLabel(String productType) throws Exception {
		if (productType.equals(PRODUCT_TYPE_REGISTER)) {
			return PRODUCT_TYPE_REGISTER_LABEL;
		}
		throw new Exception("Unknown product type: " + productType);
	}

	private String getSku(PaymentItemization itemization) {
		if (itemization.getItemDetail() != null) {
			return itemization.getItemDetail().getSku();
		}
		return "";
	}

	private String getTaxes(PaymentItemization itemization) {
		if (itemization.getTaxes().length > 0) {
			int taxes = 0;
			for (PaymentTax tax : itemization.getTaxes()) {
				taxes += tax.getAppliedMoney().getAmount();
			}
			return getCurrencyString(taxes);
		}
		return "";
	}
}
