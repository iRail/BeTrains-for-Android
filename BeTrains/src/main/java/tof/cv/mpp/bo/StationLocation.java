package tof.cv.mpp.bo;

public class StationLocation implements Comparable<Object>  {

	private String name;
    private String id;
	private double locationY;
	private double locationX;
	private String distance;
	private double away;

	public StationLocation(String station,double lat, double lon, String distance, String id) {
		this.name = station;
		this.locationY = lat;
		this.locationX = lon;
        this.id = id;
		this.distance = distance;

	}

	public String getStation() {
		return name;
	}
	
	public String getDistance() {
		return distance;
	}

	public void setAway(double p ) {
		this.away=p;
	}

	public double getLat() {
		return locationY;
	}
	
	public double getLon() {
		return locationX;
	}


    public String getId() {
        return id;
    }


    public int compareTo(Object toCompare) {
		StationLocation otherStation=(StationLocation)toCompare;
	   // return  Double.compare(Double.valueOf(this.distance),Double.valueOf(otherStation.getDistance()));
		return this.getAway() > otherStation.getAway()?1:-1;

	}

	public double getAway() {
		return away;
	}
}
