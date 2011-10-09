package tof.cv.bo;

import java.util.ArrayList;

public class Connection {

	private Station arrivalStation;
	private Station departureStation;
	private ArrayList<Via> vias;
	private String duration;
	private String delayD;
	private String delayA;


	public Connection(Station departureStation, ArrayList<Via> vias,
			Station arrivalStation,  String duration, String delayD, String delayA) {
		this.arrivalStation = arrivalStation;
		this.vias = vias;
		this.departureStation = departureStation;
		this.duration = duration;
		this.delayD = delayD;
		this.delayA = delayA;
	}


	
	public ArrayList<Via> getVias() {
		return vias;
	}

	public Station getArrivalStation() {
		return arrivalStation;
	}

	public Station getDepartureStation() {
		return departureStation;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public String getADelay() {
		return delayA;
	}
	
	public String getDDelay() {
		return delayD;
	}
	
	public boolean isDDelay() {
		return !delayD.contentEquals("0");
	}
	
	
	public boolean isADelay() {
		return !delayA.contentEquals("0");
	}

}
