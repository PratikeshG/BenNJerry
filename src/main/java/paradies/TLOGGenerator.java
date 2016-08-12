package paradies;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.squareup.connect.Employee;
import com.squareup.connect.Item;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

import paradies.TLOG;
import paradies.records.AuthorizationRecord;
import paradies.records.AuthorizationResponseRecord;
import paradies.records.ExtendedAuthorizationRecord;
import paradies.records.HeaderRecord;
import paradies.records.MerchandiseSalePartFourRecord;
import paradies.records.MerchandiseSalePartTwoRecord;
import paradies.records.MerchandiseSaleRecord;
import paradies.records.MethodOfPaymentRecord;
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
	private TLOGGeneratorPayload tlogPayload;
	private Map<String, TLOG> tlogs;

	public TLOGGenerator(TLOGGeneratorPayload payload) throws Exception {
		this.storeId = Util.getValueInParenthesis(payload.getLocation().getLocationDetails().getNickname());
		this.defaultDeviceId = payload.getDefaultDeviceId();
		this.timeZone = payload.getTimeZone();
		this.employees = getEmployeeMap(payload.getEmployees());
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

	private List<TLOGEntry> processPayment(String deviceId, Payment payment, int transactionNumber) throws ParseException {
		AtomicInteger recordSequence = new AtomicInteger(1);

		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		entries.addAll(createMerchandiseEntriesForPayment(deviceId, transactionNumber, recordSequence, payment));
		entries.addAll(createTotalSaleEntriesForPayment(deviceId, transactionNumber, recordSequence, payment));
		entries.addAll(createTotalTaxEntriesForPayment(deviceId, transactionNumber, recordSequence, payment));
		entries.addAll(createPaymentMethodEntriesForPayment(deviceId, transactionNumber, recordSequence, payment));
		
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

	private HeaderRecord createHeaderRecord(String deviceId, String recordType, int transactionNumber, AtomicInteger recordSequence, Payment payment) throws ParseException {

		String employeeId = getEmployeeIdFromPayment(payment);
		String dateTime = TimeManager.toSimpleDateTimeInTimeZone(payment.getCreatedAt(), timeZone, "yyMMddHHmmss");
		String tNumber = Integer.toString(transactionNumber);
		String rSequence = recordSequence.toString();
		String businessDate = dateTime.substring(2, 6);

		HeaderRecord header = new HeaderRecord(recordType);
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

	private List<TLOGEntry> createMerchandiseEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		for (PaymentItemization itemization : payment.getItemizations()) {
			
			// I record
			HeaderRecord headerI = createHeaderRecord(deviceId, MerchandiseSaleRecord.ID, transactionNumber, recordSequence, payment);
			MerchandiseSaleRecord iRecord = createMerchandiseSaleRecord(itemization);
			entries.add(new TLOGEntry(headerI, iRecord));
			recordSequence.getAndIncrement();
			
			// I2 record
			HeaderRecord headerI2 = createHeaderRecord(deviceId, MerchandiseSalePartTwoRecord.ID, transactionNumber, recordSequence, payment);
			MerchandiseSalePartTwoRecord i2Record = createMerchandiseSalePartTwoRecord(itemization);
			entries.add(new TLOGEntry(headerI2, i2Record));
			recordSequence.getAndIncrement();
			
			// I6 record
			HeaderRecord headerI6 = createHeaderRecord(deviceId, MerchandiseSalePartFourRecord.ID, transactionNumber, recordSequence, payment);
			MerchandiseSalePartFourRecord i6Record = createMerchandiseSalePartFourRecord(itemization);
			entries.add(new TLOGEntry(headerI6, i6Record));
			recordSequence.getAndIncrement();
		}

		return entries;
	}

	private MerchandiseSaleRecord createMerchandiseSaleRecord(PaymentItemization itemization) {

		Boolean itemDiscounted = false;
		if (itemization.getDiscounts().length > 0) {
			itemDiscounted = true;
		}

		// Get meta details from item
		Map<String, Item> catalogItems = tlogPayload.getCatalog().getItems();
		
		// Default to the item ID, but this could change if items are updated
		Item matchingItem = catalogItems.get(itemization.getItemDetail().getItemId());

		// We actually want to get the item with matching SKU
		for (Item item : catalogItems.values()) {
			String upcToMatch = item.getVariations()[0].getSku();
			
			if (upcToMatch.equals(itemization.getItemVariationName())) {
				matchingItem = item;
				break;
			}
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

	private MerchandiseSalePartTwoRecord createMerchandiseSalePartTwoRecord(PaymentItemization itemization) {

		MerchandiseSalePartTwoRecord i2Record = new MerchandiseSalePartTwoRecord();
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_REGULAR_PRICE, Integer.toString(itemization.getGrossSalesMoney().getAmount()));
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_PREPROMO_PRICE, Integer.toString(itemization.getGrossSalesMoney().getAmount()));
		i2Record.setFieldValue(MerchandiseSalePartTwoRecord.FIELD_EXTENDED_NET_PRICE, Integer.toString(itemization.getNetSalesMoney().getAmount()));
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
	
	private List<TLOGEntry> createTotalSaleEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		// T record
		HeaderRecord headerT = createHeaderRecord(deviceId, TotalSaleRecord.ID, transactionNumber, recordSequence, payment);
		TotalSaleRecord tRecord = createTotalSaleRecord(payment);
		entries.add(new TLOGEntry(headerT, tRecord));
		recordSequence.getAndIncrement();

		HeaderRecord headerT2 = createHeaderRecord(deviceId, TotalSalePartTwoRecord.ID, transactionNumber, recordSequence, payment);
		TotalSalePartTwoRecord t2Record = createTotalSalePartTwoRecord(payment);
		entries.add(new TLOGEntry(headerT2, t2Record));
		recordSequence.getAndIncrement();

		return entries;
	}

	private TotalSaleRecord createTotalSaleRecord(Payment payment) {
		TotalSaleRecord tRecord = new TotalSaleRecord();
		
		int totalSaleNoTax = payment.getTotalCollectedMoney().getAmount() - payment.getTaxMoney().getAmount(); 
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TOTAL_SALE_NO_TAX, Integer.toString(totalSaleNoTax));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TAX_2, Integer.toString(getTaxTotalFromPayment(payment, 2)));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TAX_3, Integer.toString(getTaxTotalFromPayment(payment, 3)));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_TAX_4, Integer.toString(getTaxTotalFromPayment(payment, 4)));
		tRecord.setFieldValue(TotalSaleRecord.FIELD_T2_FLAG, "1"); // always require T2 to follow

		return tRecord;
	}

	private TotalSalePartTwoRecord createTotalSalePartTwoRecord(Payment payment) {
		TotalSalePartTwoRecord t2Record = new TotalSalePartTwoRecord();

		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_5, Integer.toString(getTaxTotalFromPayment(payment, 5)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_6, Integer.toString(getTaxTotalFromPayment(payment, 6)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_7, Integer.toString(getTaxTotalFromPayment(payment, 7)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_8, Integer.toString(getTaxTotalFromPayment(payment, 8)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_9, Integer.toString(getTaxTotalFromPayment(payment, 9)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_10, Integer.toString(getTaxTotalFromPayment(payment, 10)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_11, Integer.toString(getTaxTotalFromPayment(payment, 11)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_12, Integer.toString(getTaxTotalFromPayment(payment, 12)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_13, Integer.toString(getTaxTotalFromPayment(payment, 13)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_14, Integer.toString(getTaxTotalFromPayment(payment, 14)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_15, Integer.toString(getTaxTotalFromPayment(payment, 15)));
		t2Record.setFieldValue(TotalSalePartTwoRecord.FIELD_TAX_16, Integer.toString(getTaxTotalFromPayment(payment, 16)));

		return t2Record;
	}

	private List<TLOGEntry> createTotalTaxEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment) throws ParseException {
		
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
			HeaderRecord headerTX = createHeaderRecord(deviceId, TotalTaxRecord.ID, transactionNumber, recordSequence, payment);
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
	
	private ArrayList<TLOGEntry> createPaymentMethodEntriesForPayment(String deviceId, int transactionNumber, AtomicInteger recordSequence, Payment payment) throws ParseException {
		
		ArrayList<TLOGEntry> entries = new ArrayList<TLOGEntry>();
		
		// P record(s)
		for (Tender tender : payment.getTender()) {
			HeaderRecord headerP = createHeaderRecord(deviceId, MethodOfPaymentRecord.ID, transactionNumber, recordSequence, payment);
			MethodOfPaymentRecord pRecord = createPaymentRecord(tender);
			entries.add(new TLOGEntry(headerP, pRecord));
			recordSequence.getAndIncrement();

			// Generate authorization (A, A2, AE) records
			if (tender.getType().equals("CREDIT_CARD")) {
				// A record
				HeaderRecord headerA = createHeaderRecord(deviceId, AuthorizationRecord.ID, transactionNumber, recordSequence, payment);
				AuthorizationRecord aRecord = createAuthorizationRecord(tender);
				entries.add(new TLOGEntry(headerA, aRecord));
				recordSequence.getAndIncrement();

				// A2 record
				HeaderRecord headerA2 = createHeaderRecord(deviceId, AuthorizationResponseRecord.ID, transactionNumber, recordSequence, payment);
				AuthorizationResponseRecord a2Record = createAuthorizationResponseRecord(tender);
				entries.add(new TLOGEntry(headerA2, a2Record));
				recordSequence.getAndIncrement();

				// AE record
				HeaderRecord headerAE = createHeaderRecord(deviceId, ExtendedAuthorizationRecord.ID, transactionNumber, recordSequence, payment);
				ExtendedAuthorizationRecord aeRecord = createExtendedAuthorizationRecord(payment);
				entries.add(new TLOGEntry(headerAE, aeRecord));
				recordSequence.getAndIncrement();
			}
		}

		return entries;
	}
	
	private MethodOfPaymentRecord createPaymentRecord(Tender tender) {
		MethodOfPaymentRecord pRecord = new MethodOfPaymentRecord();

		pRecord.setFieldValue(MethodOfPaymentRecord.FIELD_TENDER_ID, getTenderId(tender));
		pRecord.setFieldValue(MethodOfPaymentRecord.FIELD_REFERENCE_NUMBER_1, tender.getId());

		int tendered = tender.getTotalMoney().getAmount();
		if (tender.getTenderedMoney() != null) {
			tendered = tender.getTenderedMoney().getAmount();
		}
		pRecord.setFieldValue(MethodOfPaymentRecord.FIELD_TENDER_AMOUNT, Integer.toString(tendered));

		int changeDue = 0;
		if (tender.getChangeBackMoney() != null) {
			changeDue = tender.getChangeBackMoney().getAmount();
		}
		pRecord.setFieldValue(MethodOfPaymentRecord.FIELD_CHANGE_DUE, Integer.toString(changeDue));

		return pRecord;
	}
	
	private AuthorizationRecord createAuthorizationRecord(Tender tender) {
		AuthorizationRecord aRecord = new AuthorizationRecord();

		aRecord.setFieldValue(AuthorizationRecord.FIELD_TENDER_ID, getTenderId(tender));
		// aRecord.setFieldValue(AuthorizationRecord.FIELD_CREDIT_OR_DEBIT_TYPE, "01"); // TODO(bhartard): ??
		aRecord.setFieldValue(AuthorizationRecord.FIELD_HOW_AUTHORIZED, "0"); // online
		aRecord.setFieldValue(AuthorizationRecord.FIELD_CARD_ENTRY_TYPE, tender.getEntryMethod().equals("SWIPED") ? "0" : "1"); // 0-swipe, 1-keyed
		aRecord.setFieldValue(AuthorizationRecord.FIELD_CARD_NUMBER, tender.getPanSuffix());
		aRecord.setFieldValue(AuthorizationRecord.FIELD_REQUEST_AMOUNT, Integer.toString(tender.getTotalMoney().getAmount()));
		aRecord.setFieldValue(AuthorizationRecord.FIELD_AUTHORIZED_AMOUNT, Integer.toString(tender.getTotalMoney().getAmount()));
		aRecord.setFieldValue(AuthorizationRecord.FIELD_CREDIT_OR_DEBIT_TRANSACTION_TYPE, "8"); // 8-Sale
		aRecord.setFieldValue(AuthorizationRecord.FIELD_A2_PART1_LENGTH, "8"); // "APPROVED"

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