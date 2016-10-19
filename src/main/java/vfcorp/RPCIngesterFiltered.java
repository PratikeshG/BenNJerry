package vfcorp;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(RPCIngesterFiltered.class);

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
        String deploymentId = message.getProperty("deployment", PropertyScope.INVOCATION);
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
        // TODO(bhartard): Remove this HACK to filter PLUs with a SKU/PLU
        // whitelist
        Catalog proposed = epicor.rpc().ingest(bis, current, deploymentId);
        bis.close();

        logger.info("Performing diff");
        CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU,
                CatalogChangeRequest.PrimaryKey.NAME);

        logger.info("Diff complete");
        SquareClient client = new SquareClient(rpcIngesterPayload.getAccessToken(), apiUrl, apiVersion,
                rpcIngesterPayload.getMerchantId(), rpcIngesterPayload.getLocationId());
        ccr.setSquareClient(client);

        logger.info("[diff] Current categories total: " + current.getCategories().size());
        logger.info("[diff] Current fees total: " + current.getFees().size());
        logger.info("[diff] Current items total: " + current.getItems().size());

        logger.info("[diff] Proposed categories total: " + proposed.getCategories().size());
        logger.info("[diff] Proposed fees total: " + proposed.getFees().size());
        logger.info("[diff] Proposed items total: " + proposed.getItems().size());

        logger.info("[diff] Mappings total: " + ccr.getMappingsToApply().keySet().size());
        logger.info("[diff] Create total: " + ccr.getObjectsToCreate().size());
        logger.info("[diff] Update total: " + ccr.getObjectsToUpdate().size());

        logger.info("Updating account...");
        ccr.call();
        logger.info("Done updating account");

        return null;
    }
}
