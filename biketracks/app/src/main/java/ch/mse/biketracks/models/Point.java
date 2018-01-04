package ch.mse.biketracks.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

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

    public Point(double lat, double lng, int elev) {
        this.lat = lat;
        this.lng = lng;
        this.elev = elev;
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
}
