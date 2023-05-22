package vfcorp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.ResultSet;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Tender;

import util.CloudStorageApi;
import util.DbConnection;
import util.SquarePayload;
import vfcorp.plu.ItemDbRecord;
import vfcorp.plu.ItemSaleDbRecord;

public class Util {
    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 5000; // 5 seconds

    private static String DEPLOYMENT_BRAND_TNF = "tnf";
    private static String DEPLOYMENT_BRAND_TNF_CA = "tnfca";
    private static String DEPLOYMENT_BRAND_VANS = "vans";
    private static String DEPLOYMENT_BRAND_VANS_TEST = "test";

    public static String getValueBetweenChars(String input, char c, char d) {
        String value = "";

        if (input != null) {
            int firstIndex = input.indexOf(c);
            int lastIndex = input.indexOf(d);
            if (firstIndex > -1 && lastIndex > -1 && lastIndex > firstIndex) {
                value = input.substring(firstIndex + 1, lastIndex);
            }
        }

        return value;
    }

    public static String getValueInPipes(String input) {
        String value = "";

        if (input.indexOf("|") >= 0) {
            value = input.substring(input.indexOf("|") + 1);
            value = value.substring(0, value.indexOf("|"));
        }

        return value;
    }

    public static String getValueInBrackets(String input) {
        return getValueBetweenChars(input, '[', ']');
    }

    public static String getValueInParenthesis(String input) {
        return getValueBetweenChars(input, '(', ')');
    }

    public static String getRegisterNumber(String deviceName) {
        String registerNumber = "99"; // default

        if (deviceName != null) {
            String n = getValueInParenthesis(deviceName).replaceAll("[^\\d]", "");
            if (n.length() > 0) {
                registerNumber = n;
            }
        }

        // Pad to three characters with left zeros
        return String.format("%03d", Integer.parseInt(registerNumber));
    }

    public static String getStoreNumber(String input) {
        String n = getValueInParenthesis(input).replaceAll("[^\\d]", "");
        String storeNumber = n.length() > 0 ? n : "0";

        // Pad to five characters with left zeros
        return String.format("%05d", Integer.parseInt(storeNumber));
    }

    public static int[] divideIntegerEvenly(int amount, int totalPieces) {
        int quotient = amount / totalPieces;
        int remainder = amount % totalPieces;

        int[] results = new int[totalPieces];
        for (int i = 0; i < totalPieces; i++) {
            results[i] = i < remainder ? quotient + 1 : quotient;
        }

        // Reverse - provide smallest discounts first
        for (int i = 0; i < results.length / 2; i++) {
            int temp = results[i]; // swap numbers
            results[i] = results[results.length - 1 - i];
            results[results.length - 1 - i] = temp;
        }

        return results;
    }

