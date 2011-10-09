package tof.cv.bo;

public class OldStationInfo {
	
	private String delay;
	private String station;
	private String hour;
	private String status;
	private String trainid;

	public OldStationInfo(String station, String hour, String delay,
			String status, String trainid) {
		this.delay = delay;
		this.station = station;
		this.hour = hour;
		this.status = status;
		this.trainid = trainid;
	}

	public String getDelay() {
		return delay;
	}

	public String getStation() {
		return station;
	}

	public String getHour() {
		return hour;
	}

	public String getStatus() {
		return status;
	}

	public String getTrainid() {
		return trainid;
	}

	

}
