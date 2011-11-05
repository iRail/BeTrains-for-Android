package tof.cv.mpp.bo;

import java.util.ArrayList;

public class ConnectionOld {

	private StationOld arrivalStation;
	private StationOld departureStation;
	private ArrayList<Via> vias;
	private String duration;
	private String delayD;
	private String delayA;


	public ConnectionOld(StationOld departureStation, ArrayList<Via> vias,
			StationOld arrivalStation,  String duration, String delayD, String delayA) {
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

	public StationOld getArrivalStation() {
		return arrivalStation;
	}

	public StationOld getDepartureStation() {
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
