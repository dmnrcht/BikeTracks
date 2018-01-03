package ch.mse.biketracks.utils;

import java.util.List;

import ch.mse.biketracks.models.Track;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Define here the routes of BikeTracks API
 */
public interface BiketracksAPIInterface {
    @GET("tracks/?")
    Call<List<Track>> doGetTracks(@Query("lat") double lat,
                                  @Query("lng") double lng,
                                  @Query("radius") int radius);
}
