package vfcorp.plu;

public class ItemDbRecord {
    private String itemNumber;
    private String description;
    private String alternateDescription;
    private String retailPrice;
    private String deptNumber;
    private String classNumber;

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlternateDescription() {
        return alternateDescription;
    }

    public void setAlternateDescription(String alternateDescription) {
        this.alternateDescription = alternateDescription;
    }

    public String getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(String retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getDeptNumber() {
        return deptNumber;
    }

    public void setDeptNumber(String deptNumber) {
        this.deptNumber = deptNumber;
    }

    public String getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(String classNumber) {
        this.classNumber = classNumber;
    }
}
