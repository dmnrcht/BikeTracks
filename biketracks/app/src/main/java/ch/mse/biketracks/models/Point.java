package ch.mse.biketracks.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * POJO defining a Point
 * TODO cleanup
 */
public class Point implements Serializable {
    @SerializedName("lat")
    public double lat;
    @SerializedName("lng")
    public double lng;
    @SerializedName("elev")
    public double elev;

    public Point(double lat, double lng, int elev) {
        this.lat = lat;
        this.lng = lng;
        this.elev = elev;
    }

    /*
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

    public int getElev() {
        return elev;
    }

    public void setElev(int elev) {
        this.elev = elev;
    }
    */
}
