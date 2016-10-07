package vfcorp.loyalty;

import java.text.ParseException;

import com.squareup.connect.v2.Customer;

import util.TimeManager;

public class LoyaltyEntry {
    private String brandString;
    private String storeNumber;
    private String languageCode;
    private String customerNumber;
    private String altKeyCode;
    private String firstName;
    private String lastName;
    private String gender;
    private String maritalStatus;
    private String createSource;
    private String birthdate;
    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;
    private String extra6;
    private String extra7;
    private String extra8;
    private String extra9;
    private String extra10;
    private String extra11;
    private String extra12;
    private String extra13;
    private String extra14;
    private String extra15;
    private String extra16;
    private String extra17;
    private String createDate;
    private String associateNumber;
    private String addressFormatCode;
    private String telephoneNumber;
    private String addressTypeCode;
    private String mailIndicator;
    private String streetAddress;
    private String address2;
    private String city;
    private String state;
    private String address5;
    private String address6;
    private String zip;
    private String telephoneExtNumber;
    private String sourceDatabaseId;
    private String sourceLastUpdateDate;
    private String sourceAssociateNumber;
    private String addressLongitude;
    private String addressLatitude;
    private String emailAddress;
    private String username;
    private String password;
    private String emailIndicator;
    private String addressActionCode;
    private String addressActiveFlag;
    private String addressEffectiveDate;
    private String addressExpiryDate;
    private String addressRecurringFlag;
    private String addressId;
    private String headOfHouseholdFlag;
    private String emailOptIn;
    private String emailOptInDate;
    private String extra18;
    private String mailOptIn;
    private String mailOptInDate;
    private String phoneOptIn;
    private String phoneOptInDate;
    private String phoneIndicator;
    private String membershipTypeCode;
    private String membershipDate;

    public LoyaltyEntry(String storeId, Customer customer, String associateId) throws ParseException {
	String customerCreateDate = TimeManager.dateFormatFromRFC3339(customer.getCreatedAt(), "America/Los_Angeles",
		"MM/dd/yy");

	brandString = "4";
	storeNumber = storeId != null ? storeId : "";
	languageCode = "ENG";
	customerNumber = customer.getReferenceId() != null ? customer.getReferenceId() : "";
	altKeyCode = "SQAR";
	firstName = customer.getGivenName() != null ? customer.getGivenName() : "";
	lastName = customer.getFamilyName() != null ? customer.getFamilyName() : "";
	gender = "U";
	maritalStatus = "U";
	createSource = "Square";
	birthdate = "0";
	extra1 = "";
	extra2 = "";
	extra3 = "";
	extra4 = "";
	extra5 = "";
	extra6 = "";
	extra7 = "";
	extra8 = "";
	extra9 = "";
	extra10 = "";
	extra11 = "0";
	extra12 = "0";
	extra13 = "0";
	extra14 = "0";
	extra15 = "0";
	extra16 = "0";
	extra17 = "A";
	createDate = customerCreateDate;
	associateNumber = associateId != null ? associateId : "";
	addressFormatCode = "USA";
	telephoneNumber = customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "";
	addressTypeCode = "HOME";
	mailIndicator = "9";
	streetAddress = (customer.getAddress() != null && customer.getAddress().getAddressLine1() != null)
		? customer.getAddress().getAddressLine1() : "";
	address2 = (customer.getAddress() != null && customer.getAddress().getAddressLine2() != null)
		? customer.getAddress().getAddressLine2() : "";
	city = (customer.getAddress() != null && customer.getAddress().getLocality() != null)
		? customer.getAddress().getLocality() : "";
	state = (customer.getAddress() != null && customer.getAddress().getAdministrativeDistrictLevel1() != null)
		? customer.getAddress().getAdministrativeDistrictLevel1() : "";
	address5 = "";
	address6 = "";
	zip = (customer.getAddress() != null && customer.getAddress().getPostalCode() != null)
		? customer.getAddress().getPostalCode() : "";
	telephoneExtNumber = "";
	sourceDatabaseId = "48888";
	sourceLastUpdateDate = TimeManager.toSimpleDateTimeInTimeZone(
		TimeManager.currentCalendar("America/Los_Angeles"), "America/Los_Angeles", "MM/dd/yy");
	sourceAssociateNumber = associateId != null ? associateId : "";
	addressLongitude = "";
	addressLatitude = "";
	emailAddress = customer.getEmailAddress() != null ? customer.getEmailAddress() : "";
	username = "";
	password = "";
	emailIndicator = "9";
	addressActionCode = "i";
	addressActiveFlag = "1";
	addressEffectiveDate = customerCreateDate;
	addressExpiryDate = "";
	addressRecurringFlag = "0";
	addressId = "1";
	headOfHouseholdFlag = "1";
	emailOptIn = customer.getEmailAddress() != null ? "1" : "0";
	emailOptInDate = customerCreateDate;
	extra18 = "u";
	mailOptIn = customer.getPhoneNumber() != null ? "1" : "0";
	mailOptInDate = customerCreateDate;
	phoneOptIn = customer.getPhoneNumber() != null ? "1" : "0";
	phoneOptInDate = customerCreateDate;
	phoneIndicator = "9";
	membershipTypeCode = "REGL";
	membershipDate = customerCreateDate;
    }

