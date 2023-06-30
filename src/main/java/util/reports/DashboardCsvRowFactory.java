package util.reports;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.ProcessingFee;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemAppliedTax;
import com.squareup.connect.v2.OrderLineItemModifier;

import util.Constants;
import util.LocationContext;

public class DashboardCsvRowFactory {
	private static final String PRODUCT_TYPE_OTHER_LABEL = "OTHER";
	private static final String PRODUCT_TYPE_ECOMMERCE_API_LABEL = "eCommerce Integrations";
	private static final String PRODUCT_TYPE_ECOMMERCE_API = "ECOMMERCE_API";
	public static final String PRODUCT_TYPE_REGISTER = "SQUARE_POS";
	public static final String PRODUCT_TYPE_RETAIL = "RETAIL";
	public static final String PRODUCT_TYPE_OTHER = "OTHER";
	public static final String PRODUCT_TYPE_REGISTER_LABEL = "Point of Sale";

	private final String DETAILS_URL_ROUTE = "/dashboard/sales/transactions/";
	private final String DETAULS_URL_DELIMETER = "/by-unit/";

	public List<String> generateTransactionCsvRow(Order order, Map<String, Payment> tenderToPayment, Customer customer,
			LocationContext locationContext, String domainUrl) throws Exception {
		if(order.getId().equals("PPe44r8TqclZuqAHXZNozJ8eV")) {
			System.out.println("HERE");
		}
		ArrayList<String> fields = new ArrayList<String>();
		DashboardCsvTenderSummary tenders = DashboardCsvTenderSummary.generateTenderSummary(order);
		int totalMoney = order.getTotalMoney() != null ? order.getTotalMoney().getAmount() : 0;
    	int totalTaxMoney = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
    	int totalDiscountMoney = order.getTotalDiscountMoney() != null ? order.getTotalDiscountMoney().getAmount() : 0;
    	int totalTipMoney = order.getTotalTipMoney() != null ? order.getTotalTipMoney().getAmount() : 0;
    	int netAmounts = order.getNetAmounts() != null && order.getNetAmounts().getTotalMoney() != null ? order.getNetAmounts().getTotalMoney().getAmount() : 0;
		fields.add(getDate(order.getCreatedAt()));
		fields.add(getTime(order.getCreatedAt()));
		fields.add(getTimeZoneLabel(locationContext.getTimezone()));
		fields.add(getCurrencyString(totalMoney - totalTaxMoney + totalDiscountMoney - totalTipMoney));
		fields.add(getCurrencyString(-totalDiscountMoney));
		fields.add(getCurrencyString(totalMoney - totalTaxMoney - totalTipMoney));
		fields.add(getCurrencyString(getGiftCardSales(order)));
		fields.add(getCurrencyString(totalTaxMoney));
		fields.add(getCurrencyString(totalTipMoney));
		fields.add(""); // Partial refunds
		fields.add(getCurrencyString(netAmounts));
		fields.add(getProductTypeLabel(order, tenderToPayment));
		fields.add(getCurrencyString(tenders.getCard()));
		fields.add(tenders.getCardEntryMethods());
		fields.add(getCurrencyString(tenders.getCash()));
		fields.add(getCurrencyString(tenders.getGiftCard()));
		fields.add(getCurrencyString(0));
		fields.add("");
		fields.add("");
     	int totalProcessingFee = order.getTenders() != null ? Arrays.stream(order.getTenders())
         		.map(Tender::getProcessingFeeMoney)
         		.filter(Objects::nonNull)
         		.mapToInt(Money::getAmount)
         		.sum() : 0;
		fields.add(getCurrencyString(-totalProcessingFee));
		fields.add(getCurrencyString(netAmounts - totalProcessingFee));
		fields.add(order.getId());
		fields.add(getTenderIds(order));
		fields.add(emptyStringIfNull(tenders.getCardBrands()));
		fields.add(emptyStringIfNull(tenders.getPanSuffixes()));
		fields.add(emptyStringIfNull(getDeviceName(order, tenderToPayment)));
		fields.add("");
		fields.add("");
		fields.add(getDetailsUrl(order, domainUrl));
		fields.add(getItemizationSummary(order));
		fields.add(Constants.EVENT_TYPE_PAYMENT);
		fields.add(locationContext.getName());
		fields.add(""); // Dining options
		fields.addAll(getCustomerInfo(customer));
		fields.add("");// Device nickname, can't get this
		return fields;
	}

