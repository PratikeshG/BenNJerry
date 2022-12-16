package benjerrys;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.squareup.connect.Employee;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;

import util.Constants;
import util.SftpApi;
import util.SquarePayload;
import util.TimeManager;

public class GenerateLocationReportCallable implements Callable {
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Value("${benjerrys.sftp.ip}")
    private String sftpHost;
    @Value("${benjerrys.sftp.port}")
    private int sftpPort;
    @Value("${benjerrys.sftp.user}")
    private String sftpUser;
    @Value("${benjerrys.sftp.password}")
    private String sftpPassword;
    @Value("${benjerrys.sftp.path}")
    private String sftpPath;

    private static String VAR_LOCATION_OVERRIDE = "locationOverride";
    private static String VAR_DATE_MONTH_YEAR = "dateMonthYear";
    private static String LOCATION_OPERATOR_ROLE_ID = "SHN2xdVqy-kkP0rTn4Fr";

    private static String REPORT_ENCODING = "text/plain; charset=UTF-8";
    private static String REPORT_NAME_FORMAT = "%s - %s - %s.csv";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        Location location = (Location) message.getPayload();

        SquarePayload squarePayload = (SquarePayload) message.getProperty(Constants.SQUARE_PAYLOAD,
                PropertyScope.SESSION);
        String locationOverride = message.getProperty(VAR_LOCATION_OVERRIDE, PropertyScope.SESSION);
        Employee[] employees = (Employee[]) message.getProperty(Constants.EMPLOYEES, PropertyScope.SESSION);
        int monthOffset = Integer.parseInt(message.getProperty(Constants.OFFSET, PropertyScope.SESSION));
        String dateMonthYear = message.getProperty(VAR_DATE_MONTH_YEAR, PropertyScope.SESSION);
        String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);

        LocationReportSummaryPayload summary = new LocationReportSummaryPayload(location.getName());

        // Skip location processing?
        if (isLocationSkipped(location, locationOverride)) {
            return summary;
        }

        List<String> emailRecipients = setLocationEmailRecipients(message, location, employees);

        // Retrieve location report data
        MonthlyReportBuilder reportBuilder = getReportBuilderForLocation(apiUrl, location, squarePayload, dateMonthYear,
                monthOffset);
        reportBuilder.buildReports();

        // Skip email (and attachments) if there are no transactions or recipients
        if (isEmailSkipped(reportBuilder, emailRecipients)) {
            return summary;
        }

        uploadReportsToSftp(reportBuilder, location.getName(), dateMonthYear);

        attachMonthlyCalculationReport(eventContext, reportBuilder, location.getName(), dateMonthYear);
        attachMonthlySummaryReport(eventContext, reportBuilder, location.getName(), dateMonthYear);

        summary.setTotalTransactions(reportBuilder.getTotalTransactions());
        summary.setTotalRecipients(emailRecipients.size());
        summary.isProcessed(true);

        return summary;
    }

    private void uploadReportsToSftp(MonthlyReportBuilder reportBuilder, String locationName, String dateMonthYear)
            throws IOException, JSchException, SftpException {

        Session session = SftpApi.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);

        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.cd(sftpPath);

        InputStream calcReportStream = new ByteArrayInputStream(reportBuilder.generateCalculationReport().getBytes());
        String calcReportName = generateReportNameString("Calc", locationName, dateMonthYear);
        sftpChannel.put(calcReportStream, calcReportName);

        InputStream summaryReportStream = new ByteArrayInputStream(reportBuilder.generateSummaryReport().getBytes());
        String summaryReportName = generateReportNameString("Report", locationName, dateMonthYear);
        sftpChannel.put(summaryReportStream, summaryReportName);

        calcReportStream.close();
        summaryReportStream.close();

        sftpChannel.disconnect();
        session.disconnect();
    }

    private List<String> setLocationEmailRecipients(MuleMessage message, Location location, Employee[] employees) {
        List<String> emailRecipients = reportRecipients(location, employees);
        message.setProperty("locationRecipients", String.join(",", emailRecipients), PropertyScope.INVOCATION);

        return emailRecipients;
    }

    private void attachMonthlyCalculationReport(MuleEventContext eventContext, MonthlyReportBuilder reportBuilder,
            String locationName, String dateMonthYear) throws Exception {
        String calculationReport = reportBuilder.generateCalculationReport();
        DataHandler dataHandler = new DataHandler(calculationReport, REPORT_ENCODING);
        eventContext.getMessage().addOutboundAttachment(generateReportNameString("Calc", locationName, dateMonthYear),
                dataHandler);
    }

    private void attachMonthlySummaryReport(MuleEventContext eventContext, MonthlyReportBuilder reportBuilder,
            String locationName, String dateMonthYear) throws Exception {
        String summarySupport = reportBuilder.generateSummaryReport();
        DataHandler dataHandler = new DataHandler(summarySupport, REPORT_ENCODING);
        eventContext.getMessage().addOutboundAttachment(generateReportNameString("Report", locationName, dateMonthYear),
                dataHandler);
    }

    private String generateReportNameString(String reportName, String locationName, String dateMonthYear) {
        String name = String.format(REPORT_NAME_FORMAT, locationName, reportName, dateMonthYear);
        return name.replace("*", "");
    }

    private boolean isEmailSkipped(MonthlyReportBuilder reportBuilder, List<String> emailRecipients) {
        if (reportBuilder.getTotalTransactions() < 1 || emailRecipients.size() < 1) {
            return true;
        }
        return false;
    }

    private boolean isLocationSkipped(Location location, String locationOverride) {
        // check for override
        if (locationOverride.length() > 0 && !location.getId().equals(locationOverride)) {
            return true;
        }

        // not active
        if (!location.getStatus().equals(Location.LOCATION_STATUS_ACTIVE)) {
            return true;
        }

        return false;
    }

    private MonthlyReportBuilder getReportBuilderForLocation(String apiUrl, Location location,
            SquarePayload squarePayload, String dateMonthYear, int monthOffset) throws Exception {

        String accessToken = squarePayload.getAccessToken(this.encryptionKey);
        Map<String, String> dateParams = TimeManager.getPastMonthInterval(monthOffset, location.getTimezone());
        String locationId = location.getId();
        dateParams.put("location_id", locationId);

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, accessToken, "2022-12-14");

        MonthlyReportBuilder reportBuilder = new MonthlyReportBuilder(location.getName(), dateMonthYear,
                dateParams.get(Constants.BEGIN_TIME), dateParams.get(Constants.END_TIME));

        // Payments map
        Payment[] paymentsArray = clientV2.payments().list(dateParams);
        Map<String, Payment> paymentsMap = new HashMap<>();
        Arrays.stream(paymentsArray).forEach(payment -> paymentsMap.put(payment.getId(), payment));
        reportBuilder.setPayments(paymentsMap);

        // PaymentRefunds
        reportBuilder.setPaymentRefunds(clientV2.refunds().listPaymentRefunds(dateParams));

        // build the Orders map
        Map<String, Order> orders = new HashMap<String, Order>();

        HashSet<String> orderIdSet = new HashSet<String>();

        // get OrderIds from both payments and refunds
        for(Payment payment : reportBuilder.getPayments().values()) {
        	orderIdSet.add(payment.getOrderId());
        }
        for(PaymentRefund refund : reportBuilder.getPaymentRefunds()) {
        	orderIdSet.add(refund.getOrderId());
        }

        String[] orderIds = new String[orderIdSet.size()];
        orderIdSet.toArray(orderIds);
        Order[] ordersArray = clientV2.orders().batchRetrieve(locationId, orderIds);

        // call the catalog API three times to get the categoryData
        List<String> itemVariationIdList = new ArrayList<String>();

        for(Order order : ordersArray) {
        	if(order != null && order.getId() != null) {
            	orders.put(order.getId(), order);
            	OrderLineItem[] items = order.getLineItems();
            	if(items != null) {
            		for(OrderLineItem orderLineItem : items) {
            			if(orderLineItem != null) {
            				String catalogObjectId = orderLineItem.getCatalogObjectId();
                    		if(catalogObjectId != null) {
                    			// add the catalogObjectId to a list for a batchRetrieve of itemVariations
                    			itemVariationIdList.add(catalogObjectId);
                    		}
            			}
                	}
            	}
        	}
        }

        reportBuilder.setOrders(orders);

        String[] itemVariationIds = new String[itemVariationIdList.size()];
        itemVariationIdList.toArray(itemVariationIds);

        // retrieve item variations
        CatalogObject[] itemVariations = clientV2.catalog().batchRetrieve(itemVariationIds);

        // set item variations map
        Map<String, CatalogObject> itemVariationsMap = new HashMap<>();
        Arrays.stream(itemVariations).forEach(itemVariation -> itemVariationsMap.put(itemVariation.getId(), itemVariation));
        reportBuilder.setItemVariations(itemVariationsMap);

        // retrieve catalog items from the item variations
        List<String> itemIdList = new ArrayList<String>();
        for(CatalogObject itemVariation : itemVariations) {
        	if(itemVariation.getItemVariationData() != null) {
        		String itemId = itemVariation.getItemVariationData().getItemId();
            	if(itemId != null) {
            		itemIdList.add(itemId);
            	}
        	}
        }
        String[] itemIds = new String[itemIdList.size()];
        itemIdList.toArray(itemIds);

        //retrieve items
        CatalogObject[] items = clientV2.catalog().batchRetrieve(itemIds);

        // set items map
        Map<String, CatalogObject> itemsMap = new HashMap<>();
        Arrays.stream(items).forEach(item -> itemsMap.put(item.getId(), item));
        reportBuilder.setItems(itemsMap);

        // finally, retrieve categories from the catalog items
        List<String> categoryIdList = new ArrayList<String>();
        for(CatalogObject item : items) {
        	if(item.getItemData() != null) {
        		String categoryId = item.getItemData().getCategoryId();
        		if(categoryId != null) {
        			categoryIdList.add(categoryId);
        		}
        	}
        }
        String[] categoryIds = new String[categoryIdList.size()];
        categoryIdList.toArray(categoryIds);

        // retrieve categoryData
        CatalogObject[] categories = clientV2.catalog().batchRetrieve(categoryIds);

        // set categories map
        Map<String, CatalogObject> categoriesMap = new HashMap<>();
        Arrays.stream(categories).forEach(category -> categoriesMap.put(category.getId(), category));
        reportBuilder.setCategories(categoriesMap);

        return reportBuilder;
    }

    private List<String> reportRecipients(Location location, Employee[] employees) {
        ArrayList<String> recipientEmails = new ArrayList<String>();

        if (employees != null) {
            for (Employee employee : employees) {
                if (employeeAtLocation(employee, location) && employeeIsLocationOperator(employee)
                        && employee.getStatus().equals(Employee.STATUS_ACTIVE) && employee.getEmail() != null
                        && employee.getEmail().length() > 0) {
                    recipientEmails.add(employee.getEmail());
                }
            }
        }

        return recipientEmails;
    }

    private boolean employeeIsLocationOperator(Employee employee) {
        if (employee.getRoleIds() != null) {
            for (String roleId : employee.getRoleIds()) {
                if (roleId.equals(LOCATION_OPERATOR_ROLE_ID)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean employeeAtLocation(Employee employee, Location location) {
        if (employee != null && employee.getAuthorizedLocationIds() != null) {
            for (String authorizedLocationId : employee.getAuthorizedLocationIds()) {
                if (authorizedLocationId.equals(location.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
