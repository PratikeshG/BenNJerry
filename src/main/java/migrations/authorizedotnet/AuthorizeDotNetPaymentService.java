package migrations.authorizedotnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import migrations.PaymentService;
import migrations.StripeCardExport;
import migrations.StripeCustomerCardExport;

public class AuthorizeDotNetPaymentService extends PaymentService {
    public AuthorizeDotNetPaymentService(String inputPath, String outputPath) {
        super(inputPath, outputPath);
    }

    /*
     * Data Export Headers:
     * [0] MerchantID
     * [1] CustomerProfileID
     * [2] CustomerPaymentProfileID
     * [3] CustomerID
     * [4] Description
     * [5] Email
     * [6] CardNumber
     * [7] CardExpirationDate
     * [8] CardType
     * [9] BankAccountNumber
     * [10] BankRoutingNumber
     * [11] NameOnAccount
     * [12] BankAccountType
     * [13] ECheckRecordTypeID
     * [14] BankName
     * [15] Company
     * [16] FirstName
     * [17] LastName
     * [18] Address
     * [19] City
     * [20] StateProv
     * [21] Zip
     * [22] Country
     * [23] Phone
     * [24] Fax
     */
    @Override
    public void readFile() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), "ISO-8859-1"));
        CSVParser parser = CSVParser.parse(in,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withQuoteMode(QuoteMode.MINIMAL).withTrim());

        int recordsParsed = 0;

        System.out.println("Processing input file: " + inputPath);

        for (CSVRecord record : parser) {
            if (record.size() < 24) {
                System.out.println("Skipping invalid record: " + record.toString());
                continue;
            }

            ExportRow row = new ExportRow();

            row.setCustomerProfileID(removeQuotes(record.get(1)));
            row.setCustomerPaymentProfileID(removeQuotes(record.get(2)));
            row.setCustomerID(removeQuotes(record.get(3)));
            row.setDescription(removeQuotes(record.get(4)));
            row.setEmail(removeQuotes(record.get(5)));
            row.setCardNumber(removeQuotes(record.get(6)));
            row.setCardExpirationDate(removeQuotes(record.get(7)));
            row.setCardType(removeQuotes(record.get(8)));
            row.setCompany(removeQuotes(record.get(15)));
            row.setFirstName(removeQuotes(record.get(16)));
            row.setLastName(removeQuotes(record.get(17)));
            row.setAddress(removeQuotes(record.get(18)));
            row.setCity(removeQuotes(record.get(19)));
            row.setStateProv(removeQuotes(record.get(20)));
            row.setZip(removeQuotes(record.get(21)));
            row.setCountry(removeQuotes(record.get(22)));
            row.setPhone(removeQuotes(record.get(23)));

            exportRows.add(row);

            recordsParsed++;
            if (recordsParsed % 1000 == 0) {
                System.out.print(".");
            }
            if (recordsParsed % 100000 == 0) {
                System.out.print("\n");
            }
        }
        System.out.println("\nDone processing input file: " + inputPath);
    }

    private String removeQuotes(String input) {
        return input.replaceAll("^\"|\"$", "");
    }

    @Override
    public void exportCustomerCardDataToJson() throws IOException {
        System.out.println("Generating Stripe-formatted Customer cards JSON file");

        ArrayList<StripeCustomerCardExport> customerCardExports = new ArrayList<StripeCustomerCardExport>();

        for (ExportRow exportRow : exportRows) {
            String expYear = exportRow.getCardExpirationDate().trim().split("-")[0];
            String expMonth = exportRow.getCardExpirationDate().trim().split("-")[1];

            // We only want first five digits before the zip +4
            String postal = exportRow.getZip().trim().split("-")[0].split("\\s+")[0];

            String customerName = exportRow.getFirstName() + " " + exportRow.getLastName();

            StripeCustomerCardExport stripeCustomerCardFormat = new StripeCustomerCardExport();
            stripeCustomerCardFormat.setId(exportRow.getCustomerProfileID());
            stripeCustomerCardFormat.setName(customerName);

            StripeCardExport stripeCardFormat = new StripeCardExport();
            stripeCardFormat.setId(exportRow.getCustomerPaymentProfileID());
            stripeCardFormat.setName(customerName);
            stripeCardFormat.setNumber(exportRow.getCardNumber());
            stripeCardFormat.setExpMonth(Integer.parseInt(expMonth));
            stripeCardFormat.setExpYear(Integer.parseInt(expYear));
            stripeCardFormat.setAddressZip(postal);

            stripeCustomerCardFormat.setCards(new StripeCardExport[] { stripeCardFormat });

            customerCardExports.add(stripeCustomerCardFormat);

        }
        HashMap<String, StripeCustomerCardExport[]> exportCustomerCardMap = new HashMap<String, StripeCustomerCardExport[]>();
        exportCustomerCardMap.put("customers",
                customerCardExports.toArray(new StripeCustomerCardExport[exportCustomerCardMap.size()]));

        final Path out = Paths.get(outputPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(exportCustomerCardMap, writer);
            // read old content, manipulate, write new contents
        } finally {
            System.out.println("Done generating Stripe-formatted Customer cards JSON file: " + outputPath);
        }
    }

    @Override
    public void exportCustomerDataToCsv() throws IOException {
        System.out.println("Generating Dashboard Customer CSV import file");

        Map<String, ExportRow> uniqueCustomers = getUniqueCustomerRecords();

        String[] HEADERS = { "First Name", "Last Name", "Company Name", "Email Address", "Phone Number",
                "Street Address", "Street Address 2", "City", "State", "Postal Code", "Reference Id", "Customer Id" };
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.ISO_8859_1);
        try (CSVPrinter printer = new CSVPrinter(out,
                CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {
            for (String customerProfileId : uniqueCustomers.keySet()) {
                ExportRow customerProfile = uniqueCustomers.get(customerProfileId);
                printer.printRecord(customerProfile.getFirstName(), customerProfile.getLastName(),
                        customerProfile.getCompany(), customerProfile.getEmail(), customerProfile.getPhone(),
                        customerProfile.getAddress(), "", customerProfile.getCity(), customerProfile.getStateProv(),
                        customerProfile.getZip(), customerProfile.getCustomerProfileID(),
                        customerProfile.getCustomerID());
            }
        }

        System.out.println("Done generating Dashboard Customer CSV import file: " + outputPath);
    }

    private Map<String, ExportRow> getUniqueCustomerRecords() {
        HashMap<String, ExportRow> uniqueCustomers = new HashMap<String, ExportRow>();

        for (ExportRow row : exportRows) {
            uniqueCustomers.put(row.getCustomerProfileID(), row);
        }

        return uniqueCustomers;
    }
}
