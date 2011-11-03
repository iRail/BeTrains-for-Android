package tof.cv.mpp.bo;

/*
 * gathers all information about a certain trainstop
 */
public class Train {
	private String delay;
	private String station;
	private String hour;
	private String status;

	public Train(String station, String hour, String delay, String status) {
		this.delay = delay;
		this.station = station;
		this.hour = hour;
		this.status = status;
	}

	public String getDelay() {
		return this.delay;
	}

	public String getStation() {
		return this.station;
	}

	public String getHour() {
		return this.hour;
	}

	public String getstatus() {
		return this.status;
	}

}
