package tntfireworks.reporting;

import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

/*
 * Report 8 - "Credit Debit Report" - Placed on SFTP daily
 *
 * Report 8 contains information related to the total number of daily credits and debits for each TNT Location. This
 * report is placed on the SFTP instead of emailed, as TNT's E1 (EnterpriseOne) system is set to automatically ingest
 * report from the SFTP.
 *
 * A credit is defined as a credit card refund and debit is defined as a credit card sale. The number of debits, credits,
 * and total ticket count (# debits + # credits) is tracked within this report. The net deposit amount (debit amount +
 * credit amount) is also calculated.
 *
 * A "load number" is also set each season with a starting value of 1 and stored in a database. Each time the report is
 * generated, the current load number is included in the report and subsequently incremented. This is a requirement set
 * forth by TNT and is used for their internal systems.
 *
 */
public class CreditDebitPayload extends TntReportLocationPayload {

    // constant payload fields
    private static final String CREDIT_DEBIT_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", "Source Type", "Load Number", "File Date",
            "Net Deposit Amt", "Total Tickets", "Number of Debits", "Debit Amt", "Number of Credits", "Credit Amt",
            "Hold Amt", "Terminal ID", "ACH Amt", "ACH Date", "Merchant ID");
    private static final String SOURCE_TYPE = "SQUARE";

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

    public CreditDebitPayload(String timeZone, int loadNumber, TntLocationDetails locationDetails) {
        super(timeZone, locationDetails, CREDIT_DEBIT_FILE_HEADER);

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
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)) {
                // debits = total collected money without SQ fees but including
                // tax
                if (isTenderTotalMoneyValid(tender)) {
                    debitCount++;
                    debitAmt += tender.getTotalMoney().getAmount();
                }

                // credits = refunds without discounts (credits are negative
                // values)
                if (isTenderRefundedMoneyValid(tender)) {
                    creditCount++;
                    creditAmt += tender.getRefundedMoney().getAmount();
                }
            }
        }

        ticketCount = debitCount + creditCount;
        netDepositAmt = debitAmt + creditAmt;
    }

    public boolean isTenderTotalMoneyValid(Tender tender) {
        return tender.getTotalMoney() != null && tender.getTotalMoney().getAmount() > 0;
    }

    public boolean isTenderRefundedMoneyValid(Tender tender) {
        return tender.getRefundedMoney() != null && tender.getRefundedMoney().getAmount() < 0;
    }

    public String getRow() {
        String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", SOURCE_TYPE,
                Integer.toString(loadNumber), this.getPayloadDate(), formatCurrencyTotal(netDepositAmt),
                Integer.toString(ticketCount), Integer.toString(debitCount), formatCurrencyTotal(debitAmt),
                Integer.toString(creditCount), formatCurrencyTotal(creditAmt), formatCurrencyTotal(holdAmt),
                locationDetails.locationName, formatCurrencyTotal(achAmt), achDate, locationDetails.locationName);
        return row;
    }
}
