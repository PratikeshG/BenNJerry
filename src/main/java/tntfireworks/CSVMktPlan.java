package tntfireworks;

import java.util.ArrayList;

public class CSVMktPlan {

    public static final String HEADER_ROW = "Item Number,CAT,Category,Item Description,Case Packing,Unit Price,Pricing UOM,Suggested Selling Price,Selling UOM,UPC,Net Item,Expired Date,Eff Date Date,Buy 1Get N Free Value,3rd Item Number,Cur Cod";
	private String name;
    private ArrayList<CSVItem> items;
    
    public CSVMktPlan() {
        items = new ArrayList<CSVItem>();
        name = "";
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void addItem(CSVItem item) {
        items.add(item);
    }
    
    public ArrayList<CSVItem> getAllItems() {
        return items;
    }
    
    public void clearItems() {
        items.clear();
    }     
}
