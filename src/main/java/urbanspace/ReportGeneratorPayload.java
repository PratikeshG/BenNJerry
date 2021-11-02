package urbanspace;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.v2.CatalogObject;

import util.SquarePayload;

public class ReportGeneratorPayload {
    private SquarePayload squarePayload;
    private Payment[] payments;
    private Refund[] refunds;
    private Payment[] refundPayments;
    private CatalogObject[] categories;
    private CatalogObject[] discounts;
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

    public CatalogObject[] getCategories() {
        return categories;
    }

    public void setCategories(CatalogObject[] categories) {
        this.categories = categories;
    }

    public CatalogObject[] getDiscounts() {
        return discounts;
    }

    public void setDiscounts(CatalogObject[] discounts) {
        this.discounts = discounts;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
