package tntfireworks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;
import util.SquarePayload;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentsCallableTest extends TestCase {

    // database mock results
    private static final String locationsJson = "[{\"name\":\"WALMART #2554\",\"mktPlan\":\"6LAR\",\"locationNumber\":\"AZP0001\"},{\"name\":\"WALMART #3407 1060 S WATSON RD\",\"mktPlan\":\"2TNTC\",\"locationNumber\":\"AZP0002\"},{\"name\":\"WHEELER ALEX CITY MARKETPLACE\",\"mktPlan\":\"2TNTC\",\"locationNumber\":\"FAL0002\"},{\"name\":\"VACANT LOT\",\"mktPlan\":\"2TNTC\",\"locationNumber\":\"FAL0004\"},{\"name\":\"6924 HWY 359\",\"mktPlan\":\"6LAR\",\"locationNumber\":\"TX 0489\"}]";
    private static final String itemsJson = "[{\"itemNumber\":\"100072\",\"suggestedPrice\":\"49.99\",\"upc\":\"27736001651\",\"mktPlan\":\"2TNTC\",\"currency\":\"USD\",\"itemDescription\":\"BIG BOMB POLYBAG C\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100382\",\"suggestedPrice\":\"14.99\",\"upc\":\"27736000098\",\"mktPlan\":\"2TNTC\",\"currency\":\"USD\",\"itemDescription\":\"TNT HOT SHOT BAG C\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100605\",\"suggestedPrice\":\"39.99\",\"upc\":\"27736031030\",\"mktPlan\":\"2TNTC\",\"currency\":\"USD\",\"itemDescription\":\"RED DEVIL TRAY SS\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100687\",\"suggestedPrice\":\"79.99\",\"upc\":\"27736003037\",\"mktPlan\":\"2TNTC\",\"currency\":\"USD\",\"itemDescription\":\"SUPER TNT TRAY C     4/1\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100690\",\"suggestedPrice\":\"777.77\",\"upc\":\"27736031054\",\"mktPlan\":\"2TNTC\",\"currency\":\"USD\",\"itemDescription\":\"SUPER TNT SS\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100934\",\"suggestedPrice\":\"555.55\",\"upc\":\"27736012510\",\"mktPlan\":\"2TNTC\",\"currency\":\"USD\",\"itemDescription\":\"GOODY BAG SS  PDQ\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100041\",\"suggestedPrice\":\"199.99\",\"upc\":\"27736013784\",\"mktPlan\":\"6LAR\",\"currency\":\"USD\",\"itemDescription\":\"ARSENAL C\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100075\",\"suggestedPrice\":\"39.99\",\"upc\":\"27736024421\",\"mktPlan\":\"6LAR\",\"currency\":\"USD\",\"itemDescription\":\"BIG BOMB TRAY SS\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100382\",\"suggestedPrice\":\"9.99\",\"upc\":\"27736000098\",\"mktPlan\":\"6LAR\",\"currency\":\"USD\",\"itemDescription\":\"TNT HOT SHOT BAG C\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100687\",\"suggestedPrice\":\"39.99\",\"upc\":\"27736003037\",\"mktPlan\":\"6LAR\",\"currency\":\"USD\",\"itemDescription\":\"SUPER TNT TRAY C     4/1\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100731\",\"suggestedPrice\":\"999.99\",\"upc\":\"27736006557\",\"mktPlan\":\"6LAR\",\"currency\":\"USD\",\"itemDescription\":\"BLAST C\",\"category\":\"ASSORTMENTS\"},{\"itemNumber\":\"100934\",\"suggestedPrice\":\"888.88\",\"upc\":\"27736012510\",\"mktPlan\":\"6LAR\",\"currency\":\"USD\",\"itemDescription\":\"GOODY BAG SS  PDQ\",\"category\":\"ASSORTMENTS\"}]";
    private static final String deploymentsJson = "[{\"legacy\":\"0\",\"connectApp\":\"sq0ids-ZoQgZ4Kmx2SuGFvfZHOuWg\",\"merchantId\":\"3M4YT8JKYHAVJ\",\"id\":\"2013\",\"merchantAlias\":\"Tnt Fireworks 1\",\"deployment\":\"tntfireworks-mlent\",\"token\":\"sq0ats-RSCJWip1Slxf71xI4YKI1w\"},{\"legacy\":\"0\",\"connectApp\":\"sq0ids-mKV4YIF8SbHangaUo2KIiQ\",\"merchantId\":\"E5SKBKC452V7J\",\"id\":\"2014\",\"merchantAlias\":\"Tnt Fireworks 2\",\"deployment\":\"tntfireworks-mlent\",\"token\":\"sq0ats-oLs6rkiJP55CYhSQYwyVgQ\"}]";

    // expected results
    private static final String expectedDeploymentPayloads = "[{\"merchantId\":\"3M4YT8JKYHAVJ\",\"accessToken\":\"sq0ats-RSCJWip1Slxf71xI4YKI1w\",\"merchantAlias\":\"Tnt Fireworks 1\",\"legacySingleLocationSquareAccount\":false},{\"merchantId\":\"E5SKBKC452V7J\",\"accessToken\":\"sq0ats-oLs6rkiJP55CYhSQYwyVgQ\",\"merchantAlias\":\"Tnt Fireworks 2\",\"legacySingleLocationSquareAccount\":false}]";
    private static final String runTwolocationMarketingPlanCacheJson = "{\"FAL0004\":\"2TNTC\",\"FAL0002\":\"2TNTC\",\"AZP0002\":\"2TNTC\",\"TX 0489\":\"6LAR\",\"AZP0001\":\"6LAR\"}";
    private static final String runTwoMarketingPlanItemsCacheJson = "{\"2TNTC\":[{\"number\":\"100072\",\"category\":\"ASSORTMENTS\",\"description\":\"BIG BOMB POLYBAG C\",\"suggestedPrice\":\"49.99\",\"upc\":\"27736001651\",\"marketingPlan\":\"2TNTC\",\"currency\":\"USD\"},{\"number\":\"100382\",\"category\":\"ASSORTMENTS\",\"description\":\"TNT HOT SHOT BAG C\",\"suggestedPrice\":\"14.99\",\"upc\":\"27736000098\",\"marketingPlan\":\"2TNTC\",\"currency\":\"USD\"},{\"number\":\"100605\",\"category\":\"ASSORTMENTS\",\"description\":\"RED DEVIL TRAY SS\",\"suggestedPrice\":\"39.99\",\"upc\":\"27736031030\",\"marketingPlan\":\"2TNTC\",\"currency\":\"USD\"},{\"number\":\"100687\",\"category\":\"ASSORTMENTS\",\"description\":\"SUPER TNT TRAY C     4/1\",\"suggestedPrice\":\"79.99\",\"upc\":\"27736003037\",\"marketingPlan\":\"2TNTC\",\"currency\":\"USD\"},{\"number\":\"100690\",\"category\":\"ASSORTMENTS\",\"description\":\"SUPER TNT SS\",\"suggestedPrice\":\"777.77\",\"upc\":\"27736031054\",\"marketingPlan\":\"2TNTC\",\"currency\":\"USD\"},{\"number\":\"100934\",\"category\":\"ASSORTMENTS\",\"description\":\"GOODY BAG SS  PDQ\",\"suggestedPrice\":\"555.55\",\"upc\":\"27736012510\",\"marketingPlan\":\"2TNTC\",\"currency\":\"USD\"}],\"6LAR\":[{\"number\":\"100041\",\"category\":\"ASSORTMENTS\",\"description\":\"ARSENAL C\",\"suggestedPrice\":\"199.99\",\"upc\":\"27736013784\",\"marketingPlan\":\"6LAR\",\"currency\":\"USD\"},{\"number\":\"100075\",\"category\":\"ASSORTMENTS\",\"description\":\"BIG BOMB TRAY SS\",\"suggestedPrice\":\"39.99\",\"upc\":\"27736024421\",\"marketingPlan\":\"6LAR\",\"currency\":\"USD\"},{\"number\":\"100382\",\"category\":\"ASSORTMENTS\",\"description\":\"TNT HOT SHOT BAG C\",\"suggestedPrice\":\"9.99\",\"upc\":\"27736000098\",\"marketingPlan\":\"6LAR\",\"currency\":\"USD\"},{\"number\":\"100687\",\"category\":\"ASSORTMENTS\",\"description\":\"SUPER TNT TRAY C     4/1\",\"suggestedPrice\":\"39.99\",\"upc\":\"27736003037\",\"marketingPlan\":\"6LAR\",\"currency\":\"USD\"},{\"number\":\"100731\",\"category\":\"ASSORTMENTS\",\"description\":\"BLAST C\",\"suggestedPrice\":\"999.99\",\"upc\":\"27736006557\",\"marketingPlan\":\"6LAR\",\"currency\":\"USD\"},{\"number\":\"100934\",\"category\":\"ASSORTMENTS\",\"description\":\"GOODY BAG SS  PDQ\",\"suggestedPrice\":\"888.88\",\"upc\":\"27736012510\",\"marketingPlan\":\"6LAR\",\"currency\":\"USD\"}]}";

    // classes
    private static final Type arrayListMapStringString = new TypeToken<ArrayList<Map<String, String>>>() {
    }.getType();
    private static final Type hashmapStringString = new TypeToken<HashMap<String, String>>() {
    }.getType();
    private static final Type hashmapStringListCsvItem = new TypeToken<HashMap<String, List<CsvItem>>>() {
    }.getType();

    // captors
    @Captor
    ArgumentCaptor<HashMap<String, String>> locationMarketingPlanCacheCaptor;
    @Captor
    ArgumentCaptor<HashMap<String, List<CsvItem>>> marketingPlanItemsCacheCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeploymentsCallable() throws Exception {

        // setup inputs
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ArrayList<Map<String, String>> locations = gson.fromJson(locationsJson, arrayListMapStringString);
        ArrayList<Map<String, String>> items = gson.fromJson(itemsJson, arrayListMapStringString);
        ArrayList<Map<String, String>> deployments = gson.fromJson(deploymentsJson, arrayListMapStringString);

        // setup mocks
        TntDatabaseApi tntDatabaseApi = Mockito.mock(TntDatabaseApi.class);
        MuleMessage message = Mockito.mock(MuleMessage.class);
        Mockito.when(tntDatabaseApi.submitQuery(Mockito.any())).thenReturn(locations).thenReturn(items)
                .thenReturn(deployments);

        // call function
        TntCatalogSyncDeploymentsCallable deploymentsCallable = new TntCatalogSyncDeploymentsCallable();
        List<SquarePayload> actualDeployments = deploymentsCallable.getCatalogSyncDeploymentsFromDb(tntDatabaseApi);

        // verify session variables are set
        Mockito.verify(message).setProperty(Mockito.eq("marketingPlanItemsCache"),
                marketingPlanItemsCacheCaptor.capture(), Mockito.eq(PropertyScope.SESSION));
        Mockito.verify(message).setProperty(Mockito.eq("locationMarketingPlanCache"),
                locationMarketingPlanCacheCaptor.capture(), Mockito.eq(PropertyScope.SESSION));
        JSONAssert.assertEquals(gson.toJson(locationMarketingPlanCacheCaptor.getValue(), hashmapStringString),
                runTwolocationMarketingPlanCacheJson, false);
        JSONAssert.assertEquals(gson.toJson(marketingPlanItemsCacheCaptor.getValue(), hashmapStringListCsvItem),
                runTwoMarketingPlanItemsCacheJson, false);

        // verify payload is set
        JSONAssert.assertEquals(gson.toJson(actualDeployments), expectedDeploymentPayloads, false);

    }

}
