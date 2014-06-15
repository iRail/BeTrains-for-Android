package tof.cv.mpp.bo;

public class Via {

	private String timeBetween;
	private Station arrival;
	private Station departure;
	private String station;
	private String vehicle;

	public String getTimeBetween() {
		return timeBetween;
	}

	public String getName() {
		return station;
	}

	public String getVehicle() {
		return vehicle;
	}

	public Station getArrival() {
		return arrival;
	}

	public Station getDeparture() {
		return departure;
	}

}
