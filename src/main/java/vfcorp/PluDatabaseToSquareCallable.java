package vfcorp;

import java.util.ArrayList;
import java.util.HashSet;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Page;
import com.squareup.connect.PageCell;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.SquareClientV2;

public class PluDatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PluDatabaseToSquareCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String apiUrl;
    private int itemNumberLookupLength;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);

        // Retrieve a single deployment for credentials for master account
        VfcDeployment masterAccount = getMasterAccountDeployment(brand);

        SquareClientV2 client = new SquareClientV2(apiUrl, masterAccount.getAccessToken(),
                masterAccount.getMerchantId());

        PluCatalogBuilder catalogBuilder = new PluCatalogBuilder(client, databaseUrl, databaseUser, databasePassword,
                brand);
        catalogBuilder.setItemNumberLookupLength(itemNumberLookupLength);
        catalogBuilder.setPluFiltered(masterAccount.isPluFiltered());

        catalogBuilder.syncCategoriesFromDatabaseToSquare();
        catalogBuilder.syncItemsFromDatabaseToSquare();

        // TODO(bhartard): Determine how to handle fav page/cells
        /*
        updateFavoritesGrid(client);
        */

        logger.info(String.format("Done updating brand account: %s", brand));

        return null;
    }

    private VfcDeployment getMasterAccountDeployment(String brand) throws Exception {
        String whereFilter = String.format("vfcorp_deployments.deployment LIKE 'vfcorp-%s-%%'", brand);

        ArrayList<VfcDeployment> matchingDeployments = (ArrayList<VfcDeployment>) Util.getVfcDeployments(databaseUrl,
                databaseUser, databasePassword, whereFilter);

        if (matchingDeployments.size() < 1) {
            throw new Exception(String.format("No deployments for brand '%s' found.", brand));
        }

        return matchingDeployments.get(0);
    }

    private void updateFavoritesGrid(SquareClient client) throws Exception {
        Page[] pages = client.pages().list();
        PageCell discountCell = discountCell();

        HashSet<Object> ignoreFields = new HashSet<Object>();
        ignoreFields.add(PageCell.Field.PAGE_ID);

        String pageId = "";
        PageCell currentCell = null;

        for (Page page : pages) {
            if (page.getPageIndex() == 0) {
                pageId = page.getId();
                for (PageCell cell : page.getCells()) {
                    if (cell.getRow() == 0 && cell.getColumn() == 0) {
                        currentCell = cell;
                        break;
                    }
                }
            }
        }

        if (pageId.length() > 0 && (currentCell == null || !currentCell.equals(discountCell, ignoreFields))) {
            logger.info("Updating discount favorites cell...");
            client.cells().update(pageId, discountCell);
        }
    }

    private PageCell discountCell() {
        PageCell cell = new PageCell();
        cell.setRow(0);
        cell.setColumn(0);
        cell.setObjectType("PLACEHOLDER");
        cell.setPlaceholderType("DISCOUNTS_CATEGORY");
        return cell;
    }
}
