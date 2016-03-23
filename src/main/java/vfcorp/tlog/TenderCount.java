package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;
import vfcorp.TLOG;
import vfcorp.TLOG.TenderCode;

import com.squareup.connect.CashDrawerShift;

public class TenderCount extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 41;
		id = "034";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Tender Code", new FieldDetails(8, 4, "From table, left justified"));
		fields.put("Number In Drawer", new FieldDetails(6, 12, "zero filled"));
		fields.put("Amount In Drawer", new FieldDetails(10, 18, "zero filled"));
		fields.put("Amount In Drawer Sign", new FieldDetails(1, 28, "1 = Negative, zero filled"));
		fields.put("Amount Counted", new FieldDetails(10, 29, "zero filled"));
		fields.put("Amount Counted Sign", new FieldDetails(1, 39, "1 = Negative, zero filled"));
		fields.put("Currency Indicator", new FieldDetails(1, 40, "1 = Alternate, 0 = Primary, zero filled"));
		fields.put("Counted Indicator", new FieldDetails(1, 41, "1 = Dollar, 0 = Quantity, zero filled"));
	}
	
	public TenderCount() {
		super();
	}

	public TenderCount(String record) {
		super(record);
	}

	@Override
	public Map<String, FieldDetails> getFields() {
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
	
	public TenderCount parse(TenderCode tenderCode, CashDrawerShift cashDrawerShift) {
		Map<TenderCode,String> tenderCodes = TLOG.getTenderCodes();
		
		String tenderCodeString = tenderCodes.get(tenderCode);
		
		putValue("Tender Code", tenderCodeString);
		putValue("Number In Drawer", ""); // not supported
		putValue("Amount In Drawer", "" + cashDrawerShift.getExpectedCashMoney().getAmount());
		putValue("Amount In Drawer Sign", cashDrawerShift.getExpectedCashMoney().getAmount() >= 0 ? "0" : "1");
		putValue("Amount Counted", "" + cashDrawerShift.getClosedCashMoney().getAmount());
		putValue("Amount Counted Sign", cashDrawerShift.getClosedCashMoney().getAmount() >= 0 ? "0" : "1");
		putValue("Currency Indicator", "0"); // not supported
		putValue("Counted Indicator", "1"); // 1 is "dollars"; other value not supported
		
		return this;
	}
}
