package vfcorp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Merchant;
import com.squareup.connect.Money;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;
import com.squareup.connect.v2.Customer;

import vfcorp.tlog.Address;
import vfcorp.tlog.Associate;
import vfcorp.tlog.CashierRegisterIdentification;
import vfcorp.tlog.CreditCardTender;
import vfcorp.tlog.CrmLoyaltyIndicator;
import vfcorp.tlog.DiscountTypeIndicator;
import vfcorp.tlog.EmployeeDiscount;
import vfcorp.tlog.EventGiveback;
import vfcorp.tlog.ForInStoreReportingUseOnly;
import vfcorp.tlog.InternationalCustomerNameAddress;
import vfcorp.tlog.ItemTaxMerchandiseNonMerchandiseItemsFees;
import vfcorp.tlog.LineItemAccountingString;
import vfcorp.tlog.LineItemAssociateAndDiscountAccountingString;
import vfcorp.tlog.MerchandiseItem;
import vfcorp.tlog.Name;
import vfcorp.tlog.PhoneNumber;
import vfcorp.tlog.PreferredCustomer;
import vfcorp.tlog.ReasonCode;
import vfcorp.tlog.SubHeaderStoreSystemLocalizationInformation;
import vfcorp.tlog.TenderCount;
import vfcorp.tlog.TransactionHeader;
import vfcorp.tlog.TransactionSubTotal;
import vfcorp.tlog.TransactionTax;
import vfcorp.tlog.TransactionTaxExtended;
import vfcorp.tlog.TransactionTotal;

public class Tlog {
    private static final int MAX_TRANSACTION_NUMBER = 999999;
    private static final int MIN_CONFIGURED_DEVICES = 2;

    private List<Record> transactionLog;
    private int itemNumberLookupLength;
    private String deployment;
    private String timeZoneId;
    private static Logger logger = LoggerFactory.getLogger(Tlog.class);

    private ObjectStore<String> objectStore;
    private int nextTransactionNumber;
    private boolean isStoreforceTrickle;
    private boolean trackPriceOverrides;
    private int totalConfiguredDevices;

    public Tlog() {
        transactionLog = new LinkedList<Record>();
        nextTransactionNumber = 1;
    }

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public void setObjectStore(ObjectStore<String> objectStore) {
        this.objectStore = objectStore;
    }

    public void setIsStoreforceTrickle(boolean isStoreforceTrickle) {
        this.isStoreforceTrickle = isStoreforceTrickle;
    }

    public void trackPriceOverrides(boolean trackPriceOverrides) {
        this.trackPriceOverrides = trackPriceOverrides;
    }

    public void setTotalConfiguredDevices(int totalConfiguredDevices) {
        this.totalConfiguredDevices = totalConfiguredDevices;
    }

    public void parse(Merchant location, Payment[] squarePayments, Map<String, String> employees,
            Map<String, Customer> customerPaymentCache, String processingForDate) throws Exception {
        List<Payment> payments = Arrays.asList(squarePayments);

        createSaleRecords(location, payments, employees, customerPaymentCache);

        if (!isStoreforceTrickle) {
            createStoreCloseRecords(location, payments, processingForDate);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Record record : transactionLog) {
            sb.append(record.toString() + "\r\n");
        }

        return sb.toString();
    }

