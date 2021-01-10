package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class PreferredCustomer extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 25;
        id = "010";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Preferred Customer Number", new FieldDetails(20, 4, "Left justified, space filled"));
        fields.put("OffWAN Indicator", new FieldDetails(1, 24, ""));
        fields.put("Non Validated Indicator", new FieldDetails(1, 25, ""));
    }

    public PreferredCustomer() {
        super();
    }

    public PreferredCustomer(String record) {
        super(record);
    }

    @Override
    public Map<String, FieldDetails> getFields() {
        return fields;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getId() {
        return id;
    }

    public PreferredCustomer parse(String customerId) throws Exception {
        return parse(customerId, "", "1");
    }

    public PreferredCustomer parse(String customerId, String wanIndicator, String validatedIndicator) throws Exception {
        putValue("Preferred Customer Number", customerId);
        putValue("OffWAN Indicator", wanIndicator); // Null = Local PCM
        putValue("Non Validated Indicator", validatedIndicator); // 1 = PCM not validated

        return this;
    }
}
