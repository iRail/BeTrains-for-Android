package tof.cv.mpp.widget;


import android.content.Context;

import java.util.ArrayList;

import tof.cv.mpp.bo.Vehicle;

public class TrainService {

	private static TrainService instance;
	private int currentPos=1;
	private String tid;	
	private String departure;
	private String message;
	private String arrival;
	private ArrayList<Vehicle.VehicleStops> myList= new ArrayList<>();

	
	private TrainService(Context context) {
		//this.cache = new Cache(context);
	}

	public static TrainService getInstance(Context context) {
		if (instance == null)
			instance = new TrainService(context);
		return instance;
	}
	
	
	public String getTid() {
		return tid;
	}

	public void setTid(String ptid) {
		tid = ptid;
	}
	

	public String getDeparture() {
		return departure;
	}

	public void setDeparture(String pDeparture) {
		departure = pDeparture;
	}
	
	public String getArrival() {
		return arrival;
	}
	
	public int getCurrentPos() {
		return this.currentPos;
	}
	
	public void setCurrentPos(int pCurrentPos) {
		this.currentPos=pCurrentPos;
	}

	public void setArrival(String parrival) {
		arrival = parrival;
	}
	
	public void addArret(String pgare, String pheure, String pretard) {
		//maliste.add(new Arret(pgare,pheure,pretard));
	}
	
	public void resetStop() {
		this.myList.clear();
	}
	
	public ArrayList<Vehicle.VehicleStops> getAllStops() {
		return myList;
	}
	
	public void setAllSTOPS(ArrayList<Vehicle.VehicleStops> pStop) {
		this.myList=pStop;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String pMessage) {
		message = pMessage;
	}
	
	

}
