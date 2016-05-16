package vfcorp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Record {

	/*
	 * The "values" map is a mapping between a string name and a string value.
	 * The name is derived from a record field, found in in the "fields" map.
	 * The value is an alphanumeric string.
	 */
	protected Map<String,String> values = new HashMap<String,String>();
	protected char[] record;
	
	private static Logger logger = LoggerFactory.getLogger(Record.class);
	
	public Record() {
		clearCharArray();
		values.put("Identifier", getId());
	}
	
	public Record(String record) {
		for (String key : getFields().keySet()) {
			FieldDetails details = getFields().get(key);
			
			// In the Epicor documentation, value locations are one-indexed,
			// not zero-indexed.
			int zeroIndexStartLocation = details.getStartLocation() - 1;
			int zeroIndexEndLocation = zeroIndexStartLocation + details.getCharacters();
			
			// If input is too short, pad it out with spaces
			if (zeroIndexEndLocation > record.length()) {
				record = String.format("%1$-" + zeroIndexEndLocation + "s", record);
			}
			String value = record.substring(zeroIndexStartLocation, zeroIndexEndLocation);
			values.put(key, value);
		}
	}
	
	/*
	 * The "fields" map is a mapping between a string name and a set of details
	 * regarding that field. Every record has a unique set of fields, each
	 * with their own unique characteristics.
	 */
	public abstract Map<String,FieldDetails> getFields();
	
	/*
	 * Every record is serialized into a string, the length of which is kept in
	 * a variable in every record; this method retrieves that length.
	 */
	public abstract int getLength();
	
	/*
	 * Every record is denoted by a unique identifier, maintained in each
	 * record; this method retrieves that identifier.
	 */
	public abstract String getId();
	
	public String toString() {
		clearCharArray();
		
		for (String field : values.keySet()) {
			String value = values.get(field);
			put(field, value);
		}
		
		return new String(record);
	}
	
	protected String putValue(String field, String value) throws Exception {
		if (getFields().get(field) == null) {
			logger.error("field details were not found for " + field + "for record type " + this.getId());
			throw new Exception("field details were not found for " + field + "for record type " + this.getId());
		}
		
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
		FieldDetails details = getFields().get(field);
		
		// Passed in value should always be a string. A null string should be treated as empty.
		if (value == null) {
			logger.info("null string passed in for field " + field + " for record type " + this.getId() + "; turning into empty string");
			value = "";
		}
		
		if (value.length() > details.getCharacters()) {
			logger.info("For class \"" + this.getClass().toString().substring(6) +
				"\", value \"" + value + "\" is too long to fit into field \"" +
				field + "\"; cutting to " + value.substring(0, details.getCharacters()));
			// Cut off value, culling characters from the right.
			value = value.substring(0, details.getCharacters());
		}
		
		boolean zeroFill = details.getComments().contains("zero filled");
		boolean spaceFill = details.getComments().contains("space filled");
		boolean rightJust = details.getComments().contains("right justified");
		boolean leftJust = details.getComments().contains("left justified");
		
		if (spaceFill || zeroFill || leftJust || rightJust) {
			int width = details.getCharacters() - value.length();
		    char fill;
		    if (zeroFill) {
		    	fill = '0';
		    } else {
		    	fill = ' ';
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
