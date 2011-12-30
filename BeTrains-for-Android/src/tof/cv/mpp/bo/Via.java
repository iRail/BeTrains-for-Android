package tof.cv.mpp.bo;

public class Via {

	private String timeBetween;
	private ApiStation arrival;
	private ApiStation departure;
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

	public ApiStation getArrival() {
		return arrival;
	}

	public ApiStation getDeparture() {
		return departure;
	}

}
