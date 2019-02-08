package migrations;

import java.io.IOException;

public abstract class PaymentService {
    protected String inputPath;
    protected String outputPath;

    public PaymentService(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    public abstract void readFile() throws Exception;

    /*
     * Generates a JSON file of provided Payment Providers Customer/Card
     * data in Stripe customer card export JSON format:
     *
     * Stripe card export JSON format:
     *
     * {
          "customers": [
            {
              "id": "cus_abc123def456",
              "email": "jenny.rosen@example.com",
              "description": "Jenny Rosen",
              "default_source": "card_edf214abc789",
              "type": "individual",
              "metadata": {
                "color_preference": "turquoise",
                ...
              },
              "cards": [
                {
                  "id": "card_edf214abc789",
                  "number":"4242424242424242",
                  "name": "Jeny Rosen",
                  "exp_month": 1,
                  "exp_year": 2020,
                  "address_line1": "123 Main St.",
                  "address_line2": null,
                  "address_city": "Springfield",
                  "address_state": "MA",
                  "address_zip": "01101",
                  "address_country": "US"
                },
                ...
              ]
            },
            ...
          ]
    */
    public abstract void exportCustomerCardDataToJson() throws IOException;

    /**
     * Generates a Square Dashboard CSV import file of the Card/Customer data
     * exported from Payment Provider. This file is meant to be manually
     * uploaded into the merchant's Square Dashboard to greatly increase the
     * speed of customer generation versus using the current Connect V2 APIs
     */
    public abstract void exportCustomerDataToCsv() throws IOException;
}
