package migrations;

public class Messages {
    public static String NEWLINE = "\n";
    public static String PERIOD = ".";

    public static String cmdHelpMessage() {
        return "migratonservice [Option]... [Action]";
    }

    public static String error(String errorMsg) {
        return String.format("error: %s", errorMsg);
    }

    public static String startProcessingInputFile(String path) {
        return String.format("Processing input file: %s", path);
    }

    public static String doneProcessingInputFile(String path) {
        return String.format("\nDone processing input file: %s", path);
    }

    public static String startGeneratingCustomerMapping() {
        return "Generating Customer External ID to Square Customer ID mapping file for card importer";
    }

    public static String doneGeneratingCustomerMapping(String outputPath) {
        return String.format(
                "\nDone generating Customer External ID to Square Customer ID mapping file for card importer: %s",
                outputPath);
    }

    public static String startGeneratingStripeCardJSON() {
        return "Generating Stripe-formatted Customer cards JSON file";
    }

    public static String doneGeneratingStripeCardJSON(String outputPath) {
        return String.format("Done generating Stripe-formatted Customer cards JSON file: %s", outputPath);
    }

    public static String startGeneratingDashboardCustomerCsv() {
        return "Generating Dashboard Customer CSV import file";
    }

    public static String doneGeneratingDashboardCustomerCsv(String outputPath) {
        return String.format("Done generating Dashboard Customer CSV import file: : %s", outputPath);
    }

    public static String skippingInvalidRecord(String record) {
        return String.format("Skipping invalid record: %s", record);
    }

    public static String invalidScriptAction(String validScriptActions) {
        return String.format("Invalid action requested. Valid actions: %s", validScriptActions);
    }

    public static String invalidPaymentService(String activePaymentServices) {
        return String.format("Invalid payment service provided. Please select one of: %s", activePaymentServices);
    }

    public static String errorParsingCardExpirationDate(String errorMessage, String customerId) {
        return String.format("Error parsing expiration date for customer record: %s -- %s", customerId, errorMessage);
    }
}
