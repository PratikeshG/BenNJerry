package paradies.pandora;

import paradies.Util;
import paradies.pandora.CatalogCallablePayload;

import org.mule.api.MuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class CatalogCallable implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		MuleMessage message = eventContext.getMessage();
		CatalogCallablePayload locationCatalog = (CatalogCallablePayload) message.getPayload();

		byte[] ftpPayload = eventContext.getMessage().getProperty("ftpPayload", PropertyScope.SESSION);

		String currencyCode = locationCatalog.getLocation().getCurrencyCode();
		String storeId = Util.getStoreIdFromLocationNickname(locationCatalog.getLocation().getLocationDetails().getNickname());
		CatalogGenerator catalogGenerator = new CatalogGenerator(storeId, currencyCode);

		Catalog currentCatalog = locationCatalog.getCatalog();
		Catalog newCatalog = catalogGenerator.parsePayload(ftpPayload, currentCatalog);

		CatalogChangeRequest ccr = CatalogChangeRequest.diff(currentCatalog, newCatalog);

		String accessToken = message.getProperty("token", PropertyScope.SESSION);
		String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
		String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);
		String merchantId = message.getProperty("merchantId", PropertyScope.SESSION);
		SquareClient client = new SquareClient(accessToken, apiUrl, apiVersion, merchantId, locationCatalog.getLocation().getId());

		ccr.setSquareClient(client);
		ccr.call();

		locationCatalog.setCatalog(newCatalog);
		return locationCatalog;
	}
}
