package ch.mse.biketracks.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

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
     * @param elapsedSeconds the amount of time in milliseconds
     * @return Formatted time "HHhmm" for example "26h17"
     */
    public static String FormatTimeHHhmm(long elapsedSeconds) {
        long hourInS = 60 * 60;
        long minuteInS = 60;
        long hours = elapsedSeconds / hourInS; // integer division
        int minutes = Math.round((elapsedSeconds - hours * hourInS) / minuteInS);

        return String.format(Locale.ENGLISH,"%02dh%02d", hours, minutes);
    }

    /**
     * Compute total hours minutes and seconds for given parameter
     * @param elapsedSeconds the amount of time in milliseconds
     * @return Formatted time "HH'hmm'mss's" for example "26h17"
     */
    public static String FormatTimeHHhmmss(long elapsedSeconds) {
        long hourInS = 60 * 60;
        long minuteInS = 60;
        long hours = elapsedSeconds / hourInS; // integer division
        long minutes = (elapsedSeconds - hours * hourInS) / minuteInS; // integer division
        long seconds = (elapsedSeconds - hours * hourInS - minutes * minuteInS);

        return String.format(Locale.ENGLISH,"%02dh%02dm%02d", hours, minutes, seconds);
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

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
}