package migrations.stripe;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import com.google.gson.Gson;

import migrations.Messages;
import migrations.PaymentService;

public class StripePaymentService extends PaymentService {
    private static final String EMPTY_STRING = "";

    static final String[] HEADERS = { "First Name", "Last Name", "Description", "Company Name", "Email Address",
            "Phone Number", "Street Address", "Street Address 2", "City", "State", "Postal Code", "Reference Id",
            "DefaultSource" };

    StripeCustomersExport exportData;

    public StripePaymentService(String inputPath, String outputPath) {
        super(inputPath, outputPath);

        exportData = new StripeCustomersExport();
    }

    @Override
    public void readFile() throws Exception {
        System.out.println(Messages.startProcessingInputFile(inputPath));

        Gson gson = new Gson();
        String inputJson = readFileAsString(inputPath);
        exportData = gson.fromJson(inputJson, StripeCustomersExport.class);

        System.out.println(Messages.doneProcessingInputFile(inputPath));
    }

    /**
     * Stripe directly provides the JSON file with this information.
     */
    @Override
    public void exportCustomerCardDataToJson() throws IOException {
        System.out.println(Messages.methodNotImplemented());
    }

    /**
     * Generates a Square Dashboard CSV import file of the Customer data
     * exported from Stripe. This file is meant to be manually uploaded
     * into the merchant's Square Dashboard to greatly increase the speed of
     * customer generation versus using the current Connect V2 APIs
     */
    @Override
    public void exportCustomerDataToCsv() throws IOException {
        System.out.println(Messages.startGeneratingDashboardCustomerCsv());

        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.ISO_8859_1);
        try (CSVPrinter printer = new CSVPrinter(out,
                CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {

            for (StripeCustomer customer : exportData.getCustomers()) {

                printer.printRecord(EMPTY_STRING, EMPTY_STRING, customer.getDescription(), EMPTY_STRING,
                        customer.getEmail(), EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, customer.getId(), customer.getDefaultSource());
            }
        }

        System.out.println(Messages.doneGeneratingDashboardCustomerCsv(outputPath));
    }

    String readFileAsString(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
}
