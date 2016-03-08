package vfcorp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Record {

	protected Map<String,String> values = new HashMap<String,String>();
	protected char[] record;
	
	private static Logger logger = LoggerFactory.getLogger(Record.class);
	
	public Record() {
		clearCharArray();
		values.put("Identifier", getId());
	}
	
	public Record(String record) {
		for (String key : getFields().keySet()) {
			RecordDetails details = getFields().get(key);
			
			// In the Epicor documentation, value locations are one-indexed,
			// not zero-indexed.
			int zeroIndexStartLocation = details.getStartLocation() - 1;
			int zeroIndexEndLocation = zeroIndexStartLocation + details.getCharacters();
			String value = record.substring(zeroIndexStartLocation, zeroIndexEndLocation);
			
			values.put(key, value);
		}
	}
	
	public abstract Map<String,RecordDetails> getFields();
	
	public abstract int getLength();
	
	public abstract String getId();
	
	public String toString() {
		clearCharArray();
		
		for (String field : values.keySet()) {
			String value = values.get(field);
			put(field, value);
		}
		
		return new String(record);
	}
	
	protected String putValue(String field, String value) {
		return values.put(field, value);
	}
	
	protected String getValue(String field) {
		return values.get(field);
	}
	
	/*
	 * Items are put into the record with right-justification and space filling
	 * by default.
	 */
	private void put(String field, String value) {
		RecordDetails details = getFields().get(field);
		
		if (value.length() > details.getCharacters()) {
			logger.info("Value " + value + " is too long to fit into field " + field +
					"; cutting to " + value.substring(0, details.getCharacters()));
			// Cut off value, culling characters from the right.
			value = value.substring(0, details.getCharacters());
		}
		
		boolean zeroFill = details.getComments().contains("zero filled");
		boolean spaceFill = details.getComments().contains("space filled");
		//boolean rightJust = details.getComments().contains("right justified");
		boolean leftJust = details.getComments().contains("left justified");
		
		if (spaceFill || zeroFill) {
			int width = details.getCharacters() - value.length();
		    char fill;
		    if (spaceFill) {
		    	fill = ' ';
		    } else {
		    	fill = '0';
		    }
		    
		    String stringFill = new String(new char[width]).replace('\0', fill);
		    if (leftJust) {
		    	value = value + stringFill;
		    } else {
		    	value = stringFill + value;
		    }
		}
		
		int start = details.getStartLocation() - 1;
		int finish = start + details.getCharacters() - 1;
		for (int i = finish, j = value.length() - 1; i >= start && j >= 0; i--, j--) {
			record[i] = value.charAt(j);
		}
	}
	
	private void clearCharArray() {
		record = new char[getLength()];
		for (int i = 0; i < getLength(); i++) {
			record[i] = ' ';
		}
	}
}
