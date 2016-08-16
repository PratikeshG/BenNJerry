package vfcorp;

public class Util {
	public static String getValueInBrackets(String input) {
		String value = "";

		int firstIndex = input.indexOf('[');
		int lastIndex = input.indexOf(']');
		if (firstIndex > -1 && lastIndex > -1 && lastIndex > firstIndex) {
			value = input.substring(firstIndex + 1, lastIndex);
		}

		return value;
	}

	public static int[] divideIntegerEvenly(int amount, int totalPieces) {
		int quotient = amount / totalPieces;
		int remainder = amount % totalPieces;

		int [] results = new int[totalPieces];
		for(int i = 0; i < totalPieces; i++) {
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
