package tof.cv.mpp.bo;

public class StationLocation implements Comparable<Object>  {

	private String station;
	private double lat;
	private double lon;
	private String distance;

	public StationLocation(String station,double lat, double lon, String distance) {
		this.station = station;
		this.lat = lat;
		this.lon = lon;
		this.distance = distance;

	}

	public String getStation() {
		return station;
	}
	
	public String getDistance() {
		return distance;
	}

	public double getLat() {
		return lat;
	}
	
	public double getLon() {
		return lon;
	}

	
	public int compareTo(Object toCompare) {
		StationLocation otherStation=(StationLocation)toCompare;
	    return  Double.compare(Double.valueOf(this.distance),Double.valueOf(otherStation.getDistance()));

	}

}
