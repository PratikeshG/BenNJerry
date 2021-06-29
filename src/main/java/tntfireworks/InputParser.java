package tntfireworks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.SQLException;
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
import tntfireworks.exceptions.MalformedInventoryFileException;
import util.DbConnection;

public class InputParser {

    private int syncGroupSize;
    private DbConnection dbConnection;
    public static final String LOCATIONS_FILENAME = "locations";
    public static final String INVENTORY_FILENAME = "inventory";

    public InputParser(DbConnection dbConnection, int syncGroupSize) {
        this.syncGroupSize = syncGroupSize;
        this.dbConnection = dbConnection;
    }

    private static Logger logger = LoggerFactory.getLogger(InputParser.class);

    public String getMarketPlan(String processingFile) {
        Preconditions.checkNotNull(processingFile);

        // parse filename - marketing plan or location file
        Pattern r = Pattern.compile("\\d+_(\\w+)_\\d+.csv");
        Matcher m = r.matcher(processingFile);
        m.find();

        try {
            return m.group(1);
        } catch (IllegalStateException e) {
            logger.error("Previous regex match operation failed for " + processingFile);
            throw new BadFilenameException();
        } catch (IndexOutOfBoundsException e) {
            logger.error("No Regex group in the filename pattern with the given index for " + processingFile);
            throw new BadFilenameException();
        }

    }

