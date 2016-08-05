package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import org.mule.tck.util.FakeObjectStore;

public class Test {

	public static void main( String[] args ) throws Exception {
		/*
		 * TLOG testing
		 */
		System.out.println("Running TLOG...");
		SquareClient client = new SquareClient("sq0ats-0LZJnEzOsjOKcgikzHYjfQ", "https://connect.squareupstaging.com", "v1", "DVKXZBNVFQAGS", "7K2P5XPK2DJ07");        
        
        System.out.println("Getting locations...");
        Merchant[] merchants = client.businessLocations().list();
        Merchant location = merchants[0];
        
        System.out.println("Getting employees...");
        Employee[] employees = client.employees().list();

        System.out.println("Getting catalog...");
        Item[] items = client.items().list();

        int offset = 0;
        int range = 3;
        final String TIMEZONE = "America/Los_Angeles";

        
        System.out.println("Getting payments...");
        Map<String,String>  paymentParams = TimeManager.getPastDayInterval(range, offset, TIMEZONE);
        Payment[] payments = client.payments().list(paymentParams);
        
        EpicorParser epicor = new EpicorParser();
        epicor.tlog().setDeployment("deploymentId");
		epicor.tlog().setTimeZoneId(TIMEZONE);
        epicor.tlog().setItemNumberLookupLength(14);
        epicor.tlog().setObjectStore(new FakeObjectStore<String>());
        epicor.tlog().parse(location, payments, items, employees);
        
        System.out.println("Saving TLOG...");
        PrintWriter writer = new PrintWriter("/Users/bhartard/Desktop/sample-tnf.txt", "UTF-8");
        writer.print(epicor.tlog().toString());
        writer.close();
		
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
		 *
		SquareClient clientMaster = new SquareClient("", "https://connect.squareupstaging.com", "v1", "DVKXZBNVFQAGS", "7K2P5XPK2DJ07");
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
		
		/*
		SquareClient clientMaster = new SquareClient("sq0atp-R8QA_3XoGz67JNhM1pX7zQ", "https://connect.squareup.com", "v1", "me", "58R606YEZ83T9");
		Item[] items = clientMaster.items().list();
		System.out.println(items.length);
		*/
        
        System.out.println("Done.");
    }
}
