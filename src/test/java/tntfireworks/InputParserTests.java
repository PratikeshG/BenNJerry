package tntfireworks;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;
import tntfireworks.exceptions.EmptyLocationArrayException;

public class InputParserTests extends TestCase {
	
	private static final String EMPTY_STRING = "";
	
	protected void setUp() {
	}
	
	public void testNullLocations() {
		InputParser inputParser = new InputParser();
		try {
			String generatedSql = inputParser.generateLocationsSQLUpsert(null);
			fail();
		} catch (NullPointerException e) {
			Assert.assertTrue(true);
		}
	}
	
	@Test
	public void testNoLocation() {
		InputParser inputParser = new InputParser();
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
		InputParser inputParser = new InputParser();
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
        String generatedSql = inputParser.generateLocationsSQLUpsert(locations);
        
        String expectedSql = "INSERT INTO tntfireworks_locations (locationNumber, addressNumber, name, address, city, state, zip, county,mktPlan, legal, disc, rbu, bp, co, saNum, saName, custNum, custName, season, year, machineType) VALUES ('AZP0001', '2226460', 'WALMART #2554', '13055 W RANCHO SANTA FE BLVD', 'AVONDALE', 'AZ', '85323', '', '5AZSAT', 'SSS', '25', '51105', '52001', 'WES', '511', 'Teresa Wiig', '2708495', 'MILLENNIUM FFA ALUMNI -2', 'XMAS', '2016', 'SQR'), ('AZP0001', '2226460', 'WALMART #2554', '13055 W RANCHO SANTA FE BLVD', 'AVONDALE', 'AZ', '85323', '', '5AZSAT', 'SSS', '25', '51105', '52001', 'WES', '511', 'Teresa Wiig', '2708495', 'MILLENNIUM FFA ALUMNI -2', 'XMAS', '2016', 'SQR') ON DUPLICATE KEY UPDATE addressNumber=VALUES(addressNumber), name=VALUES(name), address=VALUES(address), city=VALUES(city), state=VALUES(state),zip=VALUES(zip), county=VALUES(county), mktPlan=VALUES(mktPlan), legal=VALUES(legal), disc=VALUES(disc), rbu=VALUES(rbu), bp=VALUES(bp), co=VALUES(co),saNum=VALUES(saNum), saName=VALUES(saName), custNum=VALUES(custNum), custName=VALUES(custName), season=VALUES(season), year=VALUES(year), machineType=VALUES(machineType);";
        Assert.assertEquals(expectedSql, generatedSql);
	}
	
	
	

}
