package migrations.authorizedotnet;

public class ExportRow {
    private String customerProfileID;
    private String customerPaymentProfileID;
    private String customerID;
    private String description;
    private String email;
    private String cardNumber;
    private String cardExpirationDate;
    private String cardType;
    private String company;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String stateProv;
    private String zip;
    private String country;
    private String phone;

    public String getCustomerProfileID() {
        return customerProfileID;
    }

    public void setCustomerProfileID(String customerProfileID) {
        this.customerProfileID = customerProfileID;
    }

    public String getCustomerPaymentProfileID() {
        return customerPaymentProfileID;
    }

    public void setCustomerPaymentProfileID(String customerPaymentProfileID) {
        this.customerPaymentProfileID = customerPaymentProfileID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpirationDate() {
        return cardExpirationDate;
    }

    public void setCardExpirationDate(String cardExpirationDate) {
        this.cardExpirationDate = cardExpirationDate;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String customerName() {
        return firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProv() {
        return stateProv;
    }

    public void setStateProv(String stateProv) {
        this.stateProv = stateProv;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
