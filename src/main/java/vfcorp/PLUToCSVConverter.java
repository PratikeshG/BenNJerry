package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.squareup.connect.Item;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class PLUToCSVConverter {

	public static void main(String[] args) throws Exception {
		String path = "/Users/colinlam/Downloads/PLU_ANDY.RPT";
		String donePath = "/Users/colinlam/Downloads/plu_andy.csv";
		int itemNumberLookupLength = 14;
		
		RPC rpc = new RPC();
		rpc.setItemNumberLookupLength(itemNumberLookupLength);
		
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		rpc.ingest(bis);
		
		Catalog empty = new Catalog();
		Catalog catalog = rpc.convert(empty, CatalogChangeRequest.PrimaryKey.SKU);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price,Variant 1 - SKU\n");
		
		for (Item item : catalog.getItems().values()) {
			sb.append(",");
			sb.append(item.getName() + ",");
			sb.append(item.getCategory().getName() + ",");
			sb.append(",");
			sb.append(item.getVariations()[0].getName() + ",");
			sb.append(item.getVariations()[0].getPriceMoney().getAmount() + ",");
			sb.append(item.getVariations()[0].getSku() + "\n");
		}
		
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(donePath)));
		bwr.write(sb.toString());
		bwr.flush();
		bwr.close();
	}

}
