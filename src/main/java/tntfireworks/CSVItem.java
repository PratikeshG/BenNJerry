package tntfireworks;

public class CSVItem {

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
    private Boolean isValid; // TODO(wtsang): check required fields are populated and/or not null

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
}
