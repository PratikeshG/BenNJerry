package tntfireworks;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CSVItem extends CsvRow {

    @Size(min = 1)
    @NotNull
    private String number;
    private String cat;
    private String category;
    private String description;
    private String casePacking;
    private String unitPrice;
    private String pricingUOM;
    private String suggestedPrice;
    private String sellingUOM;
    private String upc;
    private String netItem;
    private String expiredDate;
    private String effectiveDate;

    private String marketingPlan;
    private String bogo;
    private String itemNum3;
    private String currency;

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getCat() {
        return cat;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setCasePacking(String casePacking) {
        this.casePacking = casePacking;
    }

    public String getCasePacking() {
        return casePacking;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setPricingUOM(String pricingUOM) {
        this.pricingUOM = pricingUOM;
    }

    public String getPricingUOM() {
        return pricingUOM;
    }

    public void setSuggestedPrice(String suggestedPrice) {
        this.suggestedPrice = suggestedPrice;
    }

    public String getSuggestedPrice() {
        return suggestedPrice;
    }

    public void setSellingUOM(String sellingUOM) {
        this.sellingUOM = sellingUOM;
    }

    public String getSellingUOM() {
        return sellingUOM;
    }

    public void setUPC(String upc) {
        this.upc = upc;
    }

    public String getUPC() {
        return upc;
    }

    public void setNetItem(String netItem) {
        this.netItem = netItem;
    }

    public String getNetItem() {
        return netItem;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setBOGO(String bogo) {
        this.bogo = bogo;
    }

    public String getBOGO() {
        return bogo;
    }

    public void setItemNum3(String itemNum3) {
        this.itemNum3 = itemNum3;
    }

    public String getItemNum3() {
        return itemNum3;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMarketingPlan() {
        return marketingPlan;
    }

    public void setMarketingPlan(String marketingPlan) {
        this.marketingPlan = marketingPlan;
    }

    public static CSVItem fromCsvItemFields(String[] itemFields) {

        CSVItem item = new CSVItem();
        // trim and replace SQL chars
        // TODO(wtsang): determine more comprehensive check
        for (int i = 0; i < itemFields.length; i++) {
            itemFields[i] = itemFields[i].trim();
            itemFields[i] = itemFields[i].replaceAll("'", "''");
        }

        // TODO(wtsang): can use a HashMap + ArrayList to read in fields + add accordingly
        //               and add item constructor to take in HashMap to initialize item
        // item string fields should be in following order:
        //     0 - number;
        //     1 - cat;
        //     2 - category;
        //     3 - description;
        //     4 - casePacking;
        //     5 - unitPrice;
        //     6 - pricingUOM;
        //     7 - suggestedPrice;
        //     8 - sellingUOM;
        //     9 - upc;
        //     10 - netItem;
        //     11- expiredDate;
        //     12 - effectiveDate;
        //     13 - bogo;
        //     14 - itemNum3;
        //     15 - currency;
        //
        if (itemFields.length != 16)
            throw new IllegalArgumentException();

        item.setNumber(itemFields[0]);
        item.setCat(itemFields[1]);
        item.setCategory(itemFields[2]);
        item.setDescription(itemFields[3]);
        item.setCasePacking(itemFields[4]);
        item.setUnitPrice(itemFields[5]);
        item.setPricingUOM(itemFields[6]);
        item.setSuggestedPrice(itemFields[7]);
        item.setSellingUOM(itemFields[8]);
        item.setUPC(itemFields[9]);
        item.setNetItem(itemFields[10]);
        item.setExpiredDate(itemFields[11]);
        item.setEffectiveDate(itemFields[12]);
        item.setBOGO(itemFields[13]);
        item.setItemNum3(itemFields[14]);
        item.setCurrency(itemFields[15]);

        if (!item.isValid())
            throw new IllegalArgumentException();

        return item;
    }

}
