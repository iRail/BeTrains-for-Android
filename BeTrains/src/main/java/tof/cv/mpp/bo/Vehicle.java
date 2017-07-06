package tof.cv.mpp.bo;

import android.text.Html;

import java.util.ArrayList;

import tof.cv.mpp.GameFragment;

/**
 * Created by 201601 on 04-Dec-15.
 */
public class Vehicle {

    private VehicleStops stops;
    private String version;
    private String vehicle;
    private long timestamp;
    private Vehicleinfo vehicleinfo;

    public Alerts getAlerts() {
        return alerts;
    }

    private Alerts alerts;

    public Vehicleinfo getVehicleInfo() {
        return vehicleinfo;
    }

    public VehicleStops getVehicleStops() {
        return stops;
    }

    public String getVersion() {
        return version;
    }

    public String getId() {
        return vehicle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public class VehicleStops {

        private ArrayList<VehicleStop> stop;

        public ArrayList<VehicleStop> getVehicleStop() {
            return stop;
        }
    }

    public static class VehicleStop {

        private String station;
        private long time;
        private int left;
        private String delay;
        private String canceled;
        Station.StationInfo stationinfo;

        PlatformInfo platforminfo;
        public PlatformInfo getPlatforminfo() {
            return platforminfo;
        }



        public boolean isCancelled(){
            return "1".contentEquals(canceled);
        }

        public Station.StationInfo getStationInfo() {
            return stationinfo;
        }

        public String getStation() {
            return Html.fromHtml(station).toString();
        }

        public long getTime() {
            return time;
        }

        public int hasLeft() {
            return left;
        }

        public String getDelay() {
            return delay;
        }

        public String getStatus() {
            if (delay.contentEquals("0"))
                return "";

            return "+" + Integer.valueOf(delay) / 60 + "'";
        }
    }

}