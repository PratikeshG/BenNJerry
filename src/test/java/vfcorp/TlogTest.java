package vfcorp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mule.tck.util.FakeObjectStore;

import com.squareup.connect.Device;
import com.squareup.connect.Employee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Merchant;
import com.squareup.connect.MerchantLocationDetails;
import com.squareup.connect.Money;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemDetail;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

public class TlogTest {

	private static final String TRANSACTION_HEADER = "000";
	private static final String MERCHANDISE_ITEM = "001";
	private static final String AUTHORIZATION_CODE = "023";
	private static final String ITEM_TAX_MERCHANDISE_NON_MERCHANDISE_ITEM_FEES = "025";
	private static final String TENDER_COUNT = "034";
	private static final String CASHIER_REGISTER_IDENTIFICATION = "036";
	private static final String FOR_IN_STORE_REPORTING_ONLY = "037";
	private static final String TRANSACTION_SUBTOTAL = "051";
	private static final String TRANSACTION_TAX = "052";
	private static final String TRANSACTION_TOTAL = "053";
	private static final String TRANSACTION_TAX_EXTENDED = "054";
	private static final String LINE_ITEM_ACCOUNTING_STRING = "055";
	private static final String LINE_ITEM_ASSOCIATE_AND_DISCOUNT_ACCOUNTING_STRING = "056";
	private static final String TENDER = "061";
	private static final String CREDIT_CARD_TENDER = "065";
	
	@Test
	public void parse_AuthorizationCodeGeneratedWithExternalEmployeeIDs_EmployeeIDsAreExternalEmployeeIDs() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("externalId");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setEmployeeId("id");
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		Tender[] tenders = new Tender[]{tender};
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Device device = new Device();
		device.setName("");
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(tenders);
		payment.setDevice(device);
		payment.setDiscountMoney(new Money(-1));
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		Tlog tlog = new Tlog();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		//tlog.parse(merchant, payments, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> authorizationCodeStrings = tlogStrings.stream().filter(s -> s.startsWith(AUTHORIZATION_CODE) == true).collect(Collectors.toList());
		
