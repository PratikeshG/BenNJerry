package tntfireworks;

import java.io.BufferedInputStream;
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

public class InputParser {
    
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private int syncGroupSize;    
    
    public InputParser() {
        this.syncGroupSize = 2500;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    void setSyncGroupSize(int syncGroupSize) {
        this.syncGroupSize = syncGroupSize;
    }
    
    private static Logger logger = LoggerFactory.getLogger(InputParser.class);

    public void syncToDatabase(BufferedInputStream inputStream, String processingFile) throws Exception {
        CSVMktPlan marketingPlan = new CSVMktPlan();
        String[] itemFields = null;
        int totalRecordsProcessed = 0;

        // parse marketing plan name
        // prepare regex for group matching
        Pattern r = Pattern.compile("\\d+_(\\w+)_\\d+.csv");
        // use regex to find date in currentFile
        Matcher m = r.matcher(processingFile);
        m.find();
        
        // set marketing plan name
        if (m.group(1) != null)
            marketingPlan.setName(m.group(1));
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        // Using less efficient Scanner because BufferedReader hangs on NULL EOF
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        
        if (!marketingPlan.getName().equals("")) {
            // ignore csv header
            logger.info(String.format("Ingesting marketing plan %s...", processingFile));
            if (scanner.hasNextLine())
                scanner.nextLine();
            while (scanner.hasNextLine()) {
                totalRecordsProcessed++;
                itemFields = scanner.nextLine().split(",");
    
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
                    item.setNumber(itemFields[0].trim());
                    item.setCat(itemFields[1].trim());
                    item.setCategory(itemFields[2].trim());
                    item.setDescription(itemFields[3].trim().replaceAll("'", "''")); // item description may contain SQL special char '
                    item.setCasePacking(itemFields[4].trim());
                    item.setUnitPrice(itemFields[5].trim());
                    item.setPricingUOM(itemFields[6].trim());
                    item.setSuggestedPrice(itemFields[7].trim());
                    item.setSellingUOM(itemFields[8].trim());
                    item.setUPC(itemFields[9].trim());
                    item.setNetItem(itemFields[10].trim());
                    item.setExpiredDate(itemFields[11].trim());
                    item.setEffectiveDate(itemFields[12].trim());
                    item.setBOGO(itemFields[13].trim());
                    item.setItemNum3(itemFields[14].trim());
                    item.setCurrency(itemFields[15].trim());
           
                    // add item to marketing plan
                    marketingPlan.addItem(item);                
                } else {
                    logger.error(String.format("Did not processing line, malformed record: %d", totalRecordsProcessed));
                }
    
                if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
                    submitQuery(conn, generateMktPlanSQLUpsert(marketingPlan.getName(), marketingPlan.getAllItems()));
    
                    marketingPlan.clearItems();
    
                    logger.info(String.format("(%s) Processed %d records", processingFile, totalRecordsProcessed));
                }
            } // end while hasNextLine
        } else {
            logger.error(String.format("Did not processing file %s, malformed filename", processingFile));
        }
        
        scanner.close();
        conn.close();

        // Scanner suppresses exceptions
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }

        logger.info(String.format("(%s) Total records processed: %d", processingFile, totalRecordsProcessed));
        if (totalRecordsProcessed == 0) {
            throw new Exception("No records processed. Invalid input stream.");
        }        
    }
    
    public String generateMktPlanSQLUpsert(String mktPlanName, ArrayList<CSVItem> items) {
        String updateStatement = "";

        if (items.size() > 0) {
            updateStatement = "INSERT INTO tntfireworks_marketing_plans (mktPlan, itemNumber, cat, category, itemDescription, casePacking, unitPrice, pricingUOM,"
                            + "suggestedPrice, sellingUOM, upc, netItem, expiredDate, effectiveDate, bogo, itemNum3, currency) VALUES ";
            String valuesFormat = "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

            ArrayList<String> updates = new ArrayList<String>();
            for (CSVItem item : items) {
                updates.add(String.format(valuesFormat, mktPlanName, item.getNumber(), item.getCat(), item.getCategory(), item.getDescription(),
                        item.getCasePacking(), item.getUnitPrice(), item.getPricingUOM(), item.getSuggestedPrice(), item.getSellingUOM(), item.getUPC(), 
                        item.getNetItem(), item.getExpiredDate(), item.getEffectiveDate(), item.getBOGO(), item.getItemNum3(), item.getCurrency()));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE cat=VALUES(cat), category=VALUES(category), itemDescription=VALUES(itemDescription), casePacking=VALUES(casePacking),"
                            + "unitPrice=VALUES(unitPrice), pricingUOM=VALUES(pricingUOM), suggestedPrice=VALUES(suggestedPrice), sellingUOM=VALUES(sellingUOM), upc=VALUES(upc),"
                            + "netItem=VALUES(netItem), expiredDate=VALUES(expiredDate), effectiveDate=VALUES(effectiveDate), bogo=VALUES(bogo), itemNum3=VALUES(itemNum3), currency=VALUES(currency);";
        }

        return updateStatement; 
    }
    
    public void submitQuery(Connection conn, String query) throws SQLException {
        if (query.isEmpty()) {
            return;
        }

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);        
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
