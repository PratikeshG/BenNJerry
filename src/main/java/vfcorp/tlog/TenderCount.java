package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.CashDrawerShift;

import vfcorp.Record;
import vfcorp.RecordDetails;
import vfcorp.TLOG;
import vfcorp.TLOG.TENDER_CODE;

public class TenderCount extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 41;
		id = "034";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Tender Code", new RecordDetails(8, 4, "From table, left justified"));
		fields.put("Number In Drawer", new RecordDetails(6, 12, "zero filled"));
		fields.put("Amount In Drawer", new RecordDetails(10, 18, "zero filled"));
		fields.put("Amount In Drawer Sign", new RecordDetails(1, 28, "1 = Negative, zero filled"));
		fields.put("Amount Counted", new RecordDetails(10, 29, "zero filled"));
		fields.put("Amount Counted Sign", new RecordDetails(1, 39, "1 = Negative, zero filled"));
		fields.put("Currency Indicator", new RecordDetails(1, 40, "1 = Alternate, 0 = Primary, zero filled"));
		fields.put("Counted Indicator", new RecordDetails(1, 41, "1 = Dollar, 0 = Quantity, zero filled"));
	}
	
	public TenderCount() {
		super();
	}

	public TenderCount(String record) {
		super(record);
	}

	@Override
	public Map<String, RecordDetails> getFields() {
		return fields;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String getId() {
		return id;
	}
	
	public TenderCount parse(TENDER_CODE tenderCode, CashDrawerShift cashDrawerShift) {
		Map<TENDER_CODE,String> tenderCodes = TLOG.getTenderCodes();
		
		String tenderCodeString = tenderCodes.get(tenderCode);
		
		values.put("Tender Code", tenderCodeString);
		values.put("Number In Drawer", ""); // not supported
		values.put("Amount In Drawer", "" + cashDrawerShift.getExpectedCashMoney().getAmount());
		values.put("Amount In Drawer Sign", cashDrawerShift.getExpectedCashMoney().getAmount() >= 0 ? "0" : "1");
		values.put("Amount Counted", "" + cashDrawerShift.getClosedCashMoney().getAmount());
		values.put("Amount Counted Sign", cashDrawerShift.getClosedCashMoney().getAmount() >= 0 ? "0" : "1");
		values.put("Currency Indicator", "0"); // not supported
		values.put("Counted Indicator", "1"); // 1 is "dollars"; other value not supported
		
		return this;
	}
}
