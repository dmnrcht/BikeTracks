package ch.mse.biketracks.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by antoi on 15.10.2017.
 */

public class Point implements Serializable {
    private double lat;
    private double lng;
    private int elev;

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

    public int getElev() {
        return elev;
    }

    public void setElev(int elev) {
        this.elev = elev;
    }
}
