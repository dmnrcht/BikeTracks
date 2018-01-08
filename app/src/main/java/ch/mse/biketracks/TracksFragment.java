package ch.mse.biketracks;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.mse.biketracks.adapters.MyTracksRecyclerViewAdapter;
import ch.mse.biketracks.adapters.RecyclerItemClickListener;
import ch.mse.biketracks.database.DatabaseHelper;
import ch.mse.biketracks.models.Point;
import ch.mse.biketracks.models.Track;

public class TracksFragment extends Fragment {

    ArrayList<Track> tracks = new ArrayList<>();
    Context mContext;
    MyTracksRecyclerViewAdapter tracksAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_tracks_list, container, false);

//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.speed);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] bitMapData = stream.toByteArray();
//        tracks.get(0).setImage(bitMapData);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            tracksAdapter = new MyTracksRecyclerViewAdapter(tracks);
            recyclerView.setAdapter(tracksAdapter);
            recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // Start a new Activity via Intent
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), TrackActivity.class);
                            intent.putExtra("trackId", tracks.get(position).getId());
                            startActivity(intent);
                        }
                    })
            );
        }

        // Retrieve tracks from DB
        tracks.clear();
        new FetchTracksTask().execute();

        return view;
    }

    /**
     * Get tracks from DB asynchronously
     */
    private class FetchTracksTask extends AsyncTask<Void, Void, ArrayList<Track>> {
        @Override
        protected ArrayList<Track> doInBackground(Void... voids) {
            return DatabaseHelper.getInstance(mContext).getTracks(true);
        }

        protected void onPostExecute(ArrayList<Track> result) {
            if (result.isEmpty()) {
                Toast.makeText(mContext, R.string.no_registered_tracks, Toast.LENGTH_SHORT).show();
                return;
            }
            tracks.addAll(result);
            tracksAdapter.notifyDataSetChanged();
        }
    }
}
