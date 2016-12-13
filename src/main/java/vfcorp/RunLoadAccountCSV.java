package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class RunLoadAccountCSV {

    int CONCURRENT_TASKS = 5;

    String DATABASE_URL = "jdbc:mysql://104.197.244.109:3306/development_bhartard";
    String DATABASE_USERNAME = "";
    String DATABASE_PASSWORD = "";

    String DIR_PATH = "/Users/bhartard/desktop/sales/12-8-2016/OUTLET/";

    public void run() {
        class PLULoadTask {
            private String inputFile;
            private String locationId;
            private String deploymentId;
            private String timeZone;

            public PLULoadTask(String inputFile, String locationId, String deploymentId, String timeZone) {
                this.inputFile = inputFile;
                this.locationId = locationId;
                this.deploymentId = deploymentId;
                this.timeZone = timeZone;
            }

            public String getInputFile() {
                return inputFile;
            }

            public String getLocationId() {
                return locationId;
            }

            public String getDeploymentId() {
                return deploymentId;
            }

            public String getTimeZone() {
                return timeZone;
            }
        }

        String TIMEZONE = "America/New_York";
        ArrayList<PLULoadTask> tasks = new ArrayList<PLULoadTask>();

        // FULL PRICE
        /*
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "2GAGTTA5Q68YJ", "vfcorp-tnf-00001", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "CH8HXB0NZW1MV", "vfcorp-tnf-00010", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "0TMNNPGCSBE5Y", "vfcorp-tnf-00012", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "CW3R3REFD71KG", "vfcorp-tnf-00014", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "7GVHAS422FBSV", "vfcorp-tnf-00016", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00017.DTA", "AM6RHNEEXS6ZD", "vfcorp-tnf-00017", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "AMS55HMEWYDN6", "vfcorp-tnf-00020", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00021.DTA", "7E392MM4J52FP", "vfcorp-tnf-00021", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00029.DTA", "BVXG4MWRGTNRE", "vfcorp-tnf-00029", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00039.DTA", "EZJM365N7T51R", "vfcorp-tnf-00039", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "FRKT9Z5KM9T0X", "vfcorp-tnf-00040", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00043.DTA", "DGVQJ4QBZG2G1", "vfcorp-tnf-00043", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00044.DTA", "A2X9KEVRVXGCC", "vfcorp-tnf-00044", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00046.DTA", "BBQDRYHM32C6P", "vfcorp-tnf-00046", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00053.DTA", "4HBRD1TZ64J8C", "vfcorp-tnf-00053", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00055.DTA", "6D8TPHJPEMH2M", "vfcorp-tnf-00055", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00059.DTA", "DDYHVFHR1F8S6", "vfcorp-tnf-00059", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "1SY99QNDRDEN5", "vfcorp-tnf-00402", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00506.DTA", "AJRZ51P0966R6", "vfcorp-tnf-00506", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00508.DTA", "96K9R192S0BJJ", "vfcorp-tnf-00508", TIMEZONE));
        tasks.add(new PLULoadTask("PLU00512.DTA", "EAEZDPYZW30DR", "vfcorp-tnf-00512", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "EN7KGE1AES2TT", "vfcorp-tnf-00513", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "E3BA9DKRGVR5Z", "vfcorp-tnf-00516", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161129170128", "7S5JD3KD1S2KY", "vfcorp-tnf-00517", TIMEZONE));
        */
        // OUTLET - EAST COAST
        /*
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "AHH2VZPBV01AN", "vfcorp-tnf-00062", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "3MHE6ZGDW83BN", "vfcorp-tnf-00064", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "49GXCVYDZFGMC", "vfcorp-tnf-00068", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "8BMEXAN3RR35M", "vfcorp-tnf-00069", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "ARYSSQ99X7JMS", "vfcorp-tnf-00075", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "C665K0DQ13K2Z", "vfcorp-tnf-00077", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "BWKTEWFKKST9N", "vfcorp-tnf-00079", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "6SSZ9FV6ZXAAY", "vfcorp-tnf-00082", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "3DCP3A0JBVWH3", "vfcorp-tnf-00084", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "BWMFQABRPCTB1", "vfcorp-tnf-00085", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "8ZRW74VNXXZSZ", "vfcorp-tnf-00086", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "DMZZJMPTT9XPY", "vfcorp-tnf-00301", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "D85N608PP6X0E", "vfcorp-tnf-00303", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "0DEZYS37XPAWM", "vfcorp-tnf-00305", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "6YCN9B38ZN5AS", "vfcorp-tnf-00306", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "AYW0XW6GVETSN", "vfcorp-tnf-00308", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "2Q6BE18439CPN", "vfcorp-tnf-00310", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "DDVH21DXAXXFY", "vfcorp-tnf-00316", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "CGGQRSJ1A09JA", "vfcorp-tnf-00319", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "DD7QBE0XXRYT4", "vfcorp-tnf-00320", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "9K36V9F32WQQ5", "vfcorp-tnf-00321", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "BJR5NZQQ6R9TB", "vfcorp-tnf-00325", TIMEZONE));
        */

        // OUTLET - CENTRAL
        /*
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "2BJNYHSPTGXMD", "vfcorp-tnf-00073", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "3M2G9CB4D5FAB", "vfcorp-tnf-00083", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "F80VW7Y0JM2BD", "vfcorp-tnf-00307", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "12QNRWHW5N3PP", "vfcorp-tnf-00312", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "572E9K2QKTXCD", "vfcorp-tnf-00315", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "5XJVMJ0HBBF0C", "vfcorp-tnf-00317", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "364KHVZF5V1GQ", "vfcorp-tnf-00322", TIMEZONE));
        */
        tasks.add(new PLULoadTask("TNF00060_plu.chg.20161208201806", "2BBHP3B5QJQYB", "vfcorp-tnf-00323", TIMEZONE));

        // OUTLET - WEST COAST
        /*
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "EFG9HHBWZD0KD", "vfcorp-tnf-00060", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "BENMQFQS1RGZR", "vfcorp-tnf-00074", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "54M0Y91VP2589", "vfcorp-tnf-00076", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "F3VCN1ZGTS41D", "vfcorp-tnf-00078", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "6ZCZT745GZBJ1", "vfcorp-tnf-00080", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "49JQE7Y15SDRH", "vfcorp-tnf-00081", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "0YA44XZCQR1QD", "vfcorp-tnf-00302", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "BT2C4972Z5YQD", "vfcorp-tnf-00304", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "8280QFJXDS9W7", "vfcorp-tnf-00311", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "303XHY26ATHTJ", "vfcorp-tnf-00313", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "B37S4A8NEVPH2", "vfcorp-tnf-00314", TIMEZONE));
        tasks.add(new PLULoadTask("plu.chg.20161128194221", "3VCQ99EZZX5FJ", "vfcorp-tnf-00318", TIMEZONE));
        */
        // CLEARANCE
        /*
        tasks.add(new PLULoadTask("TNF00326_plu.chg.20161129170710", "959D09MHSDF4V", "vfcorp-tnf-00326", TIMEZONE));
        tasks.add(new PLULoadTask("TNF00326_plu.chg.20161129170710", "3QQVQA5AR3M9X", "vfcorp-tnf-00327", TIMEZONE));
        */
        // -----------------------------------------------------------------
        // ------------- DO NOT EDIT BELOW THIS LINE -----------------------
        // -----------------------------------------------------------------

        final BlockingQueue<PLULoadTask> queue = new ArrayBlockingQueue<PLULoadTask>(tasks.size());
        for (PLULoadTask task : tasks) {
            queue.add(task);
        }

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_TASKS);

        for (int i = 1; i <= CONCURRENT_TASKS; i++) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    PLULoadTask task = null;
                    while ((task = queue.poll()) != null) {
                        try {
                            processTask(task);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                private void processTask(PLULoadTask task) throws Exception {
                    String MERCHANT_ID = "DSEAH44TJ9CF6";
                    String DONE_PATH = DIR_PATH + "load-" + task.getDeploymentId() + ".csv";
                    String LOAD_PATH = DIR_PATH + task.getInputFile();

                    System.out.println("Starting LOAD to Database to CSV for Deployment: " + task.getDeploymentId());

                    File file = new File(LOAD_PATH);
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    PLUParser parser = new PLUParser();
                    parser.setDeploymentId(task.getDeploymentId());
                    parser.setSyncGroupSize(2500);
                    parser.setDatabaseUrl(DATABASE_URL);
                    parser.setDatabaseUser(DATABASE_USERNAME);
                    parser.setDatabasePassword(DATABASE_PASSWORD);
                    parser.syncToDatabase(bis, MERCHANT_ID, task.getLocationId());
                    bis.close();

                    SquareClient client = new SquareClient("sq0atp-SK8NUp4_KvlEgBls5kGkUg",
                            "https://connect.squareup.com", "v1", MERCHANT_ID, task.getLocationId());

                    System.out.println("Downloading items...");
                    Item[] items = client.items().list();

                    System.out.println("Downloading taxes...");
                    Fee[] fees = client.fees().list();

                    Catalog current = new Catalog();

                    if (items != null) {
                        for (Item item : items) {
                            current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
                        }
                    }

                    if (fees != null) {
                        for (Fee fee : fees) {
                            current.addFee(fee);
                        }
                    }

                    Fee tax1 = (fees.length > 0) ? fees[0] : null;
                    Fee tax2 = (fees.length == 2) ? fees[1] : null;

                    String tax1Rate = (tax1 != null) ? String.valueOf(Double.parseDouble(tax1.getRate()) * 100) : "0";
                    if (tax1Rate.endsWith(".0")) {
                        tax1Rate = tax1Rate.split("\\.")[0];
                    }
                    String tax2Rate = (tax2 != null) ? String.valueOf(Double.parseDouble(tax2.getRate()) * 100) : "0";
                    if (tax2Rate.endsWith(".0")) {
                        tax2Rate = tax2Rate.split("\\.")[0];
                    }

                    PLUCatalogBuilder catalogBuilder = new PLUCatalogBuilder(DATABASE_URL, DATABASE_USERNAME,
                            DATABASE_PASSWORD);
                    catalogBuilder.setItemNumberLookupLength(14);

                    Catalog catalog = catalogBuilder.newCatalogFromDatabase(current, task.getDeploymentId(),
                            task.getLocationId(), task.getTimeZone(), true);

                    StringBuffer sb = new StringBuffer();

                    sb.append(
                            "Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU,Tax - Sales Tax ("
                                    + tax1Rate + "%), Tax - Sales Tax (" + tax2Rate + "%)\n");

                    for (Item item : catalog.getItems().values()) {

                        // Skip Boston/RhodeIsland items that we can't tax
                        if ((task.getDeploymentId().equals("vfcorp-tnf-00014")
                                || task.getDeploymentId().equals("vfcorp-tnf-00039")
                                || task.getDeploymentId().equals("vfcorp-tnf-00053"))
                                && TaxRules.CLOTHING_DEPT_CLASS
                                        .contains(Util.getValueInParenthesis(item.getVariations()[0].getName()))
                                && item.getVariations()[0].getPriceMoney().getAmount() > 17500) {
                            continue;
                        } else if (task.getDeploymentId().equals("vfcorp-tnf-00508")
                                && TaxRules.CLOTHING_DEPT_CLASS
                                        .contains(Util.getValueInParenthesis(item.getVariations()[0].getName()))
                                && item.getVariations()[0].getPriceMoney().getAmount() > 25000) {
                            continue;
                        }

                        sb.append(item.getId() + ",");
                        sb.append("\"" + item.getName().replaceAll("\"", "\"\"") + "\",");
                        sb.append("\"" + item.getCategory().getName().replaceAll("\"", "\"\"") + "\",");
                        sb.append(",");
                        sb.append(item.getVariations()[0].getName() + ",");

                        int priceInt = item.getVariations()[0].getPriceMoney().getAmount();
                        String priceString = Integer.toString(priceInt);
                        if (priceString.length() > 2) {
                            priceString = priceString.substring(0, priceString.length() - 2) + "."
                                    + priceString.substring(priceString.length() - 2);
                        }

                        sb.append(priceString + ",");
                        sb.append(item.getVariations()[0].getSku() + ",");

                        // Figure out taxes
                        String applyFirstTax = "N";
                        String applySecondTax = "N";

                        if (item.getFees().length == 1) {
                            Fee itemTax = item.getFees()[0];
                            if (tax1 != null) {
                                applyFirstTax = (tax1.getRate().equals(itemTax.getRate())) ? "Y" : "N";
                            }
                            if (tax2 != null) {
                                applySecondTax = (tax2.getRate().equals(itemTax.getRate())) ? "Y" : "N";
                            }
                        }

                        sb.append(applyFirstTax + ",");
                        sb.append(applySecondTax + "\n");
                    }

                    BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(DONE_PATH)));
                    bwr.write(sb.toString());
                    bwr.flush();
                    bwr.close();

                    System.out.println("Done: " + task.getDeploymentId());
                }
            };
            pool.execute(r);
        }
        pool.shutdown();
    }

    public static void main(String[] args) throws Exception {
        RunLoadAccountCSV app = new RunLoadAccountCSV();
        app.run();
    }

}