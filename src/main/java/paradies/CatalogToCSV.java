package paradies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.diff.Catalog;

import paradies.pandora.CatalogGenerator;

public class CatalogToCSV {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting UPC Input to CSV converter...");

		String storeId = "2017";
		String path = "/Users/bhartard/desktop/Paradies/Spec/Square_upc_160711.txt";
		String donePath = "/Users/bhartard/desktop/Paradies/Spec/square_catalog.csv";

		File file = new File(path);
		byte[] fileInBytes = new byte[(int) file.length()];
	    
		FileInputStream inputStream = null;
	    try {
	        inputStream = new FileInputStream(file);
	        inputStream.read(fileInBytes);
	    } finally {
	        inputStream.close();
	    }

	    CatalogGenerator catalogGenerator = new CatalogGenerator(storeId, "USD"); // storeId, currencyCode

	    Catalog empty = new Catalog();
		Catalog catalog = catalogGenerator.parsePayload(fileInBytes, empty);

		StringBuffer sb = new StringBuffer();
		sb.append("Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU");

		for (int i = 2; i < 17; i++) {			
			sb.append(",Tax - Tax [" + i + "] (8%)"); // set all taxes to fixed 8% for initial load
		}
		sb.append("\n");

		for (Item item : catalog.getItems().values()) {
			sb.append(","); // no id (add item)
			sb.append("\"" + item.getName().replaceAll("\"","\"\"") + "\",");
			sb.append(","); // no categories
			sb.append(","); // no description
			sb.append(item.getVariations()[0].getName() + ",");

			String price = "";
			if (item.getVariations()[0].getPriceMoney() != null) {
				price = Integer.toString(item.getVariations()[0].getPriceMoney().getAmount());

				if (price.length() > 2) {
					price = price.substring(0, price.length() - 2) + "." + price.substring(price.length() - 2);
				}
			}

			sb.append(price + ",");
			sb.append(item.getVariations()[0].getSku() + ",");
			
			// Taxes
			for (int i = 2; i < 16; i++) {			
				sb.append(itemHasTax(item, i) + ",");
			}
			sb.append(itemHasTax(item, 16) + "\n");
		}

		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(donePath)));
		bwr.write(sb.toString());
		bwr.flush();
		bwr.close();

		System.out.println("Done.");
	}
	
	private static String itemHasTax(Item item, int taxId) {
		for (Fee f : item.getFees()) {
			if (f.getName().contains("[" + taxId + "]")) {
				return "Y";
			}
		}
		return "N";
	}
}