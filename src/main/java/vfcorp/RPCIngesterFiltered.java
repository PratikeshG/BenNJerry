package vfcorp;

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

public class RPCIngesterFiltered implements Callable {
	
	private String apiUrl;
	private String apiVersion;
	private int itemNumberLookupLength;

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		RPCIngesterPayload rpcIngesterPayload = (RPCIngesterPayload) eventContext.getMessage().getPayload();
		
		SftpInputStream sis = eventContext.getMessage().getProperty("pluStreamReader", PropertyScope.INVOCATION);
		String deploymentId = eventContext.getMessage().getProperty("deployment", PropertyScope.INVOCATION);
		
		Catalog current = new Catalog();
		
		Item[] squareItems = rpcIngesterPayload.getItems();
		Category[] squareCategories = rpcIngesterPayload.getCategories();
		Fee[] squareFees = rpcIngesterPayload.getFees();

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
		epicor.rpc().ingest(sis);

		// TODO(bhartard): Remove this HACK to filter PLUs with a SKU/PLU whitelist
		Catalog proposed = epicor.rpc().convertWithFilter(current, deploymentId);

		System.out.println("performing diff");
		CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU, CatalogChangeRequest.PrimaryKey.NAME);
		System.out.println("diff done");
		SquareClient client = new SquareClient(rpcIngesterPayload.getAccessToken(), apiUrl, apiVersion, rpcIngesterPayload.getMerchantId(), rpcIngesterPayload.getLocationId());
		ccr.setSquareClient(client);
		
		System.out.println("current categories: " + current.getCategories().size());
		System.out.println("current fees: " + current.getFees().size());
		System.out.println("current items: " + current.getItems().size());
		
		System.out.println("proposed categories: " + proposed.getCategories().size());
		System.out.println("proposed fees: " + proposed.getFees().size());
		System.out.println("proposed items: " + proposed.getItems().size());

		System.out.println("mappings: " + ccr.getMappingsToApply().keySet().size());
		System.out.println("create: " + ccr.getObjectsToCreate().size());
		System.out.println("update: " + ccr.getObjectsToUpdate().size());
		
		ccr.call();
		System.out.println("updating account");
		sis.close();

		return null;
	}
}
