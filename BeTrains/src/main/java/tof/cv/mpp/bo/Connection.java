package tof.cv.mpp.bo;



public class Connection {

	public String duration;
	private Station departure;
	private Station arrival;
	private Vias vias;
	private Alerts alerts;

	public Occupancy getOccupancy() {
		return occupancy;
	}

	private Occupancy occupancy;
	
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
	public Alerts getAlerts() {
		return this.alerts;
	}


    public void removeAlerts() {
		alerts=null;
    }
}
