package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

public class TransactionsPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesPayload.class);
    private List<TransactionsPayloadEntry> transactionsPayloadEntries;

    private static final String TRANSACTIONS_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Payment Id", "Created At (UTC)", "Gross Sales", "Discounts", "Net Sales", "Tax", "Tip", "Tender Id",
            "Refund Amount", "Total Collected", "Source", "Card Amount", "Entry Method", "Cash Amount",
            "Other Tender Amount", "Other Tender Type", "Fees", "Net Total", "TNT Location #", "City", "State",
            "RBU", "SA NAME");

    public TransactionsPayload(String timeZone, String locationName, List<Map<String, String>> dbLocationRows) {
        super (timeZone, locationName, dbLocationRows, TRANSACTIONS_FILE_HEADER);
        transactionsPayloadEntries = new ArrayList<TransactionsPayloadEntry>();
    }

    public void addEntry(Payment payment, Map<String, Integer> tenderToFee, Map<String, String> tenderToEntryMethod) {
        // - tender fee and tender entry method currently only available in Connect V2
        // - use V2 map to obtain values
        String tenderFee = "";
        String tenderEntryMethod = "NA";

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
                    entry.refundAmt, formatTotal(entry.totalCollected), entry.SOURCE, entry.cardAmt, entry.tenderEntryMethod,
                    entry.cashAmt, entry.otherTenderAmt, entry.otherTenderType, entry.tenderFee, formatTotal(entry.netTotal),
                    locationNumber, city, state, rbu, saName);
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
                case "CREDIT_CARD":
                    cardAmt = formatTotal(tender.getTotalMoney().getAmount());
                    break;
                case "CASH":
                    cashAmt = formatTotal(tender.getTotalMoney().getAmount());
                    break;
                case "OTHER":
                    otherTenderAmt = formatTotal(tender.getTotalMoney().getAmount());
                    otherTenderType = "OTHER";
                    break;
                default:
                    otherTenderAmt = formatTotal(tender.getTotalMoney().getAmount());
                    otherTenderType = tender.getType();
                    break;
            }
        }
    }
}
