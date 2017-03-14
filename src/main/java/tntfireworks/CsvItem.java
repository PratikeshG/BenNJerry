package tntfireworks;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.base.Preconditions;
import com.squareup.connect.v2.Money;

public class CsvItem extends CsvRow implements Serializable {

    private static final String UNCATEGORIZED = "UNCATEGORIZED";

    @Size(min = 1)
    @NotNull
    private String number;
    private String cat;
    private String category;
    @NotNull
    @Size(min = 1)
    private String description;
    private String casePacking;
    private String unitPrice;
    private String pricingUOM;
    @NotNull
    @Size(min = 1)
    private String suggestedPrice;
    private String sellingUOM;
    @NotNull
    @Size(min = 1)
    private String upc;
    private String netItem;
    private String expiredDate;
    private String effectiveDate;
    @NotNull
    @Size(min = 1)
    private String marketingPlan;
    private String bogo;
    private String itemNum3;
    @NotNull
    @Size(min = 3)
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

    /*
     * Creates a CsvItem object from an ordered array of Strings, and sets the marketing plan
     * 
     * Note - trim() is performed on all fields, and dollar signs are stripped from suggestedPrice
     * 
     * The item string fields should be in following order:
     * 0 - number
     * 1 - cat
     * 2 - category
     * 3 - description
     * 4 - casePacking
     * 5 - unitPrice
     * 6 - pricingUOM
     * 7 - suggestedPrice
     * 8 - sellingUOM
     * 9 - upc
     * 10 - netItem
     * 11- expiredDate
     * 12 - effectiveDate
     * 13 - bogo
     * 14 - itemNum3
     * 15 - currency
     * 
     * @return the newly created CsvItem
     */
    public static CsvItem fromCsvItemFields(String[] itemFields, String marketingPlan) {

        CsvItem item = new CsvItem();
        // TODO(wtsang): determine more comprehensive cleaning operations based on prior CSV submissions
        for (int i = 0; i < itemFields.length; i++) {
            itemFields[i] = itemFields[i].trim();
            itemFields[i] = itemFields[i].replaceAll("'", "''");
        }

        if (itemFields.length != 16) {
            throw new IllegalArgumentException("Missing fields");
        }

        item.setNumber(itemFields[0]);
        item.setCat(itemFields[1]);
        item.setCategory(getCategoryNameFromCsvOrSetUncategorized(itemFields[2]));
        item.setDescription(itemFields[3]);
        item.setCasePacking(itemFields[4]);
        item.setUnitPrice(itemFields[5]);
        item.setPricingUOM(itemFields[6]);
        item.setSuggestedPrice(stripDollarSign(itemFields[7]));
        item.setSellingUOM(itemFields[8]);
        item.setUPC(itemFields[9]);
        item.setNetItem(itemFields[10]);
        item.setExpiredDate(itemFields[11]);
        item.setEffectiveDate(itemFields[12]);
        item.setBOGO(itemFields[13]);
        item.setItemNum3(itemFields[14]);
        item.setCurrency(itemFields[15]);
        item.setMarketingPlan(marketingPlan);

        if (!item.isValid()) {
            //TODO: wtsang - Add more validations
            throw new IllegalArgumentException();
        }

        return item;
    }

    private static String getCategoryNameFromCsvOrSetUncategorized(String categoryNameFromCsv) {
        if (categoryNameFromCsv == null || categoryNameFromCsv.trim().equals("")) {
            categoryNameFromCsv = UNCATEGORIZED;
        }
        return categoryNameFromCsv;
    }

    /*
     * Performs validation using Hiberante Validation framework
     * 
     * First the Annotation validators are applied (e.g. @NonNull
     * Second, we check that the Suggested Price can be converted to the Square money type
     * 
     * (non-Javadoc)
     * @see tntfireworks.CsvRow#isValid()
     */
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        try {
            this.getPriceAsSquareMoney();
        } catch (RuntimeException e) {
            return false;
        }

        return true;
    }

    private static String stripDollarSign(String string) {
        String stripped = string.replace("$", "");
        return stripped;
    }

    private String getUniqueItemString() {
        return " | MarketingPlan=" + this.getMarketingPlan() + " | ItemNumber=" + this.getNumber();
    }

    /*
     * Converts the CsvItem suggestedPrice and currency Strings to a Square Money object (cents)
     * 
     * @return Square Money object including int of cents and currency
     * 
     * @throws ArithmeticException if this has a nonzero fractional part, or will not fit in an int.
     * 
     */
    public Money getPriceAsSquareMoney() {
        Preconditions.checkNotNull(this.getSuggestedPrice(), "price cannot be null" + this.getUniqueItemString());
        Preconditions.checkArgument(Double.parseDouble(this.getSuggestedPrice()) > 0,
                "must be positive " + this.getUniqueItemString());
        Preconditions.checkNotNull(this.getCurrency(), "missing currency " + this.getUniqueItemString());
        Preconditions.checkArgument(isNumeric(this.getSuggestedPrice()));

        BigDecimal dollars = new BigDecimal(this.getSuggestedPrice());
        dollars = dollars.movePointRight(2);
        int cents = dollars.intValueExact();
        return new Money(cents, this.getCurrency());
    }

    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
