package jockey;

public class SalesOrderLineItem {
    private String thirdPartyLineItemId;
    private String lineNumber;
    private String lineCode;
    private String sku;
    private String upc;
    private String quantity;
    private String displayName;
    private String listPrice;
    private String placedPrice;
    private String discountedItemPrice;
    private String extendedPrice;
    private String orderLevelDiscountAmount;
    private String lineItemDiscountAmount;
    private WeaklyTypedProperty[] weaklyTypedProperties;

    public SalesOrderLineItem() {

    }

    public String getThirdPartyLineItemId() {
        return thirdPartyLineItemId;
    }

    public void setThirdPartyLineItemId(String thirdPartyLineItemId) {
        this.thirdPartyLineItemId = thirdPartyLineItemId;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getListPrice() {
        return listPrice;
    }

    public void setListPrice(String listPrice) {
        this.listPrice = listPrice;
    }

    public String getPlacedPrice() {
        return placedPrice;
    }

    public void setPlacedPrice(String placedPrice) {
        this.placedPrice = placedPrice;
    }

    public String getDiscountedItemPrice() {
        return discountedItemPrice;
    }

    public void setDiscountedItemPrice(String discountedItemPrice) {
        this.discountedItemPrice = discountedItemPrice;
    }

    public String getExtendedPrice() {
        return extendedPrice;
    }

    public void setExtendedPrice(String extendedPrice) {
        this.extendedPrice = extendedPrice;
    }

    public String getOrderLevelDiscountAmount() {
        return orderLevelDiscountAmount;
    }

    public void setOrderLevelDiscountAmount(String orderLevelDiscountAmount) {
        this.orderLevelDiscountAmount = orderLevelDiscountAmount;
    }

    public String getLineItemDiscountAmount() {
        return lineItemDiscountAmount;
    }

    public void setLineItemDiscountAmount(String lineItemDiscountAmount) {
        this.lineItemDiscountAmount = lineItemDiscountAmount;
    }

    public WeaklyTypedProperty[] getWeaklyTypedProperties() {
        return weaklyTypedProperties;
    }

    public void setWeaklyTypedProperties(WeaklyTypedProperty[] weaklyTypedProperties) {
        this.weaklyTypedProperties = weaklyTypedProperties;
    }
}
