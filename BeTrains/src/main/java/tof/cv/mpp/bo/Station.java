package tof.cv.mpp.bo;


import java.util.ArrayList;


public class Station {

	private String delay;
	private String station;
	private String time;
	private String platform;
	private PlatformInfo platforminfo;
	private String vehicle;

	public PlatformInfo getPlatforminfo() {
		return platforminfo;
	}

	private StationInfo stationinfo;
	private String version;
	private StationDepartures departures;
	private String canceled="";


	public String getVersion() {
		return version;
	}

	public String getStation() {
		return station;
	}

	public boolean isCancelled(){
		return "1".contentEquals(canceled);
	}

	public StationDepartures getStationDepartures() {
		return departures;
	}

    public StationInfo getStationInfo() {
        return stationinfo;
    }

	public String getDelay() {
		return this.delay;
	}
	public String getPlatform() {
		return this.platform;
	}
	public String getTime() {
		return this.time;
	}
	public String getVehicle() {
		return this.vehicle;
	}
	public String getDirection() {
		return "this.direction";
	}

	public StationInfo getStationinfo() {
		return stationinfo;
	}

	public class StationInfo {
		public String id;
		private double locationX;
		private double locationY;

		public String getId() {
			return id;
		}

		public double getLocationX() {
			return locationX;
		}

		public double getLocationY() {
			return locationY;
		}
	}

	public class StationDepartures {

		private ArrayList<StationDeparture> departure;

		public ArrayList<StationDeparture> getStationDeparture() {
			return departure;
		}
	}

	public class StationDeparture {
		private String station;
		private long time;
		private String delay;
		private String platform;
		private String vehicle;
		private String canceled="";
		PlatformInfo platforminfo;

		public Occupancy getOccupancy() {
			return occupancy;
		}

		private Occupancy occupancy;

		public PlatformInfo getPlatforminfo() {
			return platforminfo;
		}


		public Alerts getAlerts() {
			return alerts;
		}

		private Alerts alerts;

		public boolean isCancelled(){
			return "1".contentEquals(canceled);
		}

		public String getStation() {
			return station;
		}

		public long getTime() {
			return time;
		}

		public String getDelay() {
			return delay;
		}

		public String getPlatform() {
			return platform;
		}

		public String getVehicle() {
			return vehicle.replace("BE.NMBS.", "");
		}

		public String getStatus() {
			if (delay.contentEquals("0"))
				return "";
			return "+" + Integer.valueOf(delay) / 60 + "'";
		}
	}

}
