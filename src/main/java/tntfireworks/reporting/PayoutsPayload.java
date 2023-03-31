package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squareup.connect.v2.Payout;
import com.squareup.connect.v2.PayoutEntry;

/*
 * "Settlements Report" - Emailed first of each month
 *
 * Report 1 is emailed on the first of each month and contains settlements entries for the previous month of each TNT
 * location. Each row in this file contains a settlements entry for a single location and multiple entries can exist
 * for a single location as multiple deposits/settlements can occur daily. The Connect V1 Settlement endpoint is the
 * only source of information for this report.
 *
 */
public class PayoutsPayload extends TntReportLocationPayload {
    // write file header
    private static final String SETTLEMENTS_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Location Number", "Payout Id", "Initiated At (UTC)", "Payout Amount", "Payout Fee", "Type",
            "RBU", "City", "State", "Zip");
    private List<PayoutsPayloadEntry> payloadEntries;

    // each SettlementsPayload object represents all settlements within a single
    // location
    public PayoutsPayload(String timeZone, int offset, TntLocationDetails locationDetails) {
        super(timeZone, offset, locationDetails, SETTLEMENTS_FILE_HEADER);
        // initialize payload values
        payloadEntries = new ArrayList<PayoutsPayloadEntry>();
    }

    public void addEntry(Payout payout, PayoutEntry[] payoutEntries) {
        payloadEntries.add(new PayoutsPayloadEntry(payout, payoutEntries));
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (PayoutsPayloadEntry payloadEntry : payloadEntries) {
            for (PayoutEntry payoutEntry : payloadEntry.payoutEntries) {
                // write row
                String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", locationDetails.locationNumber,
                        payloadEntry.payoutId, payloadEntry.initiatedAt,
                        formatCurrencyTotal(payoutEntry.getNetAmountMoney().getAmount()),
                        formatCurrencyTotal(-payoutEntry.getFeeAmountMoney().getAmount()), payoutEntry.getType(),
                        locationDetails.rbu, locationDetails.city, locationDetails.state, locationDetails.zip);
                rows.add(row);
            }
        }

        return rows;
    }

    // each SettlementsPayloadEntry represents a single settlement
    // with multiple settlement entries
    private class PayoutsPayloadEntry {
        private String payoutId;
        private String initiatedAt;
        private List<PayoutEntry> payoutEntries;

        private PayoutsPayloadEntry(Payout payout, PayoutEntry[] payoutEntries) {
            // initialize settlement information
            payoutId = payout.getId();
            initiatedAt = payout.getCreatedAt();

            // initialize settlement entries
            this.payoutEntries = Arrays.asList(payoutEntries);
        }
    }
}
