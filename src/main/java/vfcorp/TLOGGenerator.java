package vfcorp;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import util.SquarePayload;

import com.squareup.connect.Employee;
import com.squareup.connect.Item;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class TLOGGenerator implements Callable {

	private int itemNumberLookupLength;
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		List<SquarePayload> sps = (List<SquarePayload>) eventContext.getMessage().getPayload();
		List<String> tlogs = new ArrayList<String>();
		
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
				epicor.tlog().setTransactionNumber((Integer) eventContext.getMessage().getProperty("lastTransactionNumber", PropertyScope.INVOCATION) + 1);
				
				Payment[] squarePayments = (Payment[]) sp.getResults().get("util.square.PaymentsLister");
				Item[] squareItems = (Item[]) sp.getResults().get("util.square.ItemsLister");
				Employee[] squareEmployees = (Employee[]) sp.getResults().get("util.square.EmployeesLister");
				
				epicor.tlog().parse(matchingMerchant, squarePayments, squareItems, squareEmployees);
				
				tlogs.add(epicor.tlog().toString());
				
				eventContext.getMessage().setProperty("newLastTransactionNumber", epicor.tlog().getTransactionNumber(), PropertyScope.INVOCATION);
			}
		}
		
		return tlogs;
	}
}
