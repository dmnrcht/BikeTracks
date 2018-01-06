package ch.mse.biketracks.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * POJO defining a Point
 */
public class Point implements Serializable {
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;
    @SerializedName("elev")
    private double elev;

    // Generated from activity, not present in API responses
    private long time; // timestamp in ms
    private int duration;
    private float speed;

    public Point(double lat, double lng, int elev) {
        this.lat = lat;
        this.lng = lng;
        this.elev = elev;
    }

    public Point(double lat, double lng, int elev, long time) {
        this(lat, lng, elev);
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getElev() {
        return elev;
    }

    public void setElev(double elev) {
        this.elev = elev;
    }

    /**
     * @return timestamp in ms
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the timestamp in ms
     */
    public void setTime(long time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