		assertEquals("expecting only one authorization code", 1, authorizationCodeStrings.size());
		assertEquals("authorization code should be of a specific format", "023        180externalId", authorizationCodeStrings.get(0));
	}
	/*
	@Test
	public void parse_AuthorizationCodeGeneratedWithNonExistentEmployeeID_ExceptionGetsThrown() throws Exception {
		try {
			Employee employee = new Employee();
			employee.setId("id");
			employee.setExternalId("externalId");
			Employee[] employees = new Employee[]{employee};
			
			MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
			
			Merchant merchant = new Merchant();
			merchant.setLocationDetails(merchantLocationDetails);
			
			Tender tender = new Tender();
			tender.setEmployeeId("nonExistentId");
			tender.setType("");
			tender.setTotalMoney(new Money(0));
			Tender[] tenders = new Tender[]{tender};
			
			PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
			paymentItemDetail.setSku("");
			
			PaymentItemization paymentItemization = new PaymentItemization();
			paymentItemization.setItemDetail(paymentItemDetail);
			paymentItemization.setSingleQuantityMoney(new Money(0));
			paymentItemization.setGrossSalesMoney(new Money(0));
			paymentItemization.setNetSalesMoney(new Money(0));
			paymentItemization.setDiscountMoney(new Money(0));
			paymentItemization.setTaxes(new PaymentTax[]{});
			paymentItemization.setQuantity(1.0);
			PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
			
			Device device = new Device();
			device.setName("");
			
			Payment payment = new Payment();
			payment.setItemizations(paymentItemizations);
			payment.setTender(tenders);
			payment.setDevice(device);
			payment.setDiscountMoney(new Money(-1));
			payment.setTotalCollectedMoney(new Money(0));
			payment.setTaxMoney(new Money(0));
			payment.setAdditiveTax(new PaymentTax[]{});
			payment.setInclusiveTax(new PaymentTax[]{});
			payment.setCreatedAt("2016-05-20T12:00:00Z");
			Payment[] payments = new Payment[]{payment};
			
			TLOG tlog = new TLOG();
			tlog.setItemNumberLookupLength(1);
			tlog.setObjectStore(new FakeObjectStore<String>());
			tlog.setTimeZoneId("UTC");
			tlog.parse(merchant, payments, new Item[]{}, employees);
			
			fail("expecting exception to be thrown");
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void parse_AuthorizationCodeGeneratedWithExternalEmployeeIDsMultipleTenders_AllEmployeeIDsAccountedFor() throws Exception {
		Employee employee1 = new Employee();
		employee1.setId("id1");
		employee1.setExternalId("externalId1");
		Employee employee2 = new Employee();
		employee2.setId("id2");
		employee2.setExternalId("externalId2");
		Employee[] employees = new Employee[]{employee1,employee2};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender1 = new Tender();
		tender1.setEmployeeId("id1");
		tender1.setType("");
		tender1.setTotalMoney(new Money(0));
		Tender tender2 = new Tender();
		tender2.setEmployeeId("id2");
		tender2.setType("");
		tender2.setTotalMoney(new Money(0));
		Tender[] tenders = new Tender[]{tender1,tender2};
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Device device = new Device();
		device.setName("");
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(tenders);
		payment.setDevice(device);
		payment.setDiscountMoney(new Money(-1));
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> authorizationCodeStrings = tlogStrings.stream().filter(s -> s.startsWith(AUTHORIZATION_CODE) == true).collect(Collectors.toList());
		
		assertEquals("expecting two authorization codes", 2, authorizationCodeStrings.size());
		assertTrue("first authorization code should be of a specific format", authorizationCodeStrings.contains("023        18externalId1"));
		assertTrue("second authorization code should be of a specific format", authorizationCodeStrings.contains("023        18externalId2"));
	}
	
	@Test
	public void parse_AuthorizationCodeGeneratedWithNoExternalEmployeeIDs_EmployeeIDsAreBlank() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setEmployeeId("id");
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		Tender[] tenders = new Tender[]{tender};
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Device device = new Device();
		device.setName("");
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(tenders);
		payment.setDevice(device);
		payment.setDiscountMoney(new Money(-1));
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> authorizationCodeStrings = tlogStrings.stream().filter(s -> s.startsWith(AUTHORIZATION_CODE) == true).collect(Collectors.toList());
		
		assertEquals("expecting only one authorization code", 1, authorizationCodeStrings.size());
		assertEquals("authorization code should be of a specific format", "023        1800000000000", authorizationCodeStrings.get(0));
	}
	
	@Test
	public void parse_LineItemAssociateAndDiscountAccountingStringGenerated_AllFieldsCorrect() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("externalId");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setEmployeeId("id");
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		Tender[] tenders = new Tender[]{tender};
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Device device = new Device();
		device.setName("");
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(tenders);
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setDiscountMoney(new Money(-1));
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> lineItemDiscountingStrings = tlogStrings.stream().filter(s -> s.startsWith(LINE_ITEM_ASSOCIATE_AND_DISCOUNT_ACCOUNTING_STRING) == true).collect(Collectors.toList());
		
		assertEquals("expecting only one line item associate string", 1, lineItemDiscountingStrings.size());
		assertEquals("authorization code should be of a specific format", "0560000                                                                    0000000000000000000000100000000000000000000000000000000000000000000000000100000000000000000000000000000000000010000externalId0000000000   ", lineItemDiscountingStrings.get(0));
	}
	
	@Test
	public void parse_LineItemAssociateAndDiscountAccountingStringGeneratedWithNonExistentEmployeeId_ExceptionThrown() throws Exception {
		try {
			Employee employee = new Employee();
			employee.setId("id");
			employee.setExternalId("externalId");
			Employee[] employees = new Employee[]{employee};
			
			MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
			
			Merchant merchant = new Merchant();
			merchant.setLocationDetails(merchantLocationDetails);
			
			Tender tender = new Tender();
			tender.setEmployeeId("nonExistentId");
			tender.setType("");
			tender.setTotalMoney(new Money(0));
			Tender[] tenders = new Tender[]{tender};
			
			PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
			paymentItemDetail.setSku("");
			
			PaymentItemization paymentItemization = new PaymentItemization();
			paymentItemization.setItemDetail(paymentItemDetail);
			paymentItemization.setSingleQuantityMoney(new Money(0));
			paymentItemization.setGrossSalesMoney(new Money(0));
			paymentItemization.setNetSalesMoney(new Money(0));
			paymentItemization.setDiscountMoney(new Money(0));
			paymentItemization.setTaxes(new PaymentTax[]{});
			paymentItemization.setQuantity(1.0);
			PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
			
			Device device = new Device();
			device.setName("");
			
			Payment payment = new Payment();
			payment.setItemizations(paymentItemizations);
			payment.setTender(tenders);
			payment.setDevice(device);
			payment.setTotalCollectedMoney(new Money(0));
			payment.setTaxMoney(new Money(0));
			payment.setAdditiveTax(new PaymentTax[]{});
			payment.setInclusiveTax(new PaymentTax[]{});
			payment.setDiscountMoney(new Money(-1));
			payment.setCreatedAt("2016-05-20T12:00:00Z");
			Payment[] payments = new Payment[]{payment};
			
			TLOG tlog = new TLOG();
			tlog.setItemNumberLookupLength(1);
			tlog.setObjectStore(new FakeObjectStore<String>());
			tlog.setTimeZoneId("UTC");
			tlog.parse(merchant, payments, new Item[]{}, employees);
			
			fail("expecting exception to be thrown");
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void parse_LineItemAssociateAndDiscountAccountingStringGeneratedWithTwoQuantity_AllFieldsCorrectForBoth() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("externalId");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setEmployeeId("id");
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		Tender[] tenders = new Tender[]{tender};
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(2.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Device device = new Device();
		device.setName("");
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(tenders);
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setDiscountMoney(new Money(-1));
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> lineItemDiscountingStrings = tlogStrings.stream().filter(s -> s.startsWith(LINE_ITEM_ASSOCIATE_AND_DISCOUNT_ACCOUNTING_STRING) == true).collect(Collectors.toList());
		
		assertEquals("expecting two line item associate strings", 2, lineItemDiscountingStrings.size());
		assertTrue("accounting string should be of a specific format", lineItemDiscountingStrings.contains("0560000                                                                    0000000000000000000000100000000000000000000000000000000000000000000000000100000000000000000000000000000000000010000externalId0000000000   "));
		assertTrue("accounting string should be of a specific format", lineItemDiscountingStrings.contains("0560000                                                                    0000000000000000000000100000000000000000000000000000000000000000000000000100000000000000000000000000000000000010000externalId0000000000   "));
	}
	
	@Test
	public void parse_LineItemAssociateAndDiscountAccountingStringGeneratedWithTwoQuantityAndTwoEmployees_AllFieldsCorrectForBoth() throws Exception {
		Employee employee1 = new Employee();
		employee1.setId("id1");
		employee1.setExternalId("externalId1");
		Employee employee2 = new Employee();
		employee2.setId("id2");
		employee2.setExternalId("externalId2");
		Employee[] employees = new Employee[]{employee1, employee2};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender1 = new Tender();
		tender1.setEmployeeId("id1");
		tender1.setType("");
		tender1.setTotalMoney(new Money(0));
		Tender tender2 = new Tender();
		tender2.setEmployeeId("id2");
		tender2.setType("");
		tender2.setTotalMoney(new Money(0));
		Tender[] tenders = new Tender[]{tender1,tender2};
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(2.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Device device = new Device();
		device.setName("");
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(tenders);
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setDiscountMoney(new Money(-1));
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> lineItemDiscountingStrings = tlogStrings.stream().filter(s -> s.startsWith(LINE_ITEM_ASSOCIATE_AND_DISCOUNT_ACCOUNTING_STRING) == true).collect(Collectors.toList());
		
		assertEquals("expecting four line item associate strings", 4, lineItemDiscountingStrings.size());
		assertTrue("accounting string should be of a specific format", lineItemDiscountingStrings.contains("0560000                                                                    000000000000000000000010000000000000000000000000000000000000000000000000010000000000000000000000000000000000001000externalId10000000000   "));
		assertTrue("accounting string should be of a specific format", lineItemDiscountingStrings.contains("0560000                                                                    000000000000000000000010000000000000000000000000000000000000000000000000010000000000000000000000000000000000001000externalId20000000000   "));
		assertTrue("accounting string should be of a specific format", lineItemDiscountingStrings.contains("0560000                                                                    000000000000000000000010000000000000000000000000000000000000000000000000010000000000000000000000000000000000001000externalId10000000000   "));
		assertTrue("accounting string should be of a specific format", lineItemDiscountingStrings.contains("0560000                                                                    000000000000000000000010000000000000000000000000000000000000000000000000010000000000000000000000000000000000001000externalId20000000000   "));
	}
	
	@Test
	public void parse_LineItemAccountingString_TotalsCalculatedCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(1));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> lineItemAccountingStrings = tlogStrings.stream().filter(s -> s.startsWith(LINE_ITEM_ACCOUNTING_STRING) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one line item accounting string", 1, lineItemAccountingStrings.size());
		assertTrue("line item accounting string should of a specific format", lineItemAccountingStrings.contains("055                                                                    00000000010100000000001000000000000000000000000000000000000010000000001      "));
	}
	
	@Test
	public void parse_LineItemAccountingStringForMultipleQuantities_TotalsCalculatedCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(1));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(2.0);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> lineItemAccountingStrings = tlogStrings.stream().filter(s -> s.startsWith(LINE_ITEM_ACCOUNTING_STRING) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one line item accounting string", 2, lineItemAccountingStrings.size());
		assertEquals("accounting string should be of a specific format", "055                                                                    00000000010100000000001000000000000000000000000000000000000010000000001      ", lineItemAccountingStrings.get(0));
		assertEquals("accounting string should be of a specific format", "055                                                                    00000000010100000000001000000000000000000000000000000000000010000000002      ", lineItemAccountingStrings.get(1));
	}
	
	@Test
	public void parse_LineItemAccountingStringForFlatingQuantities_TotalsCalculatedCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(1));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.5);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> lineItemAccountingStrings = tlogStrings.stream().filter(s -> s.startsWith(LINE_ITEM_ACCOUNTING_STRING) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one line item accounting string", 2, lineItemAccountingStrings.size());
		assertEquals("accounting string should be of a specific format", "055                                                                    00000000010100000000001000000000000000000000000000000000000010000000001      ", lineItemAccountingStrings.get(0));
		assertEquals("accounting string should be of a specific format", "055                                                                    00000000010100000000001000000000000000000000000000000000000005000000002      ", lineItemAccountingStrings.get(1));
	}
	
	@Test
	public void parse_ItemTaxWithDefaultName_TotalsCalculatedCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentTax paymentTax = new PaymentTax();
		paymentTax.setName("");
		paymentTax.setRate("0.5");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(1));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{paymentTax});
		paymentItemization.setQuantity(1.5);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> taxStrings = tlogStrings.stream().filter(s -> s.startsWith(ITEM_TAX_MERCHANDISE_NON_MERCHANDISE_ITEM_FEES) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one tax string", 1, taxStrings.size());
		assertEquals("tax string should be of a specific format", "025010150000000000000000                         00000000001                        ", taxStrings.get(0));
	}
	
	@Test
	public void parse_ItemTaxesWithDifferentNames_TotalsCalculatedCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setSku("");
		
		PaymentTax paymentTax1 = new PaymentTax();
		paymentTax1.setName("");
		paymentTax1.setRate("0.5");
		PaymentTax paymentTax2 = new PaymentTax();
		paymentTax2.setName("VAT");
		paymentTax2.setRate("0.7");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(1));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{paymentTax1,paymentTax2});
		paymentItemization.setQuantity(1.5);
		PaymentItemization[] paymentItemizations = new PaymentItemization[]{paymentItemization};
		
		Payment payment = new Payment();
		payment.setItemizations(paymentItemizations);
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> taxStrings = tlogStrings.stream().filter(s -> s.startsWith(ITEM_TAX_MERCHANDISE_NON_MERCHANDISE_ITEM_FEES) == true).collect(Collectors.toList());
		
		assertEquals("only expecting two tax strings", 2, taxStrings.size());
		assertEquals("tax string should be of a specific format", "025010150000000000000000                         00000000001                        ", taxStrings.get(0));
		assertEquals("tax string should be of a specific format", "025020170000000000000000                         00000000001                        ", taxStrings.get(1));
	}
	
	@Test
	public void parse_TransactionHeadersWithStandardObjects_ExpectedTransactionHeadersGenerated() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("externalId");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setEmployeeId("id");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionHeaderStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_HEADER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction header", 1, transactionHeaderStrings.size());
		assertEquals("transaction header string should be of a specific format", "000000000000000000externalId000001052020161200200010000   0100000000000605202016000000000000000                 0                                                 ", transactionHeaderStrings.get(0));
	}
	
	@Test
	public void parse_TLOGHasTimeZoneNotUTC_DatesLocalized() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("externalId");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setEmployeeId("id");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("America/Regina");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionHeaderStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_HEADER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction header", 1, transactionHeaderStrings.size());
		assertEquals("transaction header string should be of a specific format", "000000000000000000externalId000001052020160600200010000   0100000000000605202016000000000000000                 0                                                 ", transactionHeaderStrings.get(0));
	}
	
	@Test
	public void parse_TransactionHeadersWithNoSale_ExpectedTransactionHeadersGenerated() throws Exception {
		Employee employee = new Employee();
		employee.setId("id");
		employee.setExternalId("externalId");
		Employee[] employees = new Employee[]{employee};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("NO_SALE");
		tender.setEmployeeId("id");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionHeaderStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_HEADER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction header", 1, transactionHeaderStrings.size());
		assertEquals("transaction header string should be of a specific format", "000000000000000000externalId000001052020161200900010000   0100000000000305202016000000000000000                 0                                                 ", transactionHeaderStrings.get(0));
	}
	
	@Test
	public void parse_MerchandiseItemPassedtoTLOG_MerchandiseItemIncludesDepartmentAndClassNumber() throws Exception {
		Employee[] employees = new Employee[]{};
		
		ItemVariation itemVariation = new ItemVariation("");
		itemVariation.setId("id");
		itemVariation.setUserData("12345678");
		Item item = new Item();
		item.setId("id");
		item.setVariations(new ItemVariation[]{itemVariation});
		Item[] items = new Item[]{item};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setItemId("id");
		paymentItemDetail.setSku("");
		paymentItemDetail.setItemVariationId("id");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{paymentItemization});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> merchandiseItemStrings = tlogStrings.stream().filter(s -> s.startsWith(MERCHANDISE_ITEM) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one merchandise item", 1, merchandiseItemStrings.size());
		assertEquals("merchandise item should have specific format", "00100                        012345678000001000000000000000000000000000000000000000000001000000000001100000000000000000000", merchandiseItemStrings.get(0));
	}
	
	@Test
	public void parse_MerchandiseItemPassedtoTLOGMissingUserData_MerchandiseItemMissingDepartmentAndClassNumber() throws Exception {
		Employee[] employees = new Employee[]{};
		
		ItemVariation itemVariation = new ItemVariation("");
		itemVariation.setId("id");
		Item item = new Item();
		item.setId("id");
		item.setVariations(new ItemVariation[]{itemVariation});
		Item[] items = new Item[]{item};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentItemDetail paymentItemDetail = new PaymentItemDetail();
		paymentItemDetail.setItemId("id");
		paymentItemDetail.setSku("");
		paymentItemDetail.setItemVariationId("id");
		
		PaymentItemization paymentItemization = new PaymentItemization();
		paymentItemization.setItemDetail(paymentItemDetail);
		paymentItemization.setSingleQuantityMoney(new Money(0));
		paymentItemization.setGrossSalesMoney(new Money(0));
		paymentItemization.setNetSalesMoney(new Money(0));
		paymentItemization.setDiscountMoney(new Money(0));
		paymentItemization.setTaxes(new PaymentTax[]{});
		paymentItemization.setQuantity(1.0);
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{paymentItemization});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, items, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> merchandiseItemStrings = tlogStrings.stream().filter(s -> s.startsWith(MERCHANDISE_ITEM) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one merchandise item", 1, merchandiseItemStrings.size());
		assertEquals("merchandise item should have specific format", "00100                        0        000001000000000000000000000000000000000000000000001000000000001100000000000000000000", merchandiseItemStrings.get(0));
	}
	
	@Test
	public void parse_AdditiveTaxIncluded_TaxReflectedInTransactionTax() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentTax paymentTax = new PaymentTax();
		paymentTax.setName("");
		paymentTax.setRate("0.0");
		paymentTax.setAppliedMoney(new Money(0));
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{paymentTax});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionTaxStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_TAX_EXTENDED) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction tax", 1, transactionTaxStrings.size());
		assertTrue("expecting specific format for payment tax", transactionTaxStrings.contains("0540101                         0000000000000000000000000000"));
	}
	
	@Test
	public void parse_AdditiveAndInclusiveTaxIncluded_TaxReflectedInTransactionTaxes() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		PaymentTax paymentTax = new PaymentTax();
		paymentTax.setName("");
		paymentTax.setRate("0.0");
		paymentTax.setAppliedMoney(new Money(0));
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{paymentTax});
		payment.setInclusiveTax(new PaymentTax[]{paymentTax});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionTaxStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_TAX_EXTENDED) == true).collect(Collectors.toList());
		
		assertEquals("only expecting two transaction taxes", 2, transactionTaxStrings.size());
		assertTrue("expecting specific format for payment tax", transactionTaxStrings.contains("0540101                         0000000000000000000000000000"));
		assertTrue("expecting specific format for payment tax", transactionTaxStrings.contains("0540101                         0000000000000000000000000000"));
	}
	
	@Test
	public void parse_PaymentTransactionTotalSet_TransactionTotalReflectsMoneyAccurately() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(500));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionTotalStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_TOTAL) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction total string", 1, transactionTotalStrings.size());
		assertTrue("expecting specific format for payment total", transactionTotalStrings.contains("05300000005000"));
	}
	
	@Test
	public void parse_PaymentTransactionTaxSet_TransactionTaxReflectsMoneyAccurately() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(500));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionTaxStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_TAX) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction tax string", 1, transactionTaxStrings.size());
		assertTrue("expecting specific format for payment tax", transactionTaxStrings.contains("05200000005000"));
	}
	
	@Test
	public void parse_PaymentTransactionSubtotalSet_TransactionSubtotalReflectsMoneyAccurately() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(500));
		payment.setTaxMoney(new Money(250));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionSubtotalStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_SUBTOTAL) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction subtotal string", 1, transactionSubtotalStrings.size());
		assertTrue("expecting specific format for payment tax", transactionSubtotalStrings.contains("05100000002500"));
	}
	
	@Test
	public void parse_PassInTender_TenderReflectsAccuratePaymentAmount() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(500));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> tenderStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one tender string", 1, tenderStrings.size());
		assertTrue("expecting specific format for tender", tenderStrings.contains("06199      00000005000000000000000000000"));
	}
	
	@Test
	public void parse_PassInCreditCardTender_TenderReflectsAccuratePaymentAmount() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("CREDIT_CARD");
		tender.setCardBrand("VISA");
		tender.setTotalMoney(new Money(500));
		tender.setEntryMethod("SWIPED");
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> tenderStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER) == true).collect(Collectors.toList());
		List<String> creditCardTenderStrings = tlogStrings.stream().filter(s -> s.startsWith(CREDIT_CARD_TENDER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one tender string", 1, tenderStrings.size());
		assertEquals("expecting specific format for tender", "0617       00000005000000000000000000000", tenderStrings.get(0));
		assertEquals("only expecting one credit card tender string", 1, creditCardTenderStrings.size());
		assertEquals("expecting specific format for credit card tender", "065                                                                                                                        0000000000000000012        VISA                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ", creditCardTenderStrings.get(0));
	}
	
	@Test
	public void parse_DeviceNamePassedWithRegisterNumber_ClosingSaleRecordsShowCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		device.setName("(123)");
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setNetTotalMoney(new Money(0));
		payment.setDiscountMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> cashierRegisterIDStrings = tlogStrings.stream().filter(s -> s.startsWith(CASHIER_REGISTER_IDENTIFICATION) == true).collect(Collectors.toList());
		List<String> tenderCountStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER_COUNT) == true).collect(Collectors.toList());
		List<String> forInStoreReportingOnlyStrings = tlogStrings.stream().filter(s -> s.startsWith(FOR_IN_STORE_REPORTING_ONLY) == true).collect(Collectors.toList());

		assertEquals("only expecting one cashier register ID string", 1, cashierRegisterIDStrings.size());
		assertEquals("expecting specific format for cashier register ID string", "03600012300", cashierRegisterIDStrings.get(0));
		
		assertEquals("expecting sixteen tender count strings", 16, tenderCountStrings.size());
		assertEquals("expecting specific format for cash tender count string", "0341       000000000000000000000000000001", tenderCountStrings.get(0));
		assertEquals("expecting specific format for amex tender count string", "03411      000000000000000000000000000001", tenderCountStrings.get(1));
		assertEquals("expecting specific format for mall gift card tender count string", "03412      000000000000000000000000000001", tenderCountStrings.get(2));
		assertEquals("expecting specific format for discover tender count string", "03413      000000000000000000000000000001", tenderCountStrings.get(3));
		assertEquals("expecting specific format for jcb tender count string", "03414      000000000000000000000000000001", tenderCountStrings.get(4));
		assertEquals("expecting specific format for debit tender count string", "03419      000000000000000000000000000001", tenderCountStrings.get(5));
		assertEquals("expecting specific format for cheque tender count string", "0342       000000000000000000000000000001", tenderCountStrings.get(6));
		assertEquals("expecting specific format for mail cheque tender count string", "03420      000000000000000000000000000001", tenderCountStrings.get(7));
		assertEquals("expecting specific format for electronic gift card tender count string", "03430      000000000000000000000000000001", tenderCountStrings.get(8));
		assertEquals("expecting specific format for store credit tender count string", "0344       000000000000000000000000000001", tenderCountStrings.get(9));
		assertEquals("expecting specific format for travellers cheque tender count string", "0345       000000000000000000000000000001", tenderCountStrings.get(10));
		assertEquals("expecting specific format for gift certificate tender count string", "0346       000000000000000000000000000001", tenderCountStrings.get(11));
		assertEquals("expecting specific format for visa tender count string", "0347       000000000000000000000000000001", tenderCountStrings.get(12));
		assertEquals("expecting specific format for mastercard tender count string", "0349       000000000000000000000000000001", tenderCountStrings.get(13));
		assertEquals("expecting specific format for 98 tender count string", "03498      000000000000000000000000000001", tenderCountStrings.get(14));
		assertEquals("expecting specific format for echeck tender count string", "03499      000000000000000000000000000001", tenderCountStrings.get(15));
		
		assertEquals("expecting seven in store reporting strings", 3, forInStoreReportingOnlyStrings.size());
		assertEquals("expecting specific format for merchandise sales reporting string", "037002000001000000000000", forInStoreReportingOnlyStrings.get(0));
		assertEquals("expecting specific format for discounts reporting string", "037009000000000000000000", forInStoreReportingOnlyStrings.get(1));
		assertEquals("expecting specific format for sales tax reporting string", "037013000000000000000000", forInStoreReportingOnlyStrings.get(2));
	}
	
	@Test
	public void parse_DeviceNamePassedWithRegisterNumberAndDifferentValuesForMoney_ClosingSaleRecordsShowCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("CASH");
		tender.setTotalMoney(new Money(5));
		
		Device device = new Device();
		device.setName("(123)");
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(1));
		payment.setTaxMoney(new Money(2));
		payment.setNetTotalMoney(new Money(3));
		payment.setDiscountMoney(new Money(-4));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> cashierRegisterIDStrings = tlogStrings.stream().filter(s -> s.startsWith(CASHIER_REGISTER_IDENTIFICATION) == true).collect(Collectors.toList());
		List<String> tenderCountStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER_COUNT) == true).collect(Collectors.toList());
		List<String> forInStoreReportingOnlyStrings = tlogStrings.stream().filter(s -> s.startsWith(FOR_IN_STORE_REPORTING_ONLY) == true).collect(Collectors.toList());

		assertEquals("only expecting one cashier register ID string", 1, cashierRegisterIDStrings.size());
		assertEquals("expecting specific format for cashier register ID string", "03600012300", cashierRegisterIDStrings.get(0));
		
		assertEquals("expecting sixteen tender count strings", 16, tenderCountStrings.size());
		assertEquals("expecting specific format for cash tender count string", "0341       000001000000000500000000005001", tenderCountStrings.get(0));
		assertEquals("expecting specific format for amex tender count string", "03411      000000000000000000000000000001", tenderCountStrings.get(1));
		assertEquals("expecting specific format for mall gift card tender count string", "03412      000000000000000000000000000001", tenderCountStrings.get(2));
		assertEquals("expecting specific format for discover tender count string", "03413      000000000000000000000000000001", tenderCountStrings.get(3));
		assertEquals("expecting specific format for jcb tender count string", "03414      000000000000000000000000000001", tenderCountStrings.get(4));
		assertEquals("expecting specific format for debit tender count string", "03419      000000000000000000000000000001", tenderCountStrings.get(5));
		assertEquals("expecting specific format for cheque tender count string", "0342       000000000000000000000000000001", tenderCountStrings.get(6));
		assertEquals("expecting specific format for mail cheque tender count string", "03420      000000000000000000000000000001", tenderCountStrings.get(7));
		assertEquals("expecting specific format for electronic gift card tender count string", "03430      000000000000000000000000000001", tenderCountStrings.get(8));
		assertEquals("expecting specific format for store credit tender count string", "0344       000000000000000000000000000001", tenderCountStrings.get(9));
		assertEquals("expecting specific format for travellers cheque tender count string", "0345       000000000000000000000000000001", tenderCountStrings.get(10));
		assertEquals("expecting specific format for gift certificate tender count string", "0346       000000000000000000000000000001", tenderCountStrings.get(11));
		assertEquals("expecting specific format for visa tender count string", "0347       000000000000000000000000000001", tenderCountStrings.get(12));
		assertEquals("expecting specific format for mastercard tender count string", "0349       000000000000000000000000000001", tenderCountStrings.get(13));
		assertEquals("expecting specific format for 98 tender count string", "03498      000000000000000000000000000001", tenderCountStrings.get(14));
		assertEquals("expecting specific format for echeck tender count string", "03499      000000000000000000000000000001", tenderCountStrings.get(15));
		
		assertEquals("expecting seven in store reporting strings", 3, forInStoreReportingOnlyStrings.size());
		assertEquals("expecting specific format for merchandise sales reporting string", "037002000001000000000100", forInStoreReportingOnlyStrings.get(0));
		assertEquals("expecting specific format for discounts reporting string", "037009000001000000000410", forInStoreReportingOnlyStrings.get(1));
		assertEquals("expecting specific format for sales tax reporting string", "037013000001000000000200", forInStoreReportingOnlyStrings.get(2));
	}
	
	@Test
	public void parse_DeviceNamePassedWithRegisterNumberAndDifferentValuesForMoneyMultiplePayments_ClosingSaleRecordsShowCorrectly() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("CASH");
		tender.setTotalMoney(new Money(5));
		
		Device device = new Device();
		device.setName("(123)");
		
		Payment payment1 = new Payment();
		payment1.setItemizations(new PaymentItemization[]{});
		payment1.setTender(new Tender[]{tender});
		payment1.setDevice(device);
		payment1.setTotalCollectedMoney(new Money(1));
		payment1.setTaxMoney(new Money(2));
		payment1.setNetTotalMoney(new Money(3));
		payment1.setDiscountMoney(new Money(-4));
		payment1.setAdditiveTax(new PaymentTax[]{});
		payment1.setInclusiveTax(new PaymentTax[]{});
		payment1.setCreatedAt("2016-05-20T12:00:00Z");
		
		Payment payment2 = new Payment();
		payment2.setItemizations(new PaymentItemization[]{});
		payment2.setTender(new Tender[]{tender});
		payment2.setDevice(device);
		payment2.setTotalCollectedMoney(new Money(1));
		payment2.setTaxMoney(new Money(2));
		payment2.setNetTotalMoney(new Money(3));
		payment2.setDiscountMoney(new Money(-4));
		payment2.setAdditiveTax(new PaymentTax[]{});
		payment2.setInclusiveTax(new PaymentTax[]{});
		payment2.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment1,payment2};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> cashierRegisterIDStrings = tlogStrings.stream().filter(s -> s.startsWith(CASHIER_REGISTER_IDENTIFICATION) == true).collect(Collectors.toList());
		List<String> tenderCountStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER_COUNT) == true).collect(Collectors.toList());
		List<String> forInStoreReportingOnlyStrings = tlogStrings.stream().filter(s -> s.startsWith(FOR_IN_STORE_REPORTING_ONLY) == true).collect(Collectors.toList());

		assertEquals("only expecting one cashier register ID string", 1, cashierRegisterIDStrings.size());
		assertEquals("expecting specific format for cashier register ID string", "03600012300", cashierRegisterIDStrings.get(0));
		
		assertEquals("expecting sixteen tender count strings", 16, tenderCountStrings.size());
		assertEquals("expecting specific format for cash tender count string", "0341       000002000000001000000000010001", tenderCountStrings.get(0));
		assertEquals("expecting specific format for amex tender count string", "03411      000000000000000000000000000001", tenderCountStrings.get(1));
		assertEquals("expecting specific format for mall gift card tender count string", "03412      000000000000000000000000000001", tenderCountStrings.get(2));
		assertEquals("expecting specific format for discover tender count string", "03413      000000000000000000000000000001", tenderCountStrings.get(3));
		assertEquals("expecting specific format for jcb tender count string", "03414      000000000000000000000000000001", tenderCountStrings.get(4));
		assertEquals("expecting specific format for debit tender count string", "03419      000000000000000000000000000001", tenderCountStrings.get(5));
		assertEquals("expecting specific format for cheque tender count string", "0342       000000000000000000000000000001", tenderCountStrings.get(6));
		assertEquals("expecting specific format for mail cheque tender count string", "03420      000000000000000000000000000001", tenderCountStrings.get(7));
		assertEquals("expecting specific format for electronic gift card tender count string", "03430      000000000000000000000000000001", tenderCountStrings.get(8));
		assertEquals("expecting specific format for store credit tender count string", "0344       000000000000000000000000000001", tenderCountStrings.get(9));
		assertEquals("expecting specific format for travellers cheque tender count string", "0345       000000000000000000000000000001", tenderCountStrings.get(10));
		assertEquals("expecting specific format for gift certificate tender count string", "0346       000000000000000000000000000001", tenderCountStrings.get(11));
		assertEquals("expecting specific format for visa tender count string", "0347       000000000000000000000000000001", tenderCountStrings.get(12));
		assertEquals("expecting specific format for mastercard tender count string", "0349       000000000000000000000000000001", tenderCountStrings.get(13));
		assertEquals("expecting specific format for 98 tender count string", "03498      000000000000000000000000000001", tenderCountStrings.get(14));
		assertEquals("expecting specific format for echeck tender count string", "03499      000000000000000000000000000001", tenderCountStrings.get(15));
		
		assertEquals("expecting seven in store reporting strings", 3, forInStoreReportingOnlyStrings.size());
		assertEquals("expecting specific format for merchandise sales reporting string", "037002000002000000000200", forInStoreReportingOnlyStrings.get(0));
		assertEquals("expecting specific format for discounts reporting string", "037009000002000000000810", forInStoreReportingOnlyStrings.get(1));
		assertEquals("expecting specific format for sales tax reporting string", "037013000002000000000400", forInStoreReportingOnlyStrings.get(2));
	}
	
	@Test
	public void parse_DeviceNamePassedWithoutRegisterNumber_ClosingSaleRecordsShowsDefaultRegister() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		device.setName("no register number");
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setNetTotalMoney(new Money(0));
		payment.setDiscountMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> cashierRegisterIDStrings = tlogStrings.stream().filter(s -> s.startsWith(CASHIER_REGISTER_IDENTIFICATION) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one cashier register ID string", 1, cashierRegisterIDStrings.size());
		assertEquals("expecting specific format for cashier register ID string", "03600000000", cashierRegisterIDStrings.get(0));
	}
	
	@Test
	public void parse_OtherTenderTypeNotGiftCard_NotTallied() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("OTHER");
		tender.setTotalMoney(new Money(5));
		
		Device device = new Device();
		device.setName("(123)");
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(1));
		payment.setTaxMoney(new Money(2));
		payment.setNetTotalMoney(new Money(3));
		payment.setDiscountMoney(new Money(-4));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> cashierRegisterIDStrings = tlogStrings.stream().filter(s -> s.startsWith(CASHIER_REGISTER_IDENTIFICATION) == true).collect(Collectors.toList());
		List<String> tenderCountStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER_COUNT) == true).collect(Collectors.toList());

		assertEquals("only expecting one cashier register ID string", 1, cashierRegisterIDStrings.size());
		assertEquals("expecting specific format for cashier register ID string", "03600012300", cashierRegisterIDStrings.get(0));
		
		assertEquals("expecting sixteen tender count strings", 16, tenderCountStrings.size());
		assertEquals("expecting specific format for cash tender count string", "0341       000000000000000000000000000001", tenderCountStrings.get(0));
		assertEquals("expecting specific format for amex tender count string", "03411      000000000000000000000000000001", tenderCountStrings.get(1));
		assertEquals("expecting specific format for mall gift card tender count string", "03412      000000000000000000000000000001", tenderCountStrings.get(2));
		assertEquals("expecting specific format for discover tender count string", "03413      000000000000000000000000000001", tenderCountStrings.get(3));
		assertEquals("expecting specific format for jcb tender count string", "03414      000000000000000000000000000001", tenderCountStrings.get(4));
		assertEquals("expecting specific format for debit tender count string", "03419      000000000000000000000000000001", tenderCountStrings.get(5));
		assertEquals("expecting specific format for cheque tender count string", "0342       000000000000000000000000000001", tenderCountStrings.get(6));
		assertEquals("expecting specific format for mail cheque tender count string", "03420      000000000000000000000000000001", tenderCountStrings.get(7));
		assertEquals("expecting specific format for electronic gift card tender count string", "03430      000000000000000000000000000001", tenderCountStrings.get(8));
		assertEquals("expecting specific format for store credit tender count string", "0344       000000000000000000000000000001", tenderCountStrings.get(9));
		assertEquals("expecting specific format for travellers cheque tender count string", "0345       000000000000000000000000000001", tenderCountStrings.get(10));
		assertEquals("expecting specific format for gift certificate tender count string", "0346       000000000000000000000000000001", tenderCountStrings.get(11));
		assertEquals("expecting specific format for visa tender count string", "0347       000000000000000000000000000001", tenderCountStrings.get(12));
		assertEquals("expecting specific format for mastercard tender count string", "0349       000000000000000000000000000001", tenderCountStrings.get(13));
		assertEquals("expecting specific format for 98 tender count string", "03498      000000000000000000000000000001", tenderCountStrings.get(14));
		assertEquals("expecting specific format for echeck tender count string", "03499      000000000000000000000000000001", tenderCountStrings.get(15));
	}
	
	@Test
	public void parse_OtherTenderTypeGiftCard_TalliedAsGiftCard() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("OTHER");
		tender.setName("MERCHANT_GIFT_CARD");
		tender.setTotalMoney(new Money(5));
		
		Device device = new Device();
		device.setName("(123)");
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(1));
		payment.setTaxMoney(new Money(2));
		payment.setNetTotalMoney(new Money(3));
		payment.setDiscountMoney(new Money(-4));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.setTimeZoneId("UTC");
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> cashierRegisterIDStrings = tlogStrings.stream().filter(s -> s.startsWith(CASHIER_REGISTER_IDENTIFICATION) == true).collect(Collectors.toList());
		List<String> tenderCountStrings = tlogStrings.stream().filter(s -> s.startsWith(TENDER_COUNT) == true).collect(Collectors.toList());

		assertEquals("only expecting one cashier register ID string", 1, cashierRegisterIDStrings.size());
		assertEquals("expecting specific format for cashier register ID string", "03600012300", cashierRegisterIDStrings.get(0));
		
		assertEquals("expecting sixteen tender count strings", 16, tenderCountStrings.size());
		assertEquals("expecting specific format for cash tender count string", "0341       000000000000000000000000000001", tenderCountStrings.get(0));
		assertEquals("expecting specific format for amex tender count string", "03411      000000000000000000000000000001", tenderCountStrings.get(1));
		assertEquals("expecting specific format for mall gift card tender count string", "03412      000000000000000000000000000001", tenderCountStrings.get(2));
		assertEquals("expecting specific format for discover tender count string", "03413      000000000000000000000000000001", tenderCountStrings.get(3));
		assertEquals("expecting specific format for jcb tender count string", "03414      000000000000000000000000000001", tenderCountStrings.get(4));
		assertEquals("expecting specific format for debit tender count string", "03419      000000000000000000000000000001", tenderCountStrings.get(5));
		assertEquals("expecting specific format for cheque tender count string", "0342       000000000000000000000000000001", tenderCountStrings.get(6));
		assertEquals("expecting specific format for mail cheque tender count string", "03420      000000000000000000000000000001", tenderCountStrings.get(7));
		assertEquals("expecting specific format for electronic gift card tender count string", "03430      000000000000000000000000000001", tenderCountStrings.get(8));
		assertEquals("expecting specific format for store credit tender count string", "0344       000000000000000000000000000001", tenderCountStrings.get(9));
		assertEquals("expecting specific format for travellers cheque tender count string", "0345       000000000000000000000000000001", tenderCountStrings.get(10));
		assertEquals("expecting specific format for gift certificate tender count string", "0346       000001000000000500000000005001", tenderCountStrings.get(11));
		assertEquals("expecting specific format for visa tender count string", "0347       000000000000000000000000000001", tenderCountStrings.get(12));
		assertEquals("expecting specific format for mastercard tender count string", "0349       000000000000000000000000000001", tenderCountStrings.get(13));
		assertEquals("expecting specific format for 98 tender count string", "03498      000000000000000000000000000001", tenderCountStrings.get(14));
		assertEquals("expecting specific format for echeck tender count string", "03499      000000000000000000000000000001", tenderCountStrings.get(15));
	}
	
	/*
	 * fewest number of objects necessary to create a valid TLOG. Useful as a base for building new test cases.
	@Test
	public void parse_MinimumPossibleInformationToGenerateTLOG_MinimalTLOGGenerated() throws Exception {
		Employee[] employees = new Employee[]{};
		
		MerchantLocationDetails merchantLocationDetails = new MerchantLocationDetails();
		
		Merchant merchant = new Merchant();
		merchant.setLocationDetails(merchantLocationDetails);
		
		Tender tender = new Tender();
		tender.setType("");
		tender.setTotalMoney(new Money(0));
		
		Device device = new Device();
		
		Payment payment = new Payment();
		payment.setItemizations(new PaymentItemization[]{});
		payment.setTender(new Tender[]{tender});
		payment.setDevice(device);
		payment.setTotalCollectedMoney(new Money(0));
		payment.setTaxMoney(new Money(0));
		payment.setAdditiveTax(new PaymentTax[]{});
		payment.setInclusiveTax(new PaymentTax[]{});
		payment.setCreatedAt("2016-05-20T12:00:00Z");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		System.out.println(tlog.toString());
	}
	*/
}
