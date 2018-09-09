package migrations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import migrations.authorizedotnet.AuthorizeDotNetPaymentService;

public class MigrationService {
    private static final String SERVICE_AUTHORIZEDOTNET = "AUTHORIZEDOTNET";
    private static final String SERVICE_BRAINTREE = "BRAINTREE";
    private static final String SERVICE_STRIPE = "STRIPE";
    private static final String SERVICE_WORLDPAY = "WORLDPAY";

    private static final List<String> ACTIVE_SERVICES = new ArrayList<String>(Arrays.asList(SERVICE_AUTHORIZEDOTNET));

    private static final String ACTION_PREPARE_CUSTOMER_CARD_JSON = "card-json";
    private static final String ACTION_PREPARE_CUSTOMER_CSV = "customer-csv";
    private static final String ACTION_PREPARE_CUSTOMER_MAPPING = "customer-mapping";
    private static final String ACTION_IMPORT = "import";

    private static final List<String> VALID_ACTIONS = new ArrayList<String>(
            Arrays.asList(ACTION_PREPARE_CUSTOMER_CARD_JSON, ACTION_PREPARE_CUSTOMER_CSV,
                    ACTION_PREPARE_CUSTOMER_MAPPING, ACTION_IMPORT));

    private static String action;

    public static void main(String[] args) throws Exception {
        CommandLine parsedArgs = parseCommandLineArguments(args);

        String service = parsedArgs.getOptionValue("service");
        String input = parsedArgs.getOptionValue("input");
        String output = parsedArgs.getOptionValue("output");

        if (action.equals(ACTION_IMPORT)) {
            // TODO
            return;
        } else if (action.equals(ACTION_PREPARE_CUSTOMER_MAPPING)) {
            generateCustomerMapping(input, output);
            return;
        } else {
            // ACTION_PREPARE_CUSTOMER_CSV
            // ACTION_PREPARE_CARD_JSON
            if (!ACTIVE_SERVICES.contains(service)) {
                printErrorAndExit(
                        "Invalid payment service provided. Please select one of: " + ACTIVE_SERVICES.toString());
            }

            PaymentService paymentProvider;

            switch (service) {
                case SERVICE_AUTHORIZEDOTNET:
                default:
                    paymentProvider = new AuthorizeDotNetPaymentService(input, output);
                    break;
            }

            paymentProvider.readFile();

            if (action.equals(ACTION_PREPARE_CUSTOMER_CSV)) {
                paymentProvider.exportCustomerDataToCsv();
            } else if (action.equals(ACTION_PREPARE_CUSTOMER_CARD_JSON)) {
                paymentProvider.exportCustomerCardDataToJson();
            }
        }
    }

    private static CommandLine parseCommandLineArguments(String[] args) {
        Options options = new Options();

        action = args[args.length - 1];

        // Prepare
        Option optionService = new Option("service", true,
                "Payment service provider. One of: " + ACTIVE_SERVICES.toString());
        optionService.setRequired(isOptionRequiredForAction(action, "service"));
        options.addOption(optionService);

        Option optionInputPath = new Option("input", true, "File path for data import.");
        optionInputPath.setRequired(isOptionRequiredForAction(action, "input"));
        options.addOption(optionInputPath);

        Option optionOutputPath = new Option("output", true, "File path for data output.");
        optionOutputPath.setRequired(isOptionRequiredForAction(action, "output"));
        options.addOption(optionOutputPath);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        if (!isValidAction(action)) {
            printHelpAndExit("Invalid action requested. Valid actions: " + VALID_ACTIONS.toString(), options);
        }

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelpAndExit(e.getMessage(), options);
        }

        return cmd;
    }

    private static void printErrorAndExit(String error) {
        System.out.println("error: " + error);
        System.exit(1);
    }

    private static void printHelpAndExit(String message, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println("error: " + message);
        formatter.printHelp("migratonservice [Option]... [Action]", options);
        System.exit(1);
    }

    private static boolean isOptionRequiredForAction(String action, String option) {
        List<String> required = new ArrayList<String>();
        switch (action) {
            case ACTION_PREPARE_CUSTOMER_CARD_JSON: // same as below
            case ACTION_PREPARE_CUSTOMER_CSV:
                required.addAll(Arrays.asList("service", "input", "output"));
                break;
            case ACTION_PREPARE_CUSTOMER_MAPPING:
                required.addAll(Arrays.asList("input", "output"));
                break;
            case ACTION_IMPORT:
                break;
        }
        return required.contains(option);
    }

    private static boolean isValidAction(String action) {
        return VALID_ACTIONS.contains(action);
    }

    private static void generateCustomerMapping(String inputPath, String outputPath) throws IOException {
        System.out.println("Generating Customer External ID to Square Customer ID mapping file for card importer");

        HashMap<String, String> customerMapping = new HashMap<String, String>();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), "ISO-8859-1"));
        CSVParser parser = CSVParser.parse(in,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withQuoteMode(QuoteMode.MINIMAL).withTrim());

        int recordsParsed = 0;

        System.out.println("Processing input file: " + inputPath);

        for (CSVRecord record : parser) {
            String externalCustomerId = record.get(0);
            String squareCustomerId = record.get(14);

            customerMapping.put(externalCustomerId, squareCustomerId);

            recordsParsed++;
            if (recordsParsed % 1000 == 0) {
                System.out.print(".");
            }
            if (recordsParsed % 100000 == 0) {
                System.out.print("\n");
            }
        }

        final Path out = Paths.get(outputPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.ISO_8859_1,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);) {
            // read old content, manipulate, write new contents
            Gson gson = new GsonBuilder().create();
            gson.toJson(customerMapping, writer);
        } finally {
            System.out.println(
                    "\nDone generating Customer External ID to Square Customer ID mapping file for card importer: "
                            + outputPath);
        }
    }
}
