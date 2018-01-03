package ch.mse.biketracks.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * POJO representing a Track
 * TODO cleanup
 */
public class Track implements Serializable {
    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("distance")
    public float distance;
    @SerializedName("climb")
    public double climb;
    @SerializedName("descent")
    public double descent;
    @SerializedName("type")
    public String type;
    @SerializedName("points")
    public List<Point> points;

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

    /*
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
    */
}
