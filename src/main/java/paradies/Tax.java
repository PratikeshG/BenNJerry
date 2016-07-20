package paradies;

public class Tax {

	public static final String TAX_CODE_1 = "NT";
	public static final String TAX_CODE_2 = "TX";
	public static final String TAX_CODE_3 = "FAC";
	public static final String TAX_CODE_4 = "CLT";
	public static final String TAX_CODE_5 = "T05";
	public static final String TAX_CODE_6 = "T06";
	public static final String TAX_CODE_7 = "T07";
	public static final String TAX_CODE_8 = "T08";
	public static final String TAX_CODE_9 = "T09";
	public static final String TAX_CODE_10 = "T10";
	public static final String TAX_CODE_11 = "T11";
	public static final String TAX_CODE_12 = "T12";
	public static final String TAX_CODE_13 = "T13";
	public static final String TAX_CODE_14 = "T14";
	public static final String TAX_CODE_15 = "T15";
	public static final String TAX_CODE_16 = "T16";

	public static final String TAX_NAME_1 = "Non-taxable";
	public static final String TAX_NAME_2 = "Tax";
	public static final String TAX_NAME_3 = "Candy Item Tax";
	public static final String TAX_NAME_4 = "Magazine Tax";
	public static final String TAX_NAME_5 = "Newspaper Tax";
	public static final String TAX_NAME_6 = "Water Tax";
	public static final String TAX_NAME_7 = "OTC Medication Tax";
	public static final String TAX_NAME_8 = "Convenience Item Tax";
	public static final String TAX_NAME_9 = "Gum Tax";
	public static final String TAX_NAME_10 = "Fruit Juice Tax";
	public static final String TAX_NAME_11 = "Soft Drink Tax";
	public static final String TAX_NAME_12 = "Gourmet Candy Tax";
	public static final String TAX_NAME_13 = "Other Multi-Serving Food Tax";
	public static final String TAX_NAME_14 = "Clothing Tax";
	public static final String TAX_NAME_15 = "Books Tax";
	public static final String TAX_NAME_16 = "Childrens Clothing Tax";

	private int id;
	private String code;
	private String name;
	private String rate;

	public Tax(int id, String code, String name) {
		this(id, code, name, "0");
	}

	public Tax(int id, String code, String name, String rate) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.rate = rate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}
}
