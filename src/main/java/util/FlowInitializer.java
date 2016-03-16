package util;

import java.util.Collection;

import org.mule.api.MuleEventContext;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.lifecycle.Callable;
import org.mule.construct.AbstractFlowConstruct;

public class FlowInitializer implements Callable {

	private String inactiveFlows;

	public void setInactiveFlows(String inactiveFlows) {
		this.inactiveFlows = inactiveFlows;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		if (inactiveFlows != null && !inactiveFlows.equals("")) {
			Collection<FlowConstruct> flowConstructs = eventContext.getMuleContext().getRegistry().lookupFlowConstructs();
			String[] inactiveFlowTokens = inactiveFlows.split(",");
			
			for (FlowConstruct flowConstruct : flowConstructs) {
				AbstractFlowConstruct afc = (AbstractFlowConstruct) flowConstruct;
				
				String constructType = afc.getConstructType();
				if (constructType.equals("Flow")) {
					for (String inactiveFlowToken : inactiveFlowTokens) {
						if (afc.getName().startsWith(inactiveFlowToken) && !afc.isStopping() && !afc.isStopped()) {
							afc.stop();
						}
					}
				}
			}
		}
		
		return null;
	}

}
