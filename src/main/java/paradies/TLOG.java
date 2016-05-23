package paradies;

import java.util.ArrayList;

public class TLOG {
	
	private String storeId;
	private String deviceId;;
	private ArrayList<TLOGEntry> entries;
	
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

	public ArrayList<TLOGEntry> getEntries() {
		return entries;
	}

	public void addEntry(TLOGEntry newEntry) {
		entries.add(newEntry);
	}
	
	public void addEntries(ArrayList<TLOGEntry> newEntries) {
		entries.addAll(newEntries);
	}
	
	public String toString() {
		String output = "";

		for (int i = 0; i < entries.size(); i++) {
			output += entries.get(i).toString();

			if (i < entries.size() - 1) {
				output += "\r\n";
			}
		}

		return output;
	}
}