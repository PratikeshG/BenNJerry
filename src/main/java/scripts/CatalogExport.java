package scripts;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.SquareClientV2;

public class CatalogExport {
    private final static String ACCESS_TOKEN = System.getenv("SCRIPT_ACCESS_TOKEN");
    private final static String OUTPUT_PATH = System.getenv("SCRIPT_OUTPUT_PATH");

    private final static String API_URL = "https://connect.squareup.com";
    private final static String[] HEADERS = { "item_name", "item_object_id", "variation_name", "variation_object_id" };

    private static Logger logger = LoggerFactory.getLogger(CatalogExport.class);

    public static void main(String[] args) throws Exception {
        SquareClientV2 client = new SquareClientV2(API_URL, ACCESS_TOKEN);

        CatalogObject[] items = client.catalog().listItems();

        Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH), StandardCharsets.ISO_8859_1);

        int totalVariations = 0;

        try (CSVPrinter printer = new CSVPrinter(out,
                CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {

            for (CatalogObject item : items) {
                if (item.getItemData() == null || item.getItemData().getVariations() == null) {
                    continue;
                }

                for (CatalogObject variation : item.getItemData().getVariations()) {
                    totalVariations++;

                    printer.printRecord(item.getItemData().getName(), item.getId(),
                            variation.getItemVariationData().getName(), variation.getId());
                }
            }

            printer.close();
        }

        logger.info("Total items: " + items.length);
        logger.info("Total variations: " + totalVariations);
    }
}
