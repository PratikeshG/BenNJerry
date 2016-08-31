package paradies;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.squareup.connect.Discount;
import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Refund;
import com.squareup.connect.Tender;

import paradies.TLOG;
import paradies.records.AuthorizationResponseRecord;
import paradies.records.DiscountProratedRecord;
import paradies.records.DiscountRecord;
import paradies.records.EncryptedAuthorizationRecord;
import paradies.records.EncryptedMethodOfPaymentRecord;
import paradies.records.ExtendedAuthorizationRecord;
import paradies.records.HeaderRecord;
import paradies.records.MerchandiseSalePartFourRecord;
import paradies.records.MerchandiseSalePartTwoRecord;
import paradies.records.MerchandiseSaleRecord;
import paradies.records.Record;
import paradies.records.TotalSalePartTwoRecord;
import paradies.records.TotalSaleRecord;
import paradies.records.TotalTaxRecord;
import util.TimeManager;

public class TLOGGenerator {

	static final int MAX_DEVICE_ID_LENGTH = 2;
	static final int SKU_LENGTH = 11;

	private String storeId;
	private String defaultDeviceId;
	private String timeZone;
	private Map<String, Employee> employees;
	private Map<String, Discount> discounts;
	private TLOGGeneratorPayload tlogPayload;
	private Map<String, TLOG> tlogs;

	public TLOGGenerator(TLOGGeneratorPayload payload) throws Exception {
		this.storeId = Util.getValueInParenthesis(payload.getLocation().getLocationDetails().getNickname());
		this.defaultDeviceId = payload.getDefaultDeviceId();
		this.timeZone = payload.getTimeZone();
		this.employees = getEmployeeMap(payload.getEmployees());
		this.discounts = getDiscountMap(payload.getDiscounts());
		this.tlogPayload = payload;
		this.tlogs = new HashMap<String, TLOG>();

		if (storeId != null && !storeId.trim().isEmpty()) {
			run();
		}
	}

	public String getStoreId() {
		return storeId;
	}
	
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public Map<String, TLOG> getTlogs() {
		return tlogs;
	}

	public void run() throws Exception {
		HashMap<String, ArrayList<Payment>> devicePaymentsCache = new HashMap<String, ArrayList<Payment>>();
		devicePaymentsCache.put(defaultDeviceId, new ArrayList<Payment>());

		for (Payment p : tlogPayload.getPayments()) {
			if (p.getDevice() == null || p.getDevice().getName() == null) {
				devicePaymentsCache.get(defaultDeviceId).add(p);
			} else {
				String deviceId = Util.getValueInParenthesis(p.getDevice().getName());
				if (deviceId.length() == 0 || deviceId.length() > MAX_DEVICE_ID_LENGTH) {
					devicePaymentsCache.get(defaultDeviceId).add(p);
				} else {
					if (!devicePaymentsCache.containsKey(deviceId)) {
						devicePaymentsCache.put(deviceId, new ArrayList<Payment>());
					}
					devicePaymentsCache.get(deviceId).add(p);
				}
			}
		}

		for (String deviceId : devicePaymentsCache.keySet()) {
			// TODO(bhartard): Do we create TLOG for devices with no sales?
			TLOG tl = generateDeviceTlog(deviceId, devicePaymentsCache.get(deviceId));
			if (tl.getEntries().size() > 0) {
				tlogs.put(deviceId, tl);
			}
		}
	}

	private TLOG generateDeviceTlog(String deviceId, ArrayList<Payment> payments) throws ParseException {

		TLOG deviceTLOG = new TLOG(storeId, deviceId);
		int sequentialTransactionNumber = 1;

		for (Payment payment : payments) {
			// skip no sale event
			if (payment.getTender()[0].getType().equals("NO_SALE")) {
				continue;
			}

			deviceTLOG.addEntries(processPayment(deviceId, payment, sequentialTransactionNumber));
			++sequentialTransactionNumber;

			// TODO(bhartard): Should refunds be processed in correct sequential order in TLOG?
			// There should be only one full refund
			for (Refund refund : payment.getRefunds()) {
				if (refund.getType().equals(Refund.TYPE_FULL)) {
					deviceTLOG.addEntries(processPayment(deviceId, payment, sequentialTransactionNumber, true));
					++sequentialTransactionNumber;
					break;
				}
			}
		}

		return deviceTLOG;
	}
	
	private Map<String, Employee> getEmployeeMap(Employee[] employees) {
		HashMap<String, Employee> employeeMap = new HashMap<String, Employee>();

		for (Employee e : employees) {
			employeeMap.put(e.getId(), e);
		}

		return employeeMap;
	}
	
