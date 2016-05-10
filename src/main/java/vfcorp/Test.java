package vfcorp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import util.TimeManager;

import com.squareup.connect.CashDrawerShift;
import com.squareup.connect.Category;
import com.squareup.connect.Employee;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.Merchant;
import com.squareup.connect.ModifierList;
import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.SquareClient;
import com.squareup.connect.Tender;
import com.squareup.connect.diff.CatalogChangeRequest;
import com.squareup.connect.diff.Catalog;

public class Test {

	public static void main( String[] args ) throws Exception {
		/*
		 * TLOG testing
		 *
		//SquareClient client = new SquareClient("sq0ats-hqRgaU2PkvwxBOqIcdfGYg", "https://connect.squareupstaging.com", "v1", "me", "D67SWP5DZ9AWG");
		SquareClient client = new SquareClient("sq0atp-eWyKq9VkPuj-ZKuwi6XYew", "https://connect.squareup.com", "v1", "me", "E8V3AF2CWMNWV"); // VF test location
		Payment[] payments = client.payments().list();
        Item[] items = client.items().list();
        Employee[] employees = client.employees().list();
        Merchant[] merchants = client.businessLocations().list();
        
        EpicorParser epicor = new EpicorParser();
        epicor.tlog().setItemNumberLookupLength(16);
        epicor.tlog().parse(merchants[1], payments, items, employees);
        
        //System.out.println(epicor.tlog().toString());
        PrintWriter writer = new PrintWriter("/Users/colinlam/Desktop/sample.txt", "UTF-8");
        writer.print(epicor.tlog().toString());
        writer.close();
        */
		
		/*
		 * IM testing
		 *
		File f = new File("/Users/colinlam/Desktop/PLU.RPT");
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().generate(bis);
		 */
		
		/*
		 * Testing account diff
		 *
		SquareClient current = new SquareClient("sq0ats-hqRgaU2PkvwxBOqIcdfGYg", "https://connect.squareupstaging.com", "v1", "me", "6DMEYHD342E7D"); // colinlam+eldmslave2
		SquareClient proposed = new SquareClient("sq0ats-hqRgaU2PkvwxBOqIcdfGYg", "https://connect.squareupstaging.com", "v1", "me", "296T830F8YV30"); // colinlam+eldmslave1
		
		Catalog master = Catalog.getCatalog(current, CatalogChangeRequest.PrimaryKey.NAME);
		Catalog slave = Catalog.getCatalog(proposed, CatalogChangeRequest.PrimaryKey.NAME);
		
		CatalogChangeRequest req = CatalogChangeRequest.diff(master, slave, CatalogChangeRequest.PrimaryKey.SKU, CatalogChangeRequest.PrimaryKey.NAME, new HashSet<Object>());
		req.setSquareClient(current).call();
		*/
		
		/*
		 * RPC testing
		 *
		SquareClient clientMaster = new SquareClient("sq0ats-hqRgaU2PkvwxBOqIcdfGYg", "https://connect.squareupstaging.com", "v1", "me", "D67SWP5DZ9AWG");
		Catalog master = Catalog.getCatalog(clientMaster, CatalogChangeRequest.PrimaryKey.NAME);
		
		File f = new File("/Users/colinlam/Desktop/PLU2.RPT");
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		EpicorParser epicor = new EpicorParser();
		epicor.rpc().setItemNumberLookupLength(14);
		epicor.rpc().ingest(bis);
		Catalog slave = epicor.rpc().convert(master, CatalogChangeRequest.PrimaryKey.NAME);
		
		CatalogChangeRequest ccr = CatalogChangeRequest.diff(master, slave, CatalogChangeRequest.PrimaryKey.SKU, CatalogChangeRequest.PrimaryKey.NAME, new HashSet<Object>());
		
		ccr.setSquareClient(clientMaster);
		
		System.out.println("starting resolution");
		ccr.call();
		*/
		
		SquareClient client = new SquareClient("sq0atp-c-QNyU8TxlLFsgRuc6XlDQ", "https://connect.squareup.com", "v1", "8W5RH94104PGR", "8W5RH94104PGR");
		
		HashMap<String,String> map = new HashMap<String,String>();
		
		map.put("begin_time", "2016-04-30T00:00:00-04:00");
		map.put("end_time", "2016-04-30T23:59:59-04:00");
		
		Refund[] refunds = client.refunds().list(map);
		
		int total = 0;
		for (Refund refund : refunds) {
			total += refund.getRefundedMoney().getAmount();
		}
		
		System.out.println(total);
    }
}
