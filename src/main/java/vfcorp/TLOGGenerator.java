package vfcorp;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.store.ObjectStore;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class TLOGGenerator implements Callable {

	private int itemNumberLookupLength;

	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		TLOGGeneratorPayload tlogGeneratorPayload = (TLOGGeneratorPayload) message.getPayload();

		Merchant matchingMerchant = null;
		for (Merchant merchant : (Merchant[]) tlogGeneratorPayload.getLocations()) {
			if (merchant.getId().equals(tlogGeneratorPayload.getLocationId())) {
				matchingMerchant = merchant;
			}
		}

		if (matchingMerchant != null) {

			String deploymentId = (String) message.getProperty("deploymentId", PropertyScope.SESSION) + 1;
			String timeZoneId = (String) message.getProperty("timeZone", PropertyScope.INVOCATION);

			EpicorParser epicor = new EpicorParser();
			epicor.tlog().setItemNumberLookupLength(itemNumberLookupLength);
			epicor.tlog().setDeployment(deploymentId);
			epicor.tlog().setTimeZoneId(timeZoneId);

			// Get Cloudhub default object store
			ObjectStore<String> objectStore = eventContext.getMuleContext().getRegistry().lookupObject("_defaultUserObjectStore");
			epicor.tlog().setObjectStore(objectStore);

			Payment[] squarePayments = tlogGeneratorPayload.getPayments();
			Employee[] squareEmployees = tlogGeneratorPayload.getEmployees();

			epicor.tlog().parse(matchingMerchant, squarePayments, squareEmployees);

			eventContext.getMessage().setProperty("vfcorpStoreNumber", getStoreNumber(matchingMerchant), PropertyScope.INVOCATION);

			return epicor.tlog().toString();
		}

		return null;
	}
	
	private String getStoreNumber(Merchant merchant) {
		String storeNumber = "";
		if (merchant.getLocationDetails().getNickname() != null) {
			int storeNumberFirstIndex = merchant.getLocationDetails().getNickname().indexOf('(');
			int storeNumberLastIndex = merchant.getLocationDetails().getNickname().indexOf(')');
			if (storeNumberFirstIndex > -1 && storeNumberLastIndex > -1) {
				storeNumber = merchant.getLocationDetails().getNickname().substring(storeNumberFirstIndex + 1, storeNumberLastIndex);
				storeNumber = storeNumber.replaceAll("[^\\d]", "");
			}
		}
		
		// Pad to five characters with left zeros
		return String.format("%05d", Integer.parseInt(storeNumber));
	}
}
