package ch.mse.biketracks.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.mse.biketracks.R;
import ch.mse.biketracks.models.Track;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyTracksRecyclerViewAdapter extends RecyclerView.Adapter<MyTracksRecyclerViewAdapter.ViewHolder> {

    private final List<Track> mValues;

    public MyTracksRecyclerViewAdapter(List<Track> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tracks, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.location.setText(mValues.get(position).name);
        holder.distance.setText(String.valueOf(mValues.get(position).distance + "m"));
        holder.speed.setText(String.valueOf(mValues.get(position).getSpeed() + "km/h"));

        // TODO : Support multiple formats of dates depending on locale
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH'h'mm");
        holder.date.setText(sdf.format(mValues.get(position).getDate()));
        holder.duration.setText(mValues.get(position).getDuration()/60 + " h " + mValues.get(position).getDuration()%60);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView location;
        public final TextView date;
        public final TextView distance;
        public final TextView duration;
        public final TextView speed;
        public Track mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            location = (TextView) view.findViewById(R.id.location);
            date = (TextView) view.findViewById(R.id.date);
            distance = (TextView) view.findViewById(R.id.distance);
            duration = (TextView) view.findViewById(R.id.duration);
            speed = (TextView) view.findViewById(R.id.speed);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + location.getText() + "'";
        }
    }
}
