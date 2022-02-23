package vfcorp.reporting;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Transaction;

public class LocationTransactionDetails {
    private Location location;
    private Transaction[] transactions;
    private Payment[] payments;
    private Map<String, Customer> customers;

    public LocationTransactionDetails(Location location) {
        this(location, new Transaction[0], new Payment[0], new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(Location location, Transaction[] transactions) {
        this(location, transactions, new Payment[0], new HashMap<String, Customer>());

    }

    public LocationTransactionDetails(Location location, Transaction[] transactions, Payment[] payments) {
        this(location, transactions, payments, new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(Location location, Transaction[] transactions, Payment[] payments,
            Map<String, Customer> customers) {
        this.setLocation(location);
        this.transactions = transactions;
        this.payments = payments;
        this.customers = customers;
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
}
