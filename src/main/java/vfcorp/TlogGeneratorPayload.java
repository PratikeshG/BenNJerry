package vfcorp;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;

import util.SquarePayload;

public class TlogGeneratorPayload {
    private SquarePayload squarePayload;
    private Merchant[] locations;
    private Payment[] payments;
    private Employee[] employees;
    private Map<String, Customer> customers = new HashMap<String, Customer>();
    private Map<String, String> params = new HashMap<String, String>();

    public SquarePayload getSquarePayload() {
        return squarePayload;
    }

    public void setSquarePayload(SquarePayload squarePayload) {
        this.squarePayload = squarePayload;
    }

    public Merchant[] getLocations() {
        return locations;
    }

    public void setLocations(Merchant[] locations) {
        this.locations = locations;
    }

    public Payment[] getPayments() {
        return payments;
    }

    public void setPayments(Payment[] payments) {
        this.payments = payments;
    }

    public Employee[] getEmployees() {
        return employees;
    }

    public void setEmployees(Employee[] employees) {
        this.employees = employees;
    }

    public Map<String, Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Map<String, Customer> customers) {
        this.customers = customers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
