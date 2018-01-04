package ch.mse.biketracks.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * POJO representing a Track
 */
public class Track implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("distance")
    private float distance;
    @SerializedName("climb")
    private double climb;
    @SerializedName("descent")
    private double descent;
    @SerializedName("type")
    private String type;
    @SerializedName("points")
    private List<Point> points;

    // Generated from activity, not present in API calls
    private Date date;
    private int duration;
    private float speed;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getClimb() { return climb; }

    public void setClimb(double climb) {
        this.climb = climb;
    }

    public double getDescent() {
        return descent;
    }

    public void setDescent(double descent) {
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
