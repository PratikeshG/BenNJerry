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
import com.squareup.connect.Merchant;
import com.squareup.connect.MerchantLocationDetails;
import com.squareup.connect.Money;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemDetail;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Tender;

public class TLOGTest {

	private static final String TRANSACTION_HEADER = "000";
	private static final String AUTHORIZATION_CODE = "023";
	private static final String ITEM_TAX_MERCHANDISE_NON_MERCHANDISE_ITEM_FEES = "025";
	private static final String LINE_ITEM_ACCOUNTING_STRING = "055";
	private static final String LINE_ITEM_ASSOCIATE_AND_DISCOUNT_ACCOUNTING_STRING = "056";
	
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> authorizationCodeStrings = tlogStrings.stream().filter(s -> s.startsWith(AUTHORIZATION_CODE) == true).collect(Collectors.toList());
		
		assertEquals("expecting only one authorization code", 1, authorizationCodeStrings.size());
		assertEquals("authorization code should be of a specific format", "023        180externalId", authorizationCodeStrings.get(0));
	}
	
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
			payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
			Payment[] payments = new Payment[]{payment};
			
			TLOG tlog = new TLOG();
			tlog.setItemNumberLookupLength(1);
			tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
			payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
			Payment[] payments = new Payment[]{payment};
			
			TLOG tlog = new TLOG();
			tlog.setItemNumberLookupLength(1);
			tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionHeaderStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_HEADER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction header", 1, transactionHeaderStrings.size());
		assertEquals("transaction header string should be of a specific format", "000000000000000000externalId000001xxxxxxxxxxxx200010000   01000000000006xxxxxxxx000000000000000                 0                                                 ", transactionHeaderStrings.get(0));
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		List<String> tlogStrings = Arrays.asList(tlog.toString().split("\\r?\\n"));
		List<String> transactionHeaderStrings = tlogStrings.stream().filter(s -> s.startsWith(TRANSACTION_HEADER) == true).collect(Collectors.toList());
		
		assertEquals("only expecting one transaction header", 1, transactionHeaderStrings.size());
		assertEquals("transaction header string should be of a specific format", "000000000000000000externalId000001xxxxxxxxxxxx900010000   01000000000003xxxxxxxx000000000000000                 0                                                 ", transactionHeaderStrings.get(0));
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
		payment.setCreatedAt("xxxxxxxxxxxxxxxxxxx");
		Payment[] payments = new Payment[]{payment};
		
		TLOG tlog = new TLOG();
		tlog.setItemNumberLookupLength(1);
		tlog.setObjectStore(new FakeObjectStore<String>());
		tlog.parse(merchant, payments, new Item[]{}, employees);
		
		System.out.println(tlog.toString());
	}
	*/
}
