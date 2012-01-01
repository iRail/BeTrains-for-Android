package tof.cv.mpp.bo;



public class Connection {

	public String duration;
	private Station departure;
	private Station arrival;
	private Vias vias;
	
	public Station getArrival() {
		return this.arrival;
	}
	public Station getDeparture() {
		return this.departure;
	}
	public String getDuration() {
		return this.duration;
	}
	public Vias getVias() {
		return this.vias;
	}


}
