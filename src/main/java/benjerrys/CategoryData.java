package benjerrys;

import java.util.List;

/**
 * The CategoryData class is a convenience object used to aggregate and cache
 * sales totals (example: total items sold) for a group of product categories
 * (example: all ice cream products)
 *
 * @author bhartard
 */
public class CategoryData {
    private String category;
    private int itemsSoldQty;
    private int grossSalesTotal;
    private int itemsRefundedQty;
    private int refundedTotal;
    private int discountsTotal;
    private int taxesTotal;

    /**
     * Class constructor specifying the category name
     *
     * @param category
     *            the string name of the category
     */
    public CategoryData(String category) {
        this.category = category;
        itemsSoldQty = 0;
        grossSalesTotal = 0;
        itemsRefundedQty = 0;
        refundedTotal = 0;
        discountsTotal = 0;
        taxesTotal = 0;
    }

    /**
     * Class constructor specifying the category name and list of existing
     * category data to aggregate
     *
     * @param category
     *            the string name of the category
     * @param mergeData
     *            a list of existing CategoryData objects to aggregate into
     *            this object
     */
    public CategoryData(String category, List<CategoryData> mergeData) {
        this(category);

        for (CategoryData data : mergeData) {
            itemsSoldQty += data.getItemsSoldQty();
            grossSalesTotal += data.getGrossSalesTotal();
            itemsRefundedQty += data.getItemsRefundedQty();
            refundedTotal += data.getRefundedTotal();
            discountsTotal += data.getDiscountsTotal();
            taxesTotal += data.getTaxesTotal();
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getItemsSoldQty() {
        return itemsSoldQty;
    }

    public void setItemsSoldQty(int itemsSoldQty) {
        this.itemsSoldQty = itemsSoldQty;
    }

    public void increaseItemsSoldQty(int total) {
        itemsSoldQty += total;
    }

    public int getGrossSalesTotal() {
        return grossSalesTotal;
    }

    public void setGrossSalesTotal(int grossSalesTotal) {
        this.grossSalesTotal = grossSalesTotal;
    }

    public void increaseGrossSalesTotal(int total) {
        grossSalesTotal += total;
    }

    public int getItemsRefundedQty() {
        return itemsRefundedQty;
    }

    public void setItemsRefundedQty(int itemsRefundedQty) {
        this.itemsRefundedQty = itemsRefundedQty;
    }

    public void increaseItemsRefundedQty(int total) {
        itemsRefundedQty -= total;
    }

    public int getRefundedTotal() {
        return refundedTotal;
    }

    public void setRefundedTotal(int refundedTotal) {
        this.refundedTotal = refundedTotal;
    }

    // Store refunds as negative values
    public void increaseRefundedTotal(int refundedTotal) {
        this.refundedTotal -= refundedTotal;
    }

    public int getReportableSalesTotal() {
        return grossSalesTotal + refundedTotal;
    }

    public int getDiscountsTotal() {
        return discountsTotal;
    }

    public void setDiscountsTotal(int discountsTotal) {
        this.discountsTotal = discountsTotal;
    }

    public void increaseDiscountsTotal(int total) {
        discountsTotal += total;
    }

    public void decreaseDiscountsTotal(int total) {
        discountsTotal -= total;
    }

    public int getNetSalesTotal() {
        return getReportableSalesTotal() + discountsTotal;
    }

    public int getTaxesTotal() {
        return taxesTotal;
    }

    public void setTaxesTotal(int taxesTotal) {
        this.taxesTotal = taxesTotal;
    }

    public void increaseTaxesTotal(int total) {
        this.taxesTotal += total;
    }

    public void decreaseTaxesTotal(int total) {
        this.taxesTotal -= total;
    }
}
