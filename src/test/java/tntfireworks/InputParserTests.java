package tntfireworks;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import tntfireworks.exceptions.BadFilenameException;
import tntfireworks.exceptions.EmptyLocationArrayException;
import tntfireworks.exceptions.MalformedHeaderRowException;

public class InputParserTests extends TestCase {
	
	private static final String EMPTY_STRING = "";
	@Mock private DBConnection dbConnection;
	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule(); 

	
	protected void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	public void testTwoMarketingPlansCSV() {
		String source = "Item Number,CAT,Category,Item Description,Case Packing,Unit Price,Pricing UOM,Suggested Selling Price,Selling UOM,UPC,Net Item,Expired Date,Eff Date Date,Buy 1Get N Free Value,3rd Item Number,Cur Cod\n100073,10,ASSORTMENTS ,BIG BOMB TRAY C               ,1-Apr,59.99,EA,59.99,EA,027736004485,N,12/31/2040,5/1/2015,,AS104                    ,USD\n100382,10,ASSORTMENTS ,TNT HOT SHOT BAG C            ,24/1                          ,14.99,EA,14.99,EA,027736000098,N,12/31/2040,8/1/2012,,AS123                    ,USD";
		String expectedDeleteQuery = "DELETE FROM tntfireworks_marketing_plans WHERE mktPlan='6RET'";
		String expectedInsertQuery = "INSERT INTO tntfireworks_marketing_plans (mktPlan, itemNumber, cat, category, itemDescription, casePacking, unitPrice, pricingUOM,suggestedPrice, sellingUOM, upc, netItem, expiredDate, effectiveDate, bogo, itemNum3, currency) VALUES ('6RET', '100073', '10', 'ASSORTMENTS', 'BIG BOMB TRAY C', '1-Apr', '59.99', 'EA', '59.99', 'EA', '027736004485', 'N', '12/31/2040', '5/1/2015', '', 'AS104', 'USD'), ('6RET', '100382', '10', 'ASSORTMENTS', 'TNT HOT SHOT BAG C', '24/1', '14.99', 'EA', '14.99', 'EA', '027736000098', 'N', '12/31/2040', '8/1/2012', '', 'AS123', 'USD') ON DUPLICATE KEY UPDATE cat=VALUES(cat), category=VALUES(category), itemDescription=VALUES(itemDescription), casePacking=VALUES(casePacking),unitPrice=VALUES(unitPrice), pricingUOM=VALUES(pricingUOM), suggestedPrice=VALUES(suggestedPrice), sellingUOM=VALUES(sellingUOM), upc=VALUES(upc),netItem=VALUES(netItem), expiredDate=VALUES(expiredDate), effectiveDate=VALUES(effectiveDate), bogo=VALUES(bogo), itemNum3=VALUES(itemNum3), currency=VALUES(currency);";
		try {
			InputStream in = IOUtils.toInputStream(source, "UTF-8");
			BufferedInputStream inputStream = new BufferedInputStream(IOUtils.toBufferedInputStream(in));
			Mockito.when(dbConnection.executeQuery(expectedDeleteQuery)).thenReturn(10).thenReturn(11);
			InputParser inputParser = new InputParser(dbConnection, 5);
			inputParser.syncToDatabase(inputStream, "20161223145321_6RET_12232016.csv");
			Mockito.verify(dbConnection).executeQuery(expectedDeleteQuery);
			Mockito.verify(dbConnection).executeQuery(expectedInsertQuery);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testBadMarketingPlanHeaderCSV() {
		String source = "BADItem Number,CAT,Category,Item Description,Case Packing,Unit Price,Pricing UOM,Suggested Selling Price,Selling UOM,UPC,Net Item,Expired Date,Eff Date Date,Buy 1Get N Free Value,3rd Item Number,Cur Cod\n100073,10,ASSORTMENTS ,BIG BOMB TRAY C               ,1-Apr,59.99,EA,59.99,EA,027736004485,N,12/31/2040,5/1/2015,,AS104                    ,USD\n100382,10,ASSORTMENTS ,TNT HOT SHOT BAG C            ,24/1                          ,14.99,EA,14.99,EA,027736000098,N,12/31/2040,8/1/2012,,AS123                    ,USD";
		String expectedDeleteQuery = "DELETE FROM tntfireworks_marketing_plans WHERE mktPlan='6RET'";
		String expectedInsertQuery = "INSERT INTO tntfireworks_marketing_plans (mktPlan, itemNumber, cat, category, itemDescription, casePacking, unitPrice, pricingUOM,suggestedPrice, sellingUOM, upc, netItem, expiredDate, effectiveDate, bogo, itemNum3, currency) VALUES ('6RET', '100073', '10', 'ASSORTMENTS', 'BIG BOMB TRAY C', '1-Apr', '59.99', 'EA', '59.99', 'EA', '027736004485', 'N', '12/31/2040', '5/1/2015', '', 'AS104', 'USD'), ('6RET', '100382', '10', 'ASSORTMENTS', 'TNT HOT SHOT BAG C', '24/1', '14.99', 'EA', '14.99', 'EA', '027736000098', 'N', '12/31/2040', '8/1/2012', '', 'AS123', 'USD') ON DUPLICATE KEY UPDATE cat=VALUES(cat), category=VALUES(category), itemDescription=VALUES(itemDescription), casePacking=VALUES(casePacking),unitPrice=VALUES(unitPrice), pricingUOM=VALUES(pricingUOM), suggestedPrice=VALUES(suggestedPrice), sellingUOM=VALUES(sellingUOM), upc=VALUES(upc),netItem=VALUES(netItem), expiredDate=VALUES(expiredDate), effectiveDate=VALUES(effectiveDate), bogo=VALUES(bogo), itemNum3=VALUES(itemNum3), currency=VALUES(currency);";
		try {
			InputStream in = IOUtils.toInputStream(source, "UTF-8");
			BufferedInputStream inputStream = new BufferedInputStream(IOUtils.toBufferedInputStream(in));
			Mockito.when(dbConnection.executeQuery(expectedDeleteQuery)).thenReturn(10).thenReturn(11);
			InputParser inputParser = new InputParser(dbConnection, 5);
			inputParser.syncToDatabase(inputStream, "20161223145321_6RET_12232016.csv");
			Mockito.verify(dbConnection).executeQuery(expectedDeleteQuery);
			Mockito.verify(dbConnection).executeQuery(expectedInsertQuery);
		} catch (MalformedHeaderRowException e) {
			Assert.assertTrue(true);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
		
	public void testNullLocations() {
		InputParser inputParser = new InputParser(dbConnection, 5);
		try {
			String generatedSql = inputParser.generateLocationsSQLUpsert(null);
			fail();
		} catch (NullPointerException e) {
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void testNoLocation() {
		InputParser inputParser = new InputParser(dbConnection, 5);
		ArrayList<CSVLocation> emptyLocationsArray = new ArrayList<>();
		try {
			String generatedSql = inputParser.generateLocationsSQLUpsert(emptyLocationsArray);
			fail();
		} catch (EmptyLocationArrayException e) {
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void testProperlyFormattedLocation() {
		InputParser inputParser = new InputParser(dbConnection, 5);
		ArrayList<CSVLocation> locations = generateTwoLocations();
				
        String generatedSql = inputParser.generateLocationsSQLUpsert(locations);
        
        String expectedSql = "INSERT INTO tntfireworks_locations (locationNumber, addressNumber, name, address, city, state, zip, county,mktPlan, legal, disc, rbu, bp, co, saNum, saName, custNum, custName, season, year, machineType) VALUES ('AZP0001', '2226460', 'WALMART #2554', '13055 W RANCHO SANTA FE BLVD', 'AVONDALE', 'AZ', '85323', '', '5AZSAT', 'SSS', '25', '51105', '52001', 'WES', '511', 'Teresa Wiig', '2708495', 'MILLENNIUM FFA ALUMNI -2', 'XMAS', '2016', 'SQR'), ('AZP0001', '2226460', 'WALMART #2554', '13055 W RANCHO SANTA FE BLVD', 'AVONDALE', 'AZ', '85323', '', '5AZSAT', 'SSS', '25', '51105', '52001', 'WES', '511', 'Teresa Wiig', '2708495', 'MILLENNIUM FFA ALUMNI -2', 'XMAS', '2016', 'SQR') ON DUPLICATE KEY UPDATE addressNumber=VALUES(addressNumber), name=VALUES(name), address=VALUES(address), city=VALUES(city), state=VALUES(state),zip=VALUES(zip), county=VALUES(county), mktPlan=VALUES(mktPlan), legal=VALUES(legal), disc=VALUES(disc), rbu=VALUES(rbu), bp=VALUES(bp), co=VALUES(co),saNum=VALUES(saNum), saName=VALUES(saName), custNum=VALUES(custNum), custName=VALUES(custName), season=VALUES(season), year=VALUES(year), machineType=VALUES(machineType);";
        Assert.assertEquals(expectedSql, generatedSql);
	}
	
	@Test
	public void testBadFilename() {
		//bad filename
		BufferedInputStream inputStream = Mockito.mock(BufferedInputStream.class);
		InputParser inputParser = new InputParser(dbConnection, 5);
		Assert.assertEquals(InputParser.LOCATIONS_FILENAME, inputParser.getFilenameOrMarketPlan("20161223154529_locations_12222016.csv"));
		Assert.assertEquals("6RET", inputParser.getFilenameOrMarketPlan("20161223145321_6RET_12232016.csv"));
		try {
			inputParser.syncToDatabase(inputStream, "blah.csv");
			fail();
		} catch (BadFilenameException e) {
			Assert.assertTrue(true);
		} catch (ClassNotFoundException e) {
			fail();
		} catch (SQLException e) {
			fail();
		} catch (IOException e) {
			fail();
		}
		
		try {
			inputParser.syncToDatabase(inputStream, "20161223145321.csv");
			fail();
		} catch (BadFilenameException e) {
			Assert.assertTrue(true);
		} catch (ClassNotFoundException e) {
			fail();
		} catch (SQLException e) {
			fail();
		} catch (IOException e) {
			fail();
		}
		
		try {
			inputParser.syncToDatabase(inputStream, "6RET_20161223145321.csv");
			fail();
		} catch (BadFilenameException e) {
			Assert.assertTrue(true);
		} catch (ClassNotFoundException e) {
			fail();
		} catch (SQLException e) {
			fail();
		} catch (IOException e) {
			fail();
		}
	}
	
	private ArrayList<CSVLocation> generateTwoLocations() {
		ArrayList<CSVLocation> locations = new ArrayList<>();
		
		CSVLocation locationOne = new CSVLocation();
		locationOne.setLocationNum("AZP0001");
        locationOne.setAddressNum("2226460");
        locationOne.setName("WALMART #2554");
        locationOne.setAddress("13055 W RANCHO SANTA FE BLVD");
        locationOne.setCity("AVONDALE");
        locationOne.setState("AZ");
        locationOne.setZip("85323");
        locationOne.setCounty("");
        locationOne.setMktPlan("5AZSAT");
        locationOne.setLegal("SSS");
        locationOne.setDisc("25");
        locationOne.setRbu("51105");
        locationOne.setBp("52001");
        locationOne.setCo("WES");
        locationOne.setSaNum("511");
        locationOne.setSaName("Teresa Wiig");
        locationOne.setCustNum("2708495");
        locationOne.setCustName("MILLENNIUM FFA ALUMNI -2");
        locationOne.setSeason("XMAS");
        locationOne.setYear("2016");
        locationOne.setMachineType("SQR");
        
        CSVLocation locationTwo = new CSVLocation();
		locationTwo.setLocationNum("AZP0001");
        locationTwo.setAddressNum("2226460");
        locationTwo.setName("WALMART #2554");
        locationTwo.setAddress("13055 W RANCHO SANTA FE BLVD");
        locationTwo.setCity("AVONDALE");
        locationTwo.setState("AZ");
        locationTwo.setZip("85323");
        locationTwo.setCounty("");
        locationTwo.setMktPlan("5AZSAT");
        locationTwo.setLegal("SSS");
        locationTwo.setDisc("25");
        locationTwo.setRbu("51105");
        locationTwo.setBp("52001");
        locationTwo.setCo("WES");
        locationTwo.setSaNum("511");
        locationTwo.setSaName("Teresa Wiig");
        locationTwo.setCustNum("2708495");
        locationTwo.setCustName("MILLENNIUM FFA ALUMNI -2");
        locationTwo.setSeason("XMAS");
        locationTwo.setYear("2016");
        locationTwo.setMachineType("SQR");
        
        locations.add(locationOne);
        locations.add(locationTwo);
        
        return locations;
		
	}
	
	
	
	
	

}
