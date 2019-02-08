package migrations.stripe;

public class StripeCustomersExport {
    private StripeCustomer[] customers;

    public StripeCustomersExport() {
    }

    public StripeCustomer[] getCustomers() {
        return customers;
    }

    public void setCustomers(StripeCustomer[] customers) {
        this.customers = customers;
    }
}