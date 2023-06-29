package vfcorp.reporting;

import java.util.Map;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;

public class LocationTransactionDetails {
    private Location location;
    private Order[] orders;
    private Payment[] payments;
    private Map<String, Payment> tenderToPayment;
    private Map<String, Customer> customers;

    public LocationTransactionDetails(Location location, Order[] orders, Payment[] payments,
            Map<String, Customer> customers, Map<String, Payment> tenderToPayment) {
        this.setLocation(location);
        this.orders = orders;
        this.payments = payments;
        this.customers = customers;
        this.tenderToPayment = tenderToPayment;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
    	this.orders = orders;
    }

    public Payment[] getPayments() {
        return payments;
    }

    public void setPayments(Payment[] payments) {
        this.payments = payments;
    }

    public Map<String, Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Map<String, Customer> customers) {
        this.customers = customers;
    }

    public Map<String, Payment> getTenderToPayment() {
        return tenderToPayment;
    }

    public void setTenderToPayment(Map<String, Payment> tenderToPayment) {
        this.tenderToPayment = tenderToPayment;
    }
}