    public void syncToDatabase(BufferedInputStream inputStream, String processingFile)
            throws ClassNotFoundException, SQLException, IOException {
        Preconditions.checkNotNull(dbConnection);
        Preconditions.checkNotNull(processingFile);

        Scanner scanner = new Scanner(inputStream, "UTF-8");

        if (processingFile.contains(LOCATIONS_FILENAME)) {
            processLocations(dbConnection, scanner, processingFile);
        } else if (processingFile.contains(INVENTORY_FILENAME)) {
            processInventoryAdjustments(dbConnection, scanner, processingFile);
        } else {
            String marketPlanId = getMarketPlan(processingFile);
            processMktPlan(dbConnection, scanner, processingFile, marketPlanId);
        }

        scanner.close();
        dbConnection.close();

        // Scanner suppresses exceptions
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }
    }

    public void processMktPlan(DbConnection dbConnection, Scanner scanner, String processingFile, String marketPlanId)
            throws ClassNotFoundException, SQLException {
        Preconditions.checkNotNull(dbConnection);
        Preconditions.checkNotNull(scanner);
        Preconditions.checkNotNull(marketPlanId);

        CsvMktPlan marketingPlan = new CsvMktPlan();
        String[] itemFields = null;
        int totalRecordsProcessed = 0;

        marketingPlan.setName(marketPlanId);
        if (!marketingPlan.getName().equals("")) {
            verifyHeaderRowAndAdvanceToNextLine(scanner, CsvMktPlan.HEADER_ROW, processingFile);
            removeExistingMarketingPlan(processingFile, marketingPlan);

            while (scanner.hasNextLine()) {
                totalRecordsProcessed++;

                itemFields = scanner.nextLine().split(",");
                processItemRow(marketingPlan, itemFields, totalRecordsProcessed);

                if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
                    insertMarketingPlanBatch(marketingPlan, processingFile, totalRecordsProcessed);
                    marketingPlan.clearItems();
                }
            }
        } else {
            logger.error(String.format("Did not process file %s, malformed filename", processingFile));
        }

        logger.info(String.format("(%s) Total records processed: %d", processingFile, totalRecordsProcessed));
        if (totalRecordsProcessed == 0) {
            throw new RuntimeException("No records processed. Invalid input stream.");
        }
    }

    private void removeExistingMarketingPlan(String processingFile, CsvMktPlan marketingPlan)
            throws ClassNotFoundException, SQLException {
        logger.info(String.format("Removing old marketing plan '%s' from DB...", processingFile));
        dbConnection.executeQuery(generateMktPlanSQLDelete(marketingPlan.getName()));
    }

    private void processItemRow(CsvMktPlan marketingPlan, String[] itemFields, int totalRecordsProcessed) {
        try {
            CsvItem item = CsvItem.fromCsvItemFields(itemFields, marketingPlan.getName());
            marketingPlan.addItem(item);
        } catch (IllegalArgumentException e) {
            logMalformedRow(itemFields, totalRecordsProcessed);
        }
    }

    private void insertMarketingPlanBatch(CsvMktPlan marketingPlan, String processingFile, int totalRecordsProcessed)
            throws ClassNotFoundException, SQLException {
        dbConnection.executeQuery(generateMktPlanSQLUpsert(marketingPlan.getName(), marketingPlan.getAllItems()));
        logger.info(String.format("(%s) Processed %d records", processingFile, totalRecordsProcessed));
    }

    private void logMalformedRow(String[] itemFields, int totalRecordsProcessed) {
        String contents = "";
        for (int i = 0; i < itemFields.length; i++) {
            contents += itemFields[i] + " | ";
        }
        logger.error(String.format("Did not process line, malformed record: %d %s", totalRecordsProcessed, contents));
    }

    private void verifyHeaderRowAndAdvanceToNextLine(Scanner scanner, String expectedHeader, String processingFile) {
        logger.info(String.format("Ingesting file %s...", processingFile));
        Preconditions.checkNotNull(scanner);
        Preconditions.checkNotNull(expectedHeader);

        if (scanner.hasNextLine()) {
            String headerRow = scanner.nextLine();
            headerRow = headerRow.replaceAll("\\s+", "");
            expectedHeader = expectedHeader.replaceAll("\\s+", "");
            if (!headerRow.equalsIgnoreCase(expectedHeader)) {
                logger.error(
                        "Malformed header row.\n***EXPECTED***\n" + expectedHeader + "\n***FOUND***\n" + headerRow);
                throw new MalformedHeaderRowException();
            }
        } else {
            throw new EmptyCsvException();
        }
    }

    private void processInventoryAdjustment(String[] inventoryFields,
            ArrayList<CsvInventoryAdjustment> inventoryAdjustments, int totalRecordsProcessed) {
        Preconditions.checkNotNull(inventoryFields);
        Preconditions.checkNotNull(inventoryAdjustments);
        Preconditions.checkNotNull(totalRecordsProcessed);

        try {
            CsvInventoryAdjustment adjustment = new CsvInventoryAdjustment(inventoryFields);
            inventoryAdjustments.add(adjustment);
        } catch (IllegalArgumentException e) {
            logMalformedInventoryAdjustment(inventoryFields, totalRecordsProcessed);
            throw new MalformedInventoryFileException();
        }
    }

    private void logMalformedInventoryAdjustment(String[] inventoryFields, int totalRecordsProcessed) {
        String contents = "";

        for (int i = 0; i < inventoryFields.length; i++) {
            contents += inventoryFields[i] + " | ";
        }

        logger.error(String.format("Did not process line, malformed record: %d %s length: %s", totalRecordsProcessed,
                contents, inventoryFields.length));
    }

    public void processInventoryAdjustments(DbConnection dbConnection, Scanner scanner, String processingFile)
            throws ClassNotFoundException, SQLException {
        Preconditions.checkNotNull(dbConnection);
        Preconditions.checkNotNull(scanner);
        Preconditions.checkNotNull(processingFile);

        ArrayList<CsvInventoryAdjustment> adjustments = new ArrayList<CsvInventoryAdjustment>();
        String[] inventoryFields = null;
        int totalRecordsProcessed = 0;

        verifyHeaderRowAndAdvanceToNextLine(scanner, CsvInventoryAdjustment.HEADER_ROW, processingFile);

        while (scanner.hasNextLine()) {
            totalRecordsProcessed++;
            inventoryFields = scanner.nextLine().split(",");

            processInventoryAdjustment(inventoryFields, adjustments, totalRecordsProcessed);

            if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
                dbConnection.executeQuery(generateInventoryAdjustmentsSQLUpsert(adjustments));
                adjustments.clear();
                logger.info(String.format("(%s) Processed %d records", processingFile, totalRecordsProcessed));
            }
        }

        logger.info(String.format("(%s) Total records processed: %d", processingFile, totalRecordsProcessed));
        if (totalRecordsProcessed == 0) {
            logger.error("No records processed. Invalid input stream.");
            throw new EmptyCsvException();
        }
    }

    private void processLocation(String[] locationFields, ArrayList<CsvLocation> locations, int totalRecordsProcessed) {
        Preconditions.checkNotNull(locationFields);
        Preconditions.checkNotNull(locations);
        Preconditions.checkNotNull(totalRecordsProcessed);

        try {
            CsvLocation location = CsvLocation.fromLocationFieldsCsvRow(locationFields);
            locations.add(location);
        } catch (IllegalArgumentException e) {
            logMalformedLocation(locationFields, totalRecordsProcessed);
        }
    }

    private void logMalformedLocation(String[] locationFields, int totalRecordsProcessed) {
        String contents = "";
        for (int i = 0; i < locationFields.length; i++) {
            contents += locationFields[i] + " | ";
        }

        logger.error(String.format("Did not process line, malformed record: %d %s", totalRecordsProcessed, contents));
    }

    public void processLocations(DbConnection dbConnection, Scanner scanner, String processingFile)
            throws SQLException, ClassNotFoundException {
        Preconditions.checkNotNull(dbConnection);
        Preconditions.checkNotNull(scanner);
        Preconditions.checkNotNull(processingFile);

        ArrayList<CsvLocation> locations = new ArrayList<CsvLocation>();
        String[] locationFields = null;
        int totalRecordsProcessed = 0;

        verifyHeaderRowAndAdvanceToNextLine(scanner, CsvLocation.HEADER_ROW, processingFile);

        while (scanner.hasNextLine()) {
            totalRecordsProcessed++;
            locationFields = scanner.nextLine().split(",");

            processLocation(locationFields, locations, totalRecordsProcessed);

            if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
                dbConnection.executeQuery(generateLocationsSQLUpsert(locations));
                locations.clear();
                logger.info(String.format("(%s) Processed %d records", processingFile, totalRecordsProcessed));
            }
        }

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

    public String generateMktPlanSQLUpsert(String mktPlanName, ArrayList<CsvItem> items) {
        String updateStatement = "";

        if (items.size() > 0) {
            updateStatement = "INSERT INTO tntfireworks_marketing_plans (mktPlan, itemNumber, cat, category, itemDescription, casePacking, unitPrice, pricingUOM,"
                    + "suggestedPrice, sellingUOM, upc, netItem, expiredDate, effectiveDate, bogo, itemNum3, currency, halfOff, sellingPrice) VALUES ";
            String valuesFormat = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

            ArrayList<String> updates = new ArrayList<String>();
            for (CsvItem item : items) {
                updates.add(String.format(valuesFormat, mktPlanName, item.getNumber(), item.getCat(),
                        item.getCategory(), item.getDescription(), item.getCasePacking(), item.getUnitPrice(),
                        item.getPricingUOM(), item.getSuggestedPrice(), item.getSellingUOM(), item.getUPC(),
                        item.getNetItem(), item.getExpiredDate(), item.getEffectiveDate(), item.getBOGO(),
                        item.getItemNum3(), item.getCurrency(), item.getHalfOff(), item.getSellingPrice()));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE cat=VALUES(cat), category=VALUES(category), itemDescription=VALUES(itemDescription), casePacking=VALUES(casePacking),"
                    + "unitPrice=VALUES(unitPrice), pricingUOM=VALUES(pricingUOM), suggestedPrice=VALUES(suggestedPrice), sellingUOM=VALUES(sellingUOM), upc=VALUES(upc),"
                    + "netItem=VALUES(netItem), expiredDate=VALUES(expiredDate), effectiveDate=VALUES(effectiveDate), bogo=VALUES(bogo), itemNum3=VALUES(itemNum3), currency=VALUES(currency), "
                    + "halfOff=VALUES(halfOff), sellingPrice=VALUES(sellingPrice);";
        }

        return updateStatement;
    }

    public String generateLocationsSQLUpsert(ArrayList<CsvLocation> locations) {
        Preconditions.checkNotNull(locations);
        String updateStatement = "";

        if (locations.size() > 0) {
            updateStatement = "INSERT INTO tntfireworks_locations (locationNumber, addressNumber, name, address, city, state, zip, county,"
                    + "mktPlan, legal, disc, rbu, bp, co, saNum, saName, custNum, custName, season, year, machineType, sqDashboardEmail) VALUES ";
            String valuesFormat = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

            ArrayList<String> updates = new ArrayList<String>();
            for (CsvLocation location : locations) {
                updates.add(String.format(valuesFormat, location.getLocationNum(), location.getAddressNum(),
                        location.getName(), location.getAddress(), location.getCity(), location.getState(),
                        location.getZip(), location.getCounty(), location.getMktPlan(), location.getLegal(),
                        location.getDisc(), location.getRbu(), location.getBp(), location.getCo(), location.getSaNum(),
                        location.getSaName(), location.getCustNum(), location.getCustName(), location.getSeason(),
                        location.getYear(), location.getMachineType(), location.getSqDashboardEmail()));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE addressNumber=VALUES(addressNumber), name=VALUES(name), address=VALUES(address), city=VALUES(city), state=VALUES(state),"
                    + "zip=VALUES(zip), county=VALUES(county), mktPlan=VALUES(mktPlan), legal=VALUES(legal), disc=VALUES(disc), rbu=VALUES(rbu), bp=VALUES(bp), co=VALUES(co),"
                    + "saNum=VALUES(saNum), saName=VALUES(saName), custNum=VALUES(custNum), custName=VALUES(custName), season=VALUES(season), year=VALUES(year), machineType=VALUES(machineType),"
                    + "sqDashboardEmail=VALUES(sqDashboardEmail);";
        } else {
            throw new EmptyLocationArrayException();
        }

        return updateStatement;
    }

    public String generateInventoryAdjustmentsSQLUpsert(ArrayList<CsvInventoryAdjustment> adjustments) {
        Preconditions.checkNotNull(adjustments);
        String updateStatement = "";

        if (adjustments.size() > 0) {
            updateStatement = "INSERT INTO tntfireworks_inventory (rbu, bu, locationNum, address, alphaName, itemNum, description, upc, pkg,"
                    + "shipCondition, qtyAdj, sellingUom, um, orderAmt, primaryDg, cscv, soSeason, lt, qtyReset, reset) VALUES ";
            String valuesFormat = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

            ArrayList<String> updates = new ArrayList<String>();
            for (CsvInventoryAdjustment adjustment : adjustments) {
                updates.add(String.format(valuesFormat, adjustment.getRbu(), adjustment.getBu(),
                        adjustment.getLocationNum(), adjustment.getAddress(), adjustment.getAlphaName(),
                        adjustment.getItemNum(), adjustment.getDescription(), adjustment.getUpc(), adjustment.getPkg(),
                        adjustment.getShipCondition(), adjustment.getQtyAdj(), adjustment.getSellingUom(),
                        adjustment.getUm(), adjustment.getOrderAmt(), adjustment.getPrimaryDg(), adjustment.getCsCv(),
                        adjustment.getSoSeason(), adjustment.getLt(), adjustment.getQtyReset(), adjustment.getReset()));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
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
