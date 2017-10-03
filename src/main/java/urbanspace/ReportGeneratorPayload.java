package urbanspace;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Category;
import com.squareup.connect.Discount;
import com.squareup.connect.Payment;
import com.squareup.connect.Refund;

import util.SquarePayload;

public class ReportGeneratorPayload {
    private SquarePayload squarePayload;
    private Payment[] payments;
    private Refund[] refunds;
    private Payment[] refundPayments;
    private Category[] categories;
    private Discount[] discounts;
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

    public Refund[] getRefunds() {
        return refunds;
    }

    public void setRefunds(Refund[] refunds) {
        this.refunds = refunds;
    }

    public Payment[] getRefundPayments() {
        return refundPayments;
    }

    public void setRefundPayments(Payment[] refundPayments) {
        this.refundPayments = refundPayments;
    }

    public Category[] getCategories() {
        return categories;
    }

    public void setCategories(Category[] categories) {
        this.categories = categories;
    }

    public Discount[] getDiscounts() {
        return discounts;
    }

    public void setDiscounts(Discount[] discounts) {
        this.discounts = discounts;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
