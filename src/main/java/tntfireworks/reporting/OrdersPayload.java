package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.ProcessingFee;
import com.squareup.connect.v2.Refund;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;

/*
 * Report 2 - "Orders Report" - Emailed daily
 *
 * Report 2 contains detailed daily payment information for all TNT locations and is sent daily. Each row in this
 * file represents an entry for a single tender amount. Thus, there can be multiple rows with the same order id
 * since multiple tenders can exist on a order. Connect V1 Payment and Connect V2 Order endpoints are used
 * in conjunction to build the report as the TenderCardDetails.EntryMethod field does not exist in Connect V1.
 * Additional information such as zip- code/state is pulled from a database for each TNT location and included in each
 * row/entry of the report.
 *
 */
public class OrdersPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesPayload.class);
    private static final String TRANSACTIONS_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Order Id", "Created At (UTC)", "Gross Sales", "Discounts", "Net Sales", "Tax", "Tip", "Tender Id",
            "Refund Amount", "Total Collected", "Source", "Card Amount", "Entry Method", "Cash Amount",
            "Other Tender Amount", "Other Tender Type", "Fees", "Net Total", "TNT Location #", "City", "State", "RBU",
            "SA NAME");
    private static final String DEFAULT_TENDER_ENTRY_METHOD = "NA";

    private List<OrdersPayloadEntry> ordersPayloadEntries;

    public OrdersPayload(String timeZone, int offset, TntLocationDetails locationDetails) {
        super(timeZone, offset, locationDetails, TRANSACTIONS_FILE_HEADER);
        ordersPayloadEntries = new ArrayList<OrdersPayloadEntry>();
    }

    public void addEntry(Order order, Map<String, Integer> tenderToFee, Map<String, String> tenderToEntryMethod, Map<String, Payment> tenderToPayment) {
        // - tender fee and tender entry method currently only available in
        // Connect V2
        // - use V2 map to obtain values
        String tenderFee = "";
        String tenderEntryMethod = DEFAULT_TENDER_ENTRY_METHOD;

        if(order != null && order.getTenders() != null) {
	    	 for (Tender tender : order.getTenders()) {
	             // get fee from tenderToFee mapping
	    		 if(tender != null && tender.getType() != null && !tender.getType().equals(Tender.TENDER_TYPE_NO_SALE)) {  // do not include 0 dollar payments
	    			 if (tenderToFee.containsKey(tender.getId())) {
		                 tenderFee = formatCurrencyTotal(tenderToFee.get(tender.getId()));
		             }

		             // retrieve entry method, if null (for cash), set to NA
		             if (tenderToEntryMethod.containsKey(tender.getId())) {
		                 tenderEntryMethod = tenderToEntryMethod.get(tender.getId());
		             }
		             Payment payment = tenderToPayment.get(tender.getId());

		             ordersPayloadEntries.add(new OrdersPayloadEntry(order, tender, tenderFee, tenderEntryMethod, payment));
	    		 }
	         }
        }
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (OrdersPayloadEntry entry : ordersPayloadEntries) {
            // write file row
            String row = String.format(
                    "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                    entry.orderId, entry.createdAt, formatCurrencyTotal(entry.grossSales), formatCurrencyTotal(entry.discounts),
                    formatCurrencyTotal(entry.netSales), formatCurrencyTotal(entry.tax), formatCurrencyTotal(entry.tip), entry.tenderId,
                    entry.refundAmt, formatCurrencyTotal(entry.totalCollected), entry.SOURCE, entry.cardAmt,
                    entry.tenderEntryMethod, entry.cashAmt, entry.otherTenderAmt, entry.otherTenderType,
                    entry.tenderFee, formatCurrencyTotal(entry.netTotal), locationDetails.locationNumber, locationDetails.city,
                    locationDetails.state, locationDetails.rbu, locationDetails.saName);
            rows.add(row);
        }
        return rows;
    }

    private class OrdersPayloadEntry {
        private static final String SOURCE = "Square POS";

        // payment data
        private String createdAt;
        private String orderId;
        private int grossSales;
        private int discounts;
        private int netSales;
        private int tax;
        private int tip;
        private int totalCollected;
        private int fees;
        private int netTotal;

        // tender data
        private String tenderId;
        private String cardAmt;
        private String tenderEntryMethod;
        private String cashAmt;
        private String otherTenderAmt;
        private String otherTenderType;
        private String refundAmt;
        private String tenderFee;

        private OrdersPayloadEntry(Order order, Tender tender, String tenderFee, String tenderEntryMethod, Payment payment) {
            // initialize payment data
        	int totalMoney = order.getTotalMoney() != null ? order.getTotalMoney().getAmount() : 0;
        	int totalTaxMoney = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
        	int totalDiscountMoney = order.getTotalDiscountMoney() != null ? order.getTotalDiscountMoney().getAmount() : 0;
        	int totalTipMoney = payment.getTipMoney() != null ? payment.getTipMoney().getAmount() : 0;
        	int netAmounts = order.getNetAmounts() != null && order.getNetAmounts().getTotalMoney() != null ? order.getNetAmounts().getTotalMoney().getAmount() : 0;

        	orderId = order.getId();
            createdAt = order.getCreatedAt();
            grossSales = totalMoney - totalTaxMoney + totalDiscountMoney - totalTipMoney;
            discounts = -totalDiscountMoney;
            netSales = totalMoney - totalTaxMoney - totalTipMoney;
            tax = totalTaxMoney;
            tip = totalTipMoney;
            int totalProcessingFee = 0;
        	totalProcessingFee = Arrays.stream(order.getTenders())
            		.map(Tender::getProcessingFeeMoney)
            		.filter(Objects::nonNull)
            		.mapToInt(Money::getAmount)
            		.sum();
            totalCollected = netAmounts;
            fees = -totalProcessingFee;
            netTotal = netAmounts - totalProcessingFee;

            // initialize tender details
            cardAmt = "";
            cashAmt = "";
            otherTenderAmt = "";
            otherTenderType = "";
            int totalRefundMoney = payment.getRefundedMoney() != null ? -payment.getRefundedMoney().getAmount() : 0;
            refundAmt = formatCurrencyTotal(totalRefundMoney);
            tenderId = tender.getId();
            this.tenderFee = tenderFee;
            this.tenderEntryMethod = tenderEntryMethod;

            // get tender details
            switch (tender.getType()) {
                case Tender.TENDER_TYPE_CARD:
                    cardAmt = formatCurrencyTotal(payment.getTotalMoney().getAmount());
                    break;
                case Tender.TENDER_TYPE_CASH:
                    cashAmt = formatCurrencyTotal(payment.getTotalMoney().getAmount());
                    break;
                default:
                    otherTenderAmt = formatCurrencyTotal(payment.getTotalMoney().getAmount());
                    otherTenderType = tender.getType();
                    break;
            }
        }
    }
}
