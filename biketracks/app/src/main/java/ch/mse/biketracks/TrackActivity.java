package ch.mse.biketracks;

import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;

import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Track track;
    private SupportMapFragment supportMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        //this set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //this is set custom image to back button
        final Drawable backArrow = getResources().getDrawable(R.drawable.back);
        backArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            track = (Track)getIntent().getSerializableExtra("track"); //Obtaining data
        }

        TextView nameView = (TextView)findViewById(R.id.track_name);
        nameView.setText(track.getName());

        TextView typeView = (TextView)findViewById(R.id.track_type);
        typeView.setText(track.getType());

        TextView distanceView = (TextView)findViewById(R.id.track_distance);
        distanceView.setText(String.valueOf(track.getDistance() + "m"));

        TextView climbView = (TextView)findViewById(R.id.track_climb);
        climbView.setText(String.valueOf(track.getClimb() + "m"));

        TextView descentView = (TextView)findViewById(R.id.track_descent);
        descentView.setText(String.valueOf(track.getDescent() + "m"));

        TextView speedView = (TextView)findViewById(R.id.track_speed);
        speedView.setText(String.valueOf(track.getSpeed() + "km/h"));

        // TODO : Support multiple formats of dates depending on locale
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH'h'mm");
        TextView dateView = (TextView)findViewById(R.id.track_date);
        dateView.setText(sdf.format(track.getDate()));

        TextView durationView = (TextView)findViewById(R.id.track_duration);
        durationView.setText(track.getDuration()/60 + " h " + track.getDuration()%60);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.track_map);
        mapFragment.getMapAsync(this);


        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] dataPoints = new DataPoint[track.getPoints().size()];
        double totDistance = 0;
        for(int i = 0; i < track.getPoints().size(); i++){
            if(i > 0){
                totDistance += distance(track.getPoints().get(i).getLat(), track.getPoints().get(i-1).getLat(), track.getPoints().get(i).getLng(), track.getPoints().get(i-1).getLng(), track.getPoints().get(i).getElev(), track.getPoints().get(i-1).getElev());
            }
            dataPoints[i] = new DataPoint(totDistance, track.getPoints().get(i).getElev());
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setDrawBackground(true);
        series.setBackgroundColor(Color.argb(80,78, 166, 52));
        series.setColor(Color.argb(255,78, 166, 52));
        graph.addSeries(series);
}

    @Override
    public void onMapReady(GoogleMap map) {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        PolylineOptions lineOptions = new PolylineOptions();
        for (int j = 0; j < track.getPoints().size(); j++) {
            Point p = track.getPoints().get(j);

            // Adding all the points in the route to LineOptions
            lineOptions.add(new LatLng(p.getLat(), p.getLng()));
            lineOptions.width(10);
            lineOptions.color(Color.rgb(237, 92, 92));

            bounds.include(new LatLng(p.getLat(), p.getLng()));
        }

        // Drawing polyline in the Google Map for the i-th route
        if(lineOptions != null) {
            map.addPolyline(lineOptions);
        }
        else {
            Log.d("onPostExecute","without Polylines drawn");
        }

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
        map.moveCamera(CameraUpdateFactory.zoomTo(map.getCameraPosition().zoom - 0.7f));


        map.addMarker(new MarkerOptions()
                .position(new LatLng(track.getPoints().get(0).getLat(), track.getPoints().get(0).getLng()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(track.getPoints().get(track.getPoints().size() - 1).getLat(), track.getPoints().get(track.getPoints().size() - 1).getLng()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
