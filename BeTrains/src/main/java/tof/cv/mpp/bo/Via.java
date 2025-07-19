package tof.cv.mpp.bo;

import tof.cv.mpp.Utils.ConnectionMaker;

public class Via {

	private String timeBetween;
	private Station arrival;
	private Station departure;
	private String station;
	private String vehicle;
    private Station.StationInfo stationinfo;

    public Station.StationInfo getStationInfo() {
        return stationinfo;
    }

	public String getTimeBetween() {
		long durationSeconds = Math.abs(Long.parseLong(departure.getTime()) - Long.parseLong(arrival.getTime()));
		long minutes = durationSeconds / 60;
		return minutes + "'";


	}

	public String getName() {
		return station;
	}

	public String getVehicle() {
		return arrival.getVehicle();
	}

	public Station getArrival() {
		return arrival;
	}

	public Station getDeparture() {
		return departure;
	}

}