    private void createSaleRecords(Merchant location, List<Payment> squarePaymentsList,
            Map<String, String> squareEmployeesCache, Map<String, Customer> customerPaymentCache) throws Exception {

        for (Payment payment : squarePaymentsList) {

            Customer loyaltyCustomer = customerPaymentCache.get(payment.getId());

            String tenderType = payment.getTender()[0].getType();
            if (payment.getTender() != null && tenderType != null && tenderType.equals("NO_SALE")) {
                // Don't report $0 "No Sale" transactions
                continue;
            } else {

                LinkedList<Record> paymentList = new LinkedList<Record>();

                paymentList.add(new SubHeaderStoreSystemLocalizationInformation().parse());

                // 010 - CRM
                if (loyaltyCustomer != null) {
                    if (isVansDeployment()) {
                        paymentList.add(new PreferredCustomer().parse(loyaltyCustomer.getReferenceId(), "0", "0"));
                    } else {
                        paymentList.add(new PreferredCustomer().parse(loyaltyCustomer.getReferenceId()));
                    }
                }

                paymentList.add(new TransactionSubTotal().parse(payment));

                paymentList.add(new TransactionTax().parse(payment));

                paymentList.add(new TransactionTotal().parse(payment));

                for (PaymentTax tax : payment.getAdditiveTax()) {
                    paymentList.add(new TransactionTaxExtended().parse(payment, tax));
                }

                for (PaymentTax tax : payment.getInclusiveTax()) {
                    paymentList.add(new TransactionTaxExtended().parse(payment, tax));
                }

                if (loyaltyCustomer != null) {
                    // 029 - CRM
                    String firstName = loyaltyCustomer.getGivenName() != null ? loyaltyCustomer.getGivenName() : "";
                    String lastName = loyaltyCustomer.getFamilyName() != null ? loyaltyCustomer.getFamilyName() : "";
                    paymentList.add(new Name().parse(lastName, firstName));

                    // 030 - CRM
                    String addressLine1 = (loyaltyCustomer.getAddress() != null
                            && loyaltyCustomer.getAddress().getAddressLine1() != null)
                                    ? loyaltyCustomer.getAddress().getAddressLine1() : "";
                    String addressLine2 = (loyaltyCustomer.getAddress() != null
                            && loyaltyCustomer.getAddress().getAddressLine2() != null)
                                    ? loyaltyCustomer.getAddress().getAddressLine2() : "";
                    String city = (loyaltyCustomer.getAddress() != null
                            && loyaltyCustomer.getAddress().getLocality() != null)
                                    ? loyaltyCustomer.getAddress().getLocality() : "";
                    String state = (loyaltyCustomer.getAddress() != null
                            && loyaltyCustomer.getAddress().getAdministrativeDistrictLevel1() != null)
                                    ? loyaltyCustomer.getAddress().getAdministrativeDistrictLevel1() : "";
                    String zip = (loyaltyCustomer.getAddress() != null
                            && loyaltyCustomer.getAddress().getPostalCode() != null)
                                    ? loyaltyCustomer.getAddress().getPostalCode() : "";
                    String email = loyaltyCustomer.getEmailAddress() != null ? loyaltyCustomer.getEmailAddress() : "";
                    paymentList.add(new Address().parse(addressLine1, addressLine2, city, state, zip, email));

                    // 031 - CRM
                    String phone = loyaltyCustomer.getPhoneNumber() != null
                            ? loyaltyCustomer.getPhoneNumber().replaceAll("[^\\d]", "") : "";

                    // Vans should not have +1 prefix
                    if (isVansDeployment() && phone.length() == 11 && phone.startsWith("1")) {
                        phone = phone.substring(1);
                    }

                    paymentList.add(new PhoneNumber().parse("1", phone)); // home
                    paymentList.add(new PhoneNumber().parse("2", "")); // work
                    paymentList.add(new PhoneNumber().parse("3", "")); // cell

                    // 084
                    if (isVansDeployment()) {
                        paymentList.add(new InternationalCustomerNameAddress().parse());
                    }
                }

                String employeeId = "";
                boolean employeeIdShouldBePresent = false;
                boolean employeeFound = false;
                for (Tender tender : payment.getTender()) {
                    if (tender.getEmployeeId() != null) {
                        employeeIdShouldBePresent = true;
                        if (squareEmployeesCache.containsKey(tender.getEmployeeId())) {
                            if (squareEmployeesCache.get(tender.getEmployeeId()) != null) {
                                employeeId = squareEmployeesCache.get(tender.getEmployeeId());
                            }
                            employeeFound = true;
                        }
                    }
                }

                if (employeeIdShouldBePresent && !employeeFound) {
                    String err = "tender had an employee ID that did not match any existing employee; aborting operation";
                    logger.error(err);
                    // throw new Exception(err);
                }

                // first split up itemizations with price overrides into separate line items
                if (trackPriceOverrides) {
                    ArrayList<PaymentItemization> itemizationsToProcess = new ArrayList<PaymentItemization>();

                    for (PaymentItemization originalItemization : payment.getItemizations()) {
                        if (Util.hasPriceOverride(originalItemization)
                                && originalItemization.getQuantity().intValue() > 1) {
                            // split mutli-quantity overrides into separate itemizations
                            itemizationsToProcess.addAll(expandOverrideItemization(originalItemization));
                        } else {
                            // do nothing to normal itemizations
                            itemizationsToProcess.add(originalItemization);
                        }
                    }

                    payment.setItemizations(
                            itemizationsToProcess.toArray(new PaymentItemization[itemizationsToProcess.size()]));
                }

                // now process itemizations
                // 001
                int itemSequence = 1;
                for (PaymentItemization itemization : payment.getItemizations()) {
                    paymentList.add(new MerchandiseItem().parse(itemization, itemSequence++, itemNumberLookupLength,
                            trackPriceOverrides));

                    // 026
                    if (employeeId.length() > 0) {
                        paymentList.add(new Associate().parse(employeeId));
                    }

                    // 022 - price overrides
                    if (trackPriceOverrides && Util.hasPriceOverride(itemization)) {
                        paymentList.add(new ReasonCode().parse(ReasonCode.REASON_CODE_PRICE_CORRECT,
                                ReasonCode.FUNCTION_INDICATOR_PRICE_CORRECT));
                    }

                    // Add promo records 071 after 056
                    // LineItemAssociateAndDiscountAccountingString records
                    ArrayList<EventGiveback> promoRecords = new ArrayList<EventGiveback>();

                    for (PaymentDiscount discount : itemization.getDiscounts()) {
                        String discountType = "";
                        String discountAppyType = "";
                        String discountCode = "";
                        String discountDetails = Util.getValueInBrackets(discount.getName());

                        if (discountDetails.length() == 5) {
                            String firstChar = discountDetails.substring(0, 1);
                            if (firstChar.equals("1") || firstChar.equals("2")) {
                                discountType = firstChar;
                            } else {
                                discountType = "0";
                            }
                            discountAppyType = discountDetails.substring(1, 2).equals("1") ? "1" : "0";
                            discountCode = discountDetails.substring(2);

                            // Only create 021 record for employee applied
                            // discounts
                            if (discountType.equals("0")) {
                                paymentList.add(new DiscountTypeIndicator().parse(itemization, discount, discountCode,
                                        discountAppyType));
                            } else if (discountType.equals("2")) {
                                paymentList.add(new EmployeeDiscount().parse(itemization, discount));
                            } else if (discountType.equals("1")) {
                                String promoDetails = Util.getValueInParenthesis(discount.getName());
                                if (promoDetails.length() > 6) {
                                    promoRecords.add(new EventGiveback().parse(itemization, discount,
                                            itemNumberLookupLength, promoDetails, discountAppyType));
                                }
                            }
                        }
                    }

                    // 025
                    for (PaymentTax tax : itemization.getTaxes()) {
                        paymentList.add(new ItemTaxMerchandiseNonMerchandiseItemsFees().parse(tax, itemization));
                    }

                    // 055
                    // 056
                    int i = 1;
                    for (double q = itemization.getQuantity(); q > 0; q = q - 1) {
                        paymentList.add(new LineItemAccountingString().parse(itemization, itemNumberLookupLength, i));
                        paymentList.add(new LineItemAssociateAndDiscountAccountingString().parse(payment, itemization,
                                itemNumberLookupLength, i, employeeId));
                        i++;
                    }

                    for (EventGiveback promo : promoRecords) {
                        paymentList.add(promo);
                    }
                }

                // 061
                // 065
                for (Tender tender : payment.getTender()) {
                    // don't create 061 records for zero-value tenders
                    if (tender.getTotalMoney().getAmount() == 0) {
                        continue;
                    }

                    paymentList.add(new vfcorp.tlog.Tender().parse(tender, deployment));

                    if (tender.getType().equals("CREDIT_CARD")) {
                        paymentList.add(new CreditCardTender().parse(tender));
                    }
                }

                // 099010 - CRM
                if (loyaltyCustomer != null) {
                    // As part of TLOG generation, we have temporarily
                    // overridden company name (an unused field) to hold loyalty
                    // status).
                    boolean isLoyalty = (isVansDeployment() || (loyaltyCustomer.getCompanyName() != null
                            && loyaltyCustomer.getCompanyName().equals("1"))) ? true : false;
                    paymentList.add(new CrmLoyaltyIndicator().parse(loyaltyCustomer.getReferenceId(), isLoyalty));
                }

                String registerNumber = Util
                        .getRegisterNumber(payment.getDevice() != null ? payment.getDevice().getName() : null);
                String storeNumber = Util.getStoreNumber(location.getLocationDetails().getNickname());
                int transactionNumber = getNextTransactionNumber(storeNumber, registerNumber);

                paymentList.addFirst(new TransactionHeader().parse(transactionNumber, location, payment, employeeId,
                        TransactionHeader.TRANSACTION_TYPE_SALE, paymentList.size() + 1, timeZoneId));

                transactionLog.addAll(paymentList);
            }
        }

    }

