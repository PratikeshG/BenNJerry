package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class PLUToCSVFromDiffConverter {

    public static void main(String[] args) throws Exception {
        String DEPLOYMENT = "vfcorp-tnf-00064";
        String PATH = "/Users/bhartard/desktop/plu.chg.10052016-64-ab";
        String DONE_PATH = "/Users/bhartard/desktop/64-ab-diff.csv";

        String CONNECT_URI = "https://connect.squareup.com";
        String ACCESS_TOKEN = "sq0atp-uxcoDTyPkzFkEfw5FGNrqw";
        String MERCHANT_ID = "DSEAH44TJ9CF6";
        String LOCATION_ID = "3MHE6ZGDW83BN";

        // DO NOT EDIT BELOW THIS LINE
        System.out.println("Starting PLU to CSV DIFF converter...");

        SquareClient client = new SquareClient(ACCESS_TOKEN, CONNECT_URI, "v1", MERCHANT_ID, LOCATION_ID);

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

        RPC rpc = new RPC();
        rpc.setItemNumberLookupLength(14);

        File file = new File(PATH);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Catalog proposed = rpc.ingest(bis, current, DEPLOYMENT);
        bis.close();

        System.out.println("Performing diff");
        CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU,
                CatalogChangeRequest.PrimaryKey.NAME);

        System.out.println("Diff complete");

        StringBuffer sb = new StringBuffer();

        sb.append(
                "Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU,Tax - Sales Tax ("
                        + tax1Rate + "%), Tax - Sales Tax (" + tax2Rate + "%)\n");

        for (Object obj : ccr.getObjectsToCreate()) {
            if (obj instanceof Item) {
                Item item = (Item) obj;

                sb.append(",");
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
        }

        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(DONE_PATH)));
        bwr.write(sb.toString());
        bwr.flush();
        bwr.close();

        System.out.println("Done.");
    }

}
