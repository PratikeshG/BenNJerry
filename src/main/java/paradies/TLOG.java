package paradies;

import java.util.ArrayList;
import java.util.List;

public class TLOG {
	
	private String storeId;
	private String deviceId;;
	private List<TLOGEntry> entries;
	
	public TLOG(String storeId, String deviceId) {
		this.storeId = storeId;
		this.deviceId = deviceId;
		this.entries = new ArrayList<TLOGEntry>();
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public List<TLOGEntry> getEntries() {
		return entries;
	}

	public void addEntry(TLOGEntry newEntry) {
		entries.add(newEntry);
	}
	
	public void addEntries(List<TLOGEntry> newEntries) {
		entries.addAll(newEntries);
	}
	
	public String toString() {
		String output = "";

		for (int i = 0; i < entries.size(); i++) {
			// Note: need trailing carriage return on all lines
			output += entries.get(i).toString() + "\r\n";
		}

		return output;
	}

	public String getDeviceIdCharacter() {
		int intDeviceId = Integer.parseInt(deviceId);
		return String.valueOf((char)(intDeviceId + 64));
	}
}