package vfcorp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import vfcorp.tlog.Associate;
import vfcorp.tlog.EventGiveback;
import vfcorp.tlog.ItemTaxMerchandiseNonMerchandiseItemsFees;
import vfcorp.tlog.LineItemAccountingString;
import vfcorp.tlog.LineItemAssociateAndDiscountAccountingString;
import vfcorp.tlog.MerchandiseItem;
import vfcorp.tlog.SubHeaderStoreSystemLocalizationInformation;
import vfcorp.tlog.TransactionHeader;
import vfcorp.tlog.TransactionSubTotal;
import vfcorp.tlog.TransactionTax;
import vfcorp.tlog.TransactionTaxExtended;
import vfcorp.tlog.TransactionTotal;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

public class TLOG {
	
	private List<Record> transactionLog;
	private int itemNumberLookupLength;

	public TLOG() {
		transactionLog = new LinkedList<Record>();
	}
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	public void parse(List<Payment> squarePayments, List<Employee> squareEmployees, Merchant location) {
		// Translate each Square payment into a record in the transaction log
		
		/* Here's a list of the transaction headers in the sample file:
		 * 010 - store open
		 *   086
		 * 050 - machine started for the day - repeated for all registers in the store
		 * 699 - open register - repeated for all registers in the store
		 *   086, 023
		 * 502 - starting bank - repeated for all registers in the store
		 *   086, 023, 016
		 * 200 - for each sale
		 *   086, 010 (not supported), 051, 052, 053
		 *   054 for each tax
		 *   001, 026, 025, 055, 056, 071 for each item
		 *   005
		 *   061
		 *   Item based on tender
		 * 601 - Hotkey to Backoffice (???)
		 *   086, 023
		 * 610 - transaction number consumed without SA (???)
		 * 900 - for each no sale
		 *   086, 022
		 * 400 - tender count register
		 *   086, 036, 016, 019
		 *   034
		 *   037
		 * 040 - store close
		 *   086, 017, 038
		 */
		createItemSaleRecords(squarePayments, squareEmployees, location);
		
		// TODO(colinlam): pass refunds through here. they look similar to sales records.
	}
	
	/* This would be the method to generate a Square list, if that was needed
	public List<Payment> generate() {
		return null;
	}
	*/
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Record record : transactionLog) {
			sb.append(record.toString() + "\n");
		}
		
		return sb.toString();
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
			
			paymentList.addFirst(new TransactionHeader().parse(payment, location, squareEmployees, paymentList.size() + 1));
			
			transactionLog.addAll(paymentList);
		}
	}
}
