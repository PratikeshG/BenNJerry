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
import vfcorp.tlog.StartingEndingBank;
import vfcorp.tlog.StoreClose;
import vfcorp.tlog.SubHeaderStoreSystemLocalizationInformation;
import vfcorp.tlog.TenderCount;
import vfcorp.tlog.TransactionHeader;
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
	
	public static enum TENDER_CODE {
		CASH, VISA, MASTERCARD, AMEX, DISCOVER, DISCOVERDINERS, JCB, DEBIT, CHECK, EGC, UNKNOWN
	}
	
	private static Map<TENDER_CODE,String> tenderCodes;
	
	static {
		tenderCodes = new HashMap<TENDER_CODE,String>();
		
		// TODO(colinlam): these were found by examining the sample given to us. Seems like it can
		// be configured, though. Need to verify.
		tenderCodes.put(TENDER_CODE.CASH, "1");
		tenderCodes.put(TENDER_CODE.VISA, "7");
		tenderCodes.put(TENDER_CODE.MASTERCARD, "9");
		tenderCodes.put(TENDER_CODE.AMEX, "11");
		tenderCodes.put(TENDER_CODE.DISCOVER, "13");
		tenderCodes.put(TENDER_CODE.DEBIT, "19");
		tenderCodes.put(TENDER_CODE.EGC, "30");
		
		// TODO(colinlam): This is a guess. There doesn't seem to be one for JCB. Find this out.
		tenderCodes.put(TENDER_CODE.JCB, "15");
		tenderCodes.put(TENDER_CODE.DISCOVERDINERS, "17");
		tenderCodes.put(TENDER_CODE.UNKNOWN, "99");
	}
	
	private List<Record> transactionLog;
	private int itemNumberLookupLength;

	public TLOG() {
		transactionLog = new LinkedList<Record>();
	}
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	public void parse(List<Payment> squarePayments, List<Employee> squareEmployees, List<CashDrawerShift> cashDrawerShifts, Merchant location) {
		// Translate each Square payment into a record in the transaction log
		
		createStoreInitializationRecords(squareEmployees, cashDrawerShifts, location);
		createItemSaleRecords(squarePayments, squareEmployees, location);
		
		// TODO(colinlam): pass refunds through here. they look similar to sales records.
		
		createStoreCloseRecords(squarePayments, squareEmployees, cashDrawerShifts, location);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Record record : transactionLog) {
			sb.append(record.toString() + "\n");
		}
		
		return sb.toString();
	}
	
	private void createStoreInitializationRecords(List<Employee> squareEmployees, List<CashDrawerShift> cashDrawerShifts, Merchant location) {
		
		String earliestOpenDate = "";
		for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
			if (earliestOpenDate.equals("") || earliestOpenDate.compareTo(cashDrawerShift.getOpenedAt()) > 0) {
				earliestOpenDate = cashDrawerShift.getOpenedAt();
			}
		}
		if (earliestOpenDate.equals("")) {
			earliestOpenDate = "00000000000000000000";
		}
		
		Map<String,String> params = new HashMap<String,String>();
		
		String date = earliestOpenDate.substring(5, 7) +
				earliestOpenDate.substring(8, 10) + 
				earliestOpenDate.substring(0, 4);
		params.put("Transaction Date", date);
		String time = earliestOpenDate.substring(11,13) + earliestOpenDate.substring(14, 16);
		params.put("Transaction Time", time);
		
		params.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
		
		params.put("Transaction Type", "010");
		
		params.put("Number of Records", "" + 2);
		
		transactionLog.add(new TransactionHeader().parse(params));
		transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
		
		for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
			Map<String,String> cashDrawerParams = new HashMap<String,String>();
			
			if (cashDrawerShift.getOpeningEmployeeId() != null) {
				for (Employee squareEmployee : squareEmployees) {
					if (squareEmployee.getId().equals(cashDrawerShift.getOpeningEmployeeId())) {
						cashDrawerParams.put("Employee Number", squareEmployee.getExternalId());
					}
				}
			}
			
			if (cashDrawerShift.getDevice().getName() != null) {
				cashDrawerParams.put("Register Number", cashDrawerShift.getDevice().getName());
			}
			
			String cashDrawerDate = cashDrawerShift.getOpenedAt().substring(5, 7) +
					cashDrawerShift.getOpenedAt().substring(8, 10) + 
					cashDrawerShift.getOpenedAt().substring(0, 4);
			cashDrawerParams.put("Transaction Date", cashDrawerDate);
			String cashDrawerTime = cashDrawerShift.getOpenedAt().substring(11,13) + cashDrawerShift.getOpenedAt().substring(14, 16);
			cashDrawerParams.put("Transaction Time", cashDrawerTime);
			
			cashDrawerParams.put("Transaction Type", "050");
			cashDrawerParams.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
			cashDrawerParams.put("Number of Records", "" + 1);
			
			transactionLog.add(new TransactionHeader().parse(cashDrawerParams));
			
			cashDrawerParams.put("Transaction Type", "699");
			cashDrawerParams.put("Number of Records", "" + 3);
			
			transactionLog.add(new TransactionHeader().parse(cashDrawerParams));
			
			transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			
			transactionLog.add(new AuthorizationCode().parse("35", cashDrawerParams.get("Employee Number"))); // 35 is "open register"
			
			cashDrawerParams.put("Transaction Type", "502");
			cashDrawerParams.put("Number of Records", "" + 4);
			
			transactionLog.add(new TransactionHeader().parse(cashDrawerParams));
			
			transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			
			transactionLog.add(new AuthorizationCode().parse("41", cashDrawerParams.get("Employee Number"))); // 41 is "starting bank"
			
			transactionLog.add(new StartingEndingBank().parse(true, cashDrawerParams.get("Employee Number"), "" + cashDrawerShift.getStartingCashMoney().getAmount()));
		}
	}
	
	private void createItemSaleRecords(List<Payment> squarePayments, List<Employee> squareEmployees, Merchant location) {
		
		for (Payment payment : squarePayments) {
			
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
			
			paymentList.addFirst(new TransactionHeader().parse(payment, location, squareEmployees, "200", paymentList.size() + 1));
			
			transactionLog.addAll(paymentList);
		}
	}

	private void createStoreCloseRecords(List<Payment> squarePayments, List<Employee> squareEmployees, List<CashDrawerShift> cashDrawerShifts, Merchant location) {
		
		String latestOpenDate = "";
		for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
			if (latestOpenDate.equals("") || latestOpenDate.compareTo(cashDrawerShift.getClosedAt()) < 0) {
				latestOpenDate = cashDrawerShift.getClosedAt();
			}
		}
		if (latestOpenDate.equals("")) {
			latestOpenDate = "00000000000000000000";
		}
		
		for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
			LinkedList<Record> newRecordList = new LinkedList<Record>();
			
			String registerNumber = "";
			if (cashDrawerShift.getDevice().getName() != null) {
				registerNumber = cashDrawerShift.getDevice().getName();
			}
			
			String employeeNumber = "";
			if (cashDrawerShift.getOpeningEmployeeId() != null) {
				for (Employee squareEmployee : squareEmployees) {
					if (squareEmployee.getId().equals(cashDrawerShift.getOpeningEmployeeId())) {
						employeeNumber = squareEmployee.getExternalId();
					}
				}
			}
			
			newRecordList.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			newRecordList.add(new CashierRegisterIdentification().parse(registerNumber));
			newRecordList.add(new StartingEndingBank().parse(false, registerNumber, "" + cashDrawerShift.getClosedCashMoney().getAmount()));
			newRecordList.add(new EndingBank().parse(registerNumber, "" + cashDrawerShift.getClosedCashMoney().getAmount()));
			
			// TODO(colinlam): in the sample SA file, there were entries for every possible
			// tender code. This will only produce one for cash. Are they all necessary?
			newRecordList.add(new TenderCount().parse(TENDER_CODE.CASH, cashDrawerShift));
			
			/*
			 * 037
			 *   Supported: 002, 003, 009, 013, 014, 015, 017, 018, 036, 
			 *   Not supported: 001, 010, 011, 022, 052, 054, 055, 056, 057, 058
			 */
			newRecordList.add(new ForInStoreReportingUseOnly().parse("002", squarePayments)); // "merchandise sales"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("003", squarePayments)); // "merchandise returns"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("009", squarePayments)); // "discounts"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("013", squarePayments)); // "sales tax"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("014", squarePayments)); // "net sales"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("015", squarePayments)); // "returns"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("017", squarePayments)); // "taxable sales"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("018", squarePayments)); // "non taxable sales"
			newRecordList.add(new ForInStoreReportingUseOnly().parse("036", squarePayments)); // "transaction discount"
			
			Map<String,String> cashDrawerParams = new HashMap<String,String>();
			
			cashDrawerParams.put("Register Number", registerNumber);
			cashDrawerParams.put("Employee Number", employeeNumber);
			
			String cashDrawerDate = cashDrawerShift.getOpenedAt().substring(5, 7) +
					cashDrawerShift.getOpenedAt().substring(8, 10) + 
					cashDrawerShift.getOpenedAt().substring(0, 4);
			cashDrawerParams.put("Transaction Date", cashDrawerDate);
			String cashDrawerTime = cashDrawerShift.getOpenedAt().substring(11,13) + cashDrawerShift.getOpenedAt().substring(14, 16);
			cashDrawerParams.put("Transaction Time", cashDrawerTime);
			
			cashDrawerParams.put("Transaction Type", "400");
			cashDrawerParams.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
			int size = newRecordList.size() + 1;
			cashDrawerParams.put("Number of Records", "" + size);
			
			newRecordList.addFirst(new TransactionHeader().parse(cashDrawerParams));
			
			// Add all records in current list to transaction log
			transactionLog.addAll(newRecordList);
		}
		
		Map<String,String> params = new HashMap<String,String>();
		
		String date = latestOpenDate.substring(5, 7) +
				latestOpenDate.substring(8, 10) + 
				latestOpenDate.substring(0, 4);
		params.put("Transaction Date", date);
		String time = latestOpenDate.substring(11,13) + latestOpenDate.substring(14, 16);
		params.put("Transaction Time", time);
		
		params.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
		
		params.put("Transaction Type", "040");
		
		params.put("Number of Records", "" + 4);
		
		transactionLog.add(new TransactionHeader().parse(params));
		transactionLog.add(new SubHeaderStoreSystemLocalizationInformation().parse());
		transactionLog.add(new StoreClose().parse());
		transactionLog.add(new DepositAmount().parse(squarePayments, location)); // TODO(colinlam): is this just for cash, or for all payments?
	}
	
	public static Map<TENDER_CODE,String> getTenderCodes() {
		return tenderCodes;
	}
}
