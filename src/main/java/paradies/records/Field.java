package paradies.records;

public class Field {

	public static enum Type {
        ALPHA, NUMERIC, ALPHANUMERIC
    }
	
	private int number;
	private String name;
	private int startColumn;
	private String size;
	private Field.Type type;
	private String value;
	
	public Field(int number, String name, int startColumn, String size, Field.Type type) {
		this(number, name, startColumn, size, type, "");
	}
	
	public Field(int number, String name, int startColumn, String size, Field.Type type, String value) {
		this.number = number;
		this.name = name;
		this.startColumn = startColumn;
		this.size = size;
		this.type = type;
		this.value = value;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}

	public String getSize() {
		return size;
	}
	
	public int getSizeInt() {
		int s = 0;
		for (String val : size.split("\\.")) {
			s += Integer.parseInt(val);
	    }
		return s;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public Field.Type getType() {
		return type;
	}

	public void setType(Field.Type type) {
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static String padLeftZero(String value, int size) {
		return String.format("%" + size + "s", value).replace(" ", "0");
	}
	
	public static String padRightSpace(String value, int size) {
		return String.format("%-" + size + "s", value);
	}

	public String toString() {
		String output;
		if (type == Field.Type.NUMERIC) {
			output = padLeftZero(value, getSizeInt());
		} else {
			output = padRightSpace(value, getSizeInt());
		}
		return output;
	}
}
