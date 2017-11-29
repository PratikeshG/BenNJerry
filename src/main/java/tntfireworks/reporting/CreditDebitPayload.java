package tntfireworks.reporting;

import java.util.List;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

public class CreditDebitPayload extends TntReportLocationPayload {

    // constant payload fields
    private static final String CREDIT_DEBIT_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", "Source Type", "Load Number", "File Date",
            "Net Deposit Amt", "Total Tickets", "Number of Debits", "Debit Amt", "Number of Credits", "Credit Amt",
            "Hold Amt", "Terminal ID", "ACH Amt", "ACH Date", "Merchant ID");
    private static final String SOURCE_TYPE = "SQUARE";
    protected static final String DB_REPORT_NAME = "CreditDebitBatch";

    // variable payload fields
    protected int loadNumber;
    private int netDepositAmt;
    private int ticketCount;
    private int debitCount;
    private int debitAmt;
    private int creditCount;
    private int creditAmt;
    private int holdAmt;
    private int achAmt;
    private String achDate;

    public CreditDebitPayload(String timeZone, int loadNumber, String locationName,
            List<Map<String, String>> dbLocationRows) {
        super(timeZone, locationName, dbLocationRows, CREDIT_DEBIT_FILE_HEADER);

        // initialize values that are currently static
        // (may change in the future depending on TNT requirements)
        holdAmt = 0;
        achAmt = 0;
        achDate = "NA";

        // initialize non-static values
        netDepositAmt = 0;
        ticketCount = 0;
        debitCount = 0;
        debitAmt = 0;
        creditCount = 0;
        creditAmt = 0;
        this.loadNumber = loadNumber;
    }

    public void addEntry(Payment payment) {
        // compute debit/credit data based on interval specified in
        // RetrieveMerchantPayloadCallable
        for (Tender tender : payment.getTender()) {
            if (tender.getType().equals("CREDIT_CARD")) {
                // debits = total collected money without SQ fees but including
                // tax
                if (tender.getTotalMoney() != null && tender.getTotalMoney().getAmount() > 0) {
                    debitCount++;
                    debitAmt += tender.getTotalMoney().getAmount();
                }

                // credits = refunds without discounts (credits are negative
                // values)
                if (tender.getRefundedMoney() != null && tender.getRefundedMoney().getAmount() < 0) {
                    creditCount++;
                    creditAmt += tender.getRefundedMoney().getAmount();
                }
            }
        }

        ticketCount = debitCount + creditCount;
        netDepositAmt = debitAmt + creditAmt;
    }

    public String getRow() {
        String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", SOURCE_TYPE,
                Integer.toString(loadNumber), this.getPayloadDate(), formatTotal(netDepositAmt),
                Integer.toString(ticketCount), Integer.toString(debitCount), formatTotal(debitAmt),
                Integer.toString(creditCount), formatTotal(creditAmt), formatTotal(holdAmt), locationName,
                formatTotal(achAmt), achDate, locationName);
        return row;
    }
}
