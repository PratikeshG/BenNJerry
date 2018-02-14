package util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.v2.Location;

public class LocationContext implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String timezone;
	private String beginTime;
	private String endTime;
	private Location location;

	public String getBeginTime() {
		return beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getId() {
		return id;
	}

	public Location getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public String getTimezone() {
		return timezone;
	}

	public LocationContext(Location location, String beginTime, String endTime) {
		this.id = location.getId();
		this.name = location.getName();
		this.timezone = location.getTimezone();
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.location = location;
	}

	public LocationContext(Location location, Map<String, String> queryParams) {
		this(location, queryParams.get(Constants.BEGIN_TIME), queryParams.get(Constants.END_TIME));

	}

	public Map<String, String> generateQueryParamMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Constants.BEGIN_TIME, this.beginTime);
		map.put(Constants.END_TIME, this.endTime);
		return map;
	}
}