	public List<String> generateItemCsvRow(Order order, OrderLineItem lineItem,
			Map<String, CatalogObject> catalogMap, Map<String, CatalogObject> lineItemCategories, Map<String, Payment> tenderToPayment,
			Customer customer, LocationContext locationCtx, String domainUrl) throws Exception {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(getDate(order.getCreatedAt()));
		fields.add(getTime(order.getCreatedAt()));
		fields.add(getTimeZoneLabel(locationCtx.getTimezone()));
		CatalogObject itemVariation = catalogMap.get(lineItem.getCatalogObjectId());
		CatalogObject category = lineItemCategories.get(lineItem.getCatalogObjectId());
		fields.add(emptyStringIfNull(category != null && category.getCategoryData() != null ? category.getCategoryData().getName() : "")); // Category
		fields.add(lineItem.getName()); // Item name
		fields.add(lineItem.getQuantity().toString()); // Qty
		fields.add(emptyStringIfNull(lineItem.getVariationName())); // name
		fields.add(emptyStringIfNull(itemVariation != null && itemVariation.getItemVariationData() != null ? itemVariation.getItemVariationData().getSku() : ""));// Sku
		fields.add(getModifiers(lineItem));// Modifiers
		fields.add(getCurrencyString(lineItem.getGrossSalesMoney().getAmount()));// Gross
																					// Sales
		fields.add(getCurrencyString(-lineItem.getTotalDiscountMoney().getAmount()));// Discounts
		fields.add(getCurrencyString(lineItem.getGrossSalesMoney().getAmount() - lineItem.getTotalDiscountMoney().getAmount()));// Net
																					// Sales
		fields.add(getTaxes(lineItem));// Tax
		fields.add(order.getId());// Transaction ID
		String paymentId = order.getTenders() != null && order.getTenders().length > 0 ? order.getTenders()[0].getId() : "";
		fields.add(paymentId);
		fields.add(getDeviceName(order, tenderToPayment));// Device name
		fields.add(emptyStringIfNull(lineItem.getNote()));// Notes
		fields.add(getDetailsUrl(order, domainUrl));// Details
		fields.add(Constants.EVENT_TYPE_PAYMENT); // Event Type
		fields.add(locationCtx.getName()); // Location
		fields.add(""); // TODO: Dining Option
		fields.addAll(getCustomerInfo(customer)); // Customer ID, Name &
													// Reference ID
		fields.add(""); // Device nickname
		return fields;
	}

