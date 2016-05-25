package urbanspace;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.SquareClient;

public class AggregateCalculatorTest {

	@Test
	public void generate_PullDataFromStagingAccountWithManyDifferentKindsOfData_ExpectReportsToMatch() throws Exception {
		ReportGeneratorPayload reportGeneratorPayload = new ReportGeneratorPayload();
		SquareClient squareClient = new SquareClient("na-RKUREpIDcP4JZhfgrNQ", "https://connect.squareupstaging.com", "v1", "CH5CPYTZN2CR5", "CH5CPYTZN2CR5");
		
		String expectedReport = "Merchant Name,SUID,Gross Sales,Gross Sales (Refunds),Gross Sales (Net),Discounts,Discounts (Refunds),Discounts (Net),Net Sales,Net Sales (Refunds),Net Sales (Net),Gift Card Sales,Gift Card Sales (Refunds),Gift Card Sales (Net),Tax,Tax (Refunds),Tax (Net),Tips,Tips (Refunds),Tips (Net),Partial Refunds,Partial Refunds (Refunds),Partial Refunds (Net),Total Collected,Total Collected (Refunds),Total Collected (Net),Cash,Cash (Refunds),Cash (Net),Card,Card (Refunds),Card (Net),Gift Card,Gift Card (Refunds),Gift Card (Net),Other,Other (Refunds),Other (Net),Fees,Fees (Refunds),Fees (Net),Net Total,Net Total (Refunds),Net Total (Net)\n";
		expectedReport += "Ben,CH5CPYTZN2CR5,\"$520.54\",\"-$175.81\",\"$344.73\",\"-$5.51\",\"$5.51\",\"$0.00\",\"$515.03\",\"-$170.30\",\"$344.73\",\"$0.00\",\"$0.00\",\"$0.00\",\"$38.63\",\"-$12.77\",\"$25.86\",\"$19.22\",\"-$14.70\",\"$4.52\",\"$0.00\",\"-$253.49\",\"-$253.49\",\"$572.88\",\"-$451.26\",\"$121.62\",\"$472.26\",\"-$350.64\",\"$121.62\",\"$100.62\",\"-$100.62\",\"$0.00\",\"$0.00\",\"$0.00\",\"$0.00\",\"$0.00\",\"$0.00\",\"$0.00\",\"-$3.82\",\"$3.82\",\"$0.00\",\"$569.06\",\"-$447.44\",\"$121.62\"\n";
		
		reportGeneratorPayload.setMerchantAlias("Ben");
		reportGeneratorPayload.setMerchantId("CH5CPYTZN2CR5");
		
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("begin_time", "2016-05-05T00:00:00-07:00");
		params.put("end_time", "2016-05-05T23:59:59-07:00");
		
		reportGeneratorPayload.setPayments(squareClient.payments().list(params));
		reportGeneratorPayload.setRefunds(squareClient.refunds().list(params));
		
		List<Payment> squareRefundPayments = new LinkedList<Payment>();
        
        for (Refund refund : reportGeneratorPayload.getRefunds()) {
        	squareRefundPayments.add(squareClient.payments().retrieve(refund.getPaymentId()));
        }
        
		reportGeneratorPayload.setRefundPayments(squareRefundPayments.toArray(new Payment[squareRefundPayments.size()]));
		
		List<ReportGeneratorPayload> reportGeneratorPayloads = new LinkedList<ReportGeneratorPayload>();
		reportGeneratorPayloads.add(reportGeneratorPayload);
		
		// TODO: automate this part (the offset has to be calculated manually, currently; no good)
		AggregateReportGenerator aggregateCalculator = new AggregateReportGenerator("America/Los_Angeles", 21, 1);
		String aggregateReport = aggregateCalculator.generate(reportGeneratorPayloads);
		
		assertEquals("reports should be equal", expectedReport, aggregateReport);
	}
}
