package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squareup.connect.Settlement;
import com.squareup.connect.SettlementEntry;

/*
 * "Settlements Report" - Emailed first of each month
 *
 * Report 1 is emailed on the first of each month and contains settlements entries for the previous month of each TNT
 * location. Each row in this file contains a settlements entry for a single location and multiple entries can exist
 * for a single location as multiple deposits/settlements can occur daily. The Connect V1 Settlement endpoint is the
 * only source of information for this report.
 *
 */
public class SettlementsPayload extends TntReportLocationPayload {
    // write file header
    private static final String SETTLEMENTS_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Location Number", "Settlement Id", "Initiated At (UTC)", "Settlement Amount", "Settlement Fee", "Type",
            "RBU", "City", "State", "Zip");
    private List<SettlementsPayloadEntry> payloadEntries;

    // each SettlementsPayload object represents all settlements within a single
    // location
    public SettlementsPayload(String timeZone, int offset, TntLocationDetails locationDetails) {
        super(timeZone, offset, locationDetails, SETTLEMENTS_FILE_HEADER);
        // initialize payload values
        payloadEntries = new ArrayList<SettlementsPayloadEntry>();
    }

    public void addEntry(Settlement settlement) {
        payloadEntries.add(new SettlementsPayloadEntry(settlement));
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (SettlementsPayloadEntry payloadEntry : payloadEntries) {
            for (SettlementEntry settlementEntry : payloadEntry.settlementEntries) {
                // write row
                String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", locationDetails.locationNumber,
                        payloadEntry.settlementId, payloadEntry.initiatedAt,
                        formatCurrencyTotal(settlementEntry.getAmountMoney().getAmount()),
                        formatCurrencyTotal(settlementEntry.getFeeMoney().getAmount()), settlementEntry.getType(),
                        locationDetails.rbu, locationDetails.city, locationDetails.state, locationDetails.zip);
                rows.add(row);
            }
        }

        return rows;
    }

    // each SettlementsPayloadEntry represents a single settlement
    // with multiple settlement entries
    private class SettlementsPayloadEntry {
        private String settlementId;
        private String initiatedAt;
        private List<SettlementEntry> settlementEntries;

        private SettlementsPayloadEntry(Settlement settlement) {
            // initialize settlement information
            settlementId = settlement.getId();
            initiatedAt = settlement.getInitiatedAt();

            // initialize settlement entries
            settlementEntries = Arrays.asList(settlement.getEntries());
        }
    }
}
