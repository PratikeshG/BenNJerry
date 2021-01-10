package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class InternationalCustomerNameAddress extends Record {

    private static Map<String, FieldDetails> fields;
    private static int length;
    private static String id;

    static {
        fields = new HashMap<String, FieldDetails>();
        length = 368;
        id = "084";

        fields.put("Identifier", new FieldDetails(3, 1, ""));
        fields.put("Alternate Family Name", new FieldDetails(30, 4, "Left justified, space filled"));
        fields.put("Alternate Given Name", new FieldDetails(20, 34, "Left justified, space filled"));
        fields.put("Postal Symbol", new FieldDetails(5, 54, "space filled"));
        fields.put("Mail Flag", new FieldDetails(1, 59, ""));
        fields.put("Email Flag", new FieldDetails(1, 60, ""));
        fields.put("Birth Date", new FieldDetails(8, 61, ""));
        fields.put("Comments", new FieldDetails(100, 69, "Left justified, space filled"));
        fields.put("Cell Mail", new FieldDetails(100, 169, "Left justified, space filled"));
        fields.put("Title", new FieldDetails(10, 269, "Left justified, space filled"));
        fields.put("Reserved", new FieldDetails(90, 279, "Space-filled"));
    }

    public InternationalCustomerNameAddress() {
        super();
    }

    public InternationalCustomerNameAddress(String record) {
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

    public InternationalCustomerNameAddress parse() throws Exception {
        putValue("Mail Flag", "0");
        putValue("Email Flag", "0");

        return this;
    }
}
