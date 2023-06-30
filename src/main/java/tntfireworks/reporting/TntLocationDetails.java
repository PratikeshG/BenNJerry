package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.Payout;
import com.squareup.connect.v2.PayoutEntry;
import com.squareup.connect.v2.SearchOrdersDateTimeFilter;
import com.squareup.connect.v2.SearchOrdersFilter;
import com.squareup.connect.v2.SearchOrdersQuery;
import com.squareup.connect.v2.SearchOrdersSort;
import com.squareup.connect.v2.SearchOrdersStateFilter;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.TimeRange;

import tntfireworks.TntDatabaseApi;

public class TntLocationDetails {
	protected String locationNumber;
	protected String locationName;
	protected String city;
	protected String state;
	protected String zip;
	protected String rbu;
	protected String saName;
	protected String saNumber;
	protected String sqLocationId;
	protected String sqLocationName;
	protected String sqLocationTimeZone;
    private static Logger logger = LoggerFactory.getLogger(TntLocationDetails.class);

	public TntLocationDetails(List<Map<String, String>> dbLocationRows, Location location) {
		// init square location values
		this.sqLocationId = location.getId();
		this.sqLocationName = location.getName();
		this.sqLocationTimeZone = location.getTimezone();

		// init TNT location values
		this.locationName = location.getName().replaceAll(",", "");
		this.locationNumber = findLocationNumber(locationName);
		this.rbu = "";
		this.city = "";
		this.state = "";
		this.zip = "";
		this.saName = "";
		this.saNumber = "";

		for (Map<String, String> row : dbLocationRows) {
			if (locationNumber.equals(row.get(TntDatabaseApi.DB_LOCATION_LOCATION_NUMBER_COLUMN))) {
				this.city = row.get(TntDatabaseApi.DB_LOCATION_CITY_COLUMN);
				this.state = row.get(TntDatabaseApi.DB_LOCATION_STATE_COLUMN);
				this.rbu = row.get(TntDatabaseApi.DB_LOCATION_RBU_COLUMN);
				this.zip = row.get(TntDatabaseApi.DB_LOCATION_ZIP_COLUMN);
				this.saName = row.get(TntDatabaseApi.DB_LOCATION_SA_NAME_COLUMN);
				this.saNumber = row.get(TntDatabaseApi.DB_LOCATION_SA_NUMBER_COLUMN);
			}
		}
	}

	public static boolean isTntLocation(List<Map<String, String>> dbLocationRows, String locationName) {
		for (Map<String, String> row : dbLocationRows) {
			if (findLocationNumber(locationName).equals(row.get(TntDatabaseApi.DB_LOCATION_LOCATION_NUMBER_COLUMN))) {
				return true;
			}
		}
		return false;
	}

	public static Payment[] getPaymentsV2(SquareClientV2 squareClientV2, String locationId, Map<String, String> params) throws Exception {
		params.put("location_id", locationId);
		// All v2 payments are tied to exactly one tender, no filtering needed
		return squareClientV2.payments().list(params);
	}

	public static PaymentRefund[] getPaymentRefunds(SquareClientV2 squareClientV2, Map<String, String> params) throws Exception {
		return squareClientV2.refunds().listPaymentRefunds(params);
	}

	/*
	 * Searches orders for a given time period. We include OPEN orders in this search because
	 * OPEN orders can also have valid tenders. In order to search OPEN orders, sort field must
	 * be set to CREATED_AT instead of CLOSED_AT because OPEN orders do not have a CLOSED_AT time stamp.
	 */
	public static Order[] getOrders(SquareClientV2 squareClientV2, String locationId, Map<String, String> params) throws Exception {
		SearchOrdersQuery orderQuery = new SearchOrdersQuery();
		SearchOrdersFilter searchFilter = new SearchOrdersFilter();
        SearchOrdersSort searchSort = new SearchOrdersSort();
        orderQuery.setFilter(searchFilter);
        orderQuery.setSort(searchSort);

        SearchOrdersStateFilter stateFilter = new SearchOrdersStateFilter();
        stateFilter.setStates(new String[] { "COMPLETED", "OPEN" });
        searchFilter.setStateFilter(stateFilter);

        SearchOrdersDateTimeFilter dateFilter = new SearchOrdersDateTimeFilter();
        TimeRange timeRange = new TimeRange();
        timeRange.setStartAt(params.get(util.Constants.BEGIN_TIME));
        timeRange.setEndAt(params.get(util.Constants.END_TIME));
        dateFilter.setCreatedAt(timeRange);
        searchFilter.setDateTimeFilter(dateFilter);

        searchSort.setSortField("CREATED_AT");
        searchSort.setSortOrder(params.get(util.Constants.SORT_ORDER_V2));


        Order[] allOrders = squareClientV2.orders().search(locationId, orderQuery);
        List<Order> orders = new ArrayList<Order>();
        for(Order order : allOrders) {
        	if(hasValidTender(order)) {
        		orders.add(order);
        	}
        }
        return orders.toArray(new Order[0]);
    }

	private static boolean hasValidTender(Order order) {
		if(order.getTenders() != null) {
			for(Tender tender : order.getTenders()) {
				// check if tender is NO_SALE
				if(!tender.getType().equals(Tender.TENDER_TYPE_NO_SALE)) {
					//check if tender was a card payment, and if it was not voided or failed
					if(tender.getCardDetails() == null || (!tender.getCardDetails().getStatus().equals("VOIDED") && !tender.getCardDetails().getStatus().equals("FAILED"))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Map<String, Payment> getTenderToPayment(Order[] orders, Payment[] payments, SquareClientV2 squareClientV2, Map<String, String> params) throws Exception {
        Map<String, Payment> tenderToPayment = Arrays.stream(payments).collect(Collectors.toMap(Payment::getId, Function.identity()));
        for(Order order : orders) {
        	if(order != null && order.getTenders() != null) {
        		for(Tender tender : order.getTenders()) {
        			if(!tenderToPayment.containsKey(tender.getId())) {
        				Payment payment = squareClientV2.payments().get(tender.getId());
        				tenderToPayment.put(tender.getId(), payment);
        			}
        		}
        	}
        }
        return tenderToPayment;
	}

	public static Payout[] getPayouts(SquareClientV2 squareClientV2, String locationId, Map<String, String> params)
			throws Exception {
		params.put("location_id", locationId);
		return squareClientV2.payouts().list(params);
	}

	public static PayoutEntry[] getPayoutEntries(SquareClientV2 squareClientV2, String payoutId, Map<String, String> params)
		throws Exception {
		return squareClientV2.payouts().listEntries(payoutId, params);
	}
	/*
	 * Helper function to parse location number
	 *
	 * - per TNT spec, all upcoming seasons will follow new naming convention
	 * location name = TNT location number - old seasons followed convention of
	 * 'NAME (#LocationNumber)'
	 *
	 */
	private static String findLocationNumber(String locationName) {
		String locationNumber = "";

		// old location name = 'NAME (#Location Number)'
		String oldPattern = "\\w+\\s*\\(#([a-zA-Z0-9\\s]+)\\)";
		Pattern p = Pattern.compile(oldPattern);
		Matcher m = p.matcher(locationName);

		if (m.find()) {
			locationNumber = m.group(1);
		} else {
			if (!locationName.equals("")) {
				locationNumber = locationName;
			}
		}

		return locationNumber;
	}
}
