package ch.mse.biketracks;

/**
 * Description: Google maps based on https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
 */

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.mse.biketracks.adapters.TrackInfoWindowAdapter;
import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private static final String TAG = MapFragment.class.getSimpleName();

    private final LatLng mDefaultLocation = new LatLng(46.78896583, 6.74356617);

    private GoogleMap mMap;

    private Context mContext;
    private SupportMapFragment supportMapFragment;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FloatingActionButton recordButton;
    private FloatingActionButton locateButton;
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

        requestQueue = Volley.newRequestQueue(mContext);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

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
        mMap = googleMap;

        // Move camera to default location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 12.0f));

        // Turn on the My Location layer and the related control on the map.
        //updateLocationUI();

        // Get the current location of the device and set the position of the map.
        //getDeviceLocation();// Load the tracks
        getTracks(mDefaultLocation.latitude, mDefaultLocation.longitude, 40000);
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
        updateLocationUI();
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 12.0f));
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

    private void getTracks(double lat, double lng, int radius) {
        // First, we insert the username into the repo url.
        // The repo url is defined in GitHubs API docs (https://developer.github.com/v3/repos/).
        String url = "https://biketracks.damienrochat.ch/api/v1/tracks/?lat=" + lat + "&lng=" + lng + "&radius=" + radius;

        // Next, we create a new JsonArrayRequest. This will use Volley to make a HTTP request
        // that expects a JSON Array Response.
        // To fully understand this, I'd recommend readng the office docs: https://developer.android.com/training/volley/index.html
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Check the length of our response (to see if the user has any repos)
                        if (response.length() > 0) {
                            // The user does have repos, so let's loop through them all.
                            List<Track> tracks = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    // For each repo, add a new line to our repo list.
                                    JSONObject jsonObj = response.getJSONObject(i);
                                    int id = jsonObj.getInt("id");
                                    int distance = jsonObj.getInt("distance");
                                    int climb = jsonObj.getInt("climb");
                                    int descent = jsonObj.getInt("descent");
                                    String name = jsonObj.getString("name");
                                    String type = jsonObj.getString("type");

                                    JSONArray jsonPoints = jsonObj.getJSONArray("points");
                                    List<Point> points = new ArrayList<>();
                                    for(int j = 0; j < jsonPoints.length(); j++){
                                        JSONObject jsonPoint = jsonPoints.getJSONObject(j);
                                        double lat = jsonPoint.getDouble("lat");
                                        double lng = jsonPoint.getDouble("lng");
                                        int elev = jsonPoint.getInt("elev");
                                        points.add(new Point(lat, lng, elev));
                                    }

                                    tracks.add(new Track(id, name, new Date(), 0, 0, distance, climb, descent, type, points));
                                } catch (JSONException e) {
                                    // If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                }

                            }



                            // Traversing through all the tracks and draw on the map
                            for(int i = 0; i < tracks.size(); i++){
                                Track track = tracks.get(i);
                                PolylineOptions lineOptions = new PolylineOptions();
                                for (int j = 0; j < tracks.get(i).getPoints().size(); j++) {
                                    Point p = tracks.get(i).getPoints().get(j);

                                    // Adding all the points in the route to LineOptions
                                    lineOptions.add(new LatLng(p.getLat(), p.getLng()));
                                    lineOptions.width(10);
                                    lineOptions.color(Color.rgb(237, 92, 92));
                                }

                                // Drawing polyline in the Google Map for the i-th route
                                if(lineOptions != null) {
                                    mMap.addPolyline(lineOptions);
                                }
                                else {
                                    Log.d("onPostExecute","without Polylines drawn");
                                }

                                MarkerOptions markerOpt = new MarkerOptions().position(computeCentroid(track.getPoints()))
                                        .icon(BitmapDescriptorFactory.defaultMarker())
                                        .title(track.getName());


                                TrackInfoWindowAdapter adapter = new TrackInfoWindowAdapter(getActivity());
                                mMap.setInfoWindowAdapter(adapter);

                                Marker marker = mMap.addMarker(markerOpt);
                                marker.setTag(tracks.get(i));

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
                            // Empty response
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there a HTTP error then add a note to our repo list.
                        //setRepoListText("Error while calling REST API");
                        Log.e("Volley", error.toString());
                    }
                }
        );
        // Set the max timeout of the request
        arrReq.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request we just defined to our request queue.
        // The request queue will automatically handle the request as soon as it can.
        requestQueue.add(arrReq);
    }

    LatLng computeCentroid(List<Point> points) {
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
