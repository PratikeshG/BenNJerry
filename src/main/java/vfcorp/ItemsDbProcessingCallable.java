package vfcorp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
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

public class ItemsDbProcessingCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(ItemsDbProcessingCallable.class);

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
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        Location location = (Location) message.getPayload();
        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        logger.info("PROCESSING ITEMS LOCATION: " + location.getName());

        ArrayList<Map<String, String>> records = databaseApi.queryDbItems(location.getId(), true, brand);

        ArrayList<ItemDbRecord> dbRecords = new ArrayList<ItemDbRecord>();
        for (Map<String, String> record : records) {
            ItemDbRecord ir = new ItemDbRecord();
            ir.setItemNumber(record.get("itemNumber"));
            ir.setDescription(
                    record.get("description") != null ? record.get("description").replaceFirst("\\s+$", "") : "");
            ir.setAlternateDescription(
                    record.get("alternateDescription") != null ? record.get("alternateDescription").trim() : "");
            ir.setRetailPrice(record.get("retailPrice"));
            ir.setDeptNumber(record.get("deptNumber"));
            ir.setClassNumber(record.get("classNumber"));
            dbRecords.add(ir);
        }

        // Stream to /tmp JSON file
        String fileName = location.getId() + "-items.json";
        String fullFinalPath = "/tmp/" + fileName;
        logger.info("SAVING TMP: " + fullFinalPath);

        FileOutputStream fos = new FileOutputStream(fullFinalPath);
        OutputStreamWriter ow = new OutputStreamWriter(fos);
        ow.write("[");

        Gson gson = new Gson();
        for (int i = 0; i < dbRecords.size(); i++) {
            if (i != 0) {
                ow.write(",");
            }
            ow.write(gson.toJson(dbRecords.get(i)));
        }
        ow.write("]");
        ow.flush();
        ow.close();
        fos.close();

        // Upload to GCP
        logger.info("UPLOAD TO GCP: " + fullFinalPath);

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);

        FileInputStream fis = new FileInputStream(fullFinalPath);
        String fileKey = String.format("%s/%s/%s", "test", brand, fileName);
        try {
            cloudStorage.uploadObject(archiveBucket, fileKey, fis);
        } catch (RuntimeException e) {
            logger.error("ERROR: trying to upload to CloudStorage: " + e.getMessage());
        }

        return 1;
    }
}
