package vfcorp;

public class Util {

    public static String getValueBetweenChars(String input, char c, char d) {
	String value = "";

	if (input != null) {
	    int firstIndex = input.indexOf(c);
	    int lastIndex = input.indexOf(d);
	    if (firstIndex > -1 && lastIndex > -1 && lastIndex > firstIndex) {
		value = input.substring(firstIndex + 1, lastIndex);
	    }
	}

	return value;
    }

    public static String getValueInBrackets(String input) {
	return getValueBetweenChars(input, '[', ']');
    }

    public static String getValueInParenthesis(String input) {
	return getValueBetweenChars(input, '(', ')');
    }

    public static String getRegisterNumber(String deviceName) {
	String registerNumber = "99"; // default

	if (deviceName != null) {
	    String n = getValueInParenthesis(deviceName).replaceAll("[^\\d]", "");
	    if (n.length() > 0) {
		registerNumber = n;
	    }
	}

	// Pad to three characters with left zeros
	return String.format("%03d", Integer.parseInt(registerNumber));
    }

    public static String getStoreNumber(String input) {
	String n = getValueInParenthesis(input).replaceAll("[^\\d]", "");
	String storeNumber = n.length() > 0 ? n : "0";

	// Pad to five characters with left zeros
	return String.format("%05d", Integer.parseInt(storeNumber));
    }

    public static int[] divideIntegerEvenly(int amount, int totalPieces) {
	int quotient = amount / totalPieces;
	int remainder = amount % totalPieces;

	int[] results = new int[totalPieces];
	for (int i = 0; i < totalPieces; i++) {
	    results[i] = i < remainder ? quotient + 1 : quotient;
	}

	// Reverse - provide smallest discounts first
	for (int i = 0; i < results.length / 2; i++) {
	    int temp = results[i]; // swap numbers
	    results[i] = results[results.length - 1 - i];
	    results[results.length - 1 - i] = temp;
	}

	return results;
    }
}
