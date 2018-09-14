package vfcorp;

import java.io.BufferedInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vfcorp.rpc.DepartmentClassRecord;
import vfcorp.rpc.ItemAlternateDescription;
import vfcorp.rpc.ItemRecord;

public class PluParser {
    private static Logger logger = LoggerFactory.getLogger(PluParser.class);

    public static final String ITEM_RECORD = "01";
    public static final String ALTERNATE_RECORD = "02";
    public static final String DEPARTMENT_RECORD = "03";
    public static final String DEPARTMENT_CLASS_RECORD = "04";
    public static final String ITEM_ALTERNATE_DESCRIPTION = "29";
    public static final String ITEM_ADDITIONAL_DATA_RECORD = "36";

    private String deploymentId;
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private int syncGroupSize;

    public PluParser() {
        this.syncGroupSize = 5000;
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

    void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    void setSyncGroupSize(int syncGroupSize) {
        this.syncGroupSize = syncGroupSize;
    }

    public void syncToDatabase(BufferedInputStream pluStream, String merchantId, String locationId) throws Exception {
        LinkedList<DepartmentClassRecord> deptClassRecords = new LinkedList<DepartmentClassRecord>();
        LinkedList<ItemRecord> itemRecords = new LinkedList<ItemRecord>();
        LinkedList<ItemRecord> itemSaleRecords = new LinkedList<ItemRecord>();
        LinkedList<ItemAlternateDescription> itemAltDescriptionRecords = new LinkedList<ItemAlternateDescription>();

        int totalRecordsProcessed = 0;
        logger.info(String.format("(%s) Ingesting PLU file...", deploymentId));

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        // Using less efficient Scanner because BufferedReader hangs
        // indefinitely on sporadic NUL characters near input PLU EoF
        Scanner scanner = new Scanner(pluStream, "UTF-8");
        while (scanner.hasNextLine()) {
            String rpcLine = scanner.nextLine();
            totalRecordsProcessed++;

            if (rpcLine.length() < 2) {
                continue;
            } else {
                String recordType = rpcLine.substring(0, 2);
                switch (recordType) {
                    case DEPARTMENT_CLASS_RECORD:
                        deptClassRecords.add(new DepartmentClassRecord(rpcLine));
                        break;
                    case ITEM_ALTERNATE_DESCRIPTION:
                        // For some reason they are adding unnecessary delete
                        // records before add records
                        String altDescActionType = rpcLine.substring(2, 3);
                        if (altDescActionType.equals(ItemAlternateDescription.ACTION_TYPE_ADD)) {
                            itemAltDescriptionRecords.add(new ItemAlternateDescription(rpcLine));
                        }
                        break;
                    case ITEM_RECORD:
                        ItemRecord itemRecord = new ItemRecord(rpcLine);

                        // We need to treat sale records differently because
                        // they can send multiple for a single item
                        if (itemRecord.getValue("Action Type").equals(ItemRecord.ACTION_TYPE_PLACE_ON_SALE)) {
                            itemSaleRecords.add(itemRecord);
                        } else {
                            itemRecords.add(itemRecord);
                        }
                        break;
                }
            }

            if (!scanner.hasNextLine() || totalRecordsProcessed % syncGroupSize == 0) {
                submitQuery(conn, generateDeptClassSQLUpsert(merchantId, locationId, deptClassRecords));
                submitQuery(conn, generateItemSQLUpsert(merchantId, locationId, itemRecords));
                submitQuery(conn,
                        generateItemAltDescriptionSQLUpsert(merchantId, locationId, itemAltDescriptionRecords));
                submitQuery(conn, generateItemSalesSQLUpsert(merchantId, locationId, itemSaleRecords));
                submitQuery(conn, generateItemSaleEventsSQLUpsert(merchantId, locationId, itemSaleRecords));

                deptClassRecords.clear();
                itemRecords.clear();
                itemSaleRecords.clear();
                itemAltDescriptionRecords.clear();

                logger.info(String.format("(%s) Processed %d records", deploymentId, totalRecordsProcessed));
            }
        }

        scanner.close();

        // Scanner suppresses exceptions
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }

        conn.close();

        logger.info(String.format("(%s) Total records processed: %d", deploymentId, totalRecordsProcessed));
        if (totalRecordsProcessed == 0) {
            throw new Exception("No records processed. Invalid input stream.");
        }
    }

    private void submitQuery(Connection conn, String query) throws SQLException, ClassNotFoundException {
        if (query.isEmpty()) {
            return;
        }

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
    }