	public List<String> generateRefundCsvRow(PaymentRefund refund, LocationContext locationCtx, String domainUrl)
			throws Exception {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(getDate(refund.getCreatedAt()));
		fields.add(getTime(refund.getCreatedAt()));
		fields.add(getTimeZoneLabel(locationCtx.getTimezone()));
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

     	int totalProcessingFee = refund.getProcessingFee() != null ? Arrays.stream(refund.getProcessingFee())
         		.map(ProcessingFee::getAmountMoney)
         		.filter(Objects::nonNull)
         		.mapToInt(Money::getAmount)
         		.sum() : 0;
		fields.add(getCurrencyString(Math.negateExact(totalProcessingFee)));
		fields.add("");// net total
		fields.add(refund.getOrderId());
		fields.add(refund.getPaymentId());
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

	private String getTenderIds(Order order) {
		if(order.getTenders() != null && order.getTenders().length > 0) {
			if (order.getTenders().length > 1) {
				ArrayList<String> paymentIds = new ArrayList<String>();
				for (Tender tender : order.getTenders()) {
					paymentIds.add(tender.getId());
				}
				return String.join(", ", paymentIds);
			}
			return order.getTenders()[0].getId();
		}
		return "";
	}

	private String emptyStringIfNull(String str) {
		if (str == null) {
			return "";
		} else {
			return str;
		}
	}

	private String getDate(String iso8601string) throws ParseException {
		// Expecting a local time iso string, converting back to
		String[] prefix = iso8601string.split("-");
		String[] postFix = prefix[2].split("T");

		return prefix[1] + "/" + postFix[0] + "/" + prefix[0];
	}

	private String getTime(String iso8601string) throws ParseException {
		String[] prefix = iso8601string.split("-");
		String[] postFix = prefix[2].split("T");
		String timeStamp = postFix[1].substring(0, postFix[1].length());

		return timeStamp;
	}

	private String getDetailsUrl(Order order, String domainUrl) {
		return domainUrl + DETAILS_URL_ROUTE + order.getId() + DETAULS_URL_DELIMETER
				+ order.getLocationId();
	}

	private String getDetailsUrl(PaymentRefund refund, String domainUrl) {
		return domainUrl + DETAILS_URL_ROUTE + refund.getOrderId() + DETAULS_URL_DELIMETER
				+ refund.getLocationId();
	}

	private String getDeviceName(Order order, Map<String, Payment> tenderToPayment) {
		if(order.getTenders() != null) {
			for(Tender tender : order.getTenders()) {
				Payment payment = tenderToPayment.get(tender.getId());
				if(payment != null && payment.getDeviceDetails() != null) {
					return payment.getDeviceDetails().getDeviceName();
				}
			}
		}
		return "";
	}

	private int getGiftCardSales(Order order) {
		int giftCardSales = 0;
		if(order.getLineItems() != null) {
			for (OrderLineItem lineItem : order.getLineItems()) {
				String itemizationType = lineItem.getItemType();
				if (itemizationType.equals("GIFT_CARD_ACTIVATION") || itemizationType.equals("GIFT_CARD_RELOAD")
						|| itemizationType.equals("GIFT_CARD_UNKNOWN")) {
					giftCardSales += lineItem.getGrossSalesMoney().getAmount() + lineItem.getTotalDiscountMoney().getAmount();
				}
			}
		}

		return giftCardSales;
	}

	private String getItemizationSummary(Order order) {
		List<String> itemizationSummary = new ArrayList<String>();
		if(order.getLineItems() != null) {
			for (OrderLineItem lineItem : order.getLineItems()) {
				StringBuilder buff = new StringBuilder();

				if (Integer.parseInt(lineItem.getQuantity()) > 1) {
					buff.append(Integer.parseInt(lineItem.getQuantity()));
					buff.append(" x ");
				}

				buff.append(lineItem.getName());
				buff.append(" (");
				buff.append(lineItem.getVariationName());
				buff.append(")");

				if (lineItem.getNote() != null && !lineItem.getNote().equals("")) {
					buff.append(" - ");
					buff.append(lineItem.getNote());
				}
				itemizationSummary.add(buff.toString());
			}
		}

		return itemizationSummary.size() > 0 ? String.join(", ", itemizationSummary) : "";
	}

	private String getTimeZoneLabel(String timeZoneId) throws Exception {
		if (timeZoneId.equals("America/Dawson_Creek") || timeZoneId.equals("America/Denver")
				|| timeZoneId.equals("America/Edmonton") || timeZoneId.equals("America/Phoenix")) {
			return "Mountain Time (US & Canada)";
		} else if (timeZoneId.equals("America/Chicago") || timeZoneId.equals("America/Winnipeg")) {
			return "Central Standard Time (US & Canada)";
		} else if (timeZoneId.equals("America/Indianapolis") || timeZoneId.equals("America/Montreal")
				|| timeZoneId.equals("America/New_York")) {
			return "Eastern Standard Time (US & Canada)";
		} else if (timeZoneId.equals("America/Los_Angeles") || timeZoneId.equals("America/Vancouver")) {
			return "Pacific Standard Time (US & Canada)";
		} else {
			throw new Exception("Unsupported timeZoneId" + timeZoneId);
		}
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

	private String getModifiers(OrderLineItem lineItem) {
		if(lineItem.getModifiers() != null) {
			List<String> modsApplied = new ArrayList<String>();
			for(OrderLineItemModifier modifier : lineItem.getModifiers()) {
				modsApplied.add(modifier.getName());
			}
			return String.join(", ", modsApplied);
		}
		return "";
	}

	private String getProductTypeLabel(Order order, Map<String, Payment> tenderToPayment) throws Exception {
		if(order.getTenders() != null) {
			for(Tender tender : order.getTenders()) {
				if(tenderToPayment.containsKey(tender.getId())) {
					Payment payment = tenderToPayment.get(tender.getId());
					String squareProduct = payment.getApplicationDetails().getSquareProduct();
					if (squareProduct.equals(PRODUCT_TYPE_REGISTER) || squareProduct.equals(PRODUCT_TYPE_RETAIL) || squareProduct.equals(PRODUCT_TYPE_OTHER)) {
						return PRODUCT_TYPE_REGISTER_LABEL;
					} else if (squareProduct.equals(PRODUCT_TYPE_ECOMMERCE_API)) {
						return PRODUCT_TYPE_ECOMMERCE_API_LABEL;
					}
		        }
			}
		}
		return PRODUCT_TYPE_OTHER_LABEL;
	}

	private String getTaxes(OrderLineItem lineItem) {
		if (lineItem.getAppliedTaxes() != null && lineItem.getAppliedTaxes().length > 0) {
			int taxes = 0;
			for (OrderLineItemAppliedTax tax : lineItem.getAppliedTaxes()) {
				taxes += tax.getAppliedMoney().getAmount();
			}
			return getCurrencyString(taxes);
		}
		return "";
	}
}
