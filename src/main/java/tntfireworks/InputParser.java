package tntfireworks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import tntfireworks.exceptions.BadFilenameException;
import tntfireworks.exceptions.EmptyCsvException;
import tntfireworks.exceptions.EmptyLocationArrayException;
import tntfireworks.exceptions.MalformedHeaderRowException;

public class InputParser {

    private int syncGroupSize;
	private DBConnection dbConnection;
	public static final String LOCATIONS_FILENAME = "locations";

    public InputParser(DBConnection dbConnection, int syncGroupSize) {
        this.syncGroupSize = syncGroupSize;
        this.dbConnection = dbConnection;
    }

    private static Logger logger = LoggerFactory.getLogger(InputParser.class);
    
    public String getFilenameOrMarketPlan(String processingFile) {
    	Preconditions.checkNotNull(processingFile);
    	
    	// parse filename - marketing plan or location file
        Pattern r = Pattern.compile("\\d+_(\\w+)_\\d+.csv");
        Matcher m = r.matcher(processingFile);
        m.find();

        try {
        	return m.group(1);
        } catch (IllegalStateException e) {

        }
        logger.error("Bad filename " + processingFile);
        throw new BadFilenameException();
        
    }

    public void syncToDatabase(BufferedInputStream inputStream, String processingFile) throws ClassNotFoundException, SQLException, IOException {
    	Preconditions.checkNotNull(dbConnection);
    	Preconditions.checkNotNull(processingFile);
    	
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String filename = getFilenameOrMarketPlan(processingFile);
        
        if (filename.equals(LOCATIONS_FILENAME)) {
            processLocations(dbConnection, scanner, processingFile);
        } else {
            processMktPlan(dbConnection, scanner, processingFile, filename);
        }

        scanner.close();
        dbConnection.close();

        // Scanner suppresses exceptions
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }

    }

    public void processMktPlan(DBConnection dbConnection, Scanner scanner, String processingFile, String filename) throws ClassNotFoundException, SQLException {
        CSVMktPlan marketingPlan = new CSVMktPlan();
        String[] itemFields = null;
        int totalRecordsProcessed = 0;

        marketingPlan.setName(filename);
        if (!marketingPlan.getName().equals("")) {
            // first replace existing marketing plan from db
            logger.info(String.format("Removing old marketing plan '%s' from DB...", processingFile));
            dbConnection.executeQuery(generateMktPlanSQLDelete(marketingPlan.getName()));

            // ignore csv header
            logger.info(String.format("Ingesting marketing plan %s...", processingFile));
            verifyHeaderRowAndAdvanceToNextLine(scanner, CSVMktPlan.HEADER_ROW);

            while (scanner.hasNextLine()) {
                totalRecordsProcessed++;
                itemFields = scanner.nextLine().split(",");

                // trim and replace SQL chars
                // TODO(wtsang): determine more comprehensive check
                for (int i = 0; i < itemFields.length; i++) {
                    itemFields[i] = itemFields[i].trim();
                    itemFields[i] = itemFields[i].replaceAll("'", "''");
                }

                // TODO(wtsang): can use a HashMap + ArrayList to read in fields + add accordingly
                //               and add item constructor to take in HashMap to initialize item
                // item string fields should be in following order:
                //     0 - number;
                //     1 - cat;
                //     2 - category;
                //     3 - description;
                //     4 - casePacking;
                //     5 - unitPrice;
                //     6 - pricingUOM;
                //     7 - suggestedPrice;
                //     8 - sellingUOM;
                //     9 - upc;
                //     10 - netItem;
                //     11- expiredDate;
                //     12 - effectiveDate;
                //     13 - bogo;
                //     14 - itemNum3;
                //     15 - currency;
                //
                if (itemFields.length == 16) {
                    CSVItem item = new CSVItem();
                    item.setNumber(itemFields[0]);
                    item.setCat(itemFields[1]);
                    item.setCategory(itemFields[2]);
                    item.setDescription(itemFields[3]);
                    item.setCasePacking(itemFields[4]);
                    item.setUnitPrice(itemFields[5]);
                    item.setPricingUOM(itemFields[6]);
                    item.setSuggestedPrice(itemFields[7]);
                    item.setSellingUOM(itemFields[8]);
                    item.setUPC(itemFields[9]);
                    item.setNetItem(itemFields[10]);
                    item.setExpiredDate(itemFields[11]);
                    item.setEffectiveDate(itemFields[12]);
                    item.setBOGO(itemFields[13]);
                    item.setItemNum3(itemFields[14]);
                    item.setCurrency(itemFields[15]);

                    // add item to marketing plan
                    marketingPlan.addItem(item);
                } else {
                    String contents = "";
                    for (int i = 0; i < itemFields.length; i++) {
                        contents += itemFields[i] + " | ";
                    }
                    logger.error(String.format("Did not process line, malformed record: %d %s", totalRecordsProcessed,
                            contents));
                }

                if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
                	dbConnection.executeQuery(generateMktPlanSQLUpsert(marketingPlan.getName(), marketingPlan.getAllItems()));

                    marketingPlan.clearItems();

                    logger.info(String.format("(%s) Processed %d records", processingFile, totalRecordsProcessed));
                }
            } // end while hasNextLine
        } else {
            logger.error(String.format("Did not process file %s, malformed filename", processingFile));
        }

        logger.info(String.format("(%s) Total records processed: %d", processingFile, totalRecordsProcessed));
        if (totalRecordsProcessed == 0) {
            throw new RuntimeException("No records processed. Invalid input stream.");
        }
    }
    
    private void verifyHeaderRowAndAdvanceToNextLine(Scanner scanner, String expectedHeader) {
    	Preconditions.checkNotNull(scanner);
    	Preconditions.checkNotNull(expectedHeader);
    	
    	if (scanner.hasNextLine()) {
        	String headerRow = scanner.nextLine();
        	if (!headerRow.equals(expectedHeader)) {
        		throw new MalformedHeaderRowException();
        	}
        } else {
        	throw new EmptyCsvException();
        }
    }
    
    private void processLocation(String[] locationFields, ArrayList<CSVLocation> locations, int totalRecordsProcessed) {
    	Preconditions.checkNotNull(locationFields);
    	Preconditions.checkNotNull(locations);
    	Preconditions.checkNotNull(totalRecordsProcessed);
    	
    	// trim and replace SQL chars
        for (int i = 0; i < locationFields.length; i++) {
            locationFields[i] = locationFields[i].trim();
            locationFields[i] = locationFields[i].replaceAll("'", "''");
        }

        // TODO(wtsang): can use a HashMap + ArrayList to read in fields + add accordingly
        //               and add location constructor to take in HashMap to initialize location
        //      0 - locationNum;
        //      1 - addressNum;
        //      2 - name;
        //      3 - address;
        //      4 - city;
        //      5 - state;
        //      6 - zip;
        //      7 - county;
        //      8 - mktPlan;
        //      9 - legal;
        //      10 - disc;
        //      11 - rbu;
        //      12 - bp;
        //      13 - co;
        //      14 - saNum;
        //      15 - saName;
        //      16 - custNum;
        //      17 - custName;
        //      18 - season;
        //      19 - year;
        //      20 - machineType;
        //
        if (locationFields.length == 21) {
            CSVLocation location = new CSVLocation();
            location.setLocationNum(locationFields[0]);
            location.setAddressNum(locationFields[1]);
            location.setName(locationFields[2]);
            location.setAddress(locationFields[3]);
            location.setCity(locationFields[4]);
            location.setState(locationFields[5]);
            location.setZip(locationFields[6]);
            location.setCounty(locationFields[7]);
            location.setMktPlan(locationFields[8]);
            location.setLegal(locationFields[9]);
            location.setDisc(locationFields[10]);
            location.setRbu(locationFields[11]);
            location.setBp(locationFields[12]);
            location.setCo(locationFields[13]);
            location.setSaNum(locationFields[14]);
            location.setSaName(locationFields[15]);
            location.setCustNum(locationFields[16]);
            location.setCustName(locationFields[17]);
            location.setSeason(locationFields[18]);
            location.setYear(locationFields[19]);
            location.setMachineType(locationFields[20]);

            // add item to marketing plan
            locations.add(location);
        } else {
            String contents = "";
            for (int i = 0; i < locationFields.length; i++) {
                contents += locationFields[i] + " | ";
            }
            logger.error(String.format("Did not process line, malformed record: %d %s", totalRecordsProcessed,
                    contents));
        }
    }

    public void processLocations(DBConnection dbConnection, Scanner scanner, String processingFile) throws SQLException, ClassNotFoundException {
    	Preconditions.checkNotNull(dbConnection);
    	Preconditions.checkNotNull(scanner);
    	Preconditions.checkNotNull(processingFile);
    	
        ArrayList<CSVLocation> locations = new ArrayList<CSVLocation>();
        String[] locationFields = null;
        int totalRecordsProcessed = 0;

        verifyHeaderRowAndAdvanceToNextLine(scanner, CSVLocation.HEADER_ROW);

        logger.info(String.format("Ingesting locations file %s...", processingFile));
        while (scanner.hasNextLine()) {
            totalRecordsProcessed++;
            locationFields = scanner.nextLine().split(",");

            processLocation(locationFields, locations, totalRecordsProcessed);

            if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
            	dbConnection.executeQuery(generateLocationsSQLUpsert(locations));

                locations.clear();

                logger.info(String.format("(%s) Processed %d records", processingFile, totalRecordsProcessed));
            }
        } // end while hasNextLine

        logger.info(String.format("(%s) Total records processed: %d", processingFile, totalRecordsProcessed));
        if (totalRecordsProcessed == 0) {
        	logger.error("No records processed. Invalid input stream.");
            throw new EmptyCsvException();
        }
    }

    public String generateMktPlanSQLDelete(String mktPlanName) {
        String updateStatement = "DELETE FROM tntfireworks_marketing_plans WHERE mktPlan='" + mktPlanName + "'";

        return updateStatement;
    }

    public String generateMktPlanSQLUpsert(String mktPlanName, ArrayList<CSVItem> items) {
        String updateStatement = "";

        if (items.size() > 0) {
            updateStatement = "INSERT INTO tntfireworks_marketing_plans (mktPlan, itemNumber, cat, category, itemDescription, casePacking, unitPrice, pricingUOM,"
                    + "suggestedPrice, sellingUOM, upc, netItem, expiredDate, effectiveDate, bogo, itemNum3, currency) VALUES ";
            String valuesFormat = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

            ArrayList<String> updates = new ArrayList<String>();
            for (CSVItem item : items) {
                updates.add(String.format(valuesFormat, mktPlanName, item.getNumber(), item.getCat(),
                        item.getCategory(), item.getDescription(), item.getCasePacking(), item.getUnitPrice(),
                        item.getPricingUOM(), item.getSuggestedPrice(), item.getSellingUOM(), item.getUPC(),
                        item.getNetItem(), item.getExpiredDate(), item.getEffectiveDate(), item.getBOGO(),
                        item.getItemNum3(), item.getCurrency()));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE cat=VALUES(cat), category=VALUES(category), itemDescription=VALUES(itemDescription), casePacking=VALUES(casePacking),"
                    + "unitPrice=VALUES(unitPrice), pricingUOM=VALUES(pricingUOM), suggestedPrice=VALUES(suggestedPrice), sellingUOM=VALUES(sellingUOM), upc=VALUES(upc),"
                    + "netItem=VALUES(netItem), expiredDate=VALUES(expiredDate), effectiveDate=VALUES(effectiveDate), bogo=VALUES(bogo), itemNum3=VALUES(itemNum3), currency=VALUES(currency);";
        }

        return updateStatement;
    }
    

    String generateLocationsSQLUpsert(ArrayList<CSVLocation> locations) {
    	Preconditions.checkNotNull(locations);
    	
        String updateStatement = "";

        if (locations.size() > 0) {
            updateStatement = "INSERT INTO tntfireworks_locations (locationNumber, addressNumber, name, address, city, state, zip, county,"
                    + "mktPlan, legal, disc, rbu, bp, co, saNum, saName, custNum, custName, season, year, machineType) VALUES ";
            String valuesFormat = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

            ArrayList<String> updates = new ArrayList<String>();
            for (CSVLocation location : locations) {
                updates.add(String.format(valuesFormat, location.getLocationNum(), location.getAddressNum(),
                        location.getName(), location.getAddress(), location.getCity(), location.getState(),
                        location.getZip(), location.getCounty(), location.getMktPlan(), location.getLegal(),
                        location.getDisc(), location.getRbu(), location.getBp(), location.getCo(), location.getSaNum(),
                        location.getSaName(), location.getCustNum(), location.getCustName(), location.getSeason(),
                        location.getYear(), location.getMachineType()));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE addressNumber=VALUES(addressNumber), name=VALUES(name), address=VALUES(address), city=VALUES(city), state=VALUES(state),"
                    + "zip=VALUES(zip), county=VALUES(county), mktPlan=VALUES(mktPlan), legal=VALUES(legal), disc=VALUES(disc), rbu=VALUES(rbu), bp=VALUES(bp), co=VALUES(co),"
                    + "saNum=VALUES(saNum), saName=VALUES(saName), custNum=VALUES(custNum), custName=VALUES(custName), season=VALUES(season), year=VALUES(year), machineType=VALUES(machineType);";
        } else {
        	throw new EmptyLocationArrayException();
        }

        return updateStatement;
    }

    private String appendWithListIterator(String input, List<String> list) {
        Iterator<String> i = list.iterator();
        if (i.hasNext()) {
            input += i.next();
            while (i.hasNext()) {
                input += ", " + i.next();
            }
        }
        return input;
    }

}
