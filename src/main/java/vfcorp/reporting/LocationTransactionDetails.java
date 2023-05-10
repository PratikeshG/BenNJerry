package vfcorp.reporting;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.Transaction;

public class LocationTransactionDetails {
    private Location location;
    private Transaction[] transactions;
    private Order[] orders;
    private com.squareup.connect.Payment[] v1payments;
    private Payment[] payments;
    private Map<String, Payment> tenderToPayment;
    private Map<String, Customer> customers;

    public LocationTransactionDetails(Location location) {
        this(location, new Transaction[0], new com.squareup.connect.Payment[0], new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(Location location, Transaction[] transactions) {
        this(location, transactions, new com.squareup.connect.Payment[0], new HashMap<String, Customer>());

    }

    public LocationTransactionDetails(Location location, Transaction[] transactions, com.squareup.connect.Payment[] v1payments) {
        this(location, transactions, v1payments, new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(Location location, Transaction[] transactions, com.squareup.connect.Payment[] v1payments,
            Map<String, Customer> customers) {
        this.setLocation(location);
        this.transactions = transactions;
        this.v1payments = v1payments;
        this.customers = customers;
    }

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

    public Transaction[] getTransactions() {
        return transactions;
    }

    public void setTransactions(Transaction[] transactions) {
        this.transactions = transactions;
    }

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
    	this.orders = orders;
    }

    public com.squareup.connect.Payment[] getv1Payments() {
        return v1payments;
    }

    public void setv1Payments(com.squareup.connect.Payment[] payments) {
        this.v1payments = payments;
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
