package edu.wit.mobileapp.pocketstudio.models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Neil on 3/25/2017.
 */
public class Track {
    public String id;
    public float volume;
    public float pan;
    public int order;
    public String name;
    public List<Region> regions;

    public Track(float volume, float pan, int order, String name, List<Region> regions) {
        this.volume = volume;
        this.pan = pan;
        this.order = order;
        this.name = name;
        this.regions = regions;
    }

    public interface TrackService {
        @GET("/v1/track/{id}")
        Call<Track> getTrack(
                @Path("id") String id
        );

        @POST("/v1/track/")
        Call<Track> createTrack(
                @Body Track track
        );

        @PUT("/v1/track/{id}")
        Call<Track> updateTrack(
                @Path("id") String id,
                @Body Track track
        );

        @DELETE("/v1/track/{id}")
        Call<Void> deleteTrack(
                @Path("id") String id
        );

        @GET("/v1/track/{id}/regions")
        Call<List<Region>> getTrackRegions(
                @Path("id") String id
        );

        @PUT("/v1/track/{id}/regions/{region_id}")
        Call<List<Region>> addRegionToTrack(
                @Path("id") String id,
                @Path("region_id") String region_id
        );

        @DELETE("/v1/track/{id}/regions/{region_id}")
        Call<List<Region>> removeRegionFromTrack(
                @Path("id") String id,
                @Path("region_id") String region_id
        );

    }
}
