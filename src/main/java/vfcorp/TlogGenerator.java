package vfcorp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.CustomerGroup;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;

import util.ConnectV2MigrationHelper;
import util.DbConnection;
import util.SequentialRecord;
import util.SequentialRecordsApi;
import util.SquarePayload;
import util.TimeManager;

public class TlogGenerator implements Callable {
    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    private static Logger logger = LoggerFactory.getLogger(TlogGenerator.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        SquarePayload squarePayload = (SquarePayload) message.getPayload();

        TlogGeneratorPayload tlogGeneratorPayload = new TlogGeneratorPayload();
        tlogGeneratorPayload.setSquarePayload(squarePayload);

        String locationId = squarePayload.getLocationId();
        String storeId = message.getProperty("storeId", PropertyScope.INVOCATION);

        String deployment = (String) message.getProperty("deploymentId", PropertyScope.SESSION);
        String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);
        String tlogType = message.getProperty("tlogType", PropertyScope.SESSION);

        boolean allowCashTransactions = message.getProperty("allowCashTransactions", PropertyScope.INVOCATION)
                .equals("true") ? true : false;
        boolean trackPriceOverrides = message.getProperty("trackPriceOverrides", PropertyScope.INVOCATION)
                .equals("true") ? true : false;
        int itemNumberLookupLength = Integer
                .parseInt(message.getProperty("itemNumberLookupLength", PropertyScope.INVOCATION));

        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.INVOCATION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.INVOCATION));
        tlogGeneratorPayload.setParams(TimeManager.getPastDayInterval(range, offset, timeZone));

        boolean createCloseRecords = message.getProperty("createCloseRecords", PropertyScope.SESSION).equals("true")
                ? true
                : false;

        SquareClientV2 squareV2Client = new SquareClientV2(apiUrl,
                tlogGeneratorPayload.getSquarePayload().getAccessToken(encryptionKey));
        squareV2Client.setLogInfo(tlogGeneratorPayload.getSquarePayload().getMerchantId() + " - " + locationId);

        Location location = squareV2Client.locations().retrieve(locationId);
        if (location == null) {
            return null;
        }

        // Loyalty settings
        boolean customerLoyaltyEnabled = message.getProperty("enableCustomerLoyalty", PropertyScope.INVOCATION)
                .equals("true") ? true : false;
        String CUSTOMER_GROUP_NEW = (String) message.getProperty("customerGroupNew", PropertyScope.INVOCATION);
        String CUSTOMER_GROUP_EXISTING = (String) message.getProperty("customerGroupExisting",
                PropertyScope.INVOCATION);

        // Establish DB connection
        DbConnection conn = new DbConnection(databaseUrl, databaseUser, databasePassword);
        SequentialRecordsApi sequentialRecordsApi = new SequentialRecordsApi(conn.getConnection());
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn.getConnection());

        // Sequential Order Numbers
        Map<String, Integer> nextDeviceRecordNumbers = new HashMap<String, Integer>();
        ArrayList<Map<String, String>> records = sequentialRecordsApi.queryRecordNumbersForLocation(location.getId());
        for (Map<String, String> record : records) {
            nextDeviceRecordNumbers.put(record.get("deviceId"), Integer.parseInt(record.get("lastRecordNumber")) + 1);
        }

        // How many device codes are configured for this business?
        int totalConfiguredDevices = 2;

        // Employees
        tlogGeneratorPayload.setEmployees(databaseApi.getEmployeeIdsForBrand(getBrandFromDeployment(deployment)));

        Map<String, String> params = tlogGeneratorPayload.getParams();

        Payment[] payments = ConnectV2MigrationHelper.getPaymentsV2(squareV2Client, locationId, params);
        Order[] orders = ConnectV2MigrationHelper.getOrders(squareV2Client, locationId, params, allowCashTransactions);
        Map<String, Payment> tenderToPayment = ConnectV2MigrationHelper.getTenderToPayment(orders, payments, squareV2Client, params);
        Map<String, CatalogObject> catalog = ConnectV2MigrationHelper.getCatalogObjectsForOrder(squareV2Client, orders);

        // Get existing record numbers for Orders
        List<String> recordIds = new ArrayList<String>();

        // get closing record for each device (more than 1)
        String recordDate = TimeManager.toSimpleDateTimeInTimeZone(tlogGeneratorPayload.getParams().get("end_time"),
                timeZone, "yyyy-MM-dd");
        for (int i = 99; i > 99 - Math.max(totalConfiguredDevices, 2); i--) {
            String closingRecordId = "close-" + locationId + "-" + recordDate + "-" + String.format("%03d", i);
            recordIds.add(closingRecordId);
        }

        for(Order order : orders) {
        	recordIds.add(order.getId());
        }

        Map<String, SequentialRecord> existingRecordNumbersByDevice = new HashMap<String, SequentialRecord>();
        ArrayList<Map<String, String>> existingSequentialRecords = sequentialRecordsApi.queryRecordsById(recordIds);

        for (Map<String, String> record : existingSequentialRecords) {
            SequentialRecord sr = new SequentialRecord();
            sr.setLocationId(record.get("locationId"));
            sr.setRecordId(record.get("recordId"));
            sr.setDeviceId(record.get("deviceId"));
            sr.setRecordNumber(Integer.parseInt(record.get("recordNumber")));
            sr.setRecordCreatedAt(record.get("recordCreatedAt"));

            existingRecordNumbersByDevice.put(sr.getRecordId(), sr);
        }

        List<Order> ordersInPeriodToProcess = new ArrayList<Order>();
        for (Order order : orders) {

            // only process new sales (during range) for SAP trickle flows
            if (!tlogType.equals("SAP") || !existingRecordNumbersByDevice.containsKey(order.getId())) {
                ordersInPeriodToProcess.add(order);
            }
        }

        // Get PCM counters
        Map<String, Integer> nextPreferredCustomerNumbers = new HashMap<String, Integer>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preferredCustomerCounters = (List<Map<String, Object>>) message
                .getProperty("preferredCustomerCounters", PropertyScope.SESSION);
        for (Map<String, Object> customerCounter : preferredCustomerCounters) {
            String registerId = (String) customerCounter.get("registerId");
            int nextNumber = (int) customerCounter.get("nextPreferredCustomerNumber");
            nextPreferredCustomerNumbers.put(registerId, nextNumber);
        }

        // Get V2 orders/customers
        Map<String, Customer> customerPaymentCache = new HashMap<String, Customer>();
        if (customerLoyaltyEnabled) {
            // Get customer data

            for (Order order : orders) {
                if (order.getTenders() != null) {
                    for (Tender tender : order.getTenders()) {
                        if (tender.getCustomerId() != null) {
                            Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());

                            // Get customer's loyalty group status
                            boolean loyaltyCustomer = false;
                            if (customer.getGroups() != null) {
                                for (CustomerGroup group : customer.getGroups()) {
                                    if (group.getId().equals(CUSTOMER_GROUP_NEW)
                                            || group.getId().equals(CUSTOMER_GROUP_EXISTING)) {
                                        loyaltyCustomer = true;
                                    }
                                }
                            }

                            boolean customerProvidedEmail = customer.getEmailAddress() != null
                                    && customer.getEmailAddress().length() > 0;
                            boolean customerProvidedPhone = customer.getPhoneNumber() != null
                                    && customer.getPhoneNumber().length() > 0;

                            /*
                             * Set preferredCustomerId (if necessary)
                             *
                             * Square currently only returns a customer record when
                             * the merchant adds the customer to the sale. This
                             * could possible change in the future, so may need to
                             * verify they are on the transactions as a loyalty
                             * customer. For now, we are just going to verify the
                             * customer has provided an email
                             */
                            if (tenderToPayment.containsKey(tender.getId()) && (customerProvidedEmail || customerProvidedPhone) && (customer.getReferenceId() == null
                                    || customer.getReferenceId().length() != LOYALTY_CUSTOMER_ID_LENGTH)) {
                                String newId = generateNewPreferredCustomerId(nextPreferredCustomerNumbers,
                                        tenderToPayment.get(tender.getId()), storeId);

                                customer.setReferenceId(newId);
                                customer = squareV2Client.customers().update(customer);

                                System.out.println("New Customer ID: " + customer.getReferenceId());
                            }

                            // For now, we only track the customer if they have
                            // provided an email address or phone number
                            if (customerProvidedEmail || customerProvidedPhone) {
                                customer.setCompanyName(loyaltyCustomer ? "1" : "0");

                                customerPaymentCache.put(tender.getId(), customer);
                                customerPaymentCache.put(order.getId(), customer);
                            }
                        }
                    }
                }
            }
        }
        tlogGeneratorPayload.setCustomers(customerPaymentCache);

        String sqlUpdate = generatePreferredCustomerSQLUpdate(nextPreferredCustomerNumbers, deployment, storeId);
        message.setProperty("preferredCustomerSQLUpdate", sqlUpdate.length() > 0 ? true : false,
                PropertyScope.INVOCATION);
        message.setProperty("preferredCustomerSQLStatement", sqlUpdate, PropertyScope.INVOCATION);

        Tlog tlog = new Tlog();
        tlog.setNextRecordNumbers(nextDeviceRecordNumbers);
        tlog.setRecordNumberCache(existingRecordNumbersByDevice);
        tlog.setItemNumberLookupLength(itemNumberLookupLength);
        tlog.setDeployment(deployment);
        tlog.setTimeZoneId(timeZone);
        tlog.setType(tlogType);
        tlog.trackPriceOverrides(trackPriceOverrides);
        tlog.createCloseRecords(createCloseRecords);
        tlog.setTotalConfiguredDevices(totalConfiguredDevices);

        tlog.parse(location, orders,
        ordersInPeriodToProcess.toArray(new Order[0]), tlogGeneratorPayload.getEmployees(),
        tlogGeneratorPayload.getCustomers(), tlogGeneratorPayload.getParams().get("end_time"), tenderToPayment, catalog);


	    message.setProperty("vfcorpStoreNumber", Util.getStoreNumber(location.getName()), PropertyScope.INVOCATION);
	    message.setProperty("tlog", tlog.toString(), PropertyScope.INVOCATION);

	    List<SequentialRecord> srs = new ArrayList<SequentialRecord>(tlog.getRecordNumberCache().values());
	    sequentialRecordsApi.updateRecordNumbersForLocation(srs, location.getId());
	    sequentialRecordsApi.close();
	    return true;
    }

    private String generateNewPreferredCustomerId(Map<String, Integer> nextPreferredCustomerIds,
    		Payment customerPayment, String storeId) {

        String deviceName = (customerPayment != null && customerPayment.getDeviceDetails() != null)
                ? customerPayment.getDeviceDetails().getDeviceName()
                : null;
        String registerNumber = Util.getRegisterNumber(deviceName);

        int nextCustomerNumber = nextPreferredCustomerIds.getOrDefault(registerNumber, 1);
        int mod = 0; // Not currently performing any modulus operation

        // Update for next customer
        int updatedCounter = (nextCustomerNumber + 1 > 9999999) ? 1 : nextCustomerNumber + 1;
        nextPreferredCustomerIds.put(registerNumber, updatedCounter);

        return String.format("%s%s5%s%s", storeId, registerNumber, String.format("%07d", nextCustomerNumber), mod);
    }

    private String generatePreferredCustomerSQLUpdate(Map<String, Integer> nextPreferredCustomerIds, String deployment,
            String storeId) {
        String updateStatement = "";

        if (nextPreferredCustomerIds.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_preferred_customer_counter (deployment, storeId, registerId, nextPreferredCustomerNumber) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (Map.Entry<String, Integer> entry : nextPreferredCustomerIds.entrySet()) {
                updates.add(
                        String.format("('%s', '%s', '%s', %d)", deployment, storeId, entry.getKey(), entry.getValue()));
            }

            Iterator<String> i = updates.iterator();
            if (i.hasNext()) {
                updateStatement += i.next();
                while (i.hasNext()) {
                    updateStatement += ", " + i.next();
                }
            }

            updateStatement += " ON DUPLICATE KEY UPDATE nextPreferredCustomerNumber=VALUES(nextPreferredCustomerNumber);";
        }

        return updateStatement;
    }

    private String getBrandFromDeployment(String deployment) {
        return deployment.split("-", 3)[0] + "-" + deployment.split("-", 3)[1];
    }
}
