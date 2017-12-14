package ch.mse.biketracks.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by antoi on 15.10.2017.
 */

public class Track implements Serializable {
    private int id;
    private String name;
    private Date date;
    private int duration;
    private float speed;
    private float distance;
    private int climb;
    private int descent;
    private String type;
    private List<Point> points;

    public Track(String name, Date date, int duration, float speed, float distance) {
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.speed = speed;
        this.distance = distance;
    }

    public Track(int id, String name, Date date, int duration, float speed, float distance, int climb, int descent, String type, List<Point> points) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.speed = speed;
        this.distance = distance;
        this.climb = climb;
        this.descent = descent;
        this.type = type;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClimb() {
        return climb;
    }

    public void setClimb(int climb) {
        this.climb = climb;
    }

    public int getDescent() {
        return descent;
    }

    public void setDescent(int descent) {
        this.descent = descent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
