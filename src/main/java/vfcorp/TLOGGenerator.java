package vfcorp;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.store.ObjectStore;
import org.mule.api.transport.PropertyScope;

import util.SquarePayload;

import com.squareup.connect.Employee;
import com.squareup.connect.Item;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class TLOGGenerator implements Callable {

	private int itemNumberLookupLength;
	private String timeZoneId;
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		List<SquarePayload> sps = (List<SquarePayload>) eventContext.getMessage().getPayload();
		List<TLOGResult> tlogs = new ArrayList<TLOGResult>();
		
		for (SquarePayload sp: sps) {
			Merchant matchingMerchant = null;
			for (Merchant merchant : (Merchant[]) sp.getResults().get("util.square.LocationsLister")) {
				if (merchant.getId().equals(sp.getLocationId())) {
					matchingMerchant = merchant;
				}
			}
			
			if (matchingMerchant != null) {
				EpicorParser epicor = new EpicorParser();
				epicor.tlog().setItemNumberLookupLength(itemNumberLookupLength);
				epicor.tlog().setDeployment((String) eventContext.getMessage().getProperty("deployment", PropertyScope.INVOCATION) + 1);
				epicor.tlog().setTimeZoneId(timeZoneId);
				
				// Get Cloudhub default object store
				ObjectStore<String> objectStore = eventContext.getMuleContext().getRegistry().lookupObject("_defaultUserObjectStore");
				epicor.tlog().setObjectStore(objectStore);
				
				Payment[] squarePayments = (Payment[]) sp.getResults().get("util.square.PaymentsLister");
				Item[] squareItems = (Item[]) sp.getResults().get("util.square.ItemsLister");
				Employee[] squareEmployees = (Employee[]) sp.getResults().get("util.square.EmployeesLister");
				
				epicor.tlog().parse(matchingMerchant, squarePayments, squareItems, squareEmployees);
				
				TLOGResult tlogResult = new TLOGResult();
				tlogResult.setTlog(epicor.tlog().toString());
				tlogResult.setStoreNumber(getStoreNumber(matchingMerchant));
				
				tlogs.add(tlogResult);
			}
		}
		
		return tlogs;
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
		
		return String.format("%05d", Integer.parseInt(storeNumber));
	}
}
