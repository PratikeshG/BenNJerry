package tntfireworks.reporting;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.Settlement;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Transaction;

/*
 * tntfireworks.reporting.TntLocationDetails class
 * 
 * NOTE: This class is similar to vfcorp.reporting.LocationTransactionDetails 
 *       and should be merged into a central package
 */
public class TntLocationDetails {
    private Location location;
    private Transaction[] transactions;
    private Payment[] payments;
    private Settlement[] settlements;
    private Payment[] cumulativePayments;
    private Map<String, Employee> employees;
    private Map<String, Customer> customers;
    private String merchantId;

    public TntLocationDetails(Location location) {
        this(location, new Transaction[0], new Payment[0], new HashMap<String, Employee>(),
                new HashMap<String, Customer>(), new Settlement[0], "");
    }

    public TntLocationDetails(Location location, Transaction[] transactions) {
        this(location, transactions, new Payment[0], new HashMap<String, Employee>(), new HashMap<String, Customer>(),
                new Settlement[0], "");
    }

    public TntLocationDetails(Location location, Transaction[] transactions, Payment[] payments,
            Settlement[] settlements, Map<String, Employee> employees, String merchantId) {
        this(location, transactions, payments, employees, new HashMap<String, Customer>(),
                settlements, merchantId);
    }

    public TntLocationDetails(Location location, Transaction[] transactions, Payment[] payments,
            Map<String, Employee> employees, Map<String, Customer> customers, Settlement[] settlements,
            String merchantId) {
        this.setLocation(location);
        this.transactions = transactions;
        this.payments = payments;
        this.employees = employees;
        this.customers = customers;
        this.settlements = settlements;
        this.merchantId = merchantId;
    }

    public String getMerchantId() {
        return merchantId;
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

    public Settlement[] getSettlements() {
        return settlements;
    }

    public void setSettlements(Settlement[] settlements) {
        this.settlements = settlements;
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
