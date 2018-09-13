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

import migrations.Messages;
import migrations.MigrationService;
import migrations.PaymentService;
import migrations.stripe.StripeCustomerCardExport;

public class AuthorizeDotNetPaymentService extends PaymentService {
    private static final String INPUT_STREAM_FORMAT = "ISO-8859-1";
    private static final String CUSTOMERS = "customers";

    private static final String REGEX_CONTROL_CHARS = "\\p{Cc}";
    private static final String REGEX_NON_ASCII_CHARS = "[^\\x00-\\x7F]";
    private static final String REGEX_QUOTED_STRING = "^\"|\"$";
    private static final String EMPTY_STRING = "";

    public AuthorizeDotNetPaymentService(String inputPath, String outputPath) {
        super(inputPath, outputPath);
    }

    /*
     * Data Export Headers:
     * [0] MerchantID (ex: 1203107)
     * [1] CustomerProfileID (ex: 113596868)
     * [2] CustomerPaymentProfileID (ex: 106527814)
     * [3] CustomerID (ex: 145739)
     * [4] Description -- not used
     * [5] Email (ex: jdoe@gmail.com) - all email records seem properly formed
     * [6] CardNumber (Ex: 4111111111111111)
     * [7] CardExpirationDate (ex: 2016-08)
     * [8] CardType (one of A, V, M, or D)
     * [9] BankAccountNumber -- not used
     * [10] BankRoutingNumber -- not used
     * [11] NameOnAccount -- not used
     * [12] BankAccountType (-- not used
     * [13] ECheckRecordTypeID -- not used
     * [14] BankName -- not used
     * [15] Company (ex: Acme Corp)
     * [16] FirstName (ex: John)
     * [17] LastName (ex: Doe)
     * [18] Address (ex: 108 Main Street)
     * [19] City (ex: San Francisco)
     * [20] StateProv (ex: California)
     * [21] Zip (ex: 94104 or 94103-1234 or 94103 1234)
     * [22] Country (ex: US)
     * [23] Phone (ex: 5166251155) - all phone records seem properly formed
     * [24] Fax -- not used
     */
    @Override
    public void readFile() throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputPath), INPUT_STREAM_FORMAT));
        CSVParser parser = CSVParser.parse(in,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withQuoteMode(QuoteMode.MINIMAL).withTrim());

        int recordsParsed = 0;

        System.out.println(Messages.startProcessingInputFile(inputPath));

        for (CSVRecord record : parser) {
            if (record.size() < 24) {
                System.out.println(Messages.skippingInvalidRecord(record.toString()));
                continue;
            }

            AuthDotNetExportRow row = new AuthDotNetExportRow();

            row.setCustomerProfileID(clean(record.get(1)));
            row.setCustomerPaymentProfileID(clean(record.get(2)));
            row.setCustomerID(clean(record.get(3)));
            row.setDescription(clean(record.get(4)));
            row.setEmail(clean(record.get(5)));
            row.setCardNumber(clean(record.get(6)));
            row.setCardExpirationDate(clean(record.get(7)));
            row.setCardType(clean(record.get(8)));
            row.setCompany(clean(record.get(15)));
            row.setFirstName(clean(record.get(16)));
            row.setLastName(clean(record.get(17)));
            row.setAddress(clean(record.get(18)));
            row.setCity(clean(record.get(19)));
            row.setStateProv(clean(record.get(20)));
            row.setZip(clean(record.get(21)));
            row.setCountry(clean(record.get(22)));
            row.setPhone(clean(record.get(23)));

            exportRows.add(row);

            recordsParsed++;
            MigrationService.printStatus(recordsParsed);
        }
        System.out.println(Messages.doneProcessingInputFile(inputPath));
    }

    private String clean(String input) {
        input = stripNonAsciiCharacters(input);
        input = stripControlCharacters(input);
        return stripQuotes(input).trim();
    }

    private String stripControlCharacters(String input) {
        return input.replaceAll(REGEX_CONTROL_CHARS, EMPTY_STRING);
    }

    private String stripNonAsciiCharacters(String input) {
        return input.replaceAll(REGEX_NON_ASCII_CHARS, EMPTY_STRING);
    }

    private String stripQuotes(String input) {
        return input.replaceAll(REGEX_QUOTED_STRING, EMPTY_STRING);
    }

    /**
     * Generates a JSON file mapping the Authorize.net customerProlfileId to the
     * new Square Customer ID.
     *
     * Example;
     * {"1204856132":"0DGX1Y6BBH4T6V8Q7GKABS7YKR", ... }
     *
     * This mapping is used by the card importer script.
     */
    @Override
    public void exportCustomerCardDataToJson() throws IOException {
        System.out.println(Messages.startGeneratingStripeCardJSON());

        ArrayList<StripeCustomerCardExport> customerCardExports = new ArrayList<StripeCustomerCardExport>();

        for (AuthDotNetExportRow exportRow : exportRows) {
            customerCardExports.add(exportRow.toStripeCustomerCardExport());
        }
        HashMap<String, StripeCustomerCardExport[]> exportCustomerCardMap = new HashMap<String, StripeCustomerCardExport[]>();
        exportCustomerCardMap.put(CUSTOMERS,
                customerCardExports.toArray(new StripeCustomerCardExport[exportCustomerCardMap.size()]));

        final Path out = Paths.get(outputPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(exportCustomerCardMap, writer);
        } finally {
            System.out.println(Messages.doneGeneratingStripeCardJSON(outputPath));
        }
    }

    /**
     * Generates a Square Dashboard CSV import file of the Customer data
     * exported from Authorize.net. This file is meant to be manually uploaded
     * into the merchant's Square Dashboard to greatly increase the speed of
     * customer generation versus using the current Connect V2 APIs
     */
    @Override
    public void exportCustomerDataToCsv() throws IOException {
        System.out.println(Messages.startGeneratingDashboardCustomerCsv());

        Map<String, AuthDotNetExportRow> uniqueCustomers = getUniqueCustomerRecords();

        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.ISO_8859_1);
        try (CSVPrinter printer = new CSVPrinter(out,
                CSVFormat.DEFAULT.withHeader(AuthDotNetExportRow.HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {
            for (String customerProfileId : uniqueCustomers.keySet()) {
                AuthDotNetExportRow customerProfile = uniqueCustomers.get(customerProfileId);
                printer.printRecord(customerProfile.getFirstName(), customerProfile.getLastName(),
                        customerProfile.getCompany(), customerProfile.getEmail(), customerProfile.getPhone(),
                        customerProfile.getAddress(), EMPTY_STRING, customerProfile.getCity(),
                        customerProfile.getStateProv(), customerProfile.getZip(),
                        customerProfile.getCustomerProfileID(), customerProfile.getCustomerID());
            }
        }

        System.out.println(Messages.doneGeneratingDashboardCustomerCsv(outputPath));
    }

    private Map<String, AuthDotNetExportRow> getUniqueCustomerRecords() {
        HashMap<String, AuthDotNetExportRow> uniqueCustomers = new HashMap<String, AuthDotNetExportRow>();

        for (AuthDotNetExportRow row : exportRows) {
            uniqueCustomers.put(row.getCustomerProfileID(), row);
        }

        return uniqueCustomers;
    }
}