	private Map<String, Discount> getDiscountMap(Discount[] discounts) {
		HashMap<String, Discount> discountMap = new HashMap<String, Discount>();

		for (Discount d : discounts) {
			discountMap.put(d.getId(), d);
		}

		return discountMap;
	}

	private List<TLOGEntry> processPayment(String deviceId, Payment payment, int transactionNumber) throws ParseException {
		return processPayment(deviceId, payment, transactionNumber, false);
	}
	
	private List<TLOGEntry> processPayment(String deviceId, Payment payment, int transactionNumber, boolean isRefund) throws ParseException {
		AtomicInteger recordSequence = new AtomicInteger(1);

		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();

		entries.addAll(createMerchandiseEntriesForPayment(deviceId, transactionNumber, recordSequence, payment, isRefund));
		entries.addAll(createTransactionDiscountEntriesForPayment(deviceId, transactionNumber, recordSequence, payment, isRefund));
		entries.addAll(createTotalSaleEntriesForPayment(deviceId, transactionNumber, recordSequence, payment, isRefund));
		entries.addAll(createTotalTaxEntriesForPayment(deviceId, transactionNumber, recordSequence, payment, isRefund));
		entries.addAll(createPaymentMethodEntriesForPayment(deviceId, transactionNumber, recordSequence, payment, isRefund));

		return entries;
	}

	private String getEmployeeIdFromPayment(Payment payment) {
		String employeeId = "";

		// Transactions can have multiple tenders, processed by different employees
		// Just get the first tender's employee ID
		for (Tender tender : payment.getTender()) {
			String key = tender.getEmployeeId();
			if (key != null && !key.equals("")) {
				employeeId = employees.get(key).getExternalId();
				break;
			}
		}

		return employeeId;
	}

	private HeaderRecord createHeaderRecord(String deviceId, String recordType, int transactionNumber, AtomicInteger recordSequence, Payment payment, boolean isRefund) throws ParseException {
		String employeeId = getEmployeeIdFromPayment(payment);
		String dateTime = TimeManager.toSimpleDateTimeInTimeZone(payment.getCreatedAt(), timeZone, "yyMMddHHmmss");
		String tNumber = Integer.toString(transactionNumber);
		String rSequence = recordSequence.toString();
		String businessDate = dateTime.substring(2, 6);
		String modifier = isRefund ? "9" : "0";

		HeaderRecord header = new HeaderRecord(recordType);
		header.setFieldValue(HeaderRecord.FIELD_TRANSACTION_MODIFIER, modifier);
		header.setFieldValue(HeaderRecord.FIELD_STORE_ID, storeId);
		header.setFieldValue(HeaderRecord.FIELD_REGISTER_ID, deviceId);
		header.setFieldValue(HeaderRecord.FIELD_CASHIER_ID, employeeId);
		header.setFieldValue(HeaderRecord.FIELD_SALESPERSON_ID, employeeId);
		header.setFieldValue(HeaderRecord.FIELD_DATETIME, dateTime);
		header.setFieldValue(HeaderRecord.FIELD_TRANSACTION_NUMBER, tNumber);
		header.setFieldValue(HeaderRecord.FIELD_RECORD_SEQUENCE, rSequence);
		header.setFieldValue(HeaderRecord.FIELD_BUSINESS_DATE, businessDate);

		return header;
	}

