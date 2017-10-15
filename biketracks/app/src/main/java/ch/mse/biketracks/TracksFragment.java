package ch.mse.biketracks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import ch.mse.biketracks.adapters.MyTracksRecyclerViewAdapter;
import ch.mse.biketracks.adapters.RecyclerItemClickListener;
import ch.mse.biketracks.models.Track;

public class TracksFragment extends Fragment {

    ArrayList<Track> tracks = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TracksFragment() {
        tracks.add(new Track("Neuchatel", new Date(), 1000, 10, 10));
        tracks.add(new Track("Yverdon", new Date(), 1000, 10, 10));
        tracks.add(new Track("Col du Pillon", new Date(), 1000, 10, 10));
        tracks.add(new Track("Mont-Blanc", new Date(), 1000, 10, 10));
        tracks.add(new Track("Lausanne", new Date(), 1000, 10, 10));
        tracks.add(new Track("Delémont", new Date(), 1000, 10, 10));
        tracks.add(new Track("Lausanne", new Date(), 1000, 10, 10));
        tracks.add(new Track("Neuchatel", new Date(), 1000, 10, 10));
        tracks.add(new Track("Yverdon", new Date(), 1000, 10, 10));
        tracks.add(new Track("Delémont", new Date(), 1000, 10, 10));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyTracksRecyclerViewAdapter(tracks));
            recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Start a new Activity via Intent
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), TrackActivity.class);
                            intent.putExtra("id", 123);
                            startActivity(intent);
                        }
                    })
            );
        }

        return view;
    }
}
