package chase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.connect.v2.Error;
import com.squareup.connect.v2.Order;

public class ChasePromoResponse {
    @SerializedName("promo_code")
    private String promoCode;
    @SerializedName("promo_applied")
    private boolean promoApplied;
    private Order order;
    private Error[] errors;

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Error[] getErrors() {
        return errors;
    }

    public void setErrors(Error[] errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean isPromoApplied() {
        return promoApplied;
    }

    public void setPromoApplied(boolean promoApplied) {
        this.promoApplied = promoApplied;
    }
}
