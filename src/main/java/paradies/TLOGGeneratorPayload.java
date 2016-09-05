package paradies;

import com.squareup.connect.Discount;
import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

import com.squareup.connect.diff.Catalog;

public class TLOGGeneratorPayload {

	private Merchant location;
	private String timeZone;
	private String defaultDeviceId;
	private Catalog catalog;
	private Employee[] employees;
	private Discount[] discounts;
	private Payment[] payments;

	public TLOGGeneratorPayload() {
		
	}

	public Merchant getLocation() {
		return location;
	}

	public void setLocation(Merchant location) {
		this.location = location;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getDefaultDeviceId() {
		return defaultDeviceId;
	}

	public void setDefaultDeviceId(String defaultDeviceId) {
		this.defaultDeviceId = defaultDeviceId;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
	}

	public Employee[] getEmployees() {
		return employees;
	}

	public void setEmployees(Employee[] employees) {
		this.employees = employees;
	}

	public Discount[] getDiscounts() {
		return discounts;
	}

	public void setDiscounts(Discount[] discounts) {
		this.discounts = discounts;
	}

	public Payment[] getPayments() {
		return payments;
	}

	public void setPayments(Payment[] payments) {
		this.payments = payments;
	}
}