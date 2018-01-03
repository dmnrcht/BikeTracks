package ch.mse.biketracks;

/**
 * Description: Google maps based on https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import ch.mse.biketracks.adapters.TrackInfoWindowAdapter;
import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;
import ch.mse.biketracks.utils.BiketracksAPIClient;
import ch.mse.biketracks.utils.BiketracksAPIInterface;
import retrofit2.Call;
import retrofit2.Callback;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final float DEFAULT_ZOOM = 8.f;
    private static final String TAG = MapFragment.class.getSimpleName();

    private final LatLng mDefaultLocation = new LatLng(46.523317, 6.610430); // HES-SO Master, Provence, Lausanne

    private GoogleMap mMap;
    private ArrayList<Polyline> polylineArrayList = new ArrayList<>();
    private ArrayList<Marker> markerArrayList = new ArrayList<>();
    private BiketracksAPIInterface apiInterface;

    private Context mContext;
    private SupportMapFragment supportMapFragment;

    private boolean mLocationPermissionGranted;
    private boolean isMarkerClicked = false;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FloatingActionButton recordButton;
    private FloatingActionButton locateButton;
    private ProgressBar progressBar;
    private SparseIntArray tracksColor = new SparseIntArray(); // Define a color for each track to distinguish them <id of track, color of track>
    private Random rnd = new Random();
    RequestQueue requestQueue;  // This is our requests queue to process our HTTP requests

    private Marker lastClickedMarker;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();
        apiInterface = BiketracksAPIClient.getClient().create(BiketracksAPIInterface.class);
        requestQueue = Volley.newRequestQueue(mContext);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        recordButton = (FloatingActionButton) getView().findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        locateButton = (FloatingActionButton) getView().findViewById(R.id.locate);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();

                // Get the current location of the device and set the position of the map.
                getDeviceLocation();

                //showCurrentPlace();
            }
        });

        progressBar = getView().findViewById(R.id.progressBarMap);
        //locateButton.hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        setHasOptionsMenu(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager fm = getFragmentManager(); /// getChildFragmentManager();
        supportMapFragment = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.map, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v(TAG, googleMap.toString());
        mMap = googleMap;

        // Move camera to default location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        // Listen for camera movements, i.e. when the map moves or is zoomed in/out
        mMap.setOnCameraIdleListener(this::onCameraIdle);
        mMap.setOnCameraMoveStartedListener(this::onCameraStarted);

        // Turn on the My Location layer and the related control on the map.
        //updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();// Load the tracks
    }

    private void onCameraStarted(int i) {
        if (!isMarkerClicked)
            progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Called each time the camera is moved, i.e. each time the map changes.
     * We get the radius of visible window, then load and replace the tracks on the map.
     */
    private void onCameraIdle() {
        if (isMarkerClicked) {
            isMarkerClicked = false;
            return;
        }

        Log.d(TAG, "The camera has stopped moving. Get radius and load tracks of the visible region");
        // Get Radius of visible window
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        LatLng center = bounds.getCenter();
        LatLng NE = bounds.northeast;
        LatLng SW = bounds.southwest;

        // Compute distance and get radius
        float[] distance = new float[2];
        Location.distanceBetween(SW.latitude, SW.longitude, NE.latitude, NE.longitude, distance);

        // Distance is in distance[0] and it is in meter
        int radius = (int)Math.ceil(distance[0] / 2.0);

        // Load and replace previous tracks
        getTracks(center.latitude, center.longitude, radius);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        try {
            final Menu m = menu;
            // Associate searchable configuration with the SearchView
            final SearchManager searchManager =
                    (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView =
                    (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    // Search for location on map
                    if(search(s)){
                        // Close search
                        (m.findItem(R.id.action_search)).collapseActionView();
                    } else{
                        Toast.makeText(mContext, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });


        }catch(Exception e){e.printStackTrace();}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean search(String query) {
        if (query != null || !query.equals("")) {
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addressList;
            try {
                addressList = geocoder.getFromLocationName(query, 1);
                if(!addressList.isEmpty()){
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.v(TAG, "************************************************************ --- in onRequestPermissionsResult from MapFragment (access location)");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult WHICH IS IMPLEMENTED in MainActivity
     */
        if (ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                //mMap.getUiSettings().setMyLocationButtonEnabled(true);
                //locateButton.show();
            } else {
                mMap.setMyLocationEnabled(false);
                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                //locateButton.hide();
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Check weather location is enabled or not. If not, show alert.
     */
    public void checkLocation() {
        final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    /**
     * Display an alert to enable location
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.gps_seems_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                Log.d(TAG, "mLastKnownLocation : " + mLastKnownLocation.toString());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 11.f));
                            } else {
                                checkLocation();
                            }

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

                            Toast.makeText(mContext, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            //locateButton.hide();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Retrieve the tracks from biketracks API and display them on the map. Every previous tracks
     * loaded are cleaned from the map.
     * @param lat Latitude of center point
     * @param lng Longitude of center point
     * @param radius Radius in meter defining the circle to query the tracks.
     */
    private void getTracks(double lat, double lng, int radius) {

        Call<List<Track>> call = apiInterface.doGetTracks(lat, lng, radius);
        call.enqueue(new Callback<List<Track>>() {

            /**
             * Tracks were successfully retrieved
             * @param call
             * @param response
             */
            @Override
            public void onResponse(Call<List<Track>> call, retrofit2.Response<List<Track>> response) {
                List<Track> tracks = response.body();

                if (tracks != null) {

                    // Clean map
                    for (Polyline polyline : polylineArrayList)
                        polyline.remove();
                    for (Marker marker : markerArrayList)
                        marker.remove();

                    // Display tracks
                    for (Track track : tracks) {
                        PolylineOptions polylineOptions = new PolylineOptions();

                        // Set a unique color for each track
                        if (tracksColor.get(track.id) == 0)
                            tracksColor.append(track.id, 0xFF000000 | rnd.nextInt(0xFFFFFF));

                        // Build the track
                        for (Point point : track.points) {
                            polylineOptions.add(new LatLng(point.lat, point.lng));
                            polylineOptions.width(10);
                            polylineOptions.color(tracksColor.get(track.id));
                        }
                        Polyline polyline = mMap.addPolyline(polylineOptions);
                        polylineArrayList.add(polyline); // Store the displayed polylines to clean them before each load

                        // Add the marker (on the centroid of the track)
                        MarkerOptions markerOpt = new MarkerOptions().position(computeCentroid(track.points))
                                .icon(BitmapDescriptorFactory.defaultMarker(0))
                                .title(track.name);
                        TrackInfoWindowAdapter adapter = new TrackInfoWindowAdapter(getActivity());
                        mMap.setInfoWindowAdapter(adapter);
                        Marker marker = mMap.addMarker(markerOpt);
                        marker.setTag(track);
                        markerArrayList.add(marker);

                        // Open the track activity on info window click
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){

                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Intent intent = new Intent(mContext, TrackActivity.class).putExtra("track", (Track)marker.getTag());
                                startActivity(intent);
                            }
                        });

                        // Hide the info window on the second click of the marker
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                isMarkerClicked = true;
                                if (lastClickedMarker != null && lastClickedMarker.equals(marker)) {
                                    lastClickedMarker = null;
                                    marker.hideInfoWindow();
                                    return true;
                                } else {
                                    lastClickedMarker = marker;
                                    return false;
                                }
                            }
                        });
                    }

                } else {
                    call.cancel();
                }

                progressBar.setVisibility(View.GONE);
            }

            /**
             * Failing in retrieving the tracks from API
             * @param call
             * @param t
             */
            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.d(TAG, "error in calling tracks: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
                call.cancel();
            }
        });
    }

    LatLng computeCentroid(List<Point> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();
        for(Point point: points) {
            latitude += point.lat;
            longitude += point.lng;
        }
        return new LatLng(latitude/n, longitude/n);
    }
}
