package paradies;

public class Util {

	public static String getValueInParenthesis(String input) {
		String value = "";
		if (input != null) {
			int firstIndex = input.indexOf('(');
			int lastIndex = input.indexOf(')');
			if (firstIndex > -1 && lastIndex > -1) {
				value = input.substring(firstIndex + 1, lastIndex);
				value = value.replaceAll("[^\\d]", "");
			}
		}
		return value;
	}

	public static int getMoneyAmountFromDecimalString(String value) {
		// Remove leading zeros
		value = value.replaceFirst("^0+(?!$)", "");
		value = value.replace(".", "");
		return Integer.parseInt(value);
	}

	public static int[] divideIntegerEvenly(int amount, int totalPieces) {
		int quotient = amount / totalPieces;
		int remainder = amount % totalPieces;

		int [] results = new int[totalPieces];
		for(int i = 0; i < totalPieces; i++) {
		    results[i] = i < remainder ? quotient + 1 : quotient;
		}

		return results;
	}
}
