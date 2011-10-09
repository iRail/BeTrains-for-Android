package tof.cv.bo;


public class Station {

	private String vehicle;
	private String platform;
	private boolean platformNormal; // if it's a departure station or arrivalstation , this can be filled in with yes = 1 
	private String time;
	private String station;
	private String stationCoordinates;
	private String strDelay;
	private String status; // maybe delete this datamember ?


	

	public Station(String vehicle, String platform,
			boolean platformNormal, String time, String station,
			String stationCoordinates,String strDelay,String status) {		
		this.vehicle = vehicle;
		this.platform = platform;
		this.platformNormal = platformNormal;
		this.time = time;
		this.station = station;
		this.stationCoordinates = stationCoordinates;
		this.strDelay = strDelay;
		this.status = status;
	}
	public String getStatus() {
		return status;
	}


	public String getVehicle() {
		return vehicle;
	}
	
	public String getDelayValue() {
		return strDelay;
	}



	public String getPlatform() {
		return platform;
	}



	public boolean getPlatformNormal() {
		return platformNormal;
	}



	public String getTime() {
		return time;
	}



	public String getStation() {
		return station;
	}



	public boolean isDelay() {
		return strDelay.contentEquals("0");
	}



	public String getStationCoordinates() {
		return stationCoordinates;
	}


	

}
