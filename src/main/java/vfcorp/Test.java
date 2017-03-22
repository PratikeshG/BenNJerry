package vfcorp;

import com.squareup.connect.v2.SquareClientV2;

public class Test {
    public static void main(String[] args) throws Exception {

        final String API_ENDPOINT = "https://connect.squareupstaging.com";
        final String ACCESS_TOKEN = "sq0ats-0LZJnEzOsjOKcgikzHYjfQ";
        final String MERCHANT_ID = "DVKXZBNVFQAGS";
        final String LOCATION_ID = "A5EKHWD5C76SZ";

        final String DB_URL = "jdbc:mysql://104.197.244.109:3306/development_bhartard";
        final String DB_USER = "root";
        final String DB_PASSWORD = "DaqXUs4H]cjPU$36";
        final String BRAND = "tnf";

        /*
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

        TLOG tlog = new TLOG();
        tlog.setDeployment("deploymentId");
        tlog.setTimeZoneId(TIMEZONE);
        tlog.setItemNumberLookupLength(14);
        tlog.setObjectStore(new FakeObjectStore<String>());
        tlog.parse(location, payments, employees, customerPaymentCache);

        System.out.println("Saving TLOG...");
        PrintWriter writer = new PrintWriter("/Users/bhartard/Desktop/sample-tnf.txt", "UTF-8");
        writer.print(tlog.toString());
        writer.close();
        */

        SquareClientV2 client = new SquareClientV2(API_ENDPOINT, ACCESS_TOKEN);

        PLUCatalogBuilder catalogBuilder = new PLUCatalogBuilder(client, DB_URL, DB_USER, DB_PASSWORD, BRAND);
        catalogBuilder.setPluFiltered(false);

        catalogBuilder.syncCategoriesFromDatabaseToSquare();
        catalogBuilder.syncItemsFromDatabaseToSquare();

        System.out.println("Done.");
    }
}
