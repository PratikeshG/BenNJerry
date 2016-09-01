package paradies.health;

import java.util.List;

import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.Refund;

public class HealthCheckPayload {

	private Merchant location;
	private List<Payment> payments;
	private List<Refund> refunds;
	private List<Payment> refundPayments;

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

	public List<Refund> getRefunds() {
		return refunds;
	}

	public void setRefunds(List<Refund> refunds) {
		this.refunds = refunds;
	}

	public List<Payment> getRefundPayments() {
		return refundPayments;
	}

	public void setRefundPayments(List<Payment> refundPayments) {
		this.refundPayments = refundPayments;
	}
}