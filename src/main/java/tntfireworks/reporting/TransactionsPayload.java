package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

/*
 * Report 2 - "Transactions Report" - Emailed daily
 *
 * Report 2 contains detailed daily payment information for all TNT locations and is sent daily. Each row in this
 * file represents an entry for a single tender amount. Thus, there can be multiple rows with the same transaction id
 * since multiple tenders can exist on a transaction. Connect V1 Payment and Connect V2 Transaction endpoints are used
 * in conjunction to build the report as the TenderCardDetails.EntryMethod field does not exist in Connect V1.
 * Additional information such as zip- code/state is pulled from a database for each TNT location and included in each
 * row/entry of the report.
 *
 */
public class TransactionsPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesPayload.class);
    private static final String TRANSACTIONS_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Payment Id", "Created At (UTC)", "Gross Sales", "Discounts", "Net Sales", "Tax", "Tip", "Tender Id",
            "Refund Amount", "Total Collected", "Source", "Card Amount", "Entry Method", "Cash Amount",
            "Other Tender Amount", "Other Tender Type", "Fees", "Net Total", "TNT Location #", "City", "State", "RBU",
            "SA NAME");
    private static final String DEFAULT_TENDER_ENTRY_METHOD = "NA";

    private List<TransactionsPayloadEntry> transactionsPayloadEntries;

    public TransactionsPayload(String timeZone, TntLocationDetails locationDetails) {
        super(timeZone, locationDetails, TRANSACTIONS_FILE_HEADER);
        transactionsPayloadEntries = new ArrayList<TransactionsPayloadEntry>();
    }

    public void addEntry(Payment payment, Map<String, Integer> tenderToFee, Map<String, String> tenderToEntryMethod) {
        // - tender fee and tender entry method currently only available in
        // Connect V2
        // - use V2 map to obtain values
        String tenderFee = "";
        String tenderEntryMethod = DEFAULT_TENDER_ENTRY_METHOD;

        for (Tender tender : payment.getTender()) {
            // get fee from tenderToFee mapping
            if (tenderToFee.containsKey(tender.getId())) {
                tenderFee = formatTotal(tenderToFee.get(tender.getId()));
            }

            // retrieve entry method, if null (for cash), set to NA
            if (tenderToEntryMethod.containsKey(tender.getId())) {
                tenderEntryMethod = tenderToEntryMethod.get(tender.getId());
            }

            transactionsPayloadEntries.add(new TransactionsPayloadEntry(payment, tender, tenderFee, tenderEntryMethod));
        }
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (TransactionsPayloadEntry entry : transactionsPayloadEntries) {
            // write file row
            String row = String.format(
                    "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                    entry.paymentId, entry.createdAt, formatTotal(entry.grossSales), formatTotal(entry.discounts),
                    formatTotal(entry.netSales), formatTotal(entry.tax), formatTotal(entry.tip), entry.tenderId,
                    entry.refundAmt, formatTotal(entry.totalCollected), entry.SOURCE, entry.cardAmt,
                    entry.tenderEntryMethod, entry.cashAmt, entry.otherTenderAmt, entry.otherTenderType,
                    entry.tenderFee, formatTotal(entry.netTotal), locationDetails.locationNumber, locationDetails.city,
                    locationDetails.state, locationDetails.rbu, locationDetails.saName);
            rows.add(row);
        }
        return rows;
    }

    private class TransactionsPayloadEntry {
        private static final String SOURCE = "Square POS";

        // payment data
        private String createdAt;
        private String paymentId;
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

        private TransactionsPayloadEntry(Payment payment, Tender tender, String tenderFee, String tenderEntryMethod) {
            // initialize payment data
            paymentId = payment.getId();
            createdAt = payment.getCreatedAt();
            grossSales = payment.getGrossSalesMoney().getAmount();
            discounts = payment.getDiscountMoney().getAmount();
            netSales = payment.getNetSalesMoney().getAmount();
            tax = payment.getTaxMoney().getAmount();
            tip = payment.getTipMoney().getAmount();
            totalCollected = payment.getTotalCollectedMoney().getAmount();
            fees = payment.getProcessingFeeMoney().getAmount();
            netTotal = payment.getNetTotalMoney().getAmount();

            // initialize tender details
            cardAmt = "";
            cashAmt = "";
            otherTenderAmt = "";
            otherTenderType = "";
            refundAmt = formatTotal(tender.getRefundedMoney().getAmount());
            tenderId = tender.getId();
            this.tenderFee = tenderFee;
            this.tenderEntryMethod = tenderEntryMethod;

            // get tender details
            switch (tender.getType()) {
                case Tender.TENDER_TYPE_CARD:
                    cardAmt = formatTotal(tender.getTotalMoney().getAmount());
                    break;
                case Tender.TENDER_TYPE_CASH:
                    cashAmt = formatTotal(tender.getTotalMoney().getAmount());
                    break;
                default:
                    otherTenderAmt = formatTotal(tender.getTotalMoney().getAmount());
                    otherTenderType = tender.getType();
                    break;
            }
        }
    }
}
