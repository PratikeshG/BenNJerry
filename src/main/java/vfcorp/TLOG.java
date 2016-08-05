package vfcorp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mule.api.store.ObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vfcorp.tlog.Associate;
import vfcorp.tlog.AuthorizationCode;
import vfcorp.tlog.CashierRegisterIdentification;
import vfcorp.tlog.CreditCardTender;
import vfcorp.tlog.DiscountTypeIndicator;
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

import com.squareup.connect.Employee;
import com.squareup.connect.Item;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

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

	public void parse(Merchant location, Payment[] squarePayments, Item[] squareItems, Employee[] squareEmployees) throws Exception {
		List<Payment> squarePaymentsList = Arrays.asList(squarePayments);
		List<Item> squareItemsList = Arrays.asList(squareItems);
		List<Employee> squareEmployeesList = Arrays.asList(squareEmployees);
		
		/*
		 * Changes:
		 * 
		 * If restricted functionality is being requested, the TLOG should record when a manager authorizes that behavior. This is necessary for when sales get audited.
		 * 
		 * Tender counts (034) can be done for credit cards and others (they will be counting DisneyCard purchases this way).
		 */
		
		createSaleRecords(location, squarePaymentsList, squareItemsList, squareEmployeesList);
		createStoreCloseRecords(location, squarePaymentsList, squareEmployeesList);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Record record : transactionLog) {
			sb.append(record.toString() + "\r\n");
		}
		
		return sb.toString();
	}
	
	private void createSaleRecords(Merchant location, List<Payment> squarePaymentsList, List<Item> squareItemsList, List<Employee> squareEmployeesList) throws Exception {
		
		for (Payment payment : squarePaymentsList) {
			
			if (payment.getTender() != null && "NO_SALE".equals(payment.getTender()[0].getType())) {
				
				transactionLog.add(new TransactionHeader().parse(location, payment, squareEmployeesList, TransactionHeader.TRANSACTION_TYPE_NO_SALE, 3, objectStore, deployment, timeZoneId));
				
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
				
				for (PaymentItemization itemization : payment.getItemizations()) {
					paymentList.add(new MerchandiseItem().parse(itemization, squareItemsList, itemNumberLookupLength));
					
					String employeeId = "";
					boolean employeeIdShouldBePresent = false;
					boolean employeeFound = false;
					for (Tender tender : payment.getTender()) {
						if (tender.getEmployeeId() != null) {
							employeeIdShouldBePresent = true;
							for (Employee employee : squareEmployeesList) {
								if (tender.getEmployeeId().equals(employee.getId())) {
									employeeId = employee.getExternalId(); // this assumes external ID has been set
									employeeFound = true;
									break;
								}
							}
						}
					}

					if (employeeIdShouldBePresent && !employeeFound) {
						logger.error("tender had an employee ID that did not match any existing employee; aborting operation");
						throw new Exception("tender had an employee ID that did not match any existing employee; aborting operation");
					}

					if (employeeId.length() > 0) {
						paymentList.add(new Associate().parse(employeeId));
					}

					for (PaymentDiscount discount : itemization.getDiscounts()) {
						String discountType = "";
						String discountAppyType = "";
						String discountCode = "";
						String discountDetails = getValueInBrackets(discount.getName());

						if (discountDetails.length() == 5) {
							discountType = discountDetails.substring(0, 1).equals("1") ? "1" : "0";
							discountAppyType = discountDetails.substring(1, 2).equals("1") ? "1" : "0";
							discountCode = discountDetails.substring(2);

							// Only create 021 record for employee applied discounts
							if (discountType.equals("0")) {
								paymentList.add(new DiscountTypeIndicator().parse(itemization, discount, discountCode, discountAppyType));
							}
						}
					}

					for (PaymentTax tax : itemization.getTaxes()) {
						paymentList.add(new ItemTaxMerchandiseNonMerchandiseItemsFees().parse(tax, itemization));
					}

					int i = 1;
					for (double q = itemization.getQuantity(); q > 0; q = q - 1) {
						paymentList.add(new LineItemAccountingString().parse(itemization, itemNumberLookupLength, i++, q));
					}

					for (double q = itemization.getQuantity(); q > 0; q = q - 1) {
						paymentList.add(new LineItemAssociateAndDiscountAccountingString().parse(payment, itemization, itemNumberLookupLength, employeeId, q));
					}
				}
				
				for (Tender tender : payment.getTender()) {
					paymentList.add(new vfcorp.tlog.Tender().parse(tender));
					
					if (tender.getType().equals("CREDIT_CARD")) {
						paymentList.add(new CreditCardTender().parse(tender));
					}
				}
				
				paymentList.addFirst(new TransactionHeader().parse(location, payment, squareEmployeesList, TransactionHeader.TRANSACTION_TYPE_SALE, paymentList.size() + 1, objectStore, deployment, timeZoneId));
				
				transactionLog.addAll(paymentList);
			}
		}
	}

	private void createStoreCloseRecords(Merchant location, List<Payment> squarePaymentsList, List<Employee> squareEmployeesList) throws Exception {
		
		Set<String> deviceNames = new HashSet<String>();
		for (Payment squarePayment : squarePaymentsList) {
			if (squarePayment.getDevice() != null && squarePayment.getDevice().getName() != null && !squarePayment.getDevice().getName().equals("")) {
				deviceNames.add(squarePayment.getDevice().getName());
			}
		}
		
		for (String deviceName : deviceNames) {
		
			LinkedList<Record> newRecordList = new LinkedList<Record>();
			
			newRecordList.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			
			newRecordList.add(new CashierRegisterIdentification().parse(deviceName));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CASH, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_AMEX, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MALL_GC, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DISCOVER, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_JCB, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DEBIT, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CHEQUE, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MAIL_CHEQUE, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_EGC, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_STORE_CREDIT, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_TRAVELERS_CHEQUE, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_GIFT_CERTIFICATE, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VISA, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MASTERCARD, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_98, squarePaymentsList));
			
			newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_ECHECK, squarePaymentsList));
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_MERCHANDISE_SALES, squarePaymentsList));
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_DISCOUNTS, squarePaymentsList));
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_SALES_TAX, squarePaymentsList));
			
			newRecordList.addFirst(new TransactionHeader().parse(location, squarePaymentsList, deviceName, TransactionHeader.TRANSACTION_TYPE_TENDER_COUNT_REGISTER, newRecordList.size() + 1, objectStore, deployment, timeZoneId));
			
			transactionLog.addAll(newRecordList);
		}
	}

	private String getValueInBrackets(String input) {
		String value = "";

		int firstIndex = input.indexOf('[');
		int lastIndex = input.indexOf(']');
		if (firstIndex > -1 && lastIndex > -1 && lastIndex > firstIndex) {
			value = input.substring(firstIndex + 1, lastIndex);
		}

		return value;
	}
}
