package vfcorp.smartwool;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Transaction;

public class TestCSVGenerator {
	private final String timeZoneId = "America/Los_Angeles";
	private String transactionHeaders = "Date,Time,Time Zone,Gross Sales,Discounts,Net Sales,Gift Card Sales,Tax,Tip,Partial Refunds,Total Collected,Source,Card,Card Entry Methods,Cash,Square Gift Card,Other Tender,Other Tender Type,Other Tender Note,Fees,Net Total,Transaction ID,Payment ID,Card Brand,PAN Suffix,Device Name,Staff Name,Staff ID,Details,Description,Event Type,Location,Dining Option,Customer ID,Customer Name,Customer Reference ID,Device Nickname";
	private List<String> columnWhitelist = Arrays.asList(new String[]{"Partial Refunds", "Staff Name", "Device Nickname"});
	private List<String> multiValueColumnList = Arrays.asList(new String[]{"Card Entry Methods"});

	@Test
	public void testTransactionWithCustomer() throws Exception {
		Gson gson = new Gson();
		Customer customer = gson.fromJson(CsvExamples.testCustomer1, Customer.class);
		Payment payment = gson.fromJson(CsvExamples.testPayment1, Payment.class);
		Transaction transaction = gson.fromJson(CsvExamples.testTransaction1, Transaction.class);
		String locationName = "#1002";

		CSVRecord headersCsv = CSVFormat.DEFAULT.parse(new StringReader(this.transactionHeaders)).getRecords().get(0);

		DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();
		CSVRecord transactionCsvExpected = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionsCsv1)).getRecords().get(0);
		List<String> transactionCsvActual = csvRowFactory.generateTransactionCsvRow(payment, transaction, customer, locationName, timeZoneId);

		test(headersCsv, transactionCsvExpected, transactionCsvActual);
	}

	@Test
	public void testTransactionMultipleTenders() throws Exception {
		Gson gson = new Gson();
		Payment payment = gson.fromJson(CsvExamples.testPayment2, Payment.class);
		Transaction transaction = gson.fromJson(CsvExamples.testTransaction2, Transaction.class);
		String locationName = "#1002";

		CSVRecord headersCsv = CSVFormat.DEFAULT.parse(new StringReader(this.transactionHeaders)).getRecords().get(0);

		DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();
		CSVRecord transactionCsvExpected = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionCsv2)).getRecords().get(0);
		List<String> transactionCsvActual = csvRowFactory.generateTransactionCsvRow(payment, transaction, null, locationName, timeZoneId);

		test(headersCsv, transactionCsvExpected, transactionCsvActual);
	}

	@Test
	public void testItemization() throws Exception {
		Gson gson = new Gson();
		Payment payment = gson.fromJson(CsvExamples.testPayment1, Payment.class);
		Transaction transaction = gson.fromJson(CsvExamples.testTransaction1, Transaction.class);
		String locationName = "#1002";

		CSVRecord headersCsv = CSVFormat.DEFAULT.parse(new StringReader(this.transactionHeaders)).getRecords().get(0);

		DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();
		CSVRecord transactionCsvExpected = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionCsv2)).getRecords().get(0);

		for (PaymentItemization itemization : payment.getItemizations()) {
			List<String> transactionCsvActual = csvRowFactory.generateItemCsvRow(payment, itemization, transaction, null, locationName, timeZoneId);
			test(headersCsv, transactionCsvExpected, transactionCsvActual);
		}
	}

	public void test(CSVRecord headersCsv, CSVRecord transactionCsvExpected, List<String> transactionCsvActual) {
		for (int i = 0; i < transactionCsvExpected.size(); i++) {
			System.out.println(headersCsv.get(i) + ", col: " + i);
			System.out.println("ACTUAL:   " + transactionCsvActual.get(i));
			System.out.println("EXPECTED: " + transactionCsvExpected.get(i));
			if (columnWhitelist.contains(headersCsv.get(i))) {
				System.out.println("Skipped\n");
				continue;
			} else {
				if (multiValueColumnList.contains(headersCsv.get(i))) {
					List<String> actualValues = Arrays.asList(transactionCsvActual.get(i).split(", "));
					List<String> expectedValues = Arrays.asList(transactionCsvExpected.get(i).split(", "));
					for (String expected : expectedValues) {
						if (!actualValues.contains(expected.toUpperCase())){
							System.out.println();
							Assert.fail();
						}
					}
				} else {
					assertEquals("Test", transactionCsvActual.get(i), transactionCsvExpected.get(i));
				}
				System.out.println("\n");
			}
		}
	}
}
