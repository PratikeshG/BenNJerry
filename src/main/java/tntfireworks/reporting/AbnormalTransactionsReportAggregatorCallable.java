package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.Payment;

public class AbnormalTransactionsReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        String adhoc = message.getProperty("adhoc", PropertyScope.SESSION);

        logger.info("Start abnormal orders report generation");

        // build report from payloads
        List<List<AbnormalOrdersPayload>> payloadAggregate = (List<List<AbnormalOrdersPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";

        // retrieve file header and file date from first payload (one location)
        if (payloadExists(payloadAggregate)) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // - payloadAggregate => contains master/merchant account payloads
        // - masterPayload => contains all location payloads
        for (List<AbnormalOrdersPayload> masterPayload : payloadAggregate) {
            addAlert4OrdersToMasterPayload(masterPayload);

            // add rows to report/file
            for (AbnormalOrdersPayload locationPayload : masterPayload) {
                for (String fileRow : locationPayload.getRows()) {
                    reportBuilder.append(fileRow);
                }
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-3.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        // if ad-hoc run, store report on SFTP in adhoc directory
        if (adhoc.equals("TRUE")) {
        	return storeReport(reportName, generatedReport,TntReportAggregator.ADHOC_DIRECTORY);
        }

        return storeOrAttachReport(eventContext.getMessage(), reportName, generatedReport);
    }

    private void addAlert4OrdersToMasterPayload(List<AbnormalOrdersPayload> masterPayload)
            throws Exception {
        // this map is used to find cards used multiple times across an entire merchant account
        // (k, v) == (fingerprint, List<Order>)
        Map<String, ArrayList<Order>> fingerprintToOrders = new HashMap<String, ArrayList<Order>>();

        // this map of location payloads is used to add alert 4 orders later
        // (k, v) == (location_id, AbnormalOrdersPayload)
        Map<String, AbnormalOrdersPayload> locationIdToLocationPayload = new HashMap<String, AbnormalOrdersPayload>();

        // - map location payloads to location ids
        // - map card fingerprints to matching orders
        for (AbnormalOrdersPayload locationPayload : masterPayload) {
            locationIdToLocationPayload.put(locationPayload.getLocationId(), locationPayload);
            fingerprintToOrders = addLocationFingerprints(locationPayload, fingerprintToOrders);
        }

        // add orders with fingerprints that qualify as alert 4 into location payloads as
        // alert 4 entries
        for (String fingerprint : fingerprintToOrders.keySet()) {
            if (isAlert4Fingerprint(fingerprint, fingerprintToOrders)) {
                addAlert4OrdersToLocationPayload(fingerprint, fingerprintToOrders,
                        locationIdToLocationPayload);
            }
        }
    }

    private void addAlert4OrdersToLocationPayload(String fingerprint,
            Map<String, ArrayList<Order>> fingerprintToOrders,
            Map<String, AbnormalOrdersPayload> locationIdToLocationPayload) throws Exception {
    	if(fingerprintToOrders.containsKey(fingerprint)) {
    		for (Order order : fingerprintToOrders.get(fingerprint)) {
                if (locationIdToLocationPayload.containsKey(order.getLocationId())) {
                    locationIdToLocationPayload.get(order.getLocationId()).addAlert4Entry(order);
                }
            }
    	}
    }

    private boolean isAlert4Fingerprint(String fingerprint,
            Map<String, ArrayList<Order>> fingerprintToOrders) {
        return fingerprintToOrders.get(fingerprint)
                .size() >= AbnormalOrdersPayload.SAME_CARD_PER_MERCHANT_LIMIT;
    }

    private Map<String, ArrayList<Order>> addLocationFingerprints(AbnormalOrdersPayload locationPayload,
            Map<String, ArrayList<Order>> fingerprintToOrders) {
    	Map<String, Payment> tenderToPayment = locationPayload.getTenderToPayment();
    	if(locationPayload.getLocationOrders() != null) {
    		for (Order order : locationPayload.getLocationOrders()) {
    			if(order.getTenders() != null) {
    				for (Tender tender : order.getTenders()) {
                        if (isRegister(tender, tenderToPayment) && tender.getCardDetails() != null && tender.getCardDetails().getCard() != null) {
                            String fingerprint = tender.getCardDetails().getCard().getFingerprint();

                            ArrayList<Order> orders;
                            if (fingerprintToOrders.containsKey(fingerprint)) {
                                orders = fingerprintToOrders.get(fingerprint);
                            } else {
                                orders = new ArrayList<Order>();
                            }
                            orders.add(order);
                            fingerprintToOrders.put(fingerprint, orders);
                        }
    				}
    			}
            }
    	}

        return fingerprintToOrders;
    }

    private boolean payloadExists(List<List<AbnormalOrdersPayload>> payloadAggregate) {
        return !(payloadAggregate.isEmpty() || payloadAggregate.get(0).isEmpty());
    }

    private boolean isRegister(Tender tender, Map<String, Payment> tenderMap) {
      	 if (tender != null && tender.getType().equals(Tender.TENDER_TYPE_CARD) && tenderMap != null && tenderMap.get(tender.getId()) != null) {
      		 Payment payment = tenderMap.get(tender.getId());
      		 String squareProduct = payment.getApplicationDetails().getSquareProduct();
      		 if (squareProduct.equals("SQUARE_POS") || squareProduct.equals("VIRTUAL_TERMINAL")) {
      			 return true;
      		 }
      	 }
      	 return false;
    }
}
