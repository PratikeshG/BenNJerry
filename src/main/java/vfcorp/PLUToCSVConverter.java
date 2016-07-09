package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.squareup.connect.Item;
import com.squareup.connect.diff.Catalog;

public class PLUToCSVConverter {

	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting PLU to CSV converter...");
		
		String path = "/Users/bhartard/desktop/VFC/xaa";
		String donePath = "/Users/bhartard/desktop/VFC/catalog1.csv";
		int itemNumberLookupLength = 14;
		
		RPC rpc = new RPC();
		rpc.setItemNumberLookupLength(itemNumberLookupLength);
		
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		rpc.ingest(bis);
		
		Catalog empty = new Catalog();
		Catalog catalog = rpc.convert(empty);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU\n");
		
		for (Item item : catalog.getItems().values()) {
			sb.append(",");
			sb.append("\"" + item.getName().replaceAll("\"","\"\"") + "\",");
			sb.append("\"" + item.getCategory().getName().replaceAll("\"","\"\"") + "\",");
			sb.append(",");
			sb.append(item.getVariations()[0].getName() + ",");

			String price = Integer.toString(item.getVariations()[0].getPriceMoney().getAmount());
			if (price.length() > 2) {
				price = price.substring(0, price.length() - 2) + "." + price.substring(price.length() - 2);
			}

			sb.append(price + ",");
			sb.append(item.getVariations()[0].getSku() + "\n");
		}
		
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(donePath)));
		bwr.write(sb.toString());
		bwr.flush();
		bwr.close();

		System.out.println("Done.");
	}

}
