package tntfireworks.reporting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squareup.connect.Payment;
import com.squareup.connect.Tender;
import com.squareup.connect.v2.Transaction;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.TimeManager;

public class TransactionsBatchFile {
    private String fileDate;
    private List<TransactionEntry> transactionEntries;
    private Map<String, String> tenderToFee;
    private Map<String, String> tenderToEntryMethod;

    public TransactionsBatchFile(List<List<TntLocationDetails>> deploymentAggregate, DbConnection dbConnection)
            throws Exception {
        // initialize non-static values
        fileDate = getDate("America/Los_Angeles", "MM-dd-yy", 0);
        transactionEntries = new ArrayList<TransactionEntry>();
        tenderToFee = new HashMap<String, String>();
        tenderToEntryMethod = new HashMap<String, String>();

        // cache location data from tnt database to limit to 1 query submission
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<Map<String, String>> dbLocationRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
        tntDatabaseApi.close();

        // ingest location details into rows of payment data
        for (List<TntLocationDetails> deployment : deploymentAggregate) {
            for (TntLocationDetails locationDetails : deployment) {
                // map tender ids to v2 tender fees
                for (Transaction transaction : locationDetails.getTransactions()) {
                    for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                        tenderToFee.put(tender.getId(), formatTotal(tender.getProcessingFeeMoney().getAmount()));
                        if (tender.getCardDetails() != null && tender.getCardDetails().getEntryMethod() != null) {
                            tenderToEntryMethod.put(tender.getId(), tender.getCardDetails().getEntryMethod());
                        }
                    }
                }

                for (Payment payment : locationDetails.getPayments()) {
                    transactionEntries.add(new TransactionEntry(payment, locationDetails, dbLocationRows));
                }
            }
        }
    }

    public String generateBatchReport() throws ParseException {
        StringBuilder reportBuilder = new StringBuilder();

        // write file header
        String fileHeader = String.format(
                "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                "Payment Id", "Created At (UTC)", "Gross Sales", "Discounts", "Net Sales", "Tax", "Tip", "Tender Id",
                "Refund Amount", "Total Collected", "Source", "Card Amount", "Entry Method", "Cash Amount",
                "Other Tender Amount", "Other Tender Type", "Fees", "Net Total", "TNT Location #", "City", "State",
                "RBU", "SA NAME");
        reportBuilder.append(fileHeader);

        for (TransactionEntry entry : transactionEntries) {
            for (Tender tender : entry.tenders) {
                // initialize tender details
                String source = "SQUARE POS";
                String cardAmt = "";
                String entryMethod = "NA";
                String cashAmt = "";
                String otherTenderAmt = "";
                String otherTenderType = "";
                String refundAmt = "";
                String tenderFee = "";

                // get fee from tenderToFee mapping
                if (tenderToFee.containsKey(tender.getId())) {
                    tenderFee = tenderToFee.get(tender.getId());
                }

                // retrieve entry method, if null (for cash), set to NA
                if (tenderToEntryMethod.containsKey(tender.getId())) {
                    entryMethod = tenderToEntryMethod.get(tender.getId());
                }

                refundAmt = formatTotal(tender.getRefundedMoney().getAmount());

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

                // write file row
                String fileRow = String.format(
                        "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                        entry.paymentId, entry.createdAt, formatTotal(entry.grossSales), formatTotal(entry.discounts),
                        formatTotal(entry.netSales), formatTotal(entry.tax), formatTotal(entry.tip), tender.getId(),
                        refundAmt, formatTotal(entry.totalCollected), source, cardAmt, entryMethod, cashAmt,
                        otherTenderAmt, otherTenderType, tenderFee, formatTotal(entry.netTotal), entry.locationNumber,
                        entry.city, entry.state, entry.rbu, entry.saName);
                reportBuilder.append(fileRow);
            }
        }

        return reportBuilder.toString();
    }

    private String getDate(String timezone, String dateFormat, int offset) throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));

        // 0 = current date
        cal.add(Calendar.DAY_OF_YEAR, offset);

        return TimeManager.toSimpleDateTimeInTimeZone(cal, timezone, dateFormat);
    }

    public String getFileDate() {
        return fileDate;
    }

    private String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0).replaceAll(",", "");
    }

    private class TransactionEntry {
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
        private List<Tender> tenders;
        private String locationNumber;
        private String rbu;
        private String city;
        private String state;
        private String saName;

        public TransactionEntry(Payment payment, TntLocationDetails locationDetails,
                List<Map<String, String>> dbLocationRows) {
            // initialize payment information
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

            // initialize list to be used during report generation
            tenders = Arrays.asList(payment.getTender());

            // initialize tnt-specific location information
            rbu = "";
            city = "";
            state = "";
            saName = "";

            // get data from location rows in db
            this.locationNumber = findLocationNumber(locationDetails.getLocation().getName());
            for (Map<String, String> row : dbLocationRows) {
                if (this.locationNumber.equals(row.get("locationNumber"))) {
                    this.city = row.get("city");
                    this.state = row.get("state");
                    this.rbu = row.get("rbu");
                    this.saName = row.get("saName");
                }
            }
        }

        /* 
         * Helper function to parse location number
         * 
         * - per TNT spec, all upcoming seasons will follow new naming convention
         *   location name = TNT location number
         * - old seasons followed convention of 'NAME (#LocationNumber)'
         * 
         */
        private String findLocationNumber(String locationName) {
            String locationNumber = "";

            // old location name =  'NAME (#Location Number)'
            String oldPattern = "\\w+\\s*\\(#([a-zA-Z0-9\\s]+)\\)";
            Pattern p = Pattern.compile(oldPattern);
            Matcher m = p.matcher(locationName);

            if (m.find()) {
                locationNumber = m.group(1);
            } else {
                if (!locationName.equals("")) {
                    locationNumber = locationName;
                }
            }

            return locationNumber;
        }
    }
}
