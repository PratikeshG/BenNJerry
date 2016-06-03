package urbanspace;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

public class ReportGenerator implements Callable {

	private String timeMethod;
	private String timeZone;
	private int offset;
	private int range;
	
	public void setTimeMethod(String timeMethod) {
		this.timeMethod = timeMethod;
	}
	
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public void setRange(int range) {
		this.range = range;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		@SuppressWarnings("unchecked")
		List<ReportGeneratorPayload> reportGeneratorPayloads = (List<ReportGeneratorPayload>) eventContext.getMessage().getPayload();
		
		// Calculate day of report generated
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(c.getTimeZone());
		if (timeMethod.equals("getPastDayInterval")) {
			c.add(Calendar.DATE, -offset);
		}
		String currentDate = sdf.format(c.getTime());
		
		// Generate aggregate report
		AggregateReportGenerator aggregateCalculator = new AggregateReportGenerator(timeZone, offset, range);
		String aggregateReport = aggregateCalculator.generate(reportGeneratorPayloads);
		DataHandler dataHandler = new DataHandler(aggregateReport, "text/plain; charset=UTF-8");
		eventContext.getMessage().addOutboundAttachment(currentDate + "-aggregate.csv", dataHandler);
		
		// Generate individual reports
		IndividualReportGenerator individualReportGenerator = new IndividualReportGenerator(timeZone, offset, range);
		for (ReportGeneratorPayload reportGeneratorPayload : reportGeneratorPayloads) {
			String individualReport = individualReportGenerator.generate(reportGeneratorPayload);
			dataHandler = new DataHandler(individualReport, "text/plain; charset=UTF-8");
			eventContext.getMessage().addOutboundAttachment(currentDate + "-" + reportGeneratorPayload.getMerchantAlias() + ".csv", dataHandler);
		}
		
		return eventContext.getMessage();
	}
}
