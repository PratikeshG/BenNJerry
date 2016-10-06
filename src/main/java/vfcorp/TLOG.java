package vfcorp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mule.api.store.ObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

import vfcorp.tlog.Associate;
import vfcorp.tlog.CashierRegisterIdentification;
import vfcorp.tlog.CreditCardTender;
import vfcorp.tlog.DiscountTypeIndicator;
import vfcorp.tlog.EmployeeDiscount;
import vfcorp.tlog.EventGiveback;
import vfcorp.tlog.ForInStoreReportingUseOnly;
import vfcorp.tlog.ItemTaxMerchandiseNonMerchandiseItemsFees;
import vfcorp.tlog.LineItemAccountingString;
import vfcorp.tlog.LineItemAssociateAndDiscountAccountingString;
import vfcorp.tlog.MerchandiseItem;
import vfcorp.tlog.ReasonCode;
import vfcorp.tlog.SubHeaderStoreSystemLocalizationInformation;
import vfcorp.tlog.TenderCount;
import vfcorp.tlog.TransactionHeader;
import vfcorp.tlog.TransactionSubTotal;
import vfcorp.tlog.TransactionTax;
import vfcorp.tlog.TransactionTaxExtended;
import vfcorp.tlog.TransactionTotal;

public class TLOG {

    private List<Record> transactionLog;
    private int itemNumberLookupLength;
    private String deployment;
    private String timeZoneId;
    private static Logger logger = LoggerFactory.getLogger(TLOG.class);

    private ObjectStore<String> objectStore;

