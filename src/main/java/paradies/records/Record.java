package paradies.records;

import java.util.ArrayList;

public abstract class Record {

	final int totalFields;
	final int size;
	protected ArrayList<Field> fields;
	
	public Record(int totalFields, int size) {
		this.totalFields = totalFields;
		this.size = size;
	}
	
	public void setFieldValue(String fieldName, String value) {
		for (Field f : fields) {
			if (f.getName().equals(fieldName)) {
				f.setValue(value);
				break;
			}
		}
	}
	
	public String toString() {
		String output = "";
		for (Field f : fields) {
			output += f;
		}
		return output;
	}
}