package tntfireworks.reporting;

import java.util.Map;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Order;

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
            "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", "Source Type", "Load Number", "File Date",
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

    public CreditDebitPayload(String timeZone, int offset, int loadNumber, TntLocationDetails locationDetails) {
        super(timeZone, offset, locationDetails, CREDIT_DEBIT_FILE_HEADER);

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

    public void addEntry(Order order, Map<String, Payment> tenderToPayment) {
        // compute debit/credit data based on interval specified in
        // RetrieveMerchantPayloadCallable
    	if(order != null && order.getTenders() != null) {
    		for (Tender tender : order.getTenders()) {
                if (tender.getType().equals(Tender.TENDER_TYPE_CARD)) {
                	Payment payment = tenderToPayment.get(tender.getId());
                    // debits = total collected money without SQ fees but including
                    // tax
                    if (isPaymentTotalMoneyValid(payment)) {
                        debitCount++;
                        debitAmt += payment.getTotalMoney().getAmount();
                    }

                    // credits = refunds without discounts (credits are negative
                    // values)
                    if (isPaymentRefundedMoneyValid(payment)) {
                        creditCount++;
                        creditAmt -= payment.getRefundedMoney().getAmount();
                    }
                }
            }

            ticketCount = debitCount + creditCount;
            netDepositAmt = debitAmt + creditAmt;
    	}
    }

    public boolean isPaymentTotalMoneyValid(Payment payment) {
    	return payment.getTotalMoney() != null && payment.getTotalMoney().getAmount() > 0;
    }

    public boolean isPaymentRefundedMoneyValid(Payment payment) {
        return payment.getRefundedMoney() != null && payment.getRefundedMoney().getAmount() > 0;
    }

    public int getNetDepositAmt() {
    	return netDepositAmt;
    }

    public String getRow() {
        String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", SOURCE_TYPE,
                Integer.toString(loadNumber), this.getPayloadDate(), formatDecimalTotal(netDepositAmt),
                Integer.toString(ticketCount), Integer.toString(debitCount), formatDecimalTotal(debitAmt),
                Integer.toString(creditCount), formatDecimalTotal(creditAmt), formatDecimalTotal(holdAmt),
                locationDetails.locationName, formatDecimalTotal(achAmt), achDate, locationDetails.locationName);
        return row;
    }
}
