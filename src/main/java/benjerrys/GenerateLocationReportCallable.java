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
import java.util.Set;

import javax.activation.DataHandler;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.squareup.connect.Employee;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.SquareClientV2;

import util.ConnectV2MigrationHelper;
import util.Constants;
import util.SftpApi;
import util.SquarePayload;
import util.TimeManager;

public class GenerateLocationReportCallable {
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

        Order[] orders = ConnectV2MigrationHelper.getOrders(clientV2, locationId, dateParams);
		Map<String, Order> ordersMap = new HashMap<>();
		//pratikesh
		//Arrays.stream(orders).forEach(order -> ordersMap.put(order.getId(), order));

		PaymentRefund[] refunds = clientV2.refunds().listPaymentRefunds(dateParams);

		Map<String, Payment> paymentsMap = new HashMap<>();
		List<String> orderIdsFromPaymentRefunds = new ArrayList<String>();
		if(refunds != null) {
		    for(PaymentRefund refund : refunds) {
		    	if(refund != null && refund.getPaymentId() != null) {
		    		// not many refunds, better to do one-offs than list entire payments
		    		Payment payment = clientV2.payments().get(refund.getPaymentId());
		    		paymentsMap.put(refund.getPaymentId(), payment);
		    		// check if the order for the payment from the refund exists - the order might have been out of the UPDATED_AT time frame
		    		if(!ordersMap.containsKey(payment.getOrderId())) {
		    			orderIdsFromPaymentRefunds.add(payment.getOrderId());
		    		}
		      	}
		    }
		}

		Order[] refundOrders = clientV2.orders().batchRetrieve(locationId, orderIdsFromPaymentRefunds.toArray(new String[0]));
		for(Order order : refundOrders) {
			ordersMap.put(order.getId(), order);
		}

		reportBuilder.setPayments(paymentsMap);
        reportBuilder.setPaymentRefunds(refunds);
        reportBuilder.setOrders(ordersMap);

        // go through orders and extract ItemVariationIds
        String[] itemVariationIds = getItemVariationIds(orders);

        // get catalogMap which consists of ITEM and ITEM_VARIATION data
	  	Map<String, CatalogObject> catalogMap = getCatalogMap(itemVariationIds, clientV2);
	  	// list categories (fast)
	  	Map<String, CatalogObject> categoriesMap = getCategoriesMap(clientV2);

	  	// map itemVariationId -> category
	  	Map<String, CatalogObject> itemVariationIdToCategory = getItemVariationIdToCategory(itemVariationIds, catalogMap, categoriesMap);

		reportBuilder.setCategories(itemVariationIdToCategory);

        return reportBuilder;
    }

    private Map<String, CatalogObject> getCatalogMap(String[] itemVariationIds, SquareClientV2 clientV2) throws Exception {
        CatalogObject[] relatedItems = clientV2.catalog().batchRetrieve(itemVariationIds, true);
	  	Map<String, CatalogObject> catalogMap = new HashMap<>();
	  	//pratikesh
	  	//Arrays.stream(relatedItems).forEach(relatedItem -> catalogMap.put(relatedItem.getId(), relatedItem));
	  	return catalogMap;
    }

    private String[] getItemVariationIds(Order[] orders) {
    	Set<String> set = new HashSet<String>();
    	if(orders != null) {
    		for(Order order : orders) {
        		if(order != null && order.getLineItems() != null) {
    	            for(OrderLineItem orderLineItem : order.getLineItems()) {
        	        	if(orderLineItem != null) {
        	        		String catalogObjectId = orderLineItem.getCatalogObjectId();
    	              		if(catalogObjectId != null) {
    	              			// the catalogObjectId from an orderLineItem translates to the itemVariationId
    	              			set.add(catalogObjectId);
    	              		}
    	      			}
    	        	}
    	        }
    		}
    	}
    	String[] ids = new String[set.size()];
        set.toArray(ids);

    	return ids;
    }

    private Map<String, CatalogObject> getCategoriesMap(SquareClientV2 clientV2) throws Exception {
    	Map<String, CatalogObject> categoriesMap = new HashMap<>();
	  	CatalogObject[] categories = clientV2.catalog().listCategories();
	  	//pratikesh
        //Arrays.stream(categories).forEach(category -> categoriesMap.put(category.getId(), category));
        return categoriesMap;
    }

    private Map<String, CatalogObject> getItemVariationIdToCategory(String[] itemVariationIdList,
    		Map<String, CatalogObject> catalogMap,
    		Map<String, CatalogObject> categoriesMap) {
	  	Map<String, CatalogObject> itemVariationIdToCategory = new HashMap<>();
    	if(itemVariationIdList != null) {
    		for(String itemVariationId : itemVariationIdList) {
    			CatalogObject itemVariation = catalogMap.get(itemVariationId);
    			if(itemVariation != null && itemVariation.getItemVariationData() != null && itemVariation.getItemVariationData().getItemId() != null) {
    				CatalogObject item = catalogMap.get(itemVariation.getItemVariationData().getItemId());
    				if(item != null && item.getItemData() != null && item.getItemData().getCategoryId() != null) {
    					CatalogObject category = categoriesMap.get(item.getItemData().getCategoryId());
    			        itemVariationIdToCategory.put(itemVariationId, category);
    				}
    			}
    		}
    	}

    	return itemVariationIdToCategory;
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
