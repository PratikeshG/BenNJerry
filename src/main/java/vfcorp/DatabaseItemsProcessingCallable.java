package vfcorp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;
import com.squareup.connect.v2.Location;

import util.CloudStorageApi;
import vfcorp.plu.ItemDbRecord;
import vfcorp.plu.ItemSaleDbRecord;

public class DatabaseItemsProcessingCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseItemsProcessingCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Value("${google.storage.bucket.archive}")
    private String storageBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    private Gson gson = new Gson();

    private static final int RETRY_COUNT = 6;
    private static final int RETRY_DELAY_MS = 15000; // 15 seconds

    /* We want to calculate all sales records for the same sale date -- most often "today".
     * We should always calculate for first time zone that has store locations open (EST).
     *
     * This allows us to run sales calculations stating at 12:00am EST, even though its still 9pm PST and 7pm HST
     * Latest stores in HI close at 8pm.
     */
    private static String SALES_TIMEZONE = "America/New_York";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        Location location = (Location) message.getPayload();
        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);
        boolean isPluFiltered = message.getProperty("pluFiltered", PropertyScope.SESSION);

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);

        Exception lastException = null;

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                // Get ITEMS
                ArrayList<ItemDbRecord> dbItemRecords = getItemRecords(databaseApi, location, brand, isPluFiltered);
                if (dbItemRecords.size() > 0) {
                    // Stream ITEMS to /tmp JSON file
                    String itemFileName = location.getId() + "-items.json";
                    Util.saveTmpFile(itemFileName, dbItemRecords);

                    // Upload ITEMS to GCP
                    Util.uploadBrandPluFileFromTmpToGCP(cloudStorage, storageBucket, brand, itemFileName);
                }

                // Get SALE EVENTS
                ArrayList<ItemSaleDbRecord> dbSaleRecords = getSaleRecords(databaseApi, location);
                if (dbSaleRecords.size() > 0) {
                    // Stream SALE EVENTS to /tmp JSON file
                    String saleFileName = location.getId() + "-sales.json";
                    Util.saveTmpFile(saleFileName, dbSaleRecords);

                    // Upload SALE EVENTS to GCP
                    Util.uploadBrandPluFileFromTmpToGCP(cloudStorage, storageBucket, brand, saleFileName);
                }
                break;
            } catch (Exception e) {
                lastException = e;
                logger.info(String.format("ERROR trying to save items DB for location %s for brand %s: %s",
                        location.getId(), brand, e.toString()));
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        return 1;
    }

    private ArrayList<ItemDbRecord> getItemRecords(VfcDatabaseApi databaseApi, Location location, String brand,
            boolean isPluFiltered) throws SQLException, IOException {

        ArrayList<Map<String, String>> itemRecords = databaseApi.queryDbItems(location.getId(), isPluFiltered, brand);

        ArrayList<ItemDbRecord> dbItemRecords = new ArrayList<ItemDbRecord>();
        for (Map<String, String> itemRecord : itemRecords) {
            ItemDbRecord ir = new ItemDbRecord();
            ir.setItemNumber(itemRecord.get("itemNumber"));
            ir.setDescription(itemRecord.get("description") != null
                    ? itemRecord.get("description").replaceFirst("\\s+$", "") : "");
            ir.setAlternateDescription(itemRecord.get("alternateDescription") != null
                    ? itemRecord.get("alternateDescription").trim() : "");
            ir.setRetailPrice(itemRecord.get("retailPrice"));
            ir.setDeptNumber(itemRecord.get("deptNumber"));
            ir.setClassNumber(itemRecord.get("classNumber"));
            dbItemRecords.add(ir);
        }

        return dbItemRecords;
    }

    private ArrayList<ItemSaleDbRecord> getSaleRecords(VfcDatabaseApi databaseApi, Location location)
            throws SQLException, IOException, ParseException {
        ArrayList<Map<String, String>> saleRecords = databaseApi.queryDbItemSaleEvents(location.getId(),
                SALES_TIMEZONE);

        ArrayList<ItemSaleDbRecord> dbSaleRecords = new ArrayList<ItemSaleDbRecord>();
        for (Map<String, String> saleRecord : saleRecords) {
            ItemSaleDbRecord is = new ItemSaleDbRecord();
            is.setItemNumber(saleRecord.get("itemNumber"));
            is.setSalePrice(saleRecord.get("salePrice"));
            dbSaleRecords.add(is);
        }

        return dbSaleRecords;
    }
}
