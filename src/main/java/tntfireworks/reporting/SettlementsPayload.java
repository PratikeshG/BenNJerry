package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Settlement;
import com.squareup.connect.SettlementEntry;

public class SettlementsPayload extends TntReportPayload {
    // write file header
    private static final String SETTLEMENTS_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
            "Location Number", "Settlement Id", "Initiated At (UTC)", "Settlement Amount",
            "Settlement Fee", "Type", "RBU", "City", "State", "Zip");

    private String locationNumber;
    private String city;
    private String state;
    private String zip;
    private String rbu;
    private List<SettlementsPayloadEntry> payloadEntries;

    // each SettlementsPayload object represents all settlements within a single location
    public SettlementsPayload(String timeZone, String locationNumber, List<Map<String, String>> dbLocationRows) {
    	super(timeZone, SETTLEMENTS_FILE_HEADER);
    	// initialize payload values
    	payloadEntries = new ArrayList<SettlementsPayloadEntry>();
    	this.locationNumber = locationNumber;
    	rbu = "";
    	city = "";
    	state = "";
    	zip = "";

        for (Map<String, String> row : dbLocationRows) {
            if (locationNumber.equals(row.get("locationNumber"))) {
                this.city = row.get("city");
                this.state = row.get("state");
                this.rbu = row.get("rbu");
                this.zip = row.get("zip");
            }
        }
    }

    public void addSettlement(Settlement settlement) {
        payloadEntries.add(new SettlementsPayloadEntry(settlement));
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (SettlementsPayloadEntry payloadEntry : payloadEntries) {
            for (SettlementEntry settlementEntry : payloadEntry.settlementEntries) {
                // write row
                String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                        locationNumber, payloadEntry.settlementId, payloadEntry.initiatedAt,
                        formatTotal(settlementEntry.getAmountMoney().getAmount()),
                        formatTotal(settlementEntry.getFeeMoney().getAmount()),
                        settlementEntry.getType(), rbu, city, state, zip);
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
