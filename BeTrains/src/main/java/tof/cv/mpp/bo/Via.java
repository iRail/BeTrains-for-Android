package tof.cv.mpp.bo;

import tof.cv.mpp.Utils.ConnectionMaker;

public class Via {

	private String timeBetween;
	private Station arrival;
	private Station departure;
	private String station;
	private String vehicle;
    ConnectionMaker.StationInfo stationinfo;

    public ConnectionMaker.StationInfo getStationInfo() {
        return stationinfo;
    }

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
