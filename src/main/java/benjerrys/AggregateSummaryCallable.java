package benjerrys;

import java.util.List;

public class AggregateSummaryCallable {

    public Object agregateSummaryCallable() throws Exception {
        //MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<LocationReportSummaryPayload> reportSummaryByLocation = null;//(List<LocationReportSummaryPayload>) message.getPayload();

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        builder.append("<body>Ben & Jerry's monthly reports generated successfully.<br/><br/><table><tr>");
        builder.append("<td><b>Location</b></td>");
        builder.append("<td><b>Total Transactions</b></td>");
        builder.append("<td><b>Recipients</b></td>");
        builder.append("<td><b>Processed</b></td>");
        builder.append("</tr>");

        for (LocationReportSummaryPayload reportSummary : reportSummaryByLocation) {
            builder.append(appendRow(reportSummary));
        }

        builder.append("</table></body></html>");
        return builder.toString();
    }

    private String appendRow(LocationReportSummaryPayload reportSummary) {
        return String.format("<tr><td>%s</td><td>%d</td><td>%d</td><td>%b</td></tr>", reportSummary.getLocationName(),
                reportSummary.getTotalTransactions(), reportSummary.getTotalRecipients(), reportSummary.isProcessed());
    }
}
