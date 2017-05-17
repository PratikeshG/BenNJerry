package tntfireworks.reporting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Payment;

import util.TimeManager;

public class CreditDebitBatchFile {
    private static Logger logger = LoggerFactory.getLogger(CreditDebitBatchFile.class);

    // file fields
    private String sourceType;
    private int loadNumber;
    private String fileDate;
    private List<CreditDebitEntry> batchFileEntries;

    public CreditDebitBatchFile(List<List<TntLocationDetails>> deploymentAggregate) throws Exception {
        // initialize values that are currently static
        sourceType = "SQUARE";
        loadNumber = 0;

        // initialize non-static values
        fileDate = getDate("America/Los_Angeles", "MM-dd-yy", 0);
        batchFileEntries = new ArrayList<CreditDebitEntry>();

        // ingest location details into rows of payment data
        for (List<TntLocationDetails> deployment : deploymentAggregate) {
            for (TntLocationDetails locationDetails : deployment) {
                batchFileEntries.add(new CreditDebitEntry(locationDetails));
            }
        }
    }

    private String getDate(String timezone, String dateFormat, int offset) throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));

        // 0 = current date
        cal.add(Calendar.DAY_OF_YEAR, offset);

        return TimeManager.toSimpleDateTimeInTimeZone(cal, timezone, dateFormat);
    }

    private String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0).replaceAll(",", "");
    }

    public String getFileDate() {
        return fileDate;
    }

    public String generateBatchReport() {
        StringBuilder reportBuilder = new StringBuilder();

        // write file header
        String fileHeader = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                "Source Type", "Load Number", "File Date", "Net Deposit Amt", "Total Tickets", "Number of Debits",
                "Debit Amt", "Number of Credits", "Credit Amt", "Hold Amt", "Terminal ID", "ACH Amt", "ACH Date",
                "SQ Merchant ID");

        reportBuilder.append(fileHeader);

        for (CreditDebitEntry entry : batchFileEntries) {
            String fileRow = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                    sourceType, Integer.toString(loadNumber), fileDate, formatTotal(entry.netDepositAmt),
                    Integer.toString(entry.ticketCount), Integer.toString(entry.debitCount),
                    formatTotal(entry.debitAmt),
                    Integer.toString(entry.creditCount), formatTotal(entry.creditAmt), formatTotal(entry.holdAmt),
                    entry.locationName, formatTotal(entry.achAmt), entry.achDate, entry.sqMerchantId);
            reportBuilder.append(fileRow);
        }

        return reportBuilder.toString();
    }

    private class CreditDebitEntry {
        private int netDepositAmt;
        private int ticketCount;
        private int debitCount;
        private int debitAmt;
        private int creditCount;
        private int creditAmt;
        private int holdAmt;
        private String locationName;
        private int achAmt;
        private String achDate;
        private String sqMerchantId;

        private CreditDebitEntry(TntLocationDetails locationDetails) {
            // initialize values that are currently static to row
            holdAmt = 0;
            achAmt = 0;
            sqMerchantId = locationDetails.getMerchantId();
            achDate = "NA";

            // initialize non-static values
            netDepositAmt = 0;
            ticketCount = 0;
            debitCount = 0;
            debitAmt = 0;
            creditCount = 0;
            creditAmt = 0;
            locationName = locationDetails.getLocation().getName().replaceAll(",", "");

            // compute debit/credit data
            for (Payment payment : locationDetails.getPayments()) {
                ticketCount++;

                // debits = total collected money without SQ fees but including tax
                if (payment.getTotalCollectedMoney() != null && payment.getTotalCollectedMoney().getAmount() > 0) {
                    debitCount++;
                    debitAmt += payment.getTotalCollectedMoney().getAmount();
                }

                // credits = refunds without discounts (credits are negative values)
                if (payment.getRefundedMoney() != null && payment.getRefundedMoney().getAmount() < 0) {
                    creditCount++;
                    creditAmt += payment.getRefundedMoney().getAmount();
                }
            }

            // net deposits = debits + credits
            netDepositAmt = debitAmt + creditAmt;
        }
    }
}
