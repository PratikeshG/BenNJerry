package vfcorp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vfcorp.tlog.Associate;
import vfcorp.tlog.AuthorizationCode;
import vfcorp.tlog.CashierRegisterIdentification;
import vfcorp.tlog.CreditCardTender;
import vfcorp.tlog.DepositAmount;
import vfcorp.tlog.EndingBank;
import vfcorp.tlog.EventGiveback;
import vfcorp.tlog.ForInStoreReportingUseOnly;
import vfcorp.tlog.ItemTaxMerchandiseNonMerchandiseItemsFees;
import vfcorp.tlog.LineItemAccountingString;
import vfcorp.tlog.LineItemAssociateAndDiscountAccountingString;
import vfcorp.tlog.MerchandiseItem;
import vfcorp.tlog.ReasonCode;
import vfcorp.tlog.StartingEndingBank;
import vfcorp.tlog.StoreClose;
import vfcorp.tlog.SubHeaderStoreSystemLocalizationInformation;
import vfcorp.tlog.TenderCount;
import vfcorp.tlog.TransactionHeader;
import vfcorp.tlog.TransactionHeader.TransactionType;
import vfcorp.tlog.TransactionSubTotal;
import vfcorp.tlog.TransactionTax;
import vfcorp.tlog.TransactionTaxExtended;
import vfcorp.tlog.TransactionTotal;

import com.squareup.connect.CashDrawerShift;
import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

public class TLOG {
	
	public static enum TenderCode {
		CASH, VISA, MASTERCARD, AMEX, DISCOVER, DISCOVERDINERS, JCB, DEBIT, CHECK, EGC, UNKNOWN
	}
	
	private static Map<TenderCode,String> tenderCodes;
	
	static {
		tenderCodes = new HashMap<TenderCode,String>();
		
		// TODO(colinlam): these were found by examining the sample given to us. Seems like it can
		// be configured, though. Need to verify.
		tenderCodes.put(TenderCode.CASH, "1");
		tenderCodes.put(TenderCode.VISA, "7");
		tenderCodes.put(TenderCode.MASTERCARD, "9");
		tenderCodes.put(TenderCode.AMEX, "11");
		tenderCodes.put(TenderCode.DISCOVER, "13");
		tenderCodes.put(TenderCode.DEBIT, "19");
		tenderCodes.put(TenderCode.EGC, "30");
		
		// TODO(colinlam): This is a guess. There doesn't seem to be one for JCB. Find this out.
		tenderCodes.put(TenderCode.JCB, "15");
		tenderCodes.put(TenderCode.DISCOVERDINERS, "17");
		tenderCodes.put(TenderCode.UNKNOWN, "99");
	}
	
	private List<Record> transactionLog;
	private int itemNumberLookupLength;

	public TLOG() {
		transactionLog = new LinkedList<Record>();
	}
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	public void parse(Merchant location, List<Payment> squarePayments, List<Employee> squareEmployees, List<CashDrawerShift> cashDrawerShifts) {
		createStoreInitializationRecords(location, squareEmployees, cashDrawerShifts);
		createItemSaleRecords(location, squarePayments, squareEmployees);
		createStoreCloseRecords(location, squarePayments, squareEmployees, cashDrawerShifts);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Record record : transactionLog) {
			sb.append(record.toString() + "\n");
		}
		
