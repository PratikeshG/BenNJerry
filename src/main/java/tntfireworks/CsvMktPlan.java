package tntfireworks;

import java.util.ArrayList;

public class CsvMktPlan {

    public static final String HEADER_ROW = "Item Number,CAT,Category,Item Description,Case Packing, Unit Price,Pricing UOM,Suggested Selling Price,Selling UOM,UPC,Net Item,Expired Date, Effective Date,Discount,3rd ItemNumber,Currency Code,Not Used,Selling Price";
    private String name;
    private ArrayList<CsvItem> items;

    public CsvMktPlan() {
        items = new ArrayList<CsvItem>();
        name = "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addItem(CsvItem item) {
        items.add(item);
    }

    public ArrayList<CsvItem> getAllItems() {
        return items;
    }

    public void clearItems() {
        items.clear();
    }
}
