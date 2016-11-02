package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.diff.Catalog;

public class PLUToCSVConverter {

    public static void main(String[] args) throws Exception {
        String DEPLOYMENT = "vfcorp-tnf-00315";
        String PATH = "/Users/bhartard/desktop/load-315/PLU00315.DTA_10_25_2016";
        String DONE_PATH = "/Users/bhartard/desktop/load-315/load-315.csv";

        // Set the appropriate for this location, set to to null when there is
        // no tax
        Fee tax1 = new Fee();
        Fee tax2 = null;
        tax1.setRate("0.06875");
        // tax2.setRate("0.025");

        // DO NOT EDIT BELOW THIS LINE
        System.out.println("Starting PLU to CSV converter...");

        RPC rpc = new RPC();
        rpc.setItemNumberLookupLength(14);

        // create empty Catalog object and add taxes/fees
        Catalog empty = new Catalog();
        if (tax1 != null) {
            empty.addFee(tax1);
        }
        if (tax2 != null) {
            empty.addFee(tax2);
        }

        String tax1Rate = (tax1 != null) ? String.valueOf(Double.parseDouble(tax1.getRate()) * 100) : "0";
        if (tax1Rate.endsWith(".0")) {
            tax1Rate = tax1Rate.split("\\.")[0];
        }
        String tax2Rate = (tax2 != null) ? String.valueOf(Double.parseDouble(tax2.getRate()) * 100) : "0";
        if (tax2Rate.endsWith(".0")) {
            tax2Rate = tax2Rate.split("\\.")[0];
        }

        File file = new File(PATH);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Catalog catalog = rpc.ingest(bis, empty, DEPLOYMENT, RPC.Filter.ACTIVE);
        bis.close();

        StringBuffer sb = new StringBuffer();

        sb.append(
                "Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU,Tax - Sales Tax ("
                        + tax1Rate + "%), Tax - Sales Tax (" + tax2Rate + "%)\n");

        for (Item item : catalog.getItems().values()) {
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

        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(DONE_PATH)));
        bwr.write(sb.toString());
        bwr.flush();
        bwr.close();

        System.out.println("Done.");
    }

}
