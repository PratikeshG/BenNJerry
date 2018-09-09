package migrations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import migrations.authorizedotnet.ExportRow;

public abstract class PaymentService {
    protected List<ExportRow> exportRows;

    protected String inputPath;
    protected String outputPath;

    public PaymentService(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        exportRows = new ArrayList<ExportRow>();
    }

    public abstract void readFile() throws Exception;

    /*
     * Stripe customer card JSON format:
     *
     *# {
    #   "customers": [
    #       {
    #       "bank_accounts": [],
    #       "cards": [
    #         {
    #           "address_city": null,
    #           "address_country": null,
    #           "address_line1": null,
    #           "address_line2": null,
    #           "address_state": null,
    #           "address_zip": null,
    #           "exp_month": 4,
    #           "exp_year": 2020,
    #           "id": "card_AKuFBraZocQXP0",
    #           "name": "Jeff Jo",
    #           "number": "39393"
    #         }
    #       ],
    #       "description": "Blah",
    #       "email": "blah@blah.com",
    #       "id": "cus_XXXXXXXX",
    #       "metadata": {},
    #       "name": "Blah Blah",
    #       "type": "individual"
    #     },
    #   ]
    # }
     *
     */
    public abstract void exportCustomerCardDataToJson() throws IOException;

    public abstract void exportCustomerDataToCsv() throws IOException;
}
