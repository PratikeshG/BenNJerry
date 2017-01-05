package tntfireworks.reporting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Settlement;
import com.squareup.connect.SettlementEntry;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.TimeManager;

public class SettlementsBatchFile {
    private static Logger logger = LoggerFactory.getLogger(SettlementsBatchFile.class);
    private String fileDate;
    private List<SettlementsFileEntry> settlementFileEntries;

    public SettlementsBatchFile(List<List<TntLocationDetails>> deploymentAggregate, DbConnection dbConnection)
            throws Exception {
        // initialize non-static values
        fileDate = getDate("America/Los_Angeles", "MM-dd-yy", 0);
        settlementFileEntries = new ArrayList<SettlementsFileEntry>();

        // cache location data from tnt database to limit to 1 query submission
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<Map<String, String>> dbLocationRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
        tntDatabaseApi.close();

        // ingest location details into rows of settlement data
        for (List<TntLocationDetails> deployment : deploymentAggregate) {
            for (TntLocationDetails locationDetails : deployment) {
                for (Settlement settlement : locationDetails.getSettlements()) {
                    settlementFileEntries.add(new SettlementsFileEntry(settlement, locationDetails, dbLocationRows));
                }
            }
        }
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

    public String generateBatchReport() {
        StringBuilder reportBuilder = new StringBuilder();

        // write file header
        String fileHeader = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                "Location Number", "Settlement Id", "Initiated At (UTC)", "Settlement Amount",
                "Settlement Fee", "Type", "RBU", "City", "State", "Zip");
        reportBuilder.append(fileHeader);
        logger.info(fileHeader);

        for (SettlementsFileEntry fileEntry : settlementFileEntries) {
            for (SettlementEntry settlementEntry : fileEntry.settlementEntries) {
                // write file row
                String fileRow = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                        fileEntry.locationNumber, fileEntry.settlementId, fileEntry.initiatedAt,
                        formatTotal(settlementEntry.getAmountMoney().getAmount()),
                        formatTotal(settlementEntry.getFeeMoney().getAmount()),
                        settlementEntry.getType(), fileEntry.rbu, fileEntry.city, fileEntry.state, fileEntry.zip);
                reportBuilder.append(fileRow);
            }
        }

        return reportBuilder.toString();
    }

    private class SettlementsFileEntry {
        private String settlementId;
        private String initiatedAt;
        private List<SettlementEntry> settlementEntries;
        private String locationNumber;
        private String city;
        private String state;
        private String zip;
        private String rbu;

        private SettlementsFileEntry(Settlement settlement, TntLocationDetails locationDetails,
                List<Map<String, String>> dbLocationRows) {
            // initialize settlement information
            settlementId = settlement.getId();
            initiatedAt = settlement.getInitiatedAt();

            // initialize settlement entries
            settlementEntries = Arrays.asList(settlement.getEntries());

            // initialize tnt-specific location information
            rbu = "";
            city = "";
            state = "";
            zip = "";

            // get data from location rows in db
            this.locationNumber = findLocationNumber(locationDetails.getLocation().getName());
            for (Map<String, String> row : dbLocationRows) {
                if (this.locationNumber.equals(row.get("locationNumber"))) {
                    this.city = row.get("city");
                    this.state = row.get("state");
                    this.rbu = row.get("rbu");
                    this.zip = row.get("zip");
                }
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
