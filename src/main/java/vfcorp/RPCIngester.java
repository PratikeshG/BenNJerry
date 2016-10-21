package vfcorp;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Category;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class RPCIngester implements Callable {

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
        MuleMessage message = eventContext.getMessage();

        RPCIngesterPayload rpcIngesterPayload = (RPCIngesterPayload) message.getProperty("rpcPayload",
                PropertyScope.INVOCATION);
        InputStream is = message.getProperty("pluInputStream", PropertyScope.INVOCATION);
        BufferedInputStream bis = new BufferedInputStream(is);

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
                current.addFee(fee); // default primary is ID
            }
        }

        EpicorParser epicor = new EpicorParser();
        epicor.rpc().setItemNumberLookupLength(itemNumberLookupLength);
        Catalog proposed = epicor.rpc().ingest(bis, current, null, RPC.Filter.INACTIVE); 
        bis.close();

        CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU,
                CatalogChangeRequest.PrimaryKey.NAME);

        SquareClient client = new SquareClient(rpcIngesterPayload.getAccessToken(), apiUrl, apiVersion,
                rpcIngesterPayload.getMerchantId(), rpcIngesterPayload.getLocationId());
        ccr.setSquareClient(client);

        ccr.call();

        return null;
    }
}