	private List<TLOGEntry> createMerchandiseEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment, boolean isRefund) throws ParseException {

		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		for (PaymentItemization itemization : payment.getItemizations()) {
			
			// I record
			HeaderRecord headerI = createHeaderRecord(deviceId, MerchandiseSaleRecord.ID, transactionNumber, recordSequence, payment, isRefund);
			MerchandiseSaleRecord iRecord = createMerchandiseSaleRecord(itemization, isRefund);
			entries.add(new TLOGEntry(headerI, iRecord));
			recordSequence.getAndIncrement();
			
			// I2 record
			HeaderRecord headerI2 = createHeaderRecord(deviceId, MerchandiseSalePartTwoRecord.ID, transactionNumber, recordSequence, payment, isRefund);
			MerchandiseSalePartTwoRecord i2Record = createMerchandiseSalePartTwoRecord(itemization, isRefund);
			entries.add(new TLOGEntry(headerI2, i2Record));
			recordSequence.getAndIncrement();
			
			// I6 record
			HeaderRecord headerI6 = createHeaderRecord(deviceId, MerchandiseSalePartFourRecord.ID, transactionNumber, recordSequence, payment, isRefund);
			MerchandiseSalePartFourRecord i6Record = createMerchandiseSalePartFourRecord(itemization);
			entries.add(new TLOGEntry(headerI6, i6Record));
			recordSequence.getAndIncrement();
			
			// Discount record(s)
			for (PaymentDiscount discount : itemization.getDiscounts()) {
				int discountExtendedAmount = -discount.getAppliedMoney().getAmount();
				int originalAmount = itemDiscountOriginalAmount(itemization, discount.getDiscountId());
				
				HeaderRecord headerD;
				Record dRecord;
				if (isTransactionDiscount(payment, discount.getDiscountId())) {
					headerD = createHeaderRecord(deviceId, DiscountProratedRecord.ID, transactionNumber, recordSequence, payment, isRefund);
					dRecord = new DiscountProratedRecord();
				} else {
					headerD = createHeaderRecord(deviceId, DiscountRecord.ID, transactionNumber, recordSequence, payment, isRefund);
					dRecord = new DiscountRecord();
				}

				dRecord.setFieldValue(DiscountRecord.FIELD_DISCOUNT_TYPE, discountType(discount.getName()));
				dRecord.setFieldValue(DiscountRecord.FIELD_EXTENDED_DISCOUNT_AMT, Integer.toString(discountExtendedAmount));
				dRecord.setFieldValue(DiscountRecord.FIELD_POSITIVE_FLAG_DISCOUNT, isRefund ? "" : "-");
				dRecord.setFieldValue(DiscountRecord.FIELD_ORIGINAL_PRICE, Integer.toString(originalAmount));
				dRecord.setFieldValue(DiscountRecord.FIELD_POSITIVE_FLAG_ORIGINAL_PRICE, isRefund ? "-" : "");
				dRecord.setFieldValue(DiscountRecord.FIELD_DISCOUNT_ID, discountId(discount.getName()));			
				dRecord.setFieldValue(DiscountRecord.FIELD_AFFECT_NET_SALES, "1");
				dRecord.setFieldValue(DiscountRecord.FIELD_DISCOUNT_QUANTITY, getItemQuantity(1.0));
				dRecord.setFieldValue(DiscountRecord.FIELD_TXN_DISCOUNT_ON_ITEM_FLAG, "1");
				dRecord.setFieldValue(DiscountRecord.FIELD_TXN_DISCOUNT_ON_TXN_FLAG, "1");
				
				entries.add(new TLOGEntry(headerD, dRecord));
				recordSequence.getAndIncrement();
			}
		}

		return entries;
	}

	private boolean isTransactionDiscount(Payment payment, String id) {
		for (String discountId : getTransactionDiscountIds(payment)) {
			if (discountId.equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	private MerchandiseSaleRecord createMerchandiseSaleRecord(PaymentItemization itemization, boolean isRefund) {

		boolean itemDiscounted = false;
		if (itemization.getDiscounts().length > 0) {
			itemDiscounted = true;
		}

		String variationName = itemization.getItemVariationName();
		String sku = variationName.replace("-", "");
		sku = sku.substring(0, Math.min(sku.length(), SKU_LENGTH));

		String cost = "";
		String deptCode = "";
		if (variationName.length() > 4) {
			deptCode = variationName.substring(variationName.length() - 4);
		}
		
		MerchandiseSaleRecord iRecord = new MerchandiseSaleRecord();
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_SKU, sku);
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_DEPARTMENT, deptCode);
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_QUANTITY, getItemQuantity(itemization.getQuantity()));
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_EXTENDED_SELLING_PRICE, Integer.toString(itemization.getNetSalesMoney().getAmount()));
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_POSITIVE_FLAG, isRefund ? "-" : "");
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_EXTENDED_ORIGINAL_PRICE, Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_EXT_ORIG_PRICE_OVR, Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_COST, cost);
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_PLU_FLAG, "1"); // if not equal to the unknown code
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_DISCOUNT_FLAG, itemDiscounted ? "1" : "0");

		// Apply taxes
		// Tax1 in tlog is actually a flag that the item was non-taxable
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_TAX1, itemization.getTaxes().length < 1 ? "1" : "0");
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_TAX2, itemizationHasTaxId(itemization.getTaxes(), 2) ? "1" : "0");
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_TAX_3, itemizationHasTaxId(itemization.getTaxes(), 3) ? "1" : "0");
		iRecord.setFieldValue(MerchandiseSaleRecord.FIELD_TAX_4, itemizationHasTaxId(itemization.getTaxes(), 4) ? "1" : "0");

		return iRecord;
	}

	private MerchandiseSalePartTwoRecord createMerchandiseSalePartTwoRecord(PaymentItemization itemization, boolean isRefund) {

		MerchandiseSalePartTwoRecord i2Record = new MerchandiseSalePartTwoRecord();
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_REGULAR_PRICE, Integer.toString(itemization.getGrossSalesMoney().getAmount()));
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_PREPROMO_PRICE, Integer.toString(itemization.getGrossSalesMoney().getAmount()));
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_NET_PRICE, Integer.toString(itemization.getNetSalesMoney().getAmount()));
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_POSITIVE_FLAG, isRefund ? "-" : "");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_TAXABLE_AMOUNT, Integer.toString(itemization.getNetSalesMoney().getAmount()));

		// Apply taxes
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_5, itemizationHasTaxId(itemization.getTaxes(), 5) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_6, itemizationHasTaxId(itemization.getTaxes(), 6) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_7, itemizationHasTaxId(itemization.getTaxes(), 7) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_8, itemizationHasTaxId(itemization.getTaxes(), 8) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_9, itemizationHasTaxId(itemization.getTaxes(), 9) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_10, itemizationHasTaxId(itemization.getTaxes(), 10) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_11, itemizationHasTaxId(itemization.getTaxes(), 11) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_12, itemizationHasTaxId(itemization.getTaxes(), 12) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_13, itemizationHasTaxId(itemization.getTaxes(), 13) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_14, itemizationHasTaxId(itemization.getTaxes(), 14) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_15, itemizationHasTaxId(itemization.getTaxes(), 15) ? "1" : "0");
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_TAX_16, itemizationHasTaxId(itemization.getTaxes(), 16) ? "1" : "0");

		return i2Record;
	}
	
	private MerchandiseSalePartFourRecord createMerchandiseSalePartFourRecord(PaymentItemization itemization) {

		MerchandiseSalePartFourRecord i6Record = new MerchandiseSalePartFourRecord();
		i6Record.setFieldValue(MerchandiseSalePartFourRecord.FIELD_STANDARD_UNIT_PRICE, Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		i6Record.setFieldValue(MerchandiseSalePartFourRecord.FIELD_EXTENDED_NET_PRICE, Integer.toString(itemization.getNetSalesMoney().getAmount()));

		return i6Record;
	}

	private List<TLOGEntry> createTransactionDiscountEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment, boolean isRefund) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		for (String discountId : getTransactionDiscountIds(payment)) {
			Discount discount = discounts.get(discountId);

			int discountExtendedAmount = transactionDiscountExtendedAmount(payment, discountId);
			int originalAmount = transactionDiscountOriginalAmount(payment, discountId);
			double appliedCount = transactionDiscountAppliedCount(payment, discountId);

			// D record
			HeaderRecord headerD = createHeaderRecord(deviceId, DiscountRecord.ID, transactionNumber, recordSequence, payment, isRefund);
			DiscountRecord dRecord = new DiscountRecord();
			dRecord.setFieldValue(DiscountRecord.FIELD_DISCOUNT_TYPE, discountType(discount.getName()));
			dRecord.setFieldValue(DiscountRecord.FIELD_EXTENDED_DISCOUNT_AMT, Integer.toString(discountExtendedAmount));
			dRecord.setFieldValue(DiscountRecord.FIELD_POSITIVE_FLAG_DISCOUNT, isRefund ? "" : "-");
			dRecord.setFieldValue(DiscountRecord.FIELD_ORIGINAL_PRICE, Integer.toString(originalAmount));
			dRecord.setFieldValue(DiscountRecord.FIELD_POSITIVE_FLAG_ORIGINAL_PRICE, isRefund ? "-" : "");
			dRecord.setFieldValue(DiscountRecord.FIELD_DISCOUNT_ID, discountId(discount.getName()));			
			dRecord.setFieldValue(DiscountRecord.FIELD_AFFECT_NET_SALES, "1");
			dRecord.setFieldValue(DiscountRecord.FIELD_DISCOUNT_QUANTITY, getItemQuantity(appliedCount));
			dRecord.setFieldValue(DiscountRecord.FIELD_TXN_DISCOUNT_ON_ITEM_FLAG, "1");
			dRecord.setFieldValue(DiscountRecord.FIELD_TXN_DISCOUNT_ON_TXN_FLAG, "1");

			entries.add(new TLOGEntry(headerD, dRecord));
			recordSequence.getAndIncrement();
		}

		return entries;
	}

	private List<String> getTransactionDiscountIds(Payment payment) {
		ArrayList<String> transactionDiscounts = new ArrayList<String>();
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// Only validate against known transaction scoped discounts
		for (PaymentItemization itemization : payment.getItemizations()) {
			for (PaymentDiscount discount : itemization.getDiscounts()) {
				String discountName = discount.getName();				
				if (discountName.equals("% Trans Off") || discountName.equals("$ Trans Off") ||
						discountName.equals("Airport 10%") || discountName.equals("Associate Gift Cert") ||
						discountName.equals("Paradies 25%")) {

					int c = counts.getOrDefault(discount.getDiscountId(), 0);
					counts.put(discount.getDiscountId(), c + 1);
				}
			}
		}

		// Find the discounts applied to all itemizations
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			if (entry.getValue() == payment.getItemizations().length) {
		    	transactionDiscounts.add(entry.getKey());
		    }
		}

		return transactionDiscounts;
	}
	
	private String discountType(String name) {
		String type = "";

		switch (name) {
		case "Item % Discount":
			type = "1";
			break;
		case "Item $ Discount": // not supported
			type = "2";
			break;
		case "$ Trans Off":
		case "Associate Gift Cert":
			type = "5";
			break;
		case "Paradies 25%":
		case "Airport 10%":
        case "% Trans Off":
        default:
        	type = "4";
        	break;
		}

		return type;
	}
	
	private String discountId(String name) {
		String id = "";

		switch (name) {
		case "Item % Discount":
			id = "2";
			break;
		case "Item $ Discount": // not supported
			id = "3";
			break;
		case "Associate Gift Cert":
			id = "4";
			break;
		case "Airport 10%":
			id = "6";
			break;
		case "Paradies 25%":
			id = "7";
			break;
        case "% Trans Off":
        	id = "11";
        	break;
        case "$ Trans Off":
        	id = "12";
        	break;
		}

		return id;
	}
	
	private int transactionDiscountAppliedCount(Payment payment, String id) {
		int count = 0;

		for (PaymentItemization itemization : payment.getItemizations()) {
			for (PaymentDiscount discount : itemization.getDiscounts()) {			
				if (discount.getDiscountId().equals(id)) {
					count += 1;
				}
			}
		}

		return count;
	}

	private int transactionDiscountExtendedAmount(Payment payment, String id) {
		int total = 0;
		for (PaymentItemization itemization : payment.getItemizations()) {
			for (PaymentDiscount discount : itemization.getDiscounts()) {			
				if (discount.getDiscountId().equals(id)) {
					total += -discount.getAppliedMoney().getAmount();  // negative value
				}
			}
		}
		return total;
	}

	private int itemDiscountOriginalAmount(PaymentItemization itemization, String id) {
		int beforeTotal = itemization.getGrossSalesMoney().getAmount();
		for (PaymentDiscount prevDiscount : itemization.getDiscounts()) {
			if (prevDiscount.getDiscountId().equals(id)) {
				break;
			}
			beforeTotal += prevDiscount.getAppliedMoney().getAmount(); // negative value
		}
		return beforeTotal;
	}
	
	private int transactionDiscountOriginalAmount(Payment payment, String id) {
		int beforeTotal = payment.getGrossSalesMoney().getAmount();

		for (PaymentItemization itemization : payment.getItemizations()) {
			for (PaymentDiscount prevDiscount : itemization.getDiscounts()) {
				if (prevDiscount.getDiscountId().equals(id)) {
					break;
				}
				beforeTotal += prevDiscount.getAppliedMoney().getAmount(); // negative value
			}
		}

		return beforeTotal;
	}

	private List<TLOGEntry> createTotalSaleEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment, boolean isRefund) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		// T record
		HeaderRecord headerT = createHeaderRecord(deviceId, TotalSaleRecord.ID, transactionNumber, recordSequence, payment, isRefund);
		TotalSaleRecord tRecord = createTotalSaleRecord(payment, isRefund);
		entries.add(new TLOGEntry(headerT, tRecord));
		recordSequence.getAndIncrement();

		HeaderRecord headerT2 = createHeaderRecord(deviceId, TotalSalePartTwoRecord.ID, transactionNumber, recordSequence, payment, isRefund);
		TotalSalePartTwoRecord t2Record = createTotalSalePartTwoRecord(payment, isRefund);
		entries.add(new TLOGEntry(headerT2, t2Record));
		recordSequence.getAndIncrement();

		return entries;
	}

	private TotalSaleRecord createTotalSaleRecord(Payment payment, boolean isRefund) {
		TotalSaleRecord tRecord = new TotalSaleRecord();
		
		int totalSaleNoTax = payment.getTotalCollectedMoney().getAmount() - payment.getTaxMoney().getAmount(); 
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TOTAL_SALE_NO_TAX, Integer.toString(totalSaleNoTax));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_POSITIVE_FLAG_TOTAL, isRefund ? "-" : "");

		int tax2 = getTaxTotalFromPayment(payment, 2);
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TAX_2, Integer.toString(tax2));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_POSITIVE_FLAG_TAX_2, isRefund && tax2 > 0 ? "-" : "");

		int tax3 = getTaxTotalFromPayment(payment, 3);
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TAX_3, Integer.toString(tax3));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_POSITIVE_FLAG_TAX_3, isRefund && tax3 > 0 ? "-" : "");

		int tax4 = getTaxTotalFromPayment(payment, 4);
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TAX_4, Integer.toString(tax4));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_POSITIVE_FLAG_TAX_4, isRefund && tax4 > 0 ? "-" : "");

		tRecord.setFieldValue(TotalSaleRecord.FIELD_T2_FLAG, "1"); // always require T2 to follow

		return tRecord;
	}

	private TotalSalePartTwoRecord createTotalSalePartTwoRecord(Payment payment, boolean isRefund) {
		String[] recordNames = {
			TotalSalePartTwoRecord.FIELD_TAX_5, // index 0
			TotalSalePartTwoRecord.FIELD_TAX_6,
			TotalSalePartTwoRecord.FIELD_TAX_7,
			TotalSalePartTwoRecord.FIELD_TAX_8,
			TotalSalePartTwoRecord.FIELD_TAX_9,
			TotalSalePartTwoRecord.FIELD_TAX_10,
			TotalSalePartTwoRecord.FIELD_TAX_11,
			TotalSalePartTwoRecord.FIELD_TAX_12,
			TotalSalePartTwoRecord.FIELD_TAX_13,
			TotalSalePartTwoRecord.FIELD_TAX_14,
			TotalSalePartTwoRecord.FIELD_TAX_15,
			TotalSalePartTwoRecord.FIELD_TAX_16 // index 11
		};
		
		String[] recordNamesFlags = {
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_5, // index 0
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_6,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_7,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_8,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_9,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_10,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_11,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_12,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_13,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_14,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_15,
			TotalSalePartTwoRecord.FIELD_POSITIVE_FLAG_TAX_16 // index 11
		};

		TotalSalePartTwoRecord t2Record = new TotalSalePartTwoRecord();

		for (int i = 0; i < 12; i++) {
			int taxValue = getTaxTotalFromPayment(payment, i + 5);
			t2Record.setFieldValue(recordNames[i], Integer.toString(taxValue));
			t2Record.setFieldValue(recordNamesFlags[i], isRefund && taxValue > 0 ? "-" : "");
		}

		return t2Record;
	}

	private List<TLOGEntry> createTotalTaxEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment, boolean isRefund) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();

		HashMap<String, Integer> taxableTotalsByTaxId = new HashMap<String, Integer>();
		HashMap<String, Integer> appliedTaxById = new HashMap<String, Integer>();

		// Calculate taxable amounts over each itemization
		for (PaymentItemization itemization : payment.getItemizations()) {
			int taxableAmount = itemization.getGrossSalesMoney().getAmount();
			
			for (PaymentTax tax : itemization.getTaxes()) {
				String taxId = getTaxIdFromTaxName(tax.getName());
				
				// Increment taxable amount
				if (taxableTotalsByTaxId.containsKey(taxId)) {
					taxableTotalsByTaxId.put(taxId, taxableTotalsByTaxId.get(taxId) + taxableAmount);
				} else {
					taxableTotalsByTaxId.put(taxId, taxableAmount);
				}

				// Increment taxed amount
				int appliedTax = tax.getAppliedMoney().getAmount();
				if (appliedTaxById.containsKey(taxId)) {
					appliedTaxById.put(taxId, appliedTaxById.get(taxId) + appliedTax);
				} else {
					appliedTaxById.put(taxId, appliedTax);
				}
			}
		}

		// TX record for each tax applied
		for (String taxId : appliedTaxById.keySet()) {
			HeaderRecord headerTX = createHeaderRecord(deviceId, TotalTaxRecord.ID, transactionNumber, recordSequence, payment, isRefund);
			TotalTaxRecord txRecord = createTotalTaxRecord(taxId, taxableTotalsByTaxId.get(taxId), appliedTaxById.get(taxId));
			entries.add(new TLOGEntry(headerTX, txRecord));
			recordSequence.getAndIncrement();
		}

		return entries;
	}

	private TotalTaxRecord createTotalTaxRecord(String taxId, int taxableTotal, int appliedTax) {
		TotalTaxRecord txRecord = new TotalTaxRecord();

		txRecord.setFieldValue(TotalTaxRecord.FIELD_TAX_ID, taxId);
		txRecord.setFieldValue(TotalTaxRecord.FIELD_TAX_AMOUNT, Integer.toString(appliedTax));
		txRecord.setFieldValue(TotalTaxRecord.FIELD_TAXABLE_AMOUNT, Integer.toString(taxableTotal));

		return txRecord;
	}
	
	private String getTaxIdFromTaxName(String taxName) {
		String taxId = "2"; // default
		if (taxName.indexOf("[") != -1 && taxName.indexOf("]") != -1) {
			taxId = taxName.substring(taxName.indexOf("[")+1, taxName.indexOf("]"));
		}
		return taxId;
	}
	
	private ArrayList<TLOGEntry> createPaymentMethodEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment, boolean isRefund) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		// P record(s)
		for (Tender tender : payment.getTender()) {
			HeaderRecord headerP = createHeaderRecord(deviceId, EncryptedMethodOfPaymentRecord.ID, transactionNumber, recordSequence, payment, isRefund);
			EncryptedMethodOfPaymentRecord pRecord = createPaymentRecord(tender, isRefund);
			entries.add(new TLOGEntry(headerP, pRecord));
			recordSequence.getAndIncrement();

			// Generate authorization (A, A2, AE) records
			if (tender.getType().equals("CREDIT_CARD") && !isRefund) {
				// A record
				HeaderRecord headerA = createHeaderRecord(deviceId, EncryptedAuthorizationRecord.ID, transactionNumber, recordSequence, payment, isRefund);
				EncryptedAuthorizationRecord aRecord = createAuthorizationRecord(tender);
				entries.add(new TLOGEntry(headerA, aRecord));
				recordSequence.getAndIncrement();

				// A2 record
				HeaderRecord headerA2 = createHeaderRecord(deviceId, AuthorizationResponseRecord.ID, transactionNumber, recordSequence, payment, isRefund);
				AuthorizationResponseRecord a2Record = createAuthorizationResponseRecord(tender);
				entries.add(new TLOGEntry(headerA2, a2Record));
				recordSequence.getAndIncrement();

				// AE record
				HeaderRecord headerAE = createHeaderRecord(deviceId, ExtendedAuthorizationRecord.ID, transactionNumber, recordSequence, payment, isRefund);
				ExtendedAuthorizationRecord aeRecord = createExtendedAuthorizationRecord(payment);
				entries.add(new TLOGEntry(headerAE, aeRecord));
				recordSequence.getAndIncrement();
			}
		}

		return entries;
	}
	
	private EncryptedMethodOfPaymentRecord createPaymentRecord(Tender tender, boolean isRefund) {
		EncryptedMethodOfPaymentRecord pRecord = new EncryptedMethodOfPaymentRecord();

		pRecord.setFieldValue(EncryptedMethodOfPaymentRecord.FIELD_TENDER_ID, getTenderId(tender));
		pRecord.setFieldValue(EncryptedMethodOfPaymentRecord.FIELD_REFERENCE_NUMBER_1, tender.getId());

		int tendered = tender.getTotalMoney().getAmount();
		if (!isRefund && tender.getTenderedMoney() != null) {
			tendered = tender.getTenderedMoney().getAmount();
		}
		pRecord.setFieldValue(EncryptedMethodOfPaymentRecord.FIELD_TENDER_AMOUNT, Integer.toString(tendered));
		pRecord.setFieldValue(EncryptedMethodOfPaymentRecord.FIELD_POSITIVE_FLAG_TENDER, isRefund ? "-" : "");

		int changeDue = 0;
		if (!isRefund && tender.getChangeBackMoney() != null) {
			changeDue = tender.getChangeBackMoney().getAmount();
		}
		pRecord.setFieldValue(EncryptedMethodOfPaymentRecord.FIELD_CHANGE_DUE, Integer.toString(changeDue));

		return pRecord;
	}
	
	private EncryptedAuthorizationRecord createAuthorizationRecord(Tender tender) {
		EncryptedAuthorizationRecord aRecord = new EncryptedAuthorizationRecord();

		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_TENDER_ID, getTenderId(tender));
		// aRecord.setFieldValue(AuthorizationRecord.FIELD_CREDIT_OR_DEBIT_TYPE, "01"); // TODO(bhartard): ??
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_HOW_AUTHORIZED, "0"); // online
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_CARD_ENTRY_TYPE, tender.getEntryMethod().equals("SWIPED") ? "0" : "1"); // 0-swipe, 1-keyed
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_CARD_NUMBER, tender.getPanSuffix());
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_REQUEST_AMOUNT, Integer.toString(tender.getTotalMoney().getAmount()));
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_AUTHORIZED_AMOUNT, Integer.toString(tender.getTotalMoney().getAmount()));
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_CREDIT_OR_DEBIT_TRANSACTION_TYPE, "8"); // 8-Sale
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_A2_PART1_LENGTH, "8"); // "APPROVED"

		String lastFour = tender.getId().length() > 4 ? tender.getId().substring(tender.getId().length() - 4) : "";
		aRecord.setFieldValue(EncryptedAuthorizationRecord.FIELD_AUTHORIZATION_NUMBER, lastFour + tender.getPanSuffix());

		return aRecord;
	}
	
	private AuthorizationResponseRecord createAuthorizationResponseRecord(Tender tender) {
		AuthorizationResponseRecord a2Record = new AuthorizationResponseRecord();

		a2Record.setFieldValue(AuthorizationResponseRecord.FIELD_CREDIT_SERVICE_RESPONSE_1, "APPROVED");

		return a2Record;
	}
	
	private ExtendedAuthorizationRecord createExtendedAuthorizationRecord(Payment payment) throws ParseException {
		ExtendedAuthorizationRecord aeRecord = new ExtendedAuthorizationRecord();

		String transactionDate = TimeManager.toSimpleDateTimeInTimeZone(payment.getCreatedAt(), timeZone, "yyyyMMdd");
		String transactionTime = TimeManager.toSimpleDateTimeInTimeZone(payment.getCreatedAt(), timeZone, "HHmmss");

		aeRecord.setFieldValue(ExtendedAuthorizationRecord.FIELD_RESPONSE_CODE, "0001"); // TODO(bhartard): verify?
		aeRecord.setFieldValue(ExtendedAuthorizationRecord.FIELD_TRANSACTION_DATE, transactionDate);
		aeRecord.setFieldValue(ExtendedAuthorizationRecord.FIELD_TRANSACTION_TIME, transactionTime);
		//aeRecord.setFieldValue(ExtendedAuthorizationRecord.FIELD_CUSTOMER_REFERENCE_NUMBER, "");

		// Only provide provide total tax when only tender
		if (payment.getTender().length == 1) {
			aeRecord.setFieldValue(ExtendedAuthorizationRecord.FIELD_TAX_AMOUNT, Integer.toString(payment.getTaxMoney().getAmount()));
		}
		
		return aeRecord;
	}

	/*
	 * Input: XXX.00000000
	 * Output: qty String of length 7 (XXXX.YYY = XXXXXYYY)
	 */
	private String getItemQuantity(double quantity) {
		String qty = String.format( "%.3f", quantity);
		qty = qty.split("\\.")[0] + "000";
		return qty;
	}

	private Boolean itemizationHasTaxId(PaymentTax[] taxes, int taxId) {
		if (taxes.length == 0) {
			return false;
		}

		for (PaymentTax tax : taxes) {
			int taxIdFromName = Integer.parseInt(getTaxIdFromTaxName(tax.getName()));
			if (taxIdFromName == taxId) {
				return true;
			}
		}

		return false;
	}
	
	private int getTaxTotalFromPayment(Payment payment, int taxId) {
		int taxTotal = 0;

		for (PaymentTax tax : payment.getAdditiveTax()) {
			int taxIdFromName = Integer.parseInt(getTaxIdFromTaxName(tax.getName()));
			if (taxIdFromName == taxId) {
				taxTotal += tax.getAppliedMoney().getAmount();
			}
		}

		return taxTotal;
	}
	
	// TODO(bhartard): add type comps?
	private String getTenderId(Tender tender) {		
		String tenderId = "";

		switch(tender.getType()) {
		case "CASH":
			tenderId = "1";
			break;
		case "OTHER":
			switch(tender.getName()) {
			case "MERCHANT_GIFT_CARD":
				tenderId = "24";
				break;
			case "CUSTOM":
				tenderId = "30";
				break;
			}
			break;
		case "CREDIT_CARD":
			switch(tender.getCardBrand()) {
			case "UNKNOWN":
			case "VISA":
				tenderId = "4";
				break;
			case "MASTER_CARD":
				tenderId = "5";
				break;
			case "DISCOVER":
				tenderId = "6";
				break;
			case "AMERICAN_EXPRESS":
				tenderId = "7";
				break;
			case "DISCOVER_DINERS":
				tenderId = "8";
				break;
			case "JCB":
				tenderId = "13";
				break;
			}
			break;
		}

		return tenderId;
	}
}