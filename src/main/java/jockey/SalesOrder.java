package jockey;

public class SalesOrder {
    private String thirdPartyOrderId;
    private String transactionNumber;
    private String storeNumber;
    private String dateCreated;
    private String dateCompleted;
    private String salesOrderCode;
    private String registerNumber;
    private String cashier;
    private String shippingTotal;
    private String taxTotal;
    private String total;
    private SalesOrderPayment[] payments;
    private SalesOrderLineItem[] lineItems;

    public SalesOrder() {
    }

    public String getThirdPartyOrderId() {
        return thirdPartyOrderId;
    }

    public void setThirdPartyOrderId(String thirdPartyOrderId) {
        this.thirdPartyOrderId = thirdPartyOrderId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public String getSalesOrderCode() {
        return salesOrderCode;
    }

    public void setSalesOrderCode(String salesOrderCode) {
        this.salesOrderCode = salesOrderCode;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public String getShippingTotal() {
        return shippingTotal;
    }

    public void setShippingTotal(String shippingTotal) {
        this.shippingTotal = shippingTotal;
    }

    public String getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(String taxTotal) {
        this.taxTotal = taxTotal;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public SalesOrderPayment[] getPayments() {
        return payments;
    }

    public void setPayments(SalesOrderPayment[] payments) {
        this.payments = payments;
    }

    public SalesOrderLineItem[] getLineItems() {
        return lineItems;
    }

    public void setLineItems(SalesOrderLineItem[] lineItems) {
        this.lineItems = lineItems;
    }

}
