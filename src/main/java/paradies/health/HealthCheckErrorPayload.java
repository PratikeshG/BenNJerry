package paradies.health;

import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class HealthCheckErrorPayload {

	private Merchant location;
	private Payment payment;
	private String error;

	public HealthCheckErrorPayload(Merchant location, Payment payment, String error) {
		this.location = location;
		this.payment = payment;
		this.error = error;
	}

	public Merchant getLocation() {
		return location;
	}

	public void setLocation(Merchant location) {
		this.location = location;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}