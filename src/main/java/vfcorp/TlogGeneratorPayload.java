package vfcorp;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;

import util.SquarePayload;

public class TlogGeneratorPayload {
    private SquarePayload squarePayload;
    private Payment[] payments;
    private Map<String, String> employees = new HashMap<String, String>();
    private Map<String, Customer> customers = new HashMap<String, Customer>();
    private Map<String, String> params = new HashMap<String, String>();

    public SquarePayload getSquarePayload() {
        return squarePayload;
    }

    public void setSquarePayload(SquarePayload squarePayload) {
        this.squarePayload = squarePayload;
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

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getEmployees() {
        return employees;
    }

    public void setEmployees(Map<String, String> employees) {
        this.employees = employees;
    }
}
