package tof.cv.mpp.bo;

import android.util.Log;

public class StationLocation implements Comparable<Object> {

    private String name;
    private String standardname;
    private String id;
    private double locationY;
    private double locationX;
    private String distance;
    private double away = -1;

    public String getName() {
        return name;
    }

    public String getStation() {
        if (standardname != null) {
            return standardname;
        } else {
            return name;
        }

    }

    public String getDistance() {
        return distance;
    }

    public void setAway(double p) {
        this.away = p;
    }

    public double getLat() {
        return locationY;
    }

    public double getLon() {
        return locationX;
    }


    public String getId() {
        return id;
    }


    public int compareTo(Object toCompare) {
        StationLocation otherStation = (StationLocation) toCompare;
        // return  Double.compare(Double.valueOf(this.distance),Double.valueOf(otherStation.getDistance()));
        if (this.getAway() > 0 && otherStation.getAway() > 0) {
            return this.getAway() > otherStation.getAway() ? 1 : -1;
        } else {
            return (this.getStation().compareTo(otherStation.getStation()));
        }


    }

    public double getAway() {
        return away;
    }
}