		return sb.toString();
	}
	
	private void createStoreInitializationRecords(Merchant location, List<Employee> squareEmployees, List<CashDrawerShift> cashDrawerShifts) {
		transactionLog.add(new TransactionHeader().parse(location, cashDrawerShifts, TransactionType.STORE_OPEN, 2));
		
		transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
		
		for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
			
			transactionLog.add(new TransactionHeader().parse(location, squareEmployees, cashDrawerShift, TransactionType.MACHINE_STARTED_FOR_THE_DAY, 1));

			transactionLog.add(new TransactionHeader().parse(location, squareEmployees, cashDrawerShift, TransactionType.OPEN_REGISTER, 3));
			
			transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			
			transactionLog.add(new AuthorizationCode().parse(squareEmployees, cashDrawerShift, "35")); // 35 is "open register"
			
			transactionLog.add(new TransactionHeader().parse(location, squareEmployees, cashDrawerShift, TransactionType.STARTING_BANK, 4));
			
			transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			
			transactionLog.add(new AuthorizationCode().parse(squareEmployees, cashDrawerShift, "41")); // 41 is "starting bank"
			
			transactionLog.add(new StartingEndingBank().parse(cashDrawerShift, "" + cashDrawerShift.getStartingCashMoney().getAmount(), true));
		}
	}
	
	private void createItemSaleRecords(Merchant location, List<Payment> squarePayments, List<Employee> squareEmployees) {
		
		for (Payment payment : squarePayments) {
			
			if (payment.getTender() != null && "NO_SALE".equals(payment.getTender()[0].getType())) {
				
				transactionLog.add(new TransactionHeader().parse(location, payment, squareEmployees, TransactionType.NO_SALE, 3));
				
				transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
				
				transactionLog.add(new ReasonCode().parse("05")); // 05 is "no sale"
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
					paymentList.add(new MerchandiseItem().parse(itemization, itemNumberLookupLength));
					
					Set<String> employeeIds = new HashSet<String>();
					for (Tender tender : payment.getTender()) {
						if (tender.getEmployeeId() != null) {
							paymentList.add(new Associate().parse(tender.getEmployeeId(), squareEmployees));
							employeeIds.add(tender.getEmployeeId());
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
						for (String employeeId : employeeIds) {
							paymentList.add(new LineItemAssociateAndDiscountAccountingString().parse(payment, itemization, itemNumberLookupLength, employeeId, squareEmployees, q));
						}
					}
					
					paymentList.add(new EventGiveback().parse(itemization, itemNumberLookupLength));
				}
				
				for (Tender tender : payment.getTender()) {
					paymentList.add(new vfcorp.tlog.Tender().parse(tender));
					
					if (tender.getType().equals("CREDIT_CARD")) {
						paymentList.add(new CreditCardTender().parse(tender));
					}
				}
				
				paymentList.addFirst(new TransactionHeader().parse(location, payment, squareEmployees, TransactionType.SALE, paymentList.size() + 1));
				
				transactionLog.addAll(paymentList);
			}
		}
		
		// TODO(colinlam): add refunds (and get a sample of what a refund looks like from VFC)
	}

	private void createStoreCloseRecords(Merchant location, List<Payment> squarePayments, List<Employee> squareEmployees, List<CashDrawerShift> cashDrawerShifts) {
		
		for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
			LinkedList<Record> newRecordList = new LinkedList<Record>();
			
			newRecordList.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			
			newRecordList.add(new CashierRegisterIdentification().parse(cashDrawerShift));
			
			newRecordList.add(new StartingEndingBank().parse(cashDrawerShift, "" + cashDrawerShift.getClosedCashMoney().getAmount(), false));
			
			newRecordList.add(new EndingBank().parse(cashDrawerShift, "" + cashDrawerShift.getClosedCashMoney().getAmount()));
			
			// TODO(colinlam): in the sample SA file, there were entries for every possible
			// tender code. This will only produce one for cash. Are they all necessary?
			newRecordList.add(new TenderCount().parse(TenderCode.CASH, cashDrawerShift));
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("002", squarePayments)); // "merchandise sales"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("003", squarePayments)); // "merchandise returns"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("009", squarePayments)); // "discounts"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("013", squarePayments)); // "sales tax"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("014", squarePayments)); // "net sales"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("015", squarePayments)); // "returns"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("017", squarePayments)); // "taxable sales"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("018", squarePayments)); // "non taxable sales"
			
			newRecordList.add(new ForInStoreReportingUseOnly().parse("036", squarePayments)); // "transaction discount"
			
			newRecordList.addFirst(new TransactionHeader().parse(location, squareEmployees, cashDrawerShift, TransactionType.TENDER_COUNT_REGISTER, newRecordList.size() + 1));
			
			transactionLog.addAll(newRecordList);
		}
		
		transactionLog.add(new TransactionHeader().parse(location, cashDrawerShifts, TransactionType.STORE_CLOSE, 4));
		
		transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
		
		transactionLog.add(new StoreClose().parse());
		
		transactionLog.add(new DepositAmount().parse(squarePayments, location)); // TODO(colinlam): is this just for cash, or for all payments?
	}
	
	public static Map<TenderCode,String> getTenderCodes() {
		return tenderCodes;
	}
}