    @Override
    public String toString() {
	String[] cols = { brandString + storeNumber, languageCode, customerNumber, altKeyCode, firstName, lastName,
		gender, maritalStatus, createSource, birthdate, extra1, extra2, extra3, extra4, extra5, extra6, extra7,
		extra8, extra9, extra10, extra11, extra12, extra13, extra14, extra15, extra16, extra17, createDate,
		associateNumber, addressFormatCode, telephoneNumber, addressTypeCode, mailIndicator, streetAddress,
		address2, city, state, address5, address6, zip, telephoneExtNumber, sourceDatabaseId,
		sourceLastUpdateDate, sourceAssociateNumber, addressLongitude, addressLatitude, emailAddress, username,
		password, emailIndicator, addressActionCode, addressActiveFlag, addressEffectiveDate, addressExpiryDate,
		addressRecurringFlag, addressId, headOfHouseholdFlag, emailOptIn, emailOptInDate, extra18, mailOptIn,
		mailOptInDate, phoneOptIn, phoneOptInDate, phoneIndicator, membershipTypeCode, membershipDate };

	StringBuilder builder = new StringBuilder();
	String prefix = "";
	for (int i = 0; i < cols.length; i++) {
	    builder.append(prefix).append(cols[i]);
	    prefix = "\t";
	}
	return builder.toString();
    }

    public String getBrandString() {
	return brandString;
    }

    public void setBrandString(String brandString) {
	this.brandString = brandString;
    }

    public String getStoreNumber() {
	return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
	this.storeNumber = storeNumber;
    }

    public String getLanguageCode() {
	return languageCode;
    }

    public void setLanguageCode(String languageCode) {
	this.languageCode = languageCode;
    }

    public String getCustomerNumber() {
	return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
	this.customerNumber = customerNumber;
    }

    public String getAltKeyCode() {
	return altKeyCode;
    }

