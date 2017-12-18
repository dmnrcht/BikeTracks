package ch.mse.biketracks.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;

import ch.mse.biketracks.R;
import ch.mse.biketracks.models.Track;

/**
 * Created by antoi on 18.12.2017.
 */

public class TrackInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public TrackInfoWindowAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.track_window, null);

        Track track = (Track)marker.getTag();

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(((Track)marker.getTag()).getName());

        TextView typeView = (TextView)view.findViewById(R.id.track_type);
        typeView.setText(track.getType());

        TextView distanceView = (TextView)view.findViewById(R.id.track_distance);
        distanceView.setText(String.valueOf(track.getDistance() + "m"));

        TextView climbView = (TextView)view.findViewById(R.id.track_climb);
        climbView.setText(String.valueOf(track.getClimb() + "m"));

        TextView descentView = (TextView)view.findViewById(R.id.track_descent);
        descentView.setText(String.valueOf(track.getDescent() + "m"));

        TextView speedView = (TextView)view.findViewById(R.id.track_speed);
        speedView.setText(String.valueOf(track.getSpeed() + "km/h"));

        // TODO : Support multiple formats of dates depending on locale
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        TextView dateView = (TextView)view.findViewById(R.id.track_date);
        dateView.setText(sdf.format(track.getDate()));

        TextView durationView = (TextView)view.findViewById(R.id.track_duration);
        durationView.setText(track.getDuration()/60 + " h " + track.getDuration()%60);

        return view;
    }
}