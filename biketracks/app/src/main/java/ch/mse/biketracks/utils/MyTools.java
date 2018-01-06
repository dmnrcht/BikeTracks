package ch.mse.biketracks.utils;

import android.graphics.Color;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;
import java.util.Locale;

import ch.mse.biketracks.models.Point;

/**
 * Some tools
 */
public class MyTools {

    /**
     * Compute total hours and minutes for given parameter
     * @param elapsedMillis the amount of time in milliseconds
     * @return Formatted time "HHhmm" for example "26h17"
     */
    public static String FormatTimeHHhmm(long elapsedMillis) {
        long hourInMs = 1000 * 60 * 60;
        long minuteInMs = 1000 * 60;
        long hours = elapsedMillis / hourInMs; // integer division
        int minutes = Math.round((elapsedMillis - hours * hourInMs) / minuteInMs);

        return String.format(Locale.ENGLISH,"%02dh%02d", hours, minutes);
    }

    /**
     * Build the elevation graph
     * @param points A list of Point
     * @return a tuple where first is the LineGraphSeries and second is the maximum value on x axis.
     */
    public static Tuple<LineGraphSeries<DataPoint>, Double> ElevationGraph(List<Point> points) {
        int pointsSize = points.size();
        DataPoint[] dataPoints = new DataPoint[pointsSize];
        double totDistanceKm = 0;
        Point previous = points.get(0);
        int i = 0;
        dataPoints[i++] = new DataPoint(0, previous.getElev());
        for (Point p : points.subList(1, pointsSize)) {
            totDistanceKm += (Distance.distance(previous, p) / 1000.0);
            dataPoints[i++] = new DataPoint(totDistanceKm, p.getElev());
            previous = p;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setDrawBackground(true);
        series.setBackgroundColor(Color.argb(80, 78, 166, 52));
        series.setColor(Color.argb(255, 78, 166, 52));

        return new Tuple<>(series, totDistanceKm);
    }

}
