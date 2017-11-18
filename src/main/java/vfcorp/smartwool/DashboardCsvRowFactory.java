package vfcorp.smartwool;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentModifier;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Transaction;

import util.TimeManager;

public class DashboardCsvRowFactory {
	public static final String PRODUCT_TYPE_REGISTER = "REGISTER";
	public static final String PRODUCT_TYPE_REGISTER_LABEL = "Point of Sale";

	private final String DETAILS_URL_PREFIX = "http://squareup.com/dashboard/sales/transactions/";
    private final String DETAULS_URL_DELIMETER = "/by-unit/";

	public List<String> generateTransactionCsvRow(Payment payment, Transaction transaction, Customer customer, String locationName, String timeZoneId) throws Exception {
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
		fields.add(""); //Partial refunds
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
		fields.add(getPaymentIds(payment));
		fields.add(emptyStringIfNull(tenders.getCardBrands()));
		fields.add(emptyStringIfNull(tenders.getPanSuffi()));
		fields.add(emptyStringIfNull(payment.getDevice().getName()));
		fields.add("");
		fields.add("");
		fields.add(getDetailsUrl(transaction));
		fields.add(getItemizationSummary(payment));
		fields.add(Constants.EVENT_TYPE_PAYMENT);
		fields.add(locationName);
		fields.add(""); //Dining options
		fields.addAll(getCustomerInfo(customer));
		fields.add("");//Device nickname, can't get this

		return fields;
	}
	public List<String> generateItemCsvRow(Payment payment, PaymentItemization itemization, Transaction transaction, Customer customer, String locationName, String timeZoneId) throws Exception {
		ArrayList<String> fields = new ArrayList<String>();

		fields.add(getDateWithFormat(payment.getCreatedAt(), timeZoneId, Constants.DATE_FORMAT));
		fields.add(getDateWithFormat(payment.getCreatedAt(), timeZoneId, Constants.TIME_FORMAT));
		fields.add(getTimeZoneLabel(timeZoneId));
		fields.add(itemization.getItemDetail().getCategoryName()); //Category
		fields.add(itemization.getName()); //Item name
		fields.add(itemization.getQuantity().toString()); //Qty
		fields.add(emptyStringIfNull(itemization.getItemVariationName())); //Price point name
		fields.add(getSku(itemization));//Sku
		fields.add(getModifiers(itemization));//Modifiers
		fields.add(getCurrencyString(itemization.getGrossSalesMoney().getAmount()));//Gross Sales
		fields.add(getCurrencyString(itemization.getDiscountMoney().getAmount()));//Discounts
		fields.add(getCurrencyString(itemization.getNetSalesMoney().getAmount()));//Net Sales
		fields.add(getTaxes(itemization));//Tax
		fields.add(transaction.getId());//Transaction ID
		fields.add(payment.getId());//Payment ID
		fields.add(getDeviceName(payment));//Device name
		fields.add(emptyStringIfNull(itemization.getNotes()));//Notes
		fields.add(getDetailsUrl(transaction));//Details
		fields.add(Constants.EVENT_TYPE_PAYMENT); //Event Type
		fields.add(locationName); //Location
		fields.add(""); //TODO: Dining Option
		fields.addAll(getCustomerInfo(customer)); //Customer ID, Name & Reference ID
		fields.add(""); //Device nickname

		return fields;
	}
	private String getPaymentIds(Payment payment) {
		if (payment.getTender().length > 1) {
			ArrayList<String> paymentIds = new ArrayList<String>();
			for (Tender tender : payment.getTender()) {
				paymentIds.add(tender.getId());
			}
			Collections.sort(paymentIds);
			return String.join(", ", paymentIds);
		};
		return payment.getId();
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
	private String getDetailsUrl(Transaction transaction) {
		return DETAILS_URL_PREFIX + transaction.getId() + DETAULS_URL_DELIMETER + transaction.getLocationId();
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
