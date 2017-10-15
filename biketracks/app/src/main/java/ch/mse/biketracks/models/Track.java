package ch.mse.biketracks.models;

import java.util.Date;

/**
 * Created by antoi on 15.10.2017.
 */

public class Track {
    private String location;
    private Date date;
    private int duration;
    private float speed;
    private float distance;

    public Track(String location, Date date, int duration, float speed, float distance) {
        this.location = location;
        this.date = date;
        this.duration = duration;
        this.speed = speed;
        this.distance = distance;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
