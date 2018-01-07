package ch.mse.biketracks.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;
import ch.mse.biketracks.utils.Distance;

public class TrackerService extends Service {
    private static final String TAG = TrackerService.class.getSimpleName();

    public static final String ACTION_UPDATE = "ch.mse.biketracks.TrackerService.update";

    private static final int MIN_SECONDS_BETWEEN_UPDATES = 3;
    private static final int MIN_METERS_BETWEEN_UPDATES = 5;

    /**
     * Tracking status
     */
    private Track track;
    private Point firstPoint;
    private Point lastPoint;

    /**
     * Location updates
     */
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public TrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Init location client, requests and callbacks.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                addLocationToTrack(locationResult.getLastLocation());
                broadcastTrack();
            }
        };

        locationRequest = new LocationRequest()
                .setInterval(MIN_SECONDS_BETWEEN_UPDATES * 1000)
                .setFastestInterval(MIN_SECONDS_BETWEEN_UPDATES * 1000)
                .setSmallestDisplacement(MIN_METERS_BETWEEN_UPDATES)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startTracking();
    }

    @Override
    public void onDestroy() {
        stopTracking();
        super.onDestroy();
    }

    /**
     * Init a new empty track and start listening location updates.
     *
     * Note: location permission is granted from the activity.
     */
    @SuppressLint("MissingPermission")
    private void startTracking() {
        track = new Track(new Date());
        lastPoint = null;

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.myLooper());

        Log.d(TAG, "Tracking started");
    }

    /**
     * Stop location updates.
     */
    private void stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);

        Log.d(TAG, "Tracking stopped");
    }

    /**
     * Create a new point and update track.
     */
    private void addLocationToTrack(Location location) {
        Point point = new Point(
                location.getLatitude(),
                location.getLongitude(),
                (int)location.getAltitude(),
                location.getTime()
        );

        if (lastPoint != null) {
            int duration = (int)(point.getTime() / 1000 - firstPoint.getTime() / 1000);
            int distance = (int)(track.getDistance() + Distance.distance(lastPoint, point));

            track.setDuration(duration);
            track.setDistance(distance);
            track.setSpeed((float)distance / duration);

            int climb = (int)(point.getElev() - lastPoint.getElev());
            if (climb > 0) {
                track.setClimb(track.getClimb() + climb);
            }
            else {
                track.setDescent(track.getDescent() - climb);
            }
        }
        else {
            track.setDuration(0);
            track.setDistance(0);
            track.setClimb(0);
            track.setDescent(0);
            track.setSpeed(0);
        }

        point.setDuration(track.getDuration());

        track.addPoint(point);
        lastPoint = point;

        if (firstPoint == null) {
            firstPoint = point;
        }
    }

    /**
     * Use ResultReceiver to send updated Track to activity.
     */
    private void broadcastTrack() {
        Intent in = new Intent(ACTION_UPDATE);
        in.putExtra("track", track);
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        Log.d(TAG, "Track updated");
    }
}
