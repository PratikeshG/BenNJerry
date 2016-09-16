package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import com.squareup.connect.Item;
import com.squareup.connect.diff.Catalog;

public class PLUToCSVConverter {

	public static void main(String[] args) throws Exception {
		
		boolean taxNYLocation = false;

		System.out.println("Starting PLU to CSV converter...");
		
		HashMap<String, Boolean> skuFilter = new HashMap<String, Boolean>();
		HashMap<String, Boolean> pluFilter = new HashMap<String, Boolean>();
		
		// SKUs
		String filterSKUPath = "/Users/bhartard/desktop/VFC/Filters/vfcorp-tnf-00517-sku.csv";
		try (BufferedReader br = new BufferedReader(new FileReader(filterSKUPath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	skuFilter.put(line.trim(), new Boolean(true));
		    }
		}
		
		// PLUs
		String filterPLUPath = "/Users/bhartard/desktop/VFC/Filters/vfcorp-tnf-00517-plu.csv";
		try (BufferedReader br = new BufferedReader(new FileReader(filterPLUPath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] parts = line.split("\\s+");
		    	pluFilter.put(parts[0].trim(), new Boolean(true));
		    }
		}

		System.out.println("Total SKU filtered: " + skuFilter.size());
		System.out.println("Total PLU filtered: " + pluFilter.size());
				
		System.out.println("------");
		
		String path = "/Users/bhartard/desktop/PLU00517.DTA";
		String donePath = "/Users/bhartard/desktop/00517-new.csv";
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
		
		sb.append("Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU,Tax - Sales Tax (8.75%), Tax - Sales Tax (4.125%)\n");
		
		for (Item item : catalog.getItems().values()) {
			String shortSku = item.getVariations()[0].getSku();
			String medSku = ("000000000000" + shortSku).substring(shortSku.length());
			String longSku = ("00000000000000" + shortSku).substring(shortSku.length());

			//System.out.println(longSku);
			
			String[] bits = item.getName().split("\\s+");
			String plu = bits[bits.length-1];
			
			if (skuFilter.containsKey(shortSku) || skuFilter.containsKey(medSku) || skuFilter.containsKey(longSku) || pluFilter.containsKey(plu)) {
				sb.append(",");
				sb.append("\"" + item.getName().replaceAll("\"","\"\"") + "\",");
				sb.append("\"" + item.getCategory().getName().replaceAll("\"","\"\"") + "\",");
				sb.append(",");
				sb.append(item.getVariations()[0].getName() + ",");

				int priceInt = item.getVariations()[0].getPriceMoney().getAmount();
				String priceString = Integer.toString(priceInt);
				if (priceString.length() > 2) {
					priceString = priceString.substring(0, priceString.length() - 2) + "." + priceString.substring(priceString.length() - 2);
				}

				sb.append(priceString + ",");
				sb.append(item.getVariations()[0].getSku() + ",");
				
				if (!taxNYLocation) {
					sb.append("Y,"); // Apply the default tax: "Sales Tax"
					sb.append("N\n"); // No special NY taxes
				} else {
					String higherTax = priceInt >= 11000 ? "Y" : "N";
					String lowerTax = priceInt < 11000 ? "Y" : "N";
					sb.append(higherTax + ",");
					sb.append(lowerTax + "\n");
				}
			}
		}
		
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(donePath)));
		bwr.write(sb.toString());
		bwr.flush();
		bwr.close();

		System.out.println("Done.");
	}

}
