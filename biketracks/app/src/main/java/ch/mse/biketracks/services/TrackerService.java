package ch.mse.biketracks.services;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;
import ch.mse.biketracks.utils.Distance;

public class TrackerService extends IntentService {

    private static final int MIN_SECONDS_BETWEEN_UPDATES = 10;
    private static final int MIN_METERS_BETWEEN_UPDATES = 3;

    /**
     * Tracking status
     */
    private boolean currentlyTracking = false;
    private Track track;
    private Point lastPoint;

    /**
     * Location updates
     */
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Communication
     */
    private ResultReceiver resultReceiver;

    public TrackerService() {
        super("TrackerService");
    }

    /**
     * Init location client, requests and callbacks.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest()
                .setInterval(MIN_SECONDS_BETWEEN_UPDATES * 1000)
                .setFastestInterval(MIN_SECONDS_BETWEEN_UPDATES * 1000)
                .setSmallestDisplacement(MIN_METERS_BETWEEN_UPDATES)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                addLocation(locationResult.getLastLocation());
            }
        };
    }

    /**
     * The service can track user only once at a time.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        resultReceiver = intent.getParcelableExtra("receiver");

        if (!currentlyTracking) {
            currentlyTracking = true;
            startTracking();
        }
    }

    @Override
    public void onDestroy() {
        stopTracking();
    }

    /**
     * If location permission is granted,
     * init a new empty track and start location updates.
     */
    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        track = new Track(new Date());
        lastPoint = null;

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    /**
     * Stop location updates.
     */
    private void stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Create a new point and update track.
     */
    private void addLocation(Location location) {
        Point point = new Point(
                location.getLatitude(),
                location.getLongitude(),
                (int)location.getAltitude(),
                location.getTime()
        );

        if (lastPoint != null) {
            track.setDuration((int)(point.getTime() - lastPoint.getDuration()));
            track.setDistance((int)(track.getDistance() + Distance.distance(lastPoint, point)));

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
        }

        point.setDuration(track.getDuration());

        track.addPoint(point);
        lastPoint = point;

        sendUpdate();
    }

    /**
     * Use ResultReceiver to send updated Track to activity.
     */
    private void sendUpdate() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("track", track);
        resultReceiver.send(Activity.RESULT_OK, bundle);
    }
}
