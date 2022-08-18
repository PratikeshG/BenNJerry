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

public class CatalogExportImages {
    private final static String ACCESS_TOKEN = System.getenv("SCRIPT_ACCOUNT_TOKEN");
    private final static String OUTPUT_PATH = System.getenv("SCRIPT_OUTPUT_PATH");

    private final static String API_VERSION = "2022-06-16";
    private final static String API_URL = "https://connect.squareup.com";
    private final static String[] HEADERS = { "object_type", "object_name", "object_id", "parent_object_id",
            "image_ids" };

    private static Logger logger = LoggerFactory.getLogger(CatalogExportImages.class);

    public static void main(String[] args) throws Exception {
        SquareClientV2 client = new SquareClientV2(API_URL, ACCESS_TOKEN);
        client.setVersion(API_VERSION);

        CatalogObject[] items = client.catalog().listItems();

        Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH), StandardCharsets.ISO_8859_1);

        int totalVariations = 0;

        try (CSVPrinter printer = new CSVPrinter(out,
                CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {

            for (CatalogObject item : items) {
                if (item.getItemData() == null || item.getItemData().getVariations() == null) {
                    continue;
                }

                String itemImages = item.getItemData().getImageIds() != null
                        ? String.join(",", item.getItemData().getImageIds())
                        : "";
                printer.printRecord("item", item.getItemData().getName(), item.getId(), "", itemImages);

                for (CatalogObject variation : item.getItemData().getVariations()) {
                    totalVariations++;

                    String varImages = variation.getItemVariationData().getImageIds() != null
                            ? String.join(",", variation.getItemVariationData().getImageIds())
                            : "";
                    printer.printRecord("variation", variation.getItemVariationData().getName(), variation.getId(),
                            varImages);
                }
            }

            printer.close();
        }

        logger.info("Total items: " + items.length);
        logger.info("Total variations: " + totalVariations);
    }
}
