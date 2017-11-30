package urbanspace;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import util.TimeManager;

public class ReportGenerator implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<ReportGeneratorPayload> reportGeneratorPayloads = (List<ReportGeneratorPayload>) message.getPayload();

        String timeZone = message.getProperty("timeZone", PropertyScope.SESSION);
        int offset = message.getProperty("offset", PropertyScope.SESSION);
        int range = message.getProperty("range", PropertyScope.SESSION);

        // Calculate day of report generated
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        c.add(Calendar.DATE, -offset);
        String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, timeZone), timeZone,
                "yyyy-MM-dd");

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
            eventContext.getMessage().addOutboundAttachment(
                    currentDate + "-" + reportGeneratorPayload.getSquarePayload().getMerchantAlias() + ".csv",
                    dataHandler);
        }

        return eventContext.getMessage();
    }
}
