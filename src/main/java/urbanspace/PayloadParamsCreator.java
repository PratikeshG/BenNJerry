package urbanspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import util.SquarePayload;
import util.TimeManager;

public class PayloadParamsCreator implements Callable {

	@Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
		@SuppressWarnings("unchecked")
		List<Map<String,String>> ms = (List<Map<String,String>>) eventContext.getMessage().getPayload();
		List<SquarePayload> lsp = new ArrayList<SquarePayload>();
		
		for (Map<String,String> m : ms) {
			SquarePayload sp = new SquarePayload();
			
			sp.setAccessToken(m.get("token"));
			sp.setMerchantId(m.get("merchantId"));
			
			if ("getPastDayInterval".equals(eventContext.getMessage().getProperty("timeMethod", PropertyScope.INVOCATION))) {
				String timeZone = eventContext.getMessage().getProperty("timeZone", PropertyScope.INVOCATION);
				Map<String,String> mm = TimeManager.getPastDayInterval(timeZone);
				sp.getParams().put("begin_time", mm.get("begin_time"));
				sp.getParams().put("end_time", mm.get("end_time"));
			}
			
			lsp.add(sp);
			
		}
		
		return lsp;
	}
}
