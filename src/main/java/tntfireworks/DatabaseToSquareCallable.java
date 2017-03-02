package tntfireworks;

import java.util.HashMap;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class DatabaseToSquareCallable implements Callable {

    private static Logger logger = LoggerFactory.getLogger(DatabaseToSquareCallable.class);
    private String apiUrl;

    public void setApiUrl(String apiUrl) {
        Preconditions.checkNotNull(apiUrl);
        this.apiUrl = apiUrl;
    }

    private <T> T getSessionProperty(String propertyName, MuleMessage message) {
        return message.getProperty(propertyName, PropertyScope.SESSION);
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        HashMap<String, String> locationMarketingPlanCache = getSessionProperty("locationMarketingPlanCache", message);
        HashMap<String, List<CsvItem>> marketingPlanItemsCache = getSessionProperty("marketingPlanItemsCache", message);
        SquarePayload deployment = (SquarePayload) message.getPayload();

        logger.info(String.format("Begin processing Catalog API updates for merchant token: %s",
                deployment.getMerchantId()));

        syncronizeItemsAndCategoriesForDeployment(deployment, locationMarketingPlanCache, marketingPlanItemsCache);

        logger.info(String.format("Done processing Catalog API updates for merchant token: %s",
                deployment.getMerchantId()));

        return null;
    }

    public void syncronizeItemsAndCategoriesForDeployment(SquarePayload deployment,
            HashMap<String, String> locationMarketingPlanCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache) {

        Preconditions.checkNotNull(deployment);
        Preconditions.checkNotNull(locationMarketingPlanCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        SquareClientV2 client = new SquareClientV2(apiUrl, deployment.getAccessToken());

        TntCatalogApi tntCatalogApi = new TntCatalogApi(client, locationMarketingPlanCache, marketingPlanItemsCache);

        tntCatalogApi.batchUpsertCategoriesFromDatabaseToSquare();
        tntCatalogApi.batchUpsertItemsIntoCatalog();
        tntCatalogApi.removeItemsNotPresentAtAnyLocations();
    }

}