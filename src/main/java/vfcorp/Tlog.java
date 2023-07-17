package vfcorp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemAppliedDiscount;
import com.squareup.connect.v2.OrderLineItemAppliedTax;
import com.squareup.connect.v2.OrderLineItemDiscount;
import com.squareup.connect.v2.OrderLineItemTax;

import util.SequentialRecord;
import util.TimeManager;
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

	private Map<String, Integer> nextRecordNumbers;
	private Map<String, SequentialRecord> recordNumberCache;
	private int nextTransactionNumber;
	private String type;
	private boolean trackPriceOverrides;
	private boolean createCloseRecords;
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

	public void setType(String tlogType) {
		this.type = tlogType;
	}

	public void trackPriceOverrides(boolean trackPriceOverrides) {
		this.trackPriceOverrides = trackPriceOverrides;
	}

	public void createCloseRecords(boolean createCloseRecords) {
		this.createCloseRecords = createCloseRecords;
	}

	public void setTotalConfiguredDevices(int totalConfiguredDevices) {
		this.totalConfiguredDevices = totalConfiguredDevices;
	}

	public void setNextRecordNumbers(Map<String, Integer> nextRecordNumbers) {
		this.nextRecordNumbers = nextRecordNumbers;
	}

	public Map<String, SequentialRecord> getRecordNumberCache() {
		return recordNumberCache;
	}

	public void setRecordNumberCache(Map<String, SequentialRecord> recordNumberCache) {
		this.recordNumberCache = recordNumberCache;
	}

	public void parse(Location location, Order[] ordersForPeriod, Order[] ordersToProcess,
			Map<String, String> employees, Map<String, Customer> customerPaymentCache, String processingForDate,
			Map<String, Payment> tenderToPayment, Map<String, CatalogObject> catalog) throws Exception {
		List<Order> tlogEntryOrders = Arrays.asList(ordersToProcess);
		List<Order> tlogCloseRecordOrders = Arrays.asList(ordersForPeriod);

		createSaleRecords(location, tlogEntryOrders, employees, customerPaymentCache, tenderToPayment, catalog);

		if (createCloseRecords) {
			createStoreCloseRecords(location, tlogCloseRecordOrders, tenderToPayment, processingForDate);
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

	private void createSaleRecords(Location location, List<Order> squareOrdersList,
			Map<String, String> squareEmployeesCache, Map<String, Customer> customerPaymentCache,
			Map<String, Payment> tenderToPayment, Map<String, CatalogObject> catalog) throws Exception {

		for (Order order : squareOrdersList) {
			Customer loyaltyCustomer = customerPaymentCache.get(order.getId());

			if (order.getTenders() != null && order.getTenders().length > 0 && order.getTenders()[0].getType() != null
					&& order.getTenders()[0].getType().equals(Tender.TENDER_TYPE_NO_SALE)) {
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

				paymentList.add(new TransactionSubTotal().parse(order));

				paymentList.add(new TransactionTax().parse(order));

				paymentList.add(new TransactionTotal().parse(order));

				if(order.getTaxes() != null) {
					for (OrderLineItemTax tax : order.getTaxes()) {
						if (tax != null && tax.getType().equals("ADDITIVE")) {
							paymentList.add(new TransactionTaxExtended().parse(order, tax));
						}
					}

					for (OrderLineItemTax tax : order.getTaxes()) {
						if (tax != null && tax.getType().equals("INCLUSIVE")) {
							paymentList.add(new TransactionTaxExtended().parse(order, tax));
						}
					}
				}

				if (loyaltyCustomer != null) {
					// 029 - CRM
					String firstName = loyaltyCustomer.getGivenName() != null ? loyaltyCustomer.getGivenName() : "";
					String lastName = loyaltyCustomer.getFamilyName() != null ? loyaltyCustomer.getFamilyName() : "";
					paymentList.add(new Name().parse(lastName, firstName));

					// 030 - CRM
					String addressLine1 = (loyaltyCustomer.getAddress() != null
							&& loyaltyCustomer.getAddress().getAddressLine1() != null)
									? loyaltyCustomer.getAddress().getAddressLine1()
									: "";
					String addressLine2 = (loyaltyCustomer.getAddress() != null
							&& loyaltyCustomer.getAddress().getAddressLine2() != null)
									? loyaltyCustomer.getAddress().getAddressLine2()
									: "";
					String city = (loyaltyCustomer.getAddress() != null
							&& loyaltyCustomer.getAddress().getLocality() != null)
									? loyaltyCustomer.getAddress().getLocality()
									: "";
					String state = (loyaltyCustomer.getAddress() != null
							&& loyaltyCustomer.getAddress().getAdministrativeDistrictLevel1() != null)
									? loyaltyCustomer.getAddress().getAdministrativeDistrictLevel1()
									: "";
					String zip = (loyaltyCustomer.getAddress() != null
							&& loyaltyCustomer.getAddress().getPostalCode() != null)
									? loyaltyCustomer.getAddress().getPostalCode()
									: "";
					String email = loyaltyCustomer.getEmailAddress() != null ? loyaltyCustomer.getEmailAddress() : "";
					paymentList.add(new Address().parse(addressLine1, addressLine2, city, state, zip, email));

					// 031 - CRM
					String phone = loyaltyCustomer.getPhoneNumber() != null
							? loyaltyCustomer.getPhoneNumber().replaceAll("[^\\d]", "")
							: "";

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
				if (order.getTenders() != null) {
					for (Tender tender : order.getTenders()) {
						if (tenderToPayment.containsKey(tender.getId())) {
							Payment payment = tenderToPayment.get(tender.getId());
							if (payment.getTeamMemberId() != null) {
								String teamMemberId = payment.getTeamMemberId();
								employeeIdShouldBePresent = true;
								if (squareEmployeesCache.containsKey(teamMemberId)) {
									if (squareEmployeesCache.get(teamMemberId) != null) {
										employeeId = squareEmployeesCache.get(teamMemberId);
									}
									employeeFound = true;
								}
							}
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
					ArrayList<OrderLineItem> lineItemsToProcess = new ArrayList<OrderLineItem>();

					for (OrderLineItem originalLineItem : order.getLineItems()) {
						if (Util.hasPriceOverride(originalLineItem)
								&& Integer.parseInt(originalLineItem.getQuantity()) > 1) {
							// split mutli-quantity overrides into separate itemizations
							lineItemsToProcess.addAll(expandOverrideLineItem(originalLineItem));
						} else {
							// do nothing to normal itemizations
							lineItemsToProcess.add(originalLineItem);
						}
					}

					order.setLineItems(
							lineItemsToProcess.toArray(new OrderLineItem[lineItemsToProcess.size()]));
				}

				// now process itemizations
				// 001
				int itemSequence = 1;
				if(order.getLineItems() != null) {
					for (OrderLineItem lineItem : order.getLineItems()) {
						CatalogObject itemVariationData = catalog.get(lineItem.getCatalogObjectId());
						paymentList.add(new MerchandiseItem().parse(lineItem, itemSequence++, itemNumberLookupLength,
								trackPriceOverrides, itemVariationData));

						// 026
						if (employeeId.length() > 0) {
							paymentList.add(new Associate().parse(employeeId));
						}

						// 022 - price overrides
						if (trackPriceOverrides && Util.hasPriceOverride(lineItem)) {
							paymentList.add(new ReasonCode().parse(ReasonCode.REASON_CODE_PRICE_CORRECT,
									ReasonCode.FUNCTION_INDICATOR_PRICE_CORRECT));
						}

						// Add promo records 071 after 056
						// LineItemAssociateAndDiscountAccountingString records
						ArrayList<EventGiveback> promoRecords = new ArrayList<EventGiveback>();
						Map<String, OrderLineItemDiscount> lineItemDiscountDetails = order.getDiscounts() != null ?
								Arrays.stream(order.getDiscounts()).collect(Collectors.toMap(OrderLineItemDiscount::getUid, Function.identity())) : new HashMap<>();
						Map<String, OrderLineItemTax> lineItemTaxDetails = order.getTaxes() != null ?
								Arrays.stream(order.getTaxes()).collect(Collectors.toMap(OrderLineItemTax::getUid, Function.identity())) : new HashMap<>();

						if(lineItem.getAppliedDiscounts() != null) {
							for (OrderLineItemAppliedDiscount discount : lineItem.getAppliedDiscounts()) {
								String discountType = "";
								String discountAppyType = "";
								String discountCode = "";
								String name = lineItemDiscountDetails.containsKey(discount.getDiscountUid()) ? lineItemDiscountDetails.get(discount.getDiscountUid()).getName() : "";
								String discountDetails = Util.getValueInBrackets(name);

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
										paymentList.add(new DiscountTypeIndicator().parse(lineItem, discount, discountCode,
												discountAppyType));
									} else if (discountType.equals("2")) {
										paymentList.add(new EmployeeDiscount().parse(lineItem, discount));
									} else if (discountType.equals("1")) {
										String promoDetails = Util.getValueInParenthesis(name);
										if (promoDetails.length() > 6) {
											promoRecords.add(new EventGiveback().parse(lineItem, discount,
													itemNumberLookupLength, promoDetails, discountAppyType, catalog));
										}
									}
								}
							}
						}

						if(lineItem.getAppliedTaxes() != null) {
							// 025
							for (OrderLineItemAppliedTax tax : lineItem.getAppliedTaxes()) {
								OrderLineItemTax taxDetails = lineItemTaxDetails.get(tax.getTaxUid());
								if(taxDetails != null) {
									paymentList.add(new ItemTaxMerchandiseNonMerchandiseItemsFees().parse(tax, lineItem, taxDetails));
								}
							}
						}


						// 055
						// 056
						int i = 1;
						for (double q = Double.parseDouble(lineItem.getQuantity()); q > 0; q = q - 1) {
							paymentList.add(new LineItemAccountingString().parse(lineItem, itemNumberLookupLength, i, catalog));
							paymentList.add(new LineItemAssociateAndDiscountAccountingString().parse(order, lineItem,
									itemNumberLookupLength, i, employeeId, catalog));
							i++;
						}

						for (EventGiveback promo : promoRecords) {
							paymentList.add(promo);
						}
					}
				}


				if(order.getTenders() != null) {
					// 061
					// 065
					for (Tender tender : order.getTenders()) {
						Payment p = tenderToPayment.get(tender.getId());
						// don't create 061 records for zero-value tenders
						if (p != null && p.getTotalMoney().getAmount() == 0) {
							continue;
						}

						paymentList.add(new vfcorp.tlog.Tender().parse(tender, deployment, p));

						if (tender.getType().equals(Tender.TENDER_TYPE_CARD)) {
							paymentList.add(new CreditCardTender().parse(tender, p));
						}
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

				String registerNumber = Util.getDeviceName(order, tenderToPayment);

				int transactionNumber = getNextTransactionNumberForRegister(location, registerNumber,
						order.getId(), order.getCreatedAt());

				paymentList.addFirst(new TransactionHeader().parse(transactionNumber, location, order, registerNumber, employeeId,
						TransactionHeader.TRANSACTION_TYPE_SALE, paymentList.size() + 1, timeZoneId));

				transactionLog.addAll(paymentList);
			}
		}
	}

	private void createStoreCloseRecords(Location location, List<Order> orders, Map<String, Payment> tenderToPayment, String processingForDate) throws Exception {
		Map<String, List<Order>> deviceOrdersList = new HashMap<String, List<Order>>();

		for (Order order : orders) {
			String deviceName = Util.getDeviceName(order, tenderToPayment);
			String regNumber = Util.getRegisterNumber(deviceName);

			// Add payment to device-specific payment list
			List<Order> deviceOrders = deviceOrdersList.get(regNumber);
			if (deviceOrders == null) {
				deviceOrders = new ArrayList<Order>();
				deviceOrdersList.put(regNumber, deviceOrders);
			}
			deviceOrders.add(order);
		}

		// get empty payments list for all missing devices
		int expectedNumberOfDevices = Math.max(MIN_CONFIGURED_DEVICES, totalConfiguredDevices);
		if ((isVansDeployment()) && deviceOrdersList.keySet().size() < expectedNumberOfDevices) {
			Set<String> missingDeviceIds = getDeviceIdsWithNoPayments(deviceOrdersList.keySet(),
					expectedNumberOfDevices);
			for (String d : missingDeviceIds) {
				List<Order> emptyDeviceOrders = new ArrayList<Order>();
				deviceOrdersList.put(d, emptyDeviceOrders);
			}
		}

		for (String registerNumber : deviceOrdersList.keySet()) {
			String recordDate = TimeManager.toSimpleDateTimeInTimeZone(processingForDate, timeZoneId, "yyyy-MM-dd");
			String closingRecordId = "close-" + location.getId() + "-" + recordDate + "-" + registerNumber;

			List<Order> registerOrders = deviceOrdersList.get(registerNumber);
			LinkedList<Record> newRecordList = new LinkedList<Record>();

			newRecordList.add(new SubHeaderStoreSystemLocalizationInformation().parse());
			newRecordList.add(new CashierRegisterIdentification().parse(registerNumber));

			if (isVansDeployment()) {
				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CASH, registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VANS_CARD, registerOrders, tenderToPayment,
						deployment));
				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MALL_GC, registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MAIL_CHEQUE, registerOrders, tenderToPayment,
						deployment));
				newRecordList
						.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_EGC, registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_STORE_CREDIT, registerOrders, tenderToPayment,
						deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_TRAVELERS_CHEQUE,
						registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_GIFT_CERTIFICATE,
						registerOrders, tenderToPayment, deployment));
				newRecordList
						.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_98, registerOrders, tenderToPayment, deployment));
				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_ECHECK, registerOrders, tenderToPayment, deployment));
			} else {
				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CASH, registerOrders, tenderToPayment, deployment));

				if (deployment.contains("tnf")) {
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_AMEX_BETA,
							registerOrders, tenderToPayment, deployment));
				} else {
					newRecordList.add(
							new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_AMEX, registerOrders, tenderToPayment, deployment));
				}

				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MALL_GC, registerOrders, tenderToPayment, deployment));

				if (deployment.contains("tnf")) {
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DISCOVER_BETA,
							registerOrders, tenderToPayment, deployment));
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_JCB_BETA, registerOrders, tenderToPayment,
							deployment));
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DEBIT_BETA,
							registerOrders, tenderToPayment, deployment));
				} else {
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DISCOVER, registerOrders, tenderToPayment,
							deployment));
					newRecordList.add(
							new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_JCB, registerOrders, tenderToPayment, deployment));
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_DEBIT, registerOrders, tenderToPayment,
							deployment));
				}

				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_CHEQUE, registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MAIL_CHEQUE, registerOrders, tenderToPayment,
						deployment));
				newRecordList
						.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_EGC, registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_STORE_CREDIT, registerOrders, tenderToPayment,
						deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_TRAVELERS_CHEQUE,
						registerOrders, tenderToPayment, deployment));
				newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_GIFT_CERTIFICATE,
						registerOrders, tenderToPayment, deployment));

				if (deployment.contains("tnf")) {
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VISA_BETA,
							registerOrders, tenderToPayment, deployment));
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MASTERCARD_BETA,
							registerOrders, tenderToPayment, deployment));
				} else {
					newRecordList.add(
							new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_VISA, registerOrders, tenderToPayment, deployment));
					newRecordList.add(new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_MASTERCARD,
							registerOrders, tenderToPayment, deployment));
				}

				// Catch all for "other" - not used by TNF
				if (!deployment.contains("tnf")) {
					newRecordList.add(
							new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_98, registerOrders, tenderToPayment, deployment));
				}
				newRecordList.add(
						new TenderCount().parse(vfcorp.tlog.Tender.TENDER_CODE_ECHECK, registerOrders, tenderToPayment, deployment));
			}

			newRecordList.add(new ForInStoreReportingUseOnly()
					.parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_MERCHANDISE_SALES, registerOrders));
			newRecordList.add(new ForInStoreReportingUseOnly()
					.parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_DISCOUNTS, registerOrders));
			newRecordList.add(new ForInStoreReportingUseOnly()
					.parse(ForInStoreReportingUseOnly.TRANSACTION_IDENTIFIER_SALES_TAX, registerOrders));

			int transactionNumber = getNextTransactionNumberForRegister(location, registerNumber, closingRecordId,
					recordDate);
			newRecordList.addFirst(new TransactionHeader().parse(transactionNumber, location, registerOrders,
					registerNumber, TransactionHeader.TRANSACTION_TYPE_TENDER_COUNT_REGISTER, newRecordList.size() + 1,
					timeZoneId, processingForDate));

			transactionLog.addAll(newRecordList);
		}
	}

	private List<OrderLineItem> expandOverrideLineItem(OrderLineItem lineItem) {
		ArrayList<OrderLineItem> expandedItemizations = new ArrayList<OrderLineItem>();

		int itemQty = Integer.parseInt(lineItem.getQuantity());

		for (int i = 0; i < itemQty; i++) {
			OrderLineItem li = new OrderLineItem();
			li.setName(lineItem.getName());
			li.setQuantity("1");
			li.setItemType(lineItem.getItemType());
			li.setNote(lineItem.getNote());
			li.setVariationName(lineItem.getVariationName());
			li.setBasePriceMoney(lineItem.getBasePriceMoney());

			int[] totalMonies = Util.divideIntegerEvenly(lineItem.getTotalMoney().getAmount(), itemQty);
			li.setTotalMoney(new Money(totalMonies[i]));

			int[] grossSaleMonies = Util.divideIntegerEvenly(lineItem.getGrossSalesMoney() != null ? lineItem.getGrossSalesMoney().getAmount() : 0, itemQty);
			li.setGrossSalesMoney(new Money(grossSaleMonies[i]));

			int[] discountMonies = Util.divideIntegerEvenly(lineItem.getTotalDiscountMoney() != null ? lineItem.getTotalDiscountMoney().getAmount() : 0, itemQty);
			li.setTotalDiscountMoney(new Money(discountMonies[i]));

			ArrayList<OrderLineItemAppliedTax> newTaxes = new ArrayList<OrderLineItemAppliedTax>();
			if(lineItem.getAppliedTaxes() != null)  {
				for (OrderLineItemAppliedTax tax : lineItem.getAppliedTaxes()) {
					OrderLineItemAppliedTax newTax = new OrderLineItemAppliedTax();
					newTax.setTaxUid(tax.getTaxUid());

					int[] appliedMonies = Util.divideIntegerEvenly(tax.getAppliedMoney().getAmount(), itemQty);
					newTax.setAppliedMoney(new Money(appliedMonies[i]));

					newTaxes.add(newTax);
				}
			}
			li.setAppliedTaxes(newTaxes.toArray(new OrderLineItemAppliedTax[newTaxes.size()]));

			ArrayList<OrderLineItemAppliedDiscount> newDiscounts = new ArrayList<OrderLineItemAppliedDiscount>();
			if(lineItem.getAppliedDiscounts() != null) {
				for (OrderLineItemAppliedDiscount discount : lineItem.getAppliedDiscounts()) {
					OrderLineItemAppliedDiscount newDiscount = new OrderLineItemAppliedDiscount();
					newDiscount.setDiscountUid(discount.getDiscountUid());

					int[] appliedMonies = Util.divideIntegerEvenly(discount.getAppliedMoney().getAmount(), itemQty);
					newDiscount.setAppliedMoney(new Money(appliedMonies[i]));

					newDiscounts.add(newDiscount);
				}
			}
			li.setAppliedDiscounts(newDiscounts.toArray(new OrderLineItemAppliedDiscount[newDiscounts.size()]));

			expandedItemizations.add(li);
		}

		return expandedItemizations;
	}

	private int getNextTransactionNumberForRegister(Location location, String registerNumber, String recordId,
			String createdAt) {
		if (type.equals("STOREFORCE")) {
			return nextTransactionNumber++;
		}

		if (registerNumber == null || registerNumber.equals("") || !registerNumber.matches("\\d+")) {
			registerNumber = "0";
		}
		String registerNumberFormatted = String.format("%03d", Integer.parseInt(registerNumber));

		if (recordNumberCache.containsKey(recordId)) {
			return recordNumberCache.get(recordId).getRecordNumber();
		} else {
			int nextTransactionNumber = 1;

			if (nextRecordNumbers.containsKey(registerNumberFormatted)) {
				nextTransactionNumber = nextRecordNumbers.get(registerNumberFormatted);

				if (nextTransactionNumber > MAX_TRANSACTION_NUMBER) {
					nextTransactionNumber = 1;
				}
			}

			SequentialRecord sr = new SequentialRecord();
			sr.setLocationId(location.getId());
			sr.setRecordId(recordId);
			sr.setDeviceId(registerNumberFormatted);
			sr.setRecordNumber(nextTransactionNumber);
			sr.setRecordCreatedAt(createdAt);
			recordNumberCache.put(recordId, sr);

			nextRecordNumbers.put(registerNumberFormatted, nextTransactionNumber + 1);
			return nextTransactionNumber;
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
