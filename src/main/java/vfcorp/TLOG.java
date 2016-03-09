package vfcorp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

import vfcorp.tlog.Associate;
import vfcorp.tlog.EventGiveback;
import vfcorp.tlog.ItemTaxMerchandiseNonMerchandiseItemsFees;
import vfcorp.tlog.LineItemAccountingString;
import vfcorp.tlog.LineItemAssociateAndDiscountAccountingString;
import vfcorp.tlog.MerchandiseItem;
import vfcorp.tlog.TransactionHeader;

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
			
			// TODO(colinlam): transaction-specific stuff goes here
			
			// 086
			// 010
			// 051
			// 052
			// 053
			// 054
			// 029
			// 030
			
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
			
			paymentList.addFirst(new TransactionHeader().parse(payment, location, squareEmployees, paymentList.size() + 1));
			
			transactionLog.addAll(paymentList);
		}
	}
	
	// This method would never be used
	public List<Record> parse(BufferedReader br) throws IOException {
		List<Record> l = new LinkedList<Record>();
		
		for (String record = br.readLine(); record != null; record = br.readLine()) {
			String id = record.substring(0, 3);
			
			if (id.equals("000")) {
			    l.add(new TransactionHeader(record));
			} /*else if (id.equals("001")) {
			    l.add(new MerchandiseItem(record));
			} else if (id.equals("002")) {
			    l.add(new Non-Merchandise Item(record));
			} else if (id.equals("003")) {
			    l.add(new Gift Certificate Sale(record));
			} else if (id.equals("004")) {
			    l.add(new Fee(record));
			} else if (id.equals("005")) {
			    l.add(new Charity Donations(record));
			} else if (id.equals("006")) {
			    l.add(new 3rd Party Tax Document Info(record));
			} else if (id.equals("009")) {
			    l.add(new Employee Discount(record));
			} else if (id.equals("010")) {
			    l.add(new Preferred Customer(record));
			} else if (id.equals("011")) {
			    l.add(new Employee(record));
			} else if (id.equals("012")) {
			    l.add(new On Account Payment(record));
			} else if (id.equals("013")) {
			    l.add(new Resumed Transaction(record));
			} else if (id.equals("014")) {
			    l.add(new Zip Code(record));
			} else if (id.equals("015")) {
			    l.add(new Tax Exempt Transaction(record));
			} else if (id.equals("016")) {
			    l.add(new Starting/Ending Bank(record));
			} else if (id.equals("017")) {
			    l.add(new Store Close(record));
			} else if (id.equals("018")) {
			    l.add(new Payout, Payin, Pickup(record));
			} else if (id.equals("019")) {
			    l.add(new Ending Bank(record));
			} else if (id.equals("021")) {
			    l.add(new Discount Type Indicator(record));
			} else if (id.equals("022")) {
			    l.add(new Reason Code(record));
			} else if (id.equals("023")) {
			    l.add(new Authorization Code(record));
			} else if (id.equals("024")) {
			    l.add(new Color/Size/Width (not currently available)(record));
			} else if (id.equals("025")) {
			    l.add(new Item Tax – Merchandise, Non Merchandise Items, Fees(record));
			} else if (id.equals("026")) {
			    l.add(new Associate(record));
			} else if (id.equals("027")) {
			    l.add(new Incentive(record));
			} else if (id.equals("028")) {
			    l.add(new Additional Input(record));
			} else if (id.equals("029")) {
			    l.add(new Name(record));
			} else if (id.equals("030")) {
			    l.add(new Address(record));
			} else if (id.equals("031")) {
			    l.add(new Phone Number(record));
			} else if (id.equals("032")) {
			    l.add(new Layaway/Special Order Number(record));
			} else if (id.equals("033")) {
			    l.add(new Layaway/Special Order Payment(record));
			} else if (id.equals("034")) {
			    l.add(new Tender Count(record));
			} else if (id.equals("035")) {
			    l.add(new Tender Clear(record));
			} else if (id.equals("036")) {
			    l.add(new Cashier/Register Identification(record));
			} else if (id.equals("037")) {
			    l.add(new For In-Store Reporting Use Only(record));
			} else if (id.equals("038")) {
			    l.add(new Deposit Amount(record));
			} else if (id.equals("039")) {
			    l.add(new Reprint Original Transaction Information(record));
			} else if (id.equals("040")) {
			    l.add(new Original Transaction Info(record));
			} else if (id.equals("041")) {
			    l.add(new Layaway/Special Order Adjustment Info(record));
			} else if (id.equals("042")) {
			    l.add(new Layaway/Special Order Applied Deposits(record));
			} else if (id.equals("043")) {
			    l.add(new Layaway/Special Order Need By Date(record));
			} else if (id.equals("046")) {
			    l.add(new Layaway/Special Order Time Stamp(record));
			} else if (id.equals("051")) {
			    l.add(new Transaction SubTotal(record));
			} else if (id.equals("052")) {
			    l.add(new Transaction Tax(record));
			} else if (id.equals("053")) {
			    l.add(new Transaction Total(record));
			} else if (id.equals("054")) {
			    l.add(new Transaction Tax(record));
			} else if (id.equals("055")) {
			    l.add(new Line Item Accounting String(record));
			} else if (id.equals("056")) {
			    l.add(new Line Item Associate and Discount Accounting String(record));
			} else if (id.equals("057")) {
			    l.add(new Security Recovery Cashier(record));
			} else if (id.equals("060")) {
			    l.add(new Rounding Adjustment(record));
			} else if (id.equals("061")) {
			    l.add(new Tender(record));
			} else if (id.equals("062")) {
			    l.add(new Foreign Currency(record));
			} else if (id.equals("063")) {
			    l.add(new Gift Certificate Tender(record));
			} else if (id.equals("064")) {
			    l.add(new Store Credit Tender(record));
			} else if (id.equals("065")) {
			    l.add(new Credit Card Tender(record));
			} else if (id.equals("066")) {
			    l.add(new Debit Card Tender(record));
			} else if (id.equals("067")) {
			    l.add(new Check Tender(record));
			} else if (id.equals("068")) {
			    l.add(new Check Tender Identification(record));
			} else if (id.equals("069")) {
			    l.add(new Check Tender MICR(record));
			} else if (id.equals("070")) {
			    l.add(new House Charge(record));
			} else if (id.equals("071")) {
			    l.add(new Event Giveback(record));
			} else if (id.equals("072")) {
			    l.add(new Send Sale Information (not used)(record));
			} else if (id.equals("073")) {
			    l.add(new Change Date/Time(record));
			} else if (id.equals("074")) {
			    l.add(new Debit Reconciliation(record));
			} else if (id.equals("076")) {
			    l.add(new Store Credit Buyback(record));
			} else if (id.equals("077")) {
			    l.add(new Post Void(record));
			} else if (id.equals("078")) {
			    l.add(new Original Resumed Transaction(record));
			} else if (id.equals("079")) {
			    l.add(new Send Sale Information(record));
			} else if (id.equals("080")) {
			    l.add(new Send Sale Transaction Information(record));
			} else if (id.equals("081")) {
			    l.add(new Electronic Gift Card(record));
			} else if (id.equals("082")) {
			    l.add(new Undocked PDAs at Store Close(record));
			} else if (id.equals("083")) {
			    l.add(new Issue Bounceback Coupon(record));
			} else if (id.equals("084")) {
			    l.add(new International Customer Name/Address Information(record));
			} else if (id.equals("085")) {
			    l.add(new Signature Capture(record));
			} else if (id.equals("086")) {
			    l.add(new Sub Header: Store System Localization Information(record));
			} else if (id.equals("087")) {
			    l.add(new Loyalty Points(record));
			} else if (id.equals("090")) {
			    l.add(new Loyalty Reward(record));
			} else if (id.equals("097")) {
			    l.add(new Reserved for Use by SalesAudit(record));
			} else if (id.equals("098")) {
			    l.add(new Special User Defined String (utilized by Focus code)(record));
			} else if (id.equals("099")) {
			    l.add(new User Defined (utilized in User Exit coding)(record));
			} else if (id.equals("101")) {
			    l.add(new Alternate Item Identifiers(record));
			} else if (id.equals("110")) {
			    l.add(new CRM Alternate Key(record));
			} else if (id.equals("120")) {
			    l.add(new Rental Transaction Data(record));
			} else if (id.equals("121")) {
			    l.add(new Rental Discounts(record));
			} else if (id.equals("122")) {
			    l.add(new Sale of a Rental Item(record));
			} else if (id.equals("123")) {
			    l.add(new Rental Transaction Header(record));
			} else if (id.equals("125")) {
			    l.add(new Item Tax - Merchandise, Non-Merchandise Items, Fees - Third Party Tax Model(record));
			} else if (id.equals("154")) {
			    l.add(new Transaction Tax By Jurisdiction - Third Party(record));
			} else if (id.equals("165")) {
			    l.add(new Void Pre-Paid Credit Card(record));
			}
			*/
		}
		
		return l;
	}
}
