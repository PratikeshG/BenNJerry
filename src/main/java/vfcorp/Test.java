package vfcorp;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.mule.tck.util.FakeObjectStore;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import util.TimeManager;

public class Test {

    public static void main(String[] args) throws Exception {
        /*
         * TLOG testing
         */
        String API_ENDPOINT = "https://connect.squareupstaging.com";
        String ACCESS_TOKEN = "sq0ats-0LZJnEzOsjOKcgikzHYjfQ";
        String MERCHANT_ID = "DVKXZBNVFQAGS";
        String LOCATION_ID = "A5EKHWD5C76SZ";

        System.out.println("Running TLOG...");
        com.squareup.connect.SquareClient client = new com.squareup.connect.SquareClient(ACCESS_TOKEN, API_ENDPOINT,
                "v1", MERCHANT_ID, LOCATION_ID);
        com.squareup.connect.v2.SquareClientV2 clientV2 = new com.squareup.connect.v2.SquareClientV2(API_ENDPOINT,
                ACCESS_TOKEN, LOCATION_ID);

        System.out.println("Getting locations...");
        Merchant[] merchants = client.businessLocations().list();
        Merchant location = merchants[0];

        System.out.println("Getting employees...");
        Employee[] employees = client.employees().list();

        int offset = 0;
        int range = 1;
        final String TIMEZONE = "America/Los_Angeles";

        System.out.println("Getting v1 payments...");
        Map<String, String> paymentParams = TimeManager.getPastDayInterval(range, offset, TIMEZONE);
        Payment[] payments = client.payments().list(paymentParams);

        System.out.println("Getting v2 transactions...");
        Map<String, Customer> customerPaymentCache = new HashMap<String, Customer>();
        Transaction[] transactions = clientV2.transactions().list(paymentParams);
        System.out.println("Getting v2 customers...");
        for (Transaction transaction : transactions) {
            for (Tender tender : transaction.getTenders()) {
                if (tender.getCustomerId() != null) {
                    Customer customer = clientV2.customers().retrieve(tender.getCustomerId());
                    customerPaymentCache.put(tender.getId(), customer);
                    customerPaymentCache.put(tender.getTransactionId(), customer);
                }
            }
        }

        EpicorParser epicor = new EpicorParser();
        epicor.tlog().setDeployment("deploymentId");
        epicor.tlog().setTimeZoneId(TIMEZONE);
        epicor.tlog().setItemNumberLookupLength(14);
        epicor.tlog().setObjectStore(new FakeObjectStore<String>());
        epicor.tlog().parse(location, payments, employees, customerPaymentCache);

        System.out.println("Saving TLOG...");
        PrintWriter writer = new PrintWriter("/Users/bhartard/Desktop/sample-tnf.txt", "UTF-8");
        writer.print(epicor.tlog().toString());
        writer.close();

        /*
         * IM testing
         *
         * File f = new File("/Users/colinlam/Desktop/PLU.RPT"); FileInputStream
         * fis = new FileInputStream(f); BufferedInputStream bis = new
         * BufferedInputStream(fis); EpicorParser epicor = new EpicorParser();
         * epicor.rpc().generate(bis);
         */

        /*
         * Testing account diff
         *
         * SquareClient current = new
         * SquareClient("sq0ats-hqRgaU2PkvwxBOqIcdfGYg",
         * "https://connect.squareupstaging.com", "v1", "me", "6DMEYHD342E7D");
         * // colinlam+eldmslave2 SquareClient proposed = new
         * SquareClient("sq0ats-hqRgaU2PkvwxBOqIcdfGYg",
         * "https://connect.squareupstaging.com", "v1", "me", "296T830F8YV30");
         * // colinlam+eldmslave1
         *
         * Catalog master = Catalog.getCatalog(current,
         * CatalogChangeRequest.PrimaryKey.NAME); Catalog slave =
         * Catalog.getCatalog(proposed, CatalogChangeRequest.PrimaryKey.NAME);
         *
         * CatalogChangeRequest req = CatalogChangeRequest.diff(master, slave,
         * CatalogChangeRequest.PrimaryKey.SKU,
         * CatalogChangeRequest.PrimaryKey.NAME, new HashSet<Object>());
         * req.setSquareClient(current).call();
         */

        /*
         * RPC testing
         *
         *
         * SquareClient clientMaster = new SquareClient("",
         * "https://connect.squareupstaging.com", "v1", "DVKXZBNVFQAGS",
         * "7K2P5XPK2DJ07"); Catalog master = Catalog.getCatalog(clientMaster,
         * CatalogChangeRequest.PrimaryKey.NAME);
         *
         * File f = new File("/Users/colinlam/Desktop/PLU2.RPT");
         * FileInputStream fis = new FileInputStream(f); BufferedInputStream bis
         * = new BufferedInputStream(fis); EpicorParser epicor = new
         * EpicorParser(); epicor.rpc().setItemNumberLookupLength(14);
         * epicor.rpc().ingest(bis); Catalog slave =
         * epicor.rpc().convert(master, CatalogChangeRequest.PrimaryKey.NAME);
         *
         * CatalogChangeRequest ccr = CatalogChangeRequest.diff(master, slave,
         * CatalogChangeRequest.PrimaryKey.SKU,
         * CatalogChangeRequest.PrimaryKey.NAME, new HashSet<Object>());
         *
         * ccr.setSquareClient(clientMaster);
         *
         * System.out.println("starting resolution"); ccr.call();
         */

        /*
         * SquareClient clientMaster = new
         * SquareClient("sq0atp-R8QA_3XoGz67JNhM1pX7zQ",
         * "https://connect.squareup.com", "v1", "me", "58R606YEZ83T9"); Item[]
         * items = clientMaster.items().list();
         * System.out.println(items.length);
         */

        System.out.println("Done.");
    }
}