    public void setAltKeyCode(String altKeyCode) {
	this.altKeyCode = altKeyCode;
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

    public String getGender() {
	return gender;
    }

    public void setGender(String gender) {
	this.gender = gender;
    }

    public String getMaritalStatus() {
	return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
	this.maritalStatus = maritalStatus;
    }

    public String getCreateSource() {
	return createSource;
    }

    public void setCreateSource(String createSource) {
	this.createSource = createSource;
    }

    public String getBirthdate() {
	return birthdate;
    }

    public void setBirthdate(String birthdate) {
	this.birthdate = birthdate;
    }

    public String getExtra1() {
	return extra1;
    }

    public void setExtra1(String extra1) {
	this.extra1 = extra1;
    }

    public String getExtra2() {
	return extra2;
    }

    public void setExtra2(String extra2) {
	this.extra2 = extra2;
    }

    public String getExtra3() {
	return extra3;
    }

    public void setExtra3(String extra3) {
	this.extra3 = extra3;
    }

    public String getExtra4() {
	return extra4;
    }

    public void setExtra4(String extra4) {
	this.extra4 = extra4;
    }

    public String getExtra5() {
	return extra5;
    }

    public void setExtra5(String extra5) {
	this.extra5 = extra5;
    }

    public String getExtra6() {
	return extra6;
    }

    public void setExtra6(String extra6) {
	this.extra6 = extra6;
    }

    public String getExtra7() {
	return extra7;
    }

    public void setExtra7(String extra7) {
	this.extra7 = extra7;
    }

    public String getExtra8() {
	return extra8;
    }

    public void setExtra8(String extra8) {
	this.extra8 = extra8;
    }

    public String getExtra9() {
	return extra9;
    }

    public void setExtra9(String extra9) {
	this.extra9 = extra9;
    }

    public String getExtra10() {
	return extra10;
    }

    public void setExtra10(String extra10) {
	this.extra10 = extra10;
    }

    public String getExtra11() {
	return extra11;
    }

    public void setExtra11(String extra11) {
	this.extra11 = extra11;
    }

    public String getExtra12() {
	return extra12;
    }

    public void setExtra12(String extra12) {
	this.extra12 = extra12;
    }

    public String getExtra13() {
	return extra13;
    }

    public void setExtra13(String extra13) {
	this.extra13 = extra13;
    }

    public String getExtra14() {
	return extra14;
    }

    public void setExtra14(String extra14) {
	this.extra14 = extra14;
    }

    public String getExtra15() {
	return extra15;
    }

    public void setExtra15(String extra15) {
	this.extra15 = extra15;
    }

    public String getExtra16() {
	return extra16;
    }

    public void setExtra16(String extra16) {
	this.extra16 = extra16;
    }

    public String getExtra17() {
	return extra17;
    }

    public void setExtra17(String extra17) {
	this.extra17 = extra17;
    }

    public String getCreateDate() {
	return createDate;
    }

    public void setCreateDate(String createDate) {
	this.createDate = createDate;
    }

    public String getAssociateNumber() {
	return associateNumber;
    }

    public void setAssociateNumber(String associateNumber) {
	this.associateNumber = associateNumber;
    }

    public String getAddressFormatCode() {
	return addressFormatCode;
    }

    public void setAddressFormatCode(String addressFormatCode) {
	this.addressFormatCode = addressFormatCode;
    }

    public String getTelephoneNumber() {
	return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
	this.telephoneNumber = telephoneNumber;
    }

    public String getAddressTypeCode() {
	return addressTypeCode;
    }

    public void setAddressTypeCode(String addressTypeCode) {
	this.addressTypeCode = addressTypeCode;
    }

    public String getMailIndicator() {
	return mailIndicator;
    }

    public void setMailIndicator(String mailIndicator) {
	this.mailIndicator = mailIndicator;
    }

    public String getStreetAddress() {
	return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
	this.streetAddress = streetAddress;
    }

    public String getAddress2() {
	return address2;
    }

    public void setAddress2(String address2) {
	this.address2 = address2;
    }

    public String getCity() {
	return city;
    }

    public void setCity(String city) {
	this.city = city;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public String getAddress5() {
	return address5;
    }

    public void setAddress5(String address5) {
	this.address5 = address5;
    }

    public String getAddress6() {
	return address6;
    }

    public void setAddress6(String address6) {
	this.address6 = address6;
    }

    public String getZip() {
	return zip;
    }

    public void setZip(String zip) {
	this.zip = zip;
    }

    public String getTelephoneExtNumber() {
	return telephoneExtNumber;
    }

    public void setTelephoneExtNumber(String telephoneExtNumber) {
	this.telephoneExtNumber = telephoneExtNumber;
    }

    public String getSourceDatabaseId() {
	return sourceDatabaseId;
    }

    public void setSourceDatabaseId(String sourceDatabaseId) {
	this.sourceDatabaseId = sourceDatabaseId;
    }

    public String getSourceLastUpdateDate() {
	return sourceLastUpdateDate;
    }

    public void setSourceLastUpdateDate(String sourceLastUpdateDate) {
	this.sourceLastUpdateDate = sourceLastUpdateDate;
    }

    public String getSourceAssociateNumber() {
	return sourceAssociateNumber;
    }

    public void setSourceAssociateNumber(String sourceAssociateNumber) {
	this.sourceAssociateNumber = sourceAssociateNumber;
    }

    public String getAddressLongitude() {
	return addressLongitude;
    }

    public void setAddressLongitude(String addressLongitude) {
	this.addressLongitude = addressLongitude;
    }

    public String getAddressLatitude() {
	return addressLatitude;
    }

    public void setAddressLatitude(String addressLatitude) {
	this.addressLatitude = addressLatitude;
    }

    public String getEmailAddress() {
	return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
	this.emailAddress = emailAddress;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getEmailIndicator() {
	return emailIndicator;
    }

    public void setEmailIndicator(String emailIndicator) {
	this.emailIndicator = emailIndicator;
    }

    public String getAddressActionCode() {
	return addressActionCode;
    }

    public void setAddressActionCode(String addressActionCode) {
	this.addressActionCode = addressActionCode;
    }

    public String getAddressActiveFlag() {
	return addressActiveFlag;
    }

    public void setAddressActiveFlag(String addressActiveFlag) {
	this.addressActiveFlag = addressActiveFlag;
    }

    public String getAddressEffectiveDate() {
	return addressEffectiveDate;
    }

    public void setAddressEffectiveDate(String addressEffectiveDate) {
	this.addressEffectiveDate = addressEffectiveDate;
    }

    public String getAddressExpiryDate() {
	return addressExpiryDate;
    }

    public void setAddressExpiryDate(String addressExpiryDate) {
	this.addressExpiryDate = addressExpiryDate;
    }

    public String getAddressRecurringFlag() {
	return addressRecurringFlag;
    }

    public void setAddressRecurringFlag(String addressRecurringFlag) {
	this.addressRecurringFlag = addressRecurringFlag;
    }

    public String getAddressId() {
	return addressId;
    }

    public void setAddressId(String addressId) {
	this.addressId = addressId;
    }

    public String getHeadOfHouseholdFlag() {
	return headOfHouseholdFlag;
    }

    public void setHeadOfHouseholdFlag(String headOfHouseholdFlag) {
	this.headOfHouseholdFlag = headOfHouseholdFlag;
    }

    public String getEmailOptIn() {
	return emailOptIn;
    }

    public void setEmailOptIn(String emailOptIn) {
	this.emailOptIn = emailOptIn;
    }

    public String getEmailOptInDate() {
	return emailOptInDate;
    }

    public void setEmailOptInDate(String emailOptInDate) {
	this.emailOptInDate = emailOptInDate;
    }

    public String getExtra18() {
	return extra18;
    }

    public void setExtra18(String extra18) {
	this.extra18 = extra18;
    }

    public String getMailOptIn() {
	return mailOptIn;
    }

    public void setMailOptIn(String mailOptIn) {
	this.mailOptIn = mailOptIn;
    }

    public String getMailOptInDate() {
	return mailOptInDate;
    }

    public void setMailOptInDate(String mailOptInDate) {
	this.mailOptInDate = mailOptInDate;
    }

    public String getPhoneOptIn() {
	return phoneOptIn;
    }

    public void setPhoneOptIn(String phoneOptIn) {
	this.phoneOptIn = phoneOptIn;
    }

    public String getPhoneOptInDate() {
	return phoneOptInDate;
    }

    public void setPhoneOptInDate(String phoneOptInDate) {
	this.phoneOptInDate = phoneOptInDate;
    }

    public String getPhoneIndicator() {
	return phoneIndicator;
    }

    public void setPhoneIndicator(String phoneIndicator) {
	this.phoneIndicator = phoneIndicator;
    }

    public String getMembershipTypeCode() {
	return membershipTypeCode;
    }

    public void setMembershipTypeCode(String membershipTypeCode) {
	this.membershipTypeCode = membershipTypeCode;
    }

    public String getMembershipDate() {
	return membershipDate;
    }

    public void setMembershipDate(String membershipDate) {
	this.membershipDate = membershipDate;
    }
}
