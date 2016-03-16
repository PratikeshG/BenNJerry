package vfcorp;

import java.util.HashSet;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.mule.transport.sftp.SftpInputStream;

import com.squareup.connect.Category;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

import util.SquarePayload;

public class RPCIngester implements Callable {
	
	private String apiUrl;
	private String apiVersion;
	private boolean onlyAddsCheck;
	private boolean suspiciousNumberOfRecordsCheck;
	private int itemNumberLookupLength;
	private int suspiciousNumberOfRecords;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	
	public void setOnlyAddsCheck(boolean onlyAddsCheck) {
		this.onlyAddsCheck = onlyAddsCheck;
	}
	
	public void setSuspiciousNumberOfRecordsCheck(boolean suspiciousNumberOfRecordsCheck) {
		this.suspiciousNumberOfRecordsCheck = suspiciousNumberOfRecordsCheck;
	}
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	public void setSuspiciousNumberOfRecords(int suspiciousNumberOfRecords) {
		this.suspiciousNumberOfRecords = suspiciousNumberOfRecords;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		List<SquarePayload> squarePayloads = (List<SquarePayload>) eventContext.getMessage().getPayload();
		
		SftpInputStream sis = eventContext.getMessage().getProperty("pluStreamReader", PropertyScope.INVOCATION);
		
		for (SquarePayload squarePayload: squarePayloads) {
			Catalog current = new Catalog();
			
			Item[] squareItems = (Item[]) squarePayload.getResults().get("util.square.ItemsLister");
			Category[] squareCategories = (Category[]) squarePayload.getResults().get("util.square.CategoriesLister");
			Fee[] squareFees = (Fee[]) squarePayload.getResults().get("util.square.FeesLister");
			
			if (squareItems != null) {
				for (Item item : squareItems) {
					current.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
				}
			}
			
			if (squareCategories != null) {
				for (Category category : squareCategories) {
					current.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
				}
			}
			
			if (squareFees != null) {
				for (Fee fee : squareFees) {
					current.addFee(fee, CatalogChangeRequest.PrimaryKey.NAME);
				}
			}
			
			EpicorParser epicor = new EpicorParser();
			epicor.rpc().setItemNumberLookupLength(itemNumberLookupLength);
			epicor.rpc().setOnlyAddsCheck(onlyAddsCheck);
			epicor.rpc().setSuspiciousNumberOfRecordsCheck(suspiciousNumberOfRecordsCheck);
			epicor.rpc().setSuspiciousNumberOfRecords(suspiciousNumberOfRecords);
			epicor.rpc().ingest(sis);
			
			Catalog proposed = epicor.rpc().convert(current, CatalogChangeRequest.PrimaryKey.NAME);
			
			CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU, CatalogChangeRequest.PrimaryKey.NAME, new HashSet<Object>());
			
			SquareClient client = new SquareClient(squarePayload.getAccessToken(), apiUrl, apiVersion, squarePayload.getMerchantId(), squarePayload.getLocationId());
			ccr.setSquareClient(client);
			
			ccr.call();
			
			sis.close();
		}
		
		return null;
	}
}
