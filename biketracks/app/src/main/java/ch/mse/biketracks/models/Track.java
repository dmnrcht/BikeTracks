package ch.mse.biketracks.models;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
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
    private int distance;
    @SerializedName("climb")
    private int climb;
    @SerializedName("descent")
    private int descent;
    @SerializedName("type")
    private String type;
    @SerializedName("points")
    private List<Point> points;

    // Generated from activity, not present in API responses
    private Date date;
    private int duration;
    private double speed;
    private LatLngBounds latLngBounds;
    private Polyline polyline;
    private byte[] image;

    public Track(Date date) {
        this.date = date;
    }

    public Track(String name, Date date, int duration, double speed, int distance) {
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.speed = speed;
        this.distance = distance;
    }

    public Track(int id, String name, Date date, int duration, double speed, int distance, int climb, int descent, String type, List<Point> points, byte[] image) {
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
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getClimb() { return climb; }

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

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void addPoint(Point point) {
        this.points.add(point);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return duration in ms
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration in ms
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @return average speed in m/s
     */
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public LatLngBounds getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