    public static Session createSSHSession(String host, String username, String password, int port) throws Exception {
        JSch jsch = new JSch();

        Session session = null;
        for (int i = 0;; i++) {
            try {
                session = jsch.getSession(username, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                break;
            } catch (Exception e) {
                String err = String.format("ERROR trying to connect to SFTP: %s", e.toString());
                if (i < RETRY_COUNT) {
                    Thread.sleep(RETRY_DELAY_MS);
                } else {
                    throw new Exception(err);
                }
            }
        }

        return session;
    }

    public static List<VfcDeployment> getVfcDeployments(String host, String user, String password, String whereFilter)
            throws Exception {
        DbConnection conn = new DbConnection(host, user, password);

        String query = "SELECT vfcorp_deployments.deployment as deployment, storeId, timeZone, "
                + "pluPath, pluFiltered, tlogPath, encryptedAccessToken, merchantId, locationId FROM vfcorp_deployments "
                + "LEFT JOIN token ON vfcorp_deployments.deployment = token.deployment WHERE " + whereFilter + ";";

        ResultSet result = (ResultSet) conn.submitQuery(query);

        ArrayList<VfcDeployment> deployments = new ArrayList<VfcDeployment>();
        while (result.next()) {
            VfcDeployment deployment = new VfcDeployment();
            deployment.setDeployment(result.getString("deployment"));
            deployment.setStoreId(result.getString("storeId"));
            deployment.setTimeZone(result.getString("timeZone"));
            deployment.setPluPath(result.getString("pluPath"));
            deployment.setPluFiltered(result.getBoolean("pluFiltered"));
            deployment.setTlogPath(result.getString("tlogPath"));

            SquarePayload sp = new SquarePayload();
            sp.setEncryptedAccessToken(result.getString("encryptedAccessToken"));
            sp.setMerchantId(result.getString("merchantId"));
            sp.setLocationId(result.getString("locationId"));
            deployment.setSquarePayload(sp);

            deployments.add(deployment);
        }

        return deployments;
    }

    public static List<SquarePayload> getMasterAccountsForBrand(String host, String user, String password, String brand)
            throws Exception {
        DbConnection conn = new DbConnection(host, user, password);

        String query = String.format(
                "SELECT MAX(encryptedAccessToken) as encryptedAccessToken, merchantId FROM token WHERE deployment LIKE 'vfcorp-%s-%%' GROUP BY merchantId",
                brand);

        ResultSet result = (ResultSet) conn.submitQuery(query);

        ArrayList<SquarePayload> accounts = new ArrayList<SquarePayload>();
        while (result.next()) {
            SquarePayload sp = new SquarePayload();
            sp.setEncryptedAccessToken(result.getString("encryptedAccessToken"));
            sp.setMerchantId(result.getString("merchantId"));

            accounts.add(sp);
        }

        if (accounts.size() < 1) {
            throw new Exception(String.format("No accounts for brand '%s' found.", brand));
        }

        return accounts;
    }

    public static VfcDeployment getVfcDeploymentById(String host, String user, String password, String deploymentId)
            throws Exception {
        DbConnection conn = new DbConnection(host, user, password);

        String query = String.format("SELECT vfcorp_deployments.deployment as deployment, storeId, timeZone, "
                + "pluPath, pluFiltered, tlogPath, encryptedAccessToken as encryptedAccessToken, merchantId, locationId "
                + "FROM vfcorp_deployments LEFT JOIN token ON vfcorp_deployments.deployment = token.deployment "
                + "WHERE vfcorp_deployments.deployment = '%s' LIMIT 1", deploymentId);

        ResultSet result = (ResultSet) conn.submitQuery(query);

        VfcDeployment deployment = new VfcDeployment();
        while (result.next()) {
            deployment.setDeployment(result.getString("deployment"));
            deployment.setStoreId(result.getString("storeId"));
            deployment.setTimeZone(result.getString("timeZone"));
            deployment.setPluPath(result.getString("pluPath"));
            deployment.setPluFiltered(result.getBoolean("pluFiltered"));
            deployment.setTlogPath(result.getString("tlogPath"));

            SquarePayload sp = new SquarePayload();
            sp.setEncryptedAccessToken(result.getString("encryptedAccessToken"));
            sp.setMerchantId(result.getString("merchantId"));
            sp.setLocationId(result.getString("locationId"));
            deployment.setSquarePayload(sp);
        }

        return deployment;
    }

    public static boolean hasPriceOverride(PaymentItemization itemization) {
        if (itemization.getItemizationType().equals("ITEM") && itemization.getNotes() != null
                && itemization.getNotes().contains("Original Price:")) {
            return true;
        }
        return false;
    }

    public static boolean hasPriceOverride(OrderLineItem lineItem) {
        if (lineItem.getItemType().equals("ITEM") && lineItem.getNote() != null
                && lineItem.getNote().contains("Original Price:")) {
            return true;
        }
        return false;
    }

    public static int getPriceBeforeOverride(PaymentItemization itemization) {
        if (hasPriceOverride(itemization)) {
            String price = itemization.getNotes().split("Original Price:", 2)[1];
            price = price.replaceAll("[^0-9]", "");
            return Integer.valueOf(price);
        }
        return itemization.getSingleQuantityMoney().getAmount();
    }

    public static int getPriceBeforeOverride(OrderLineItem lineItem) {
        if (hasPriceOverride(lineItem)) {
            String price = lineItem.getNote().split("Original Price:", 2)[1];
            price = price.replaceAll("[^0-9]", "");
            return Integer.valueOf(price);
        }
        return lineItem.getBasePriceMoney().getAmount();
    }

    public static boolean isVansDeployment(String deployment) {
        if (deployment.contains(DEPLOYMENT_BRAND_VANS) || deployment.contains(DEPLOYMENT_BRAND_VANS_TEST)) {
            return true;
        }
        return false;
    }

    public static boolean isPluWhitelistDeployment(String brand) {
        String b = brand.toLowerCase();
        return b.equals(DEPLOYMENT_BRAND_TNF) || b.equals(DEPLOYMENT_BRAND_TNF_CA) || b.equals(DEPLOYMENT_BRAND_VANS)
                || b.equals(DEPLOYMENT_BRAND_VANS_TEST);
    }

    public static void saveTmpFile(String filename, List<?> records) throws IOException {
        Gson gson = new Gson();
        String filePath = "/tmp/" + filename;

        FileOutputStream fos = new FileOutputStream(filePath);
        OutputStreamWriter ow = new OutputStreamWriter(fos);
        ow.write("[");

        for (int i = 0; i < records.size(); i++) {
            if (i != 0) {
                ow.write(",");
            }
            ow.write(gson.toJson(records.get(i)));
        }
        ow.write("]");
        ow.flush();
        ow.close();
        fos.close();
    }

    public static void uploadBrandPluFileFromTmpToGCP(CloudStorageApi cloudStorage, String storageBucket, String brand,
            String tmpFilename) throws Exception {
        String filePath = "/tmp/" + tmpFilename;

        FileInputStream fis = new FileInputStream(filePath);
        String fileKey = String.format("%s/%s/%s", "plu", brand, tmpFilename);
        try {
            cloudStorage.uploadObject(storageBucket, fileKey, fis);
        } catch (RuntimeException e) {
            throw new Exception("ERROR: trying to upload to CloudStorage: " + e.getMessage());
        } finally {
            fis.close();
        }
    }

    public static ArrayList<CatalogObject> downloadBrandCatalogFileFromGCP(CloudStorageApi cloudStorage,
            String storageBucket, String brand, String filename) throws GeneralSecurityException, IOException {
        List<CatalogObject> cachedType = new ArrayList<CatalogObject>();
        String fileKey = String.format("%s/%s/%s", "plu", brand, filename);
        int count = 0;
        try (InputStream cis = cloudStorage.downloadObject(storageBucket, fileKey);
                BufferedInputStream bis = new BufferedInputStream(cis);
                JsonReader reader = new JsonReader(new InputStreamReader(cis));) {
            reader.beginArray();
            while (reader.hasNext()) {
                CatalogObject obj = new Gson().fromJson(reader, CatalogObject.class);
                cachedType.add(obj);
                if (count++ % 2500 == 0) {
                    System.out.print(".");
                }
            }
            reader.endArray();
        }
        return (ArrayList<CatalogObject>) cachedType;
    }

    public static Boolean brandPluFileExists(CloudStorageApi cloudStorage, String storageBucket, String brand,
            String filename) throws GeneralSecurityException, IOException {
        String fileKey = String.format("%s/%s/%s", "plu", brand, filename);
        return cloudStorage.listObjects(storageBucket, fileKey).size() > 0;
    }

    public static ArrayList<ItemDbRecord> downloadBrandItemFileFromGCP(CloudStorageApi cloudStorage,
            String storageBucket, String brand, String filename) throws GeneralSecurityException, IOException {
        List<ItemDbRecord> cachedType = new ArrayList<ItemDbRecord>();
        String fileKey = String.format("%s/%s/%s", "plu", brand, filename);
        int count = 0;
        try (InputStream cis = cloudStorage.downloadObject(storageBucket, fileKey);
                BufferedInputStream bis = new BufferedInputStream(cis);
                JsonReader reader = new JsonReader(new InputStreamReader(cis));) {
            reader.beginArray();
            while (reader.hasNext()) {
                ItemDbRecord obj = new Gson().fromJson(reader, ItemDbRecord.class);
                cachedType.add(obj);
                if (count++ % 2500 == 0) {
                    System.out.print(".");
                }
            }
            reader.endArray();
        }
        return (ArrayList<ItemDbRecord>) cachedType;
    }

    public static ArrayList<ItemSaleDbRecord> downloadBrandItemSaleFileFromGCP(CloudStorageApi cloudStorage,
            String storageBucket, String brand, String filename) throws GeneralSecurityException, IOException {
        List<ItemSaleDbRecord> cachedType = new ArrayList<ItemSaleDbRecord>();
        String fileKey = String.format("%s/%s/%s", "plu", brand, filename);
        int count = 0;
        try (InputStream cis = cloudStorage.downloadObject(storageBucket, fileKey);
                BufferedInputStream bis = new BufferedInputStream(cis);
                JsonReader reader = new JsonReader(new InputStreamReader(cis));) {
            reader.beginArray();
            while (reader.hasNext()) {
                ItemSaleDbRecord obj = new Gson().fromJson(reader, ItemSaleDbRecord.class);
                cachedType.add(obj);
                if (count++ % 2500 == 0) {
                    System.out.print(".");
                }
            }
            reader.endArray();
        }
        return (ArrayList<ItemSaleDbRecord>) cachedType;
    }

    public static String getDeviceName(Order order, Map<String, Payment> tenderToPayment) {
    	String registerNumber = null;
    	if(order != null && order.getTenders() != null) {
			for(Tender tender : order.getTenders()) {
				Payment payment = tenderToPayment.get(tender.getId());
				if(payment != null && payment.getDeviceDetails() != null) {
					registerNumber = payment.getDeviceDetails().getDeviceName();
					break;
				}
			}
		}
    	return registerNumber;
    }
}
