package paradies.health;

import java.util.List;

import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class HealthCheckPayload {

	private Merchant location;
	private List<Payment> payments;

	public HealthCheckPayload() {
		
	}

	public Merchant getLocation() {
		return location;
	}

	public void setLocation(Merchant location) {
		this.location = location;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}
}