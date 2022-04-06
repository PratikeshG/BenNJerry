package vfcorp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.services.storage.model.StorageObject;

import util.CloudStorageApi;

public class WhitelistSyncToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(WhitelistSyncToDatabaseCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Value("${vfcorp.sftp.host}")
    private String sftpHost;
    @Value("${vfcorp.sftp.port}")
    private int sftpPort;
    @Value("${vfcorp.sftp.username}")
    private String sftpUser;
    @Value("${vfcorp.sftp.password}")
    private String sftpPassword;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Value("${vfcorp.tnfca.storeIds}")
    private String tnfcaIds;
    private Set<String> tnfcaStoreIds;

    private static final String GCP_PROCESSING_PREFIX = "processing_";
    private static final String GCP_WHITELIST_DIRECTORY_FORMAT = "whitelist/%s/";
    private static final String VFC_DEPLOYMENT_FORMAT = "vfcorp-%s";
    private static final String VFC_BRAND_TNF = "tnf";
    private static final String VFC_BRAND_TNF_CA = "tnfca";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        tnfcaStoreIds = new HashSet<String>(Arrays.asList(tnfcaIds.split(",")));

        String brand = (String) message.getProperty("brand", PropertyScope.INVOCATION);
        String whitelistFilePrefix = (String) message.getProperty("whitelistFilePrefix", PropertyScope.INVOCATION);

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);

        if (isProcessingFile(cloudStorage, brand)) {
            return 0;
        }

        List<StorageObject> objects = cloudStorage.listObjects(archiveBucket,
                String.format(GCP_WHITELIST_DIRECTORY_FORMAT + whitelistFilePrefix, brand));
        ArrayList<String> filesToProcess = new ArrayList<String>();
        for (StorageObject o : objects) {
            filesToProcess.add(o.getName());
        }
        Collections.sort(filesToProcess);

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        String folderKey = String.format(GCP_WHITELIST_DIRECTORY_FORMAT, brand);

        for (String fileKey : filesToProcess) {
            String fileName = fileKey.split(folderKey)[1];
            String processingFileKey = folderKey + GCP_PROCESSING_PREFIX + fileName;
            cloudStorage.renameObject(archiveBucket, fileKey, archiveBucket, processingFileKey);

            InputStream whitelistInputStream = cloudStorage.downloadObject(archiveBucket, processingFileKey);
            BufferedInputStream bis = new BufferedInputStream(whitelistInputStream);

            HashMap<String, HashSet<String>> allStoreWhitelists = new HashMap<String, HashSet<String>>();

            Scanner scanner = new Scanner(bis, "UTF-8");
            while (scanner.hasNextLine()) {
                String whitelistedRow = scanner.nextLine();

                // first five characters are location Id (ex: 00990)
                if (whitelistedRow.length() == 19) {
                    String storeId = whitelistedRow.substring(0, 5);

                    HashSet<String> specificStoreWhitelist = allStoreWhitelists.get(storeId);
                    if (specificStoreWhitelist == null) {
                        specificStoreWhitelist = new HashSet<String>();
                        allStoreWhitelists.put(storeId, specificStoreWhitelist);
                    }

                    String whitelistedUpc = whitelistedRow.substring(5);
                    specificStoreWhitelist.add(whitelistedUpc);
                }
            }
            if (scanner != null) {
                scanner.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (whitelistInputStream != null) {
                whitelistInputStream.close();
            }

            for (String storeId : allStoreWhitelists.keySet()) {
                String deploymentForStoreId = getDeploymentForStoreId(brand, storeId);

                databaseApi.deleteWhitelistForBrandStore(deploymentForStoreId, storeId);

                ArrayList<String> wl = new ArrayList<String>(allStoreWhitelists.get(storeId));

                int batchSize = 1000;
                for (int b = 0; b < wl.size(); b += batchSize) {
                    databaseApi.setWhitelistForBrandStore(deploymentForStoreId, storeId,
                            wl.subList(b, Math.min(wl.size(), b + batchSize)));
                }
            }

            cloudStorage.renameObject(archiveBucket, processingFileKey, archiveBucket,
                    folderKey + "archive/" + fileName);
        }

        logger.info(String.format("Completed processing whitelist GCP to DB sync for brand: %s", brand));

        return 1;
    }

    private String getDeploymentForStoreId(String brand, String storeId) {
        if (isTnfBrand(brand)) {
            if (tnfcaStoreIds.contains(storeId)) {
                return String.format(VFC_DEPLOYMENT_FORMAT, VFC_BRAND_TNF_CA);
            }
        }
        return String.format(VFC_DEPLOYMENT_FORMAT, brand);
    }

    private boolean isTnfBrand(String brand) {
        return brand.equals(VFC_BRAND_TNF);
    }

    private boolean isProcessingFile(CloudStorageApi cloudStorage, String brand) throws IOException {
        List<StorageObject> processingFiles = cloudStorage.listObjects(archiveBucket,
                String.format(GCP_WHITELIST_DIRECTORY_FORMAT + "processing_", brand));
        if (processingFiles.size() > 0) {
            logger.info(String.format("Can't begin processing %s whitelists. Already processing another file: %s",
                    brand, processingFiles.get(0).getName()));
            return true;
        }
        return false;
    }
}
