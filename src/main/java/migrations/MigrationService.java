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
import migrations.stripe.StripePaymentService;

public class MigrationService {
    private static final String SERVICE_AUTHORIZEDOTNET = "AUTHORIZEDOTNET";
    private static final String SERVICE_STRIPE = "STRIPE";

    private static final List<String> ACTIVE_SERVICES = new ArrayList<String>(
            Arrays.asList(SERVICE_AUTHORIZEDOTNET, SERVICE_STRIPE));

    private static final String ACTION_PREPARE_CUSTOMER_CARD_JSON = "card-json";
    private static final String ACTION_PREPARE_CUSTOMER_CSV = "customer-csv";
    private static final String ACTION_PREPARE_CUSTOMER_MAPPING = "customer-mapping";

    private static final String OPTION_INPUT = "input";
    private static final String OPTION_OUTPUT = "output";
    private static final String OPTION_SERVICE = "service";

    private static final String INPUT_STREAM_FORMAT = "ISO-8859-1";

    private static final List<String> VALID_ACTIONS = new ArrayList<String>(Arrays
            .asList(ACTION_PREPARE_CUSTOMER_CARD_JSON, ACTION_PREPARE_CUSTOMER_CSV, ACTION_PREPARE_CUSTOMER_MAPPING));

    private static String action;

    public static void main(String[] args) throws Exception {
        CommandLine parsedArgs = parseCommandLineArguments(args);

        String service = parsedArgs.getOptionValue(OPTION_SERVICE);
        String input = parsedArgs.getOptionValue(OPTION_INPUT);
        String output = parsedArgs.getOptionValue(OPTION_OUTPUT);

        if (action.equals(ACTION_PREPARE_CUSTOMER_MAPPING)) {
            generateCustomerMappingFromDashboardExport(input, output);
            return;
        } else {
            // ACTION_PREPARE_CUSTOMER_CSV
            // ACTION_PREPARE_CARD_JSON
            if (!ACTIVE_SERVICES.contains(service)) {
                printErrorAndExit(Messages.invalidPaymentService(ACTIVE_SERVICES.toString()));
            }

            PaymentService paymentProvider;

            switch (service) {
                case SERVICE_STRIPE:
                    paymentProvider = new StripePaymentService(input, output);
                    break;
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
        Option optionService = new Option(OPTION_SERVICE, true,
                Messages.invalidPaymentService(ACTIVE_SERVICES.toString()));
        optionService.setRequired(isOptionRequiredForAction(action, OPTION_SERVICE));
        options.addOption(optionService);

        Option optionInputPath = new Option(OPTION_INPUT, true, "File path for data import.");
        optionInputPath.setRequired(isOptionRequiredForAction(action, OPTION_INPUT));
        options.addOption(optionInputPath);

        Option optionOutputPath = new Option(OPTION_OUTPUT, true, "File path for data output.");
        optionOutputPath.setRequired(isOptionRequiredForAction(action, OPTION_OUTPUT));
        options.addOption(optionOutputPath);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        if (!isValidAction(action)) {
            printHelpAndExit(Messages.invalidScriptAction(VALID_ACTIONS.toString()), options);
        }

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelpAndExit(e.getMessage(), options);
        }

        return cmd;
    }

    private static void printErrorAndExit(String error) {
        System.out.println(Messages.error(error));
        System.exit(1);
    }

    private static void printHelpAndExit(String message, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println(Messages.error(message));
        formatter.printHelp(Messages.cmdHelpMessage(), options);
        System.exit(1);
    }

    private static boolean isOptionRequiredForAction(String action, String option) {
        List<String> required = new ArrayList<String>();
        switch (action) {
            case ACTION_PREPARE_CUSTOMER_CARD_JSON: // same as below
            case ACTION_PREPARE_CUSTOMER_CSV:
                required.addAll(Arrays.asList(OPTION_SERVICE, OPTION_INPUT, OPTION_OUTPUT));
                break;
            case ACTION_PREPARE_CUSTOMER_MAPPING:
                required.addAll(Arrays.asList(OPTION_INPUT, OPTION_OUTPUT));
                break;
        }
        return required.contains(option);
    }

    private static boolean isValidAction(String action) {
        return VALID_ACTIONS.contains(action);
    }

    /**
     * Generates a JSON file mapping the PaymentService CustomerId to the
     * new Square Customer ID.
     *
     * Example;
     * {"1204856132":"0DGX1Y6BBH4T6V8Q7GKABS7YKR", ... }
     *
     * This mapping is used by the card importer script.
     */
    private static void generateCustomerMappingFromDashboardExport(String inputPath, String outputPath)
            throws IOException {
        System.out.println(Messages.startGeneratingCustomerMapping());

        HashMap<String, String> customerMapping = new HashMap<String, String>();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputPath), INPUT_STREAM_FORMAT));
        CSVParser parser = CSVParser.parse(in,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withQuoteMode(QuoteMode.MINIMAL).withTrim());

        int recordsParsed = 0;

        System.out.println(Messages.startProcessingInputFile(inputPath));

        for (CSVRecord record : parser) {
            String externalCustomerId = record.get(0);
            String squareCustomerId = record.get(14);

            customerMapping.put(externalCustomerId, squareCustomerId);

            recordsParsed++;
            printStatus(recordsParsed);
        }

        final Path out = Paths.get(outputPath);
        try (final BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.ISO_8859_1,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(customerMapping, writer);
        } finally {
            System.out.println(Messages.doneGeneratingCustomerMapping(outputPath));
        }
    }

    public static void printStatus(int progress) {
        if (progress % 1000 == 0) {
            System.out.print(Messages.PERIOD);
        }
        if (progress % 100000 == 0) {
            System.out.print(Messages.NEWLINE);
        }
    }
}
