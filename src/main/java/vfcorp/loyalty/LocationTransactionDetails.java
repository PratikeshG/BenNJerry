package vfcorp.loyalty;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Transaction;

public class LocationTransactionDetails {
    private Location location;
    private Transaction[] transactions;
    private Payment[] payments;
    private Payment[] cumulativePayments;
    private Map<String, Employee> employees;
    private Map<String, Customer> customers;

    public LocationTransactionDetails(Location location) {
        this(location, new Transaction[0], new Payment[0], new HashMap<String, Employee>(),
                new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(Location location, Transaction[] transactions) {
        this(location, transactions, new Payment[0], new HashMap<String, Employee>(), new HashMap<String, Customer>());

    }

    public LocationTransactionDetails(Location location, Transaction[] transactions, Payment[] payments) {
        this(location, transactions, payments, new HashMap<String, Employee>(), new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(Location location, Transaction[] transactions, Payment[] payments,
            Map<String, Employee> employees, Map<String, Customer> customers) {
        this.setLocation(location);
        this.transactions = transactions;
        this.payments = payments;
        this.employees = employees;
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

    public Payment[] getCumulativePayments() {
        return cumulativePayments;
    }

    public void setCumulativePayments(Payment[] cumulativePayments) {
        this.cumulativePayments = cumulativePayments;
    }

    public Map<String, Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Map<String, Employee> employees) {
        this.employees = employees;
    }

    public Map<String, Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Map<String, Customer> customers) {
        this.customers = customers;
    }
}
