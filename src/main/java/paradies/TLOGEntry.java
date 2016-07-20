package paradies;

import paradies.records.HeaderRecord;
import paradies.records.Record;

public class TLOGEntry {
	
	private HeaderRecord headerRecord;
	private Record entryRecord;
	
	public TLOGEntry(HeaderRecord header, Record entry) {
		this.headerRecord = header;
		this.entryRecord = entry;
	}
	
	public HeaderRecord getHeaderRecord() {
		return headerRecord;
	}

	public void setHeaderRecord(HeaderRecord headerRecord) {
		this.headerRecord = headerRecord;
	}

	public Record getEntryRecord() {
		return entryRecord;
	}

	public void setEntryRecord(Record entryRecord) {
		this.entryRecord = entryRecord;
	}
	
	public String toString() {
		return headerRecord.toString() + entryRecord.toString();
	}
}
