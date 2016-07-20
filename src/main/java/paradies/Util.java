package paradies;

public class Util {
	
	static final int MAX_STORE_ID_LENGTH = 4;
	
	public static String getStoreIdFromLocationNickname(String locationNickname) throws Exception {
		String storeId = "";
		if (locationNickname != null) {
			int firstIndex = locationNickname.indexOf('(');
			int lastIndex = locationNickname.indexOf(')');
			if (firstIndex > -1 && lastIndex > -1) {
				storeId = locationNickname.substring(firstIndex + 1, lastIndex);
				storeId = storeId.replaceAll("[^\\d]", "");
			}
		}

		if (storeId.length() == 0 || storeId.length() > MAX_STORE_ID_LENGTH) {
			throw new Exception("Invalid store ID: " + storeId);
		}

		return storeId;
	}
	
	public static String getDeviceIdFromDeviceName(String deviceName) {
		String deviceId = "";
		if (deviceName != null) {
			int registerNumberFirstIndex = deviceName.indexOf('(');
			int registerNumberLastIndex = deviceName.indexOf(')');
			if (registerNumberFirstIndex > -1 && registerNumberLastIndex > -1) {
				deviceId = deviceName.substring(registerNumberFirstIndex + 1, registerNumberLastIndex);
				deviceId = deviceId.replaceAll("[^\\d]", "");
			}				
		}
		return deviceId;
	}
	
	public static int getMoneyAmountFromDecimalString(String value) {
		// Remove leading zeros
		value = value.replaceFirst("^0+(?!$)", "");
		value = value.replace(".", "");
		return Integer.parseInt(value);
	}
}