    public TLOG() {
	transactionLog = new LinkedList<Record>();
    }

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
	this.itemNumberLookupLength = itemNumberLookupLength;
    }

    public void setDeployment(String deployment) {
	this.deployment = deployment;
    }

    public void setTimeZoneId(String timeZoneId) {
	this.timeZoneId = timeZoneId;
    }

    public void setObjectStore(ObjectStore<String> objectStore) {
	this.objectStore = objectStore;
    }

    public void parse(Merchant location, Payment[] squarePayments, Employee[] squareEmployees) throws Exception {
	List<Payment> squarePaymentsList = Arrays.asList(squarePayments);
	List<Employee> squareEmployeesList = Arrays.asList(squareEmployees);

	/*
	 * Changes:
	 * 
	 * If restricted functionality is being requested, the TLOG should
	 * record when a manager authorizes that behavior. This is necessary for
	 * when sales get audited.
	 * 
	 * Tender counts (034) can be done for credit cards and others (they
	 * will be counting DisneyCard purchases this way).
	 */

	createSaleRecords(location, squarePaymentsList, squareEmployeesList);
	createStoreCloseRecords(location, squarePaymentsList, squareEmployeesList);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	for (Record record : transactionLog) {
	    sb.append(record.toString() + "\r\n");
	}

	return sb.toString();
    }

    private void createSaleRecords(Merchant location, List<Payment> squarePaymentsList,
	    List<Employee> squareEmployeesList) throws Exception {

	for (Payment payment : squarePaymentsList) {

	    if (payment.getTender() != null && "NO_SALE".equals(payment.getTender()[0].getType())) {

		transactionLog.add(new TransactionHeader().parse(location, payment, squareEmployeesList,
			TransactionHeader.TRANSACTION_TYPE_NO_SALE, 3, objectStore, deployment, timeZoneId));

		transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());

		transactionLog.add(new ReasonCode().parse(ReasonCode.FUNCTION_INDICATOR_NO_SALE));
	    } else {

		LinkedList<Record> paymentList = new LinkedList<Record>();

		paymentList.add(new SubHeaderStoreSystemLocalizationInformation().parse());

		paymentList.add(new TransactionSubTotal().parse(payment));

		paymentList.add(new TransactionTax().parse(payment));

		paymentList.add(new TransactionTotal().parse(payment));

		for (PaymentTax tax : payment.getAdditiveTax()) {
		    paymentList.add(new TransactionTaxExtended().parse(payment, tax));
		}

		for (PaymentTax tax : payment.getInclusiveTax()) {
		    paymentList.add(new TransactionTaxExtended().parse(payment, tax));
		}

		int itemSequence = 1;
		for (PaymentItemization itemization : payment.getItemizations()) {
		    paymentList.add(new MerchandiseItem().parse(itemization, itemSequence++, itemNumberLookupLength));

		    String employeeId = "";
		    boolean employeeIdShouldBePresent = false;
		    boolean employeeFound = false;
		    for (Tender tender : payment.getTender()) {
			if (tender.getEmployeeId() != null) {
			    employeeIdShouldBePresent = true;
			    for (Employee employee : squareEmployeesList) {
				if (tender.getEmployeeId().equals(employee.getId())) {
				    if (employee.getExternalId() != null) {
					employeeId = employee.getExternalId();
				    }
				    employeeFound = true;
				    break;
				}
			    }
			}
		    }

		    if (employeeIdShouldBePresent && !employeeFound) {
			logger.error(
				"tender had an employee ID that did not match any existing employee; aborting operation");
			throw new Exception(
				"tender had an employee ID that did not match any existing employee; aborting operation");
		    }

		    if (employeeId.length() > 0) {
			paymentList.add(new Associate().parse(employeeId));
		    }

		    // Add promo records (071) after 056
		    // LineItemAssociateAndDiscountAccountingString records
		    ArrayList<EventGiveback> promoRecords = new ArrayList<EventGiveback>();

		    for (PaymentDiscount discount : itemization.getDiscounts()) {
			String discountType = "";
			String discountAppyType = "";
			String discountCode = "";
			String discountDetails = Util.getValueInBrackets(discount.getName());

			if (discountDetails.length() == 5) {
			    String firstChar = discountDetails.substring(0, 1);
			    if (firstChar.equals("1") || firstChar.equals("2")) {
				discountType = firstChar;
			    } else {
				discountType = "0";
			    }
			    discountAppyType = discountDetails.substring(1, 2).equals("1") ? "1" : "0";
			    discountCode = discountDetails.substring(2);

			    // Only create 021 record for employee applied
			    // discounts
			    if (discountType.equals("0")) {
				paymentList.add(new DiscountTypeIndicator().parse(itemization, discount, discountCode,
					discountAppyType));
			    } else if (discountType.equals("2")) {
				paymentList.add(new EmployeeDiscount().parse(itemization, discount));
			    } else if (discountType.equals("1")) {
				promoRecords.add(new EventGiveback().parse(itemization, discount,
					itemNumberLookupLength, discountCode, discountAppyType));
			    }
			}
		    }

		    for (PaymentTax tax : itemization.getTaxes()) {
			paymentList.add(new ItemTaxMerchandiseNonMerchandiseItemsFees().parse(tax, itemization));
		    }

		    int i = 1;
		    for (double q = itemization.getQuantity(); q > 0; q = q - 1) {
			paymentList.add(new LineItemAccountingString().parse(itemization, itemNumberLookupLength, i));
			paymentList.add(new LineItemAssociateAndDiscountAccountingString().parse(payment, itemization,
				itemNumberLookupLength, i, employeeId));
			i++;
		    }

		    for (EventGiveback promo : promoRecords) {
			paymentList.add(promo);
		    }
		}

		for (Tender tender : payment.getTender()) {
		    paymentList.add(new vfcorp.tlog.Tender().parse(tender));

		    if (tender.getType().equals("CREDIT_CARD")) {
			paymentList.add(new CreditCardTender().parse(tender));
		    }
		}

		paymentList.addFirst(new TransactionHeader().parse(location, payment, squareEmployeesList,
			TransactionHeader.TRANSACTION_TYPE_SALE, paymentList.size() + 1, objectStore, deployment,
			timeZoneId));

		transactionLog.addAll(paymentList);
	    }
	}
    }

    private void createStoreCloseRecords(Merchant location, List<Payment> locationPayments,
	    List<Employee> squareEmployeesList) throws Exception {
	Map<String, List<Payment>> devicePaymentsList = new HashMap<String, List<Payment>>();

	for (Payment payment : locationPayments) {
	    String regNumber = Util.getRegisterNumber(null); // get default #
	    if (payment.getDevice() != null) {
		regNumber = Util.getRegisterNumber(payment.getDevice().getName());
	    }

	    // Add payment to device-specific payment list
	    List<Payment> devicePayments = devicePaymentsList.get(regNumber);
	    if (devicePayments == null) {
		devicePayments = new ArrayList<Payment>();
		devicePaymentsList.put(regNumber, devicePayments);
	    }
	    devicePayments.add(payment);
	}

	for (String registerNumber : devicePaymentsList.keySet()) {
	    List<Payment> registerPayments = devicePaymentsList.get(registerNumber);
	    LinkedList<Record> newRecordList = new LinkedList<Record>();

	    newRecordList.add(new SubHeaderStoreSystemLocalizationInformation().parse());
	    newRecordList.add(new CashierRegisterIdentification().parse(registerNumber));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CASH, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_AMEX, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MALL_GC, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DISCOVER, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_JCB, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DEBIT, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CHEQUE, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MAIL_CHEQUE, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_EGC, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_STORE_CREDIT, registerPayments));
	    newRecordList
		    .add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_TRAVELERS_CHEQUE, registerPayments));
	    newRecordList
		    .add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_GIFT_CERTIFICATE, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VISA, registerPayments));
	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MASTERCARD, registerPayments));

	    // Catch all for "other" - only used by Kipling?
	    if (deployment.contains("kipling")) {
		newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_98, registerPayments));
	    }

	    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_ECHECK, registerPayments));
	    newRecordList.add(new ForInStoreReportingUseOnly()
		    .parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_MERCHANDISE_SALES, registerPayments));
	    newRecordList.add(new ForInStoreReportingUseOnly()
		    .parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_DISCOUNTS, registerPayments));
	    newRecordList.add(new ForInStoreReportingUseOnly()
		    .parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_SALES_TAX, registerPayments));
	    newRecordList.addFirst(new TransactionHeader().parse(location, registerPayments, registerNumber,
		    TransactionHeader.TRANSACTION_TYPE_TENDER_COUNT_REGISTER, newRecordList.size() + 1, objectStore,
		    deployment, timeZoneId));

	    transactionLog.addAll(newRecordList);
	}
    }
}