    private void createStoreCloseRecords(Merchant location, List<Payment> locationPayments, String processingForDate)
            throws Exception {
        Map<String, List<Payment>> devicePaymentsList = new HashMap<String, List<Payment>>();

        for (Payment payment : locationPayments) {
            String deviceName = payment.getDevice() != null ? payment.getDevice().getName() : null;
            String regNumber = Util.getRegisterNumber(deviceName);

            // Add payment to device-specific payment list
            List<Payment> devicePayments = devicePaymentsList.get(regNumber);
            if (devicePayments == null) {
                devicePayments = new ArrayList<Payment>();
                devicePaymentsList.put(regNumber, devicePayments);
            }
            devicePayments.add(payment);
        }

        // get empty payments list for all missing devices
        int expectedNumberOfDevices = Math.max(MIN_CONFIGURED_DEVICES, totalConfiguredDevices);
        if ((isVansDeployment()) && devicePaymentsList.keySet().size() < expectedNumberOfDevices) {
            Set<String> missingDeviceIds = getDeviceIdsWithNoPayments(devicePaymentsList.keySet(),
                    expectedNumberOfDevices);
            for (String d : missingDeviceIds) {
                List<Payment> emptyDevicePayments = new ArrayList<Payment>();
                devicePaymentsList.put(d, emptyDevicePayments);
            }
        }

        for (String registerNumber : devicePaymentsList.keySet()) {
            List<Payment> registerPayments = devicePaymentsList.get(registerNumber);
            LinkedList<Record> newRecordList = new LinkedList<Record>();

            newRecordList.add(new SubHeaderStoreSystemLocalizationInformation().parse());
            newRecordList.add(new CashierRegisterIdentification().parse(registerNumber));

            if (isVansDeployment()) {
                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CASH, registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VANS_CARD, registerPayments,
                        deployment));
                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MALL_GC, registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MAIL_CHEQUE, registerPayments,
                        deployment));
                newRecordList
                        .add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_EGC, registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_STORE_CREDIT, registerPayments,
                        deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_TRAVELERS_CHEQUE,
                        registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_GIFT_CERTIFICATE,
                        registerPayments, deployment));
                newRecordList
                        .add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_98, registerPayments, deployment));
                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_ECHECK, registerPayments, deployment));
            } else {
                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CASH, registerPayments, deployment));

                if (deployment.contains("tnf")) {
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_AMEX_BETA,
                            registerPayments, deployment));
                } else {
                    newRecordList.add(
                            new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_AMEX, registerPayments, deployment));
                }

                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MALL_GC, registerPayments, deployment));

                if (deployment.contains("tnf")) {
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DISCOVER_BETA,
                            registerPayments, deployment));
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_JCB_BETA, registerPayments,
                            deployment));
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DEBIT_BETA,
                            registerPayments, deployment));
                } else {
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DISCOVER, registerPayments,
                            deployment));
                    newRecordList.add(
                            new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_JCB, registerPayments, deployment));
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DEBIT, registerPayments,
                            deployment));
                }

                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CHEQUE, registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MAIL_CHEQUE, registerPayments,
                        deployment));
                newRecordList
                        .add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_EGC, registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_STORE_CREDIT, registerPayments,
                        deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_TRAVELERS_CHEQUE,
                        registerPayments, deployment));
                newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_GIFT_CERTIFICATE,
                        registerPayments, deployment));

                if (deployment.contains("tnf")) {
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VISA_BETA,
                            registerPayments, deployment));
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MASTERCARD_BETA,
                            registerPayments, deployment));
                } else {
                    newRecordList.add(
                            new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VISA, registerPayments, deployment));
                    newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MASTERCARD,
                            registerPayments, deployment));
                }

                // Catch all for "other" - not used by TNF
                if (!deployment.contains("tnf")) {
                    newRecordList.add(
                            new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_98, registerPayments, deployment));
                }
                newRecordList.add(
                        new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_ECHECK, registerPayments, deployment));
            }

            newRecordList.add(new ForInStoreReportingUseOnly()
                    .parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_MERCHANDISE_SALES, registerPayments));
            newRecordList.add(new ForInStoreReportingUseOnly()
                    .parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_DISCOUNTS, registerPayments));
            newRecordList.add(new ForInStoreReportingUseOnly()
                    .parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_SALES_TAX, registerPayments));

            String storeNumber = Util.getStoreNumber(location.getLocationDetails().getNickname());
            int transactionNumber = getNextTransactionNumber(storeNumber, registerNumber);
            newRecordList.addFirst(new TransactionHeader().parse(transactionNumber, location, registerPayments,
                    registerNumber, TransactionHeader.TRANSACTION_TYPE_TENDER_COUNT_REGISTER, newRecordList.size() + 1,
                    timeZoneId, processingForDate));

            transactionLog.addAll(newRecordList);
        }
    }

    private List<PaymentItemization> expandOverrideItemization(PaymentItemization itemization) {
        ArrayList<PaymentItemization> expandedItemizations = new ArrayList<PaymentItemization>();

        int itemQty = itemization.getQuantity().intValue();

        for (int i = 0; i < itemQty; i++) {
            PaymentItemization pi = new PaymentItemization();
            pi.setName(itemization.getName());
            pi.setQuantity(1.0);
            pi.setItemizationType(itemization.getItemizationType());
            pi.setItemDetail(itemization.getItemDetail());
            pi.setNotes(itemization.getNotes());
            pi.setItemVariationName(itemization.getItemVariationName());
            pi.setSingleQuantityMoney(itemization.getSingleQuantityMoney());

            int[] totalMonies = Util.divideIntegerEvenly(itemization.getTotalMoney().getAmount(), itemQty);
            pi.setTotalMoney(new Money(totalMonies[i]));

            int[] grossSaleMonies = Util.divideIntegerEvenly(itemization.getGrossSalesMoney().getAmount(), itemQty);
            pi.setGrossSalesMoney(new Money(grossSaleMonies[i]));

            int[] discountMonies = Util.divideIntegerEvenly(-itemization.getDiscountMoney().getAmount(), itemQty);
            pi.setDiscountMoney(new Money(-discountMonies[i]));

            pi.setNetSalesMoney(new Money(grossSaleMonies[i] - discountMonies[i]));

            ArrayList<PaymentTax> newTaxes = new ArrayList<PaymentTax>();
            for (PaymentTax tax : itemization.getTaxes()) {
                PaymentTax newTax = new PaymentTax();
                newTax.setName(tax.getName());
                newTax.setRate(tax.getRate());
                newTax.setInclusionType(tax.getInclusionType());
                newTax.setFeeId(tax.getFeeId());

                int[] appliedMonies = Util.divideIntegerEvenly(tax.getAppliedMoney().getAmount(), itemQty);
                newTax.setAppliedMoney(new Money(appliedMonies[i]));

                newTaxes.add(newTax);
            }
            pi.setTaxes(newTaxes.toArray(new PaymentTax[newTaxes.size()]));

            ArrayList<PaymentDiscount> newDiscounts = new ArrayList<PaymentDiscount>();
            for (PaymentDiscount discount : itemization.getDiscounts()) {
                PaymentDiscount newDiscount = new PaymentDiscount();
                newDiscount.setName(discount.getName());
                newDiscount.setDiscountId(discount.getDiscountId());

                int[] appliedMonies = Util.divideIntegerEvenly(discount.getAppliedMoney().getAmount(), itemQty);
                newDiscount.setAppliedMoney(new Money(appliedMonies[i]));

                newDiscounts.add(newDiscount);
            }
            pi.setDiscounts(newDiscounts.toArray(new PaymentDiscount[newDiscounts.size()]));

            expandedItemizations.add(pi);
        }

        return expandedItemizations;
    }

    private int getNextTransactionNumber(String storeNumber, String registerNumber) {
        if (isStoreforceTrickle) {
            return nextTransactionNumber++;
        }

        if (storeNumber == null || storeNumber.equals("")) {
            storeNumber = "0";
        }
        if (registerNumber == null || registerNumber.equals("")) {
            registerNumber = "0";
        }

        String storeNumberFormatted = String.format("%05d", Integer.parseInt(storeNumber));
        String registerNumberFormatted = String.format("%03d", Integer.parseInt(registerNumber));

        try {
            String transactionNumberKey = deployment + "-transactionNumber-" + storeNumberFormatted + "-"
                    + registerNumberFormatted;

            if (objectStore.contains(transactionNumberKey)) {
                int transactionNumber = Integer.parseInt(objectStore.retrieve(transactionNumberKey)) + 1;

                if (transactionNumber > MAX_TRANSACTION_NUMBER) {
                    transactionNumber = 1;
                }

                objectStore.remove(transactionNumberKey);
                objectStore.store(transactionNumberKey, "" + transactionNumber);
                return transactionNumber;
            } else {
                objectStore.store(transactionNumberKey, "" + 1);
                return 1;
            }
        } catch (ObjectStoreException e) {
            return 1;
        }
    }

    private Set<String> getDeviceIdsWithNoPayments(Set<String> devicePaymentsList, int expectedCount) {
        Set<String> missingDevices = new HashSet<String>();

        if (devicePaymentsList.size() >= expectedCount) {
            return missingDevices;
        }

        Set<Integer> missingDevicesAsInt = new HashSet<Integer>();
        for (String d : devicePaymentsList) {
            missingDevicesAsInt.add(Integer.parseInt(d));
        }

        for (int i = 99; i > 99 - expectedCount; i--) {
            if (!missingDevicesAsInt.contains(i)) {
                missingDevices.add(String.format("%03d", i));
            }
        }

        return missingDevices;
    }

    private boolean isVansDeployment() {
        return deployment.contains("vans") || deployment.contains("test");
    }
}