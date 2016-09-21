package paradies;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TaxTable {

	private static final Map<String, Integer> CODE_TO_ID;
	private static final Map<String, Integer> NAME_TO_ID;

	static {    
        Map<String, Integer> iMap = new HashMap<String, Integer>();
        iMap.put(Tax.TAX_CODE_1, 1);
        iMap.put(Tax.TAX_CODE_2, 2);
        iMap.put(Tax.TAX_CODE_3, 3);
        iMap.put(Tax.TAX_CODE_4, 4);
        iMap.put(Tax.TAX_CODE_5, 5);
        iMap.put(Tax.TAX_CODE_6, 6);
        iMap.put(Tax.TAX_CODE_7, 7);
        iMap.put(Tax.TAX_CODE_8, 8);
        iMap.put(Tax.TAX_CODE_9, 9);
        iMap.put(Tax.TAX_CODE_10, 10);
        iMap.put(Tax.TAX_CODE_11, 11);
        iMap.put(Tax.TAX_CODE_12, 12);
        iMap.put(Tax.TAX_CODE_13, 13);
        iMap.put(Tax.TAX_CODE_14, 14);
        iMap.put(Tax.TAX_CODE_15, 15);
        iMap.put(Tax.TAX_CODE_16, 16);
        CODE_TO_ID = Collections.unmodifiableMap(iMap);
        
        Map<String, Integer> nMap = new HashMap<String, Integer>();
        nMap.put(Tax.TAX_NAME_1, 1);
        nMap.put(Tax.TAX_NAME_2, 2);
        nMap.put(Tax.TAX_NAME_3, 3);
        nMap.put(Tax.TAX_NAME_4, 4);
        nMap.put(Tax.TAX_NAME_5, 5);
        nMap.put(Tax.TAX_NAME_6, 6);
        nMap.put(Tax.TAX_NAME_7, 7);
        nMap.put(Tax.TAX_NAME_8, 8);
        nMap.put(Tax.TAX_NAME_9, 9);
        nMap.put(Tax.TAX_NAME_10, 10);
        nMap.put(Tax.TAX_NAME_11, 11);
        nMap.put(Tax.TAX_NAME_12, 12);
        nMap.put(Tax.TAX_NAME_13, 13);
        nMap.put(Tax.TAX_NAME_14, 14);
        nMap.put(Tax.TAX_NAME_15, 15);
        nMap.put(Tax.TAX_NAME_16, 16);
        NAME_TO_ID = Collections.unmodifiableMap(nMap);
    }
	
	private HashMap<Integer, Tax> table;
	
	public TaxTable(String storeId) throws Exception {
		HashMap<Integer, Tax> taxTable = new HashMap<Integer, Tax>();
		
		taxTable.put(1, new Tax(1, Tax.TAX_CODE_1, Tax.TAX_NAME_1, "0"));
		taxTable.put(2, new Tax(2, Tax.TAX_CODE_2, Tax.TAX_NAME_2));
		taxTable.put(3, new Tax(3, Tax.TAX_CODE_3, Tax.TAX_NAME_3));
		taxTable.put(4, new Tax(4, Tax.TAX_CODE_4, Tax.TAX_NAME_4));
		taxTable.put(5, new Tax(5, Tax.TAX_CODE_5, Tax.TAX_NAME_5));
		taxTable.put(6, new Tax(6, Tax.TAX_CODE_6, Tax.TAX_NAME_6));
		taxTable.put(7, new Tax(7, Tax.TAX_CODE_7, Tax.TAX_NAME_7));
		taxTable.put(8, new Tax(8, Tax.TAX_CODE_8, Tax.TAX_NAME_8));
		taxTable.put(9, new Tax(9, Tax.TAX_CODE_9, Tax.TAX_NAME_9));
		taxTable.put(10, new Tax(10, Tax.TAX_CODE_10, Tax.TAX_NAME_10));
		taxTable.put(11, new Tax(11, Tax.TAX_CODE_11, Tax.TAX_NAME_11));
		taxTable.put(12, new Tax(12, Tax.TAX_CODE_12, Tax.TAX_NAME_12));
		taxTable.put(13, new Tax(13, Tax.TAX_CODE_13, Tax.TAX_NAME_13));
		taxTable.put(14, new Tax(14, Tax.TAX_CODE_14, Tax.TAX_NAME_14));
		taxTable.put(15, new Tax(15, Tax.TAX_CODE_15, Tax.TAX_NAME_15));
		taxTable.put(16, new Tax(16, Tax.TAX_CODE_16, Tax.TAX_NAME_16));

		String ZERO_RATE = "0";

		switch (storeId) {
        case "2":
        case "3": // Test locations
        	String testRate = "0.08";
			String lowerRate = "0.045";
			taxTable.get(2).setRate(testRate);
			taxTable.get(3).setRate(testRate);
			taxTable.get(4).setRate(testRate);
			taxTable.get(5).setRate(testRate);
			taxTable.get(6).setRate(lowerRate);
			taxTable.get(7).setRate(testRate);
			taxTable.get(8).setRate(testRate);
			taxTable.get(9).setRate(lowerRate);
			taxTable.get(10).setRate(testRate);
			taxTable.get(11).setRate(testRate);
			taxTable.get(12).setRate(lowerRate);
			taxTable.get(13).setRate(lowerRate);
			taxTable.get(14).setRate(testRate);
			taxTable.get(15).setRate(testRate);
			taxTable.get(16).setRate(testRate);
            break;
        case "2011": // Atlanta - 9706
        	String atlRate = "0.08";
        	for (int i = 2; i <= 16; i++) {
        		taxTable.get(i).setRate(atlRate);
        	}
            break;
        case "2017": // NYC JFK - 9212
        	String jfkRate = "0.08875";
			taxTable.get(2).setRate(jfkRate);
			taxTable.get(3).setRate(jfkRate);
			taxTable.get(6).setRate(jfkRate);
			taxTable.get(9).setRate(jfkRate);
			taxTable.get(11).setRate(jfkRate);
			taxTable.get(12).setRate(jfkRate);
			taxTable.get(15).setRate(jfkRate);

			taxTable.get(4).setRate(ZERO_RATE);
			taxTable.get(5).setRate(ZERO_RATE);
			taxTable.get(7).setRate(ZERO_RATE);
			taxTable.get(8).setRate(ZERO_RATE);
			taxTable.get(10).setRate(ZERO_RATE);
			taxTable.get(13).setRate(ZERO_RATE);

			String jfkClothingRate = "0.04875";
			taxTable.get(14).setRate(jfkClothingRate);
			taxTable.get(16).setRate(jfkClothingRate);

            break;
        default:
        	throw new Exception("Missing tax table for store ID " + storeId);
		}

		this.table = taxTable;
	}
	
	public Tax getTax(int taxId) {
		return table.get(taxId);
	}
	
	public Tax getTaxFromCode(String code) {
		return table.get(CODE_TO_ID.get(code));
	}
	
	public Tax getTaxFromName(String name) {
		return table.get(NAME_TO_ID.get(name));
	}
	
	public HashMap<Integer, Tax> getTaxes() {
		return table;
	}
}