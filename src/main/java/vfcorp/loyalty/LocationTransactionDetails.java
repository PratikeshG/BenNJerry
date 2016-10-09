package vfcorp.loyalty;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Transaction;

public class LocationTransactionDetails {
    private String storeId;
    private String timeZone;
    private Transaction[] transactions;
    private Payment[] payments;
    private Map<String, Employee> employees;
    private Map<String, Customer> customers;

    public LocationTransactionDetails(String storeId) {
        this(storeId, "America/Los_Angeles", new Transaction[0], new Payment[0], new HashMap<String, Employee>(),
                new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(String storeId, Transaction[] transactions) {
        this(storeId, "America/Los_Angeles", transactions, new Payment[0], new HashMap<String, Employee>(),
                new HashMap<String, Customer>());

    }

    public LocationTransactionDetails(String storeId, String timeZone, Transaction[] transactions) {
        this(storeId, timeZone, transactions, new Payment[0], new HashMap<String, Employee>(),
                new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(String storeId, String timeZone, Transaction[] transactions, Payment[] payments) {
        this(storeId, timeZone, transactions, payments, new HashMap<String, Employee>(),
                new HashMap<String, Customer>());
    }

    public LocationTransactionDetails(String storeId, String timeZone, Transaction[] transactions, Payment[] payments,
            Map<String, Employee> employees, Map<String, Customer> customers) {
        this.storeId = storeId;
        this.timeZone = timeZone;
        this.transactions = transactions;
        this.payments = payments;
        this.employees = employees;
        this.customers = customers;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
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