    private String generateDeptClassSQLUpsert(String merchantId, String locationId,
            LinkedList<DepartmentClassRecord> deptClassRecords) {
        String updateStatement = "";

        if (deptClassRecords.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_plu_dept_class (deployment, merchantId, locationId, deptNumber, classNumber, description) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (DepartmentClassRecord record : deptClassRecords) {
                String deptNumber = record.getValue("Department Number").trim();
                String classNumber = record.getValue("Class Number").trim();
                String description = record.getValue("Class Description").replaceAll("'", "''").trim();
                updates.add(String.format("('%s', '%s', '%s', '%s', '%s', '%s')", deploymentId, merchantId, locationId,
                        deptNumber, classNumber, description));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE deptNumber=VALUES(deptNumber), classNumber=VALUES(classNumber), description=VALUES(description);";
        }

        return updateStatement;
    }

    // TODO(bhartard): Refactor into a seprate SQLstatement object, use prepared
    // statements?
    private String generateItemSQLUpsert(String merchantId, String locationId, LinkedList<ItemRecord> itemRecords) {
        String updateStatement = "";

        if (itemRecords.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_plu_items ("
                    + "deployment, merchantId, locationId, itemNumber, deptNumber, "
                    + "classNumber, styleNumber, activateDate, deactivateDate, description, "
                    + "retailPrice, originalPrice) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (ItemRecord record : itemRecords) {
                String itemNumber = record.getValue("Item Number").trim();
                String deptNumber = record.getValue("Department Number").trim();
                String classNumber = record.getValue("Class Number").trim();
                String styleNumber = record.getValue("Style Number").replaceAll("'", "''").trim();
                String activateDate = record.getValue("Activate Date").trim();
                String deactivateDate = record.getValue("Deactivate Date").trim();
                String description = record.getValue("Description").replaceAll("'", "''").trim();
                String retailPrice = record.getValue("Retail Price").trim();
                String originalPrice = record.getValue("Original Price").trim();

                updates.add(String.format("('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                        deploymentId, merchantId, locationId, itemNumber, deptNumber, classNumber, styleNumber,
                        activateDate, deactivateDate, description, retailPrice, originalPrice));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE deptNumber=VALUES(deptNumber), classNumber=VALUES(classNumber), "
                    + "styleNumber=VALUES(styleNumber), activateDate=VALUES(activateDate), deactivateDate=VALUES(deactivateDate), "
                    + "description=VALUES(description), retailPrice=VALUES(retailPrice), originalPrice=VALUES(originalPrice);";
        }

        return updateStatement;
    }

    private String generateItemAltDescriptionSQLUpsert(String merchantId, String locationId,
            LinkedList<ItemAlternateDescription> itemAltDescriptionRecords) {
        String updateStatement = "";

        if (itemAltDescriptionRecords.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_plu_items (deployment, merchantId, locationId, itemNumber, alternateDescription) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (ItemAlternateDescription record : itemAltDescriptionRecords) {
                String itemNumber = record.getValue("Item Number").trim();
                String altDescription = record.getValue("Item Alternate Description").replaceAll("'", "''").trim();

                updates.add(String.format("('%s', '%s', '%s', '%s', '%s')", deploymentId, merchantId, locationId,
                        itemNumber, altDescription));
            }

            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE alternateDescription=VALUES(alternateDescription);";
        }

        return updateStatement;
    }

    private String generateItemSalesSQLUpsert(String merchantId, String locationId,
            LinkedList<ItemRecord> itemSaleRecords) {
        String updateStatement = "";

        if (itemSaleRecords.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_plu_items ("
                    + "deployment, merchantId, locationId, itemNumber, deptNumber,  classNumber, "
                    + "styleNumber, description, retailPrice, originalPrice, salePrice, "
                    + "dateSaleBegins, dateSaleEnds, timeSaleBegins, timeSaleEnds) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (ItemRecord record : itemSaleRecords) {
                String itemNumber = record.getValue("Item Number").trim();
                String deptNumber = record.getValue("Department Number").trim();
                String classNumber = record.getValue("Class Number").trim();
                String styleNumber = record.getValue("Style Number").replaceAll("'", "''").trim();
                String description = record.getValue("Description").replaceAll("'", "''").trim();
                String retailPrice = record.getValue("Retail Price").trim();
                String originalPrice = record.getValue("Original Price").trim();
                String salePrice = record.getValue("Sale Price").trim();
                String dateSaleBegins = record.getValue("Date Sale/Action Begins").trim();
                String dateSaleEnds = record.getValue("Date Sale/Action Ends").trim();
                String timeSaleBegins = record.getValue("Time Sale Begins").trim();
                String timeSaleEnds = record.getValue("Time Sale Ends").trim();

                updates.add(String.format(
                        "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                        deploymentId, merchantId, locationId, itemNumber, deptNumber, classNumber, styleNumber,
                        description, retailPrice, originalPrice, salePrice, dateSaleBegins, dateSaleEnds,
                        timeSaleBegins, timeSaleEnds));
            }

            // Only update sale fields when the item record already exists
            updateStatement = appendWithListIterator(updateStatement, updates);
            updateStatement += " ON DUPLICATE KEY UPDATE salePrice=VALUES(salePrice), dateSaleBegins=VALUES(dateSaleBegins), "
                    + "dateSaleEnds=VALUES(dateSaleEnds), timeSaleBegins=VALUES(timeSaleBegins), timeSaleEnds=VALUES(timeSaleEnds);";
        }

        return updateStatement;
    }

    private String generateItemSaleEventsSQLUpsert(String merchantId, String locationId,
            LinkedList<ItemRecord> itemSaleRecords) {
        String updateStatement = "";

        if (itemSaleRecords.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_plu_sale_events (deployment, merchantId, locationId, itemNumber, salePrice, "
                    + "dateSaleBegins, dateSaleEnds, timeSaleBegins, timeSaleEnds) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (ItemRecord record : itemSaleRecords) {
                String itemNumber = record.getValue("Item Number").trim();
                String salePrice = record.getValue("Sale Price").trim();
                String dateSaleBegins = record.getValue("Date Sale/Action Begins").trim();
                String dateSaleEnds = record.getValue("Date Sale/Action Ends").trim();
                String timeSaleBegins = record.getValue("Time Sale Begins").trim();
                String timeSaleEnds = record.getValue("Time Sale Ends").trim();

                updates.add(String.format("('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')", deploymentId,
                        merchantId, locationId, itemNumber, salePrice, dateSaleBegins, dateSaleEnds, timeSaleBegins,
                        timeSaleEnds));
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
