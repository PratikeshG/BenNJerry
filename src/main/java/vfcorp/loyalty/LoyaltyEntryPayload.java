package vfcorp.loyalty;

import com.squareup.connect.v2.Customer;

public class LoyaltyEntryPayload {
    private String storeId;
    private String associateId;
    private Customer customer;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getAssociateId() {
        return associateId;
    }

    public void setAssociateId(String associateId) {
        this.associateId = associateId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
