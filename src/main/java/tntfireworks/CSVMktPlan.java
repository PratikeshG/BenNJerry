package tntfireworks;

import java.util.ArrayList;

public class CSVMktPlan {

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
