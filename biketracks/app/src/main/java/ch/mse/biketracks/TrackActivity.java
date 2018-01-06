package ch.mse.biketracks;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;
import ch.mse.biketracks.utils.MyTools;
import ch.mse.biketracks.utils.Tuple;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Track track;
    private SupportMapFragment supportMapFragment;
    private View bottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private GoogleMap mMap;
    Bitmap startIconSmall, finishIconSmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        //this set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //this set custom image to back button
        final Drawable backArrow = getResources().getDrawable(R.drawable.back);
        backArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);

        // Get ui controllers
        TextView titleView = findViewById(R.id.trackdetail_title);
        TextView typeView = findViewById(R.id.trackdetail_type);
        TextView distanceView = findViewById(R.id.trackdetail_distance);
        TextView durationView = findViewById(R.id.trackdetail_duration);
        TextView climbView = findViewById(R.id.trackdetail_climb);
        TextView dateView = findViewById(R.id.trackdetail_date);
        TextView speedView = findViewById(R.id.trackdetail_speed);
        TextView descentView = findViewById(R.id.trackdetail_descent);
        GraphView graph = findViewById(R.id.elevationGraphTrackDetail);
        bottomSheet = findViewById(R.id.track_bottom_sheet);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // EXPAND / COLLAPSE bottom sheet on click.
        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mBottomSheetBehavior.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                }
            }
        });

        // Update google map padding on bottomsheet slides
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (mMap != null) {
                    mMap.setPadding(0,0,0,
                            mBottomSheetBehavior.getPeekHeight() +
                                    (int)(slideOffset *
                                            (bottomSheet.getHeight() - mBottomSheetBehavior.getPeekHeight())));
                }
            }
        });

        // Set marker start and finish
        int height = 52;
        int width = 52;

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.icon_start);
        Bitmap bStart = bitmapdraw.getBitmap();
        startIconSmall = Bitmap.createScaledBitmap(bStart, width, height, false);

        BitmapDrawable bitmapdraw2=(BitmapDrawable)getResources().getDrawable(R.drawable.icon_finish);
        Bitmap bFinish = bitmapdraw2.getBitmap();
        finishIconSmall = Bitmap.createScaledBitmap(bFinish, width, height, false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            track = (Track)getIntent().getSerializableExtra("track"); //Obtaining data

            // Set track values
            titleView.setText(track.getName());
            typeView.setText(track.getType());
            distanceView.setText(String.format(Locale.ENGLISH, "%.1f km", track.getDistance()/ 1000.));
            durationView.setText(MyTools.FormatTimeHHhmm(track.getDuration()));
            climbView.setText(String.format(Locale.ENGLISH, "%d m", track.getClimb()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            dateView.setText(sdf.format(track.getDate()));
            speedView.setText(String.format(Locale.ENGLISH,"%.1f km/h",track.getSpeed() * 3.6));
            descentView.setText(String.format(Locale.ENGLISH, "%d m", track.getDescent()));

            // Build graph
            Tuple<LineGraphSeries<DataPoint>, Double> elevationGraph = MyTools.ElevationGraph(track.getPoints());
            // set manual X bounds
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0.);
            graph.getViewport().setMaxX(elevationGraph.second);
            if (graph.getSeries().size() > 0)
                graph.removeAllSeries();
            graph.addSeries(elevationGraph.first);

            getSupportActionBar().setTitle(track.getName());
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.track_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        mMap.addPolyline(lineOptions);

        // Add markers start/end
        Point startPoint = track.getPoints().get(0);
        Point finishPoint = track.getPoints().get(track.getPoints().size() - 1);
        LatLng start = new LatLng(startPoint.getLat(), startPoint.getLng());
        LatLng finish = new LatLng(finishPoint.getLat(), finishPoint.getLng());

        MarkerOptions startMarkerOptions = new MarkerOptions()
                .position(start)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(startIconSmall));
        MarkerOptions finishMarkerOptions = new MarkerOptions()
                .position(finish)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(finishIconSmall));

        mMap.addMarker(finishMarkerOptions);
        mMap.addMarker(startMarkerOptions);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.setPadding(0,0,0,bottomSheet.getHeight());
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 0.7f));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
