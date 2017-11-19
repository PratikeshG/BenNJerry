package vfcorp.smartwool;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Transaction;

public class TestCSVGenerator {
	private final String timeZoneId = "America/Los_Angeles";
	private List<String> columnWhitelist = Arrays.asList(new String[]{"Partial Refunds", "Staff Name", "Device Nickname"});
	private List<String> multiValueColumnList = Arrays.asList(new String[]{"Card Entry Methods", "Payment ID", "Card Brand", "Description"});
	private String domainUrl = "http://squareup.com";

	@Test
	public void testTransactionWithCustomer() throws Exception {
		Gson gson = new Gson();
		Customer customer = gson.fromJson(CsvExamples.testCustomer1, Customer.class);
		Payment payment = gson.fromJson(CsvExamples.testPayment1, Payment.class);
		Transaction transaction = gson.fromJson(CsvExamples.testTransaction1, Transaction.class);
		String locationName = "#1002";

		CSVRecord headersCsv = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionHeaders)).getRecords().get(0);

		DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();
		CSVRecord transactionCsvExpected = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionsCsv1)).getRecords().get(0);
		List<String> transactionCsvActual = csvRowFactory.generateTransactionCsvRow(payment, transaction, customer, locationName, timeZoneId, domainUrl);

		test(headersCsv, transactionCsvExpected, transactionCsvActual);
	}

	@Test
	public void testTransactionMultipleTenders() throws Exception {
		Gson gson = new Gson();
		Payment payment = gson.fromJson(CsvExamples.testPayment2, Payment.class);
		Transaction transaction = gson.fromJson(CsvExamples.testTransaction2, Transaction.class);
		String locationName = "#1002";

		CSVRecord transactionHeadersCsv = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionHeaders)).getRecords().get(0);

		DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();
		CSVRecord transactionCsvExpected = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionCsv2)).getRecords().get(0);
		List<String> transactionCsvActual = csvRowFactory.generateTransactionCsvRow(payment, transaction, null, locationName, timeZoneId, domainUrl);

		test(transactionHeadersCsv, transactionCsvExpected, transactionCsvActual);
	}

	@Test
	public void testItemization() throws Exception {
		Gson gson = new Gson();
		Payment payment = gson.fromJson(CsvExamples.testPayment1, Payment.class);
		Transaction transaction = gson.fromJson(CsvExamples.testTransaction1, Transaction.class);
		Customer customer = gson.fromJson(CsvExamples.testCustomer1, Customer.class);
		String locationName = "#1002";

		CSVRecord headersCsv = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testItemHeaders)).getRecords().get(0);

		DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();
		List<CSVRecord> itemsCsvExpected = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testItemsCsv1)).getRecords();

		for (int index = 0; index < payment.getItemizations().length; index++) {
			List<String> itemizationCsvActual = csvRowFactory.generateItemCsvRow(payment, payment.getItemizations()[index], transaction, customer, locationName, timeZoneId, domainUrl);
			CSVRecord itemExpected = itemsCsvExpected.get(index);
			test(headersCsv, itemExpected, itemizationCsvActual);
		}
	}

	@Test
	public void testFullk603SampleLocation() throws Exception {
		List<CSVRecord> records = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.k603csv)).getRecords();

		assertEquals(CsvExamples.k603payments.size(), CsvExamples.k603transactions.size());
		assertEquals(CsvExamples.k603payments.size(),  records.size());

		Gson gson = new Gson();

		for (int index = 0; index < CsvExamples.k603payments.size(); index++) {
			Payment payment = gson.fromJson(CsvExamples.k603payments.get(index), Payment.class);
			Transaction transaction = gson.fromJson(CsvExamples.k603transactions.get(index), Transaction.class);
			Customer customer = null;
			String locationName = "K603Cherry Creek Mall Pop-Up";

			CSVRecord expected = records.get(index);
			CSVRecord transactionHeadersCsv = CSVFormat.DEFAULT.parse(new StringReader(CsvExamples.testTransactionHeaders)).getRecords().get(0);

			DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();

			List<String> actual = csvRowFactory.generateTransactionCsvRow(payment, transaction, customer, locationName, timeZoneId, domainUrl);
			test(transactionHeadersCsv, expected, actual);
		}
	}

	public void test(CSVRecord headersCsv, CSVRecord csvExpected, List<String> csvActual) {
		for (int i = 0; i < csvExpected.size(); i++) {
			String header = headersCsv.get(i);
			String actualCell = csvActual.get(i);
			String expectedCell = csvExpected.get(i);

			System.out.println(header + ", col: " + i);
			System.out.println("ACTUAL:   " + actualCell);
			System.out.println("EXPECTED: " + expectedCell);

			if (columnWhitelist.contains(header)) {
				System.out.println("Skipped\n");
				continue;
			} else {
				if (multiValueColumnList.contains(header)) {
					List<String> actualValues = Arrays.asList(csvActual.get(i).split(", "));
					List<String> expectedValues = Arrays.asList(csvExpected.get(i).split(", "));
					actualValues = normalize(actualValues);
					for (String expected : expectedValues) {
						if (!actualValues.contains(expected.toUpperCase())){
							System.out.println("\n");
							Assert.fail();
						}
					}
				} else {
					assertEquals("Test", csvExpected.get(i), csvActual.get(i));
				}
				System.out.println("\n");
			}
			System.out.flush();
		}
	}
	private List<String> normalize(List<String> input) {
		List<String> out = new ArrayList<String>(input.size());
		for (String in : input) {
			out.add(in.toUpperCase());
		}
		return out;
	}
}
