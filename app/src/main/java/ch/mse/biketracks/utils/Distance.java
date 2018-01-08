package ch.mse.biketracks.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import ch.mse.biketracks.models.Point;

public class Distance {

    public static double distance(Point a, Point b) {
        return distance(a.getLat(), b.getLat(), a.getLng(), b.getLng(), a.getElev(), b.getElev());
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    /**
     * Compute the centroid of a list of Point
     * @param points List of points to compute the centroid
     * @return the position of the centroid
     */
    public static LatLng centroid(List<Point> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();
        for(Point point: points) {
            latitude += point.getLat();
            longitude += point.getLng();
        }
        return new LatLng(latitude/n, longitude/n);
    }
}
