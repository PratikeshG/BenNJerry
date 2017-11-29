package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Settlement;
import com.squareup.connect.SettlementEntry;

public class SettlementsPayload extends TntReportLocationPayload {
    // write file header
    private static final String SETTLEMENTS_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Location Number", "Settlement Id", "Initiated At (UTC)", "Settlement Amount", "Settlement Fee", "Type",
            "RBU", "City", "State", "Zip");
    private List<SettlementsPayloadEntry> payloadEntries;

    // each SettlementsPayload object represents all settlements within a single
    // location
    public SettlementsPayload(String timeZone, String locationName, List<Map<String, String>> dbLocationRows) {
        super(timeZone, locationName, dbLocationRows, SETTLEMENTS_FILE_HEADER);
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
                String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", locationNumber,
                        payloadEntry.settlementId, payloadEntry.initiatedAt,
                        formatTotal(settlementEntry.getAmountMoney().getAmount()),
                        formatTotal(settlementEntry.getFeeMoney().getAmount()), settlementEntry.getType(), rbu, city,
                        state, zip);
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
