package tof.cv.mpp.bo;

public class Station {

	private String delay;
	private String station;
	private String time;
	private String platform;
	//private String direction;
	//private String vehicle;

	public String getStation() {
		return this.station;
	}

	public String getDelay() {
		return this.delay;
	}

	public String getPlatform() {
		return this.platform;
	}

	public String getTime() {
		return this.time;
	}

	public String getVehicle() {
		return "this.vehicle";
	}

	public String getDirection() {
		return "this.direction";
	}

}
