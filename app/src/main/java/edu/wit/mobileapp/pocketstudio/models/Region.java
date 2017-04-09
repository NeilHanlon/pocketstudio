package edu.wit.mobileapp.pocketstudio.models;

import com.google.gson.annotations.Expose;

import java.io.FileInputStream;

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
public class Region {
    @Expose(deserialize = true, serialize = false) public String id;
    @Expose() public Region region;
    @Expose() public float Start;
    @Expose() public FileInputStream data;

    public Region(Region region, float start, FileInputStream data) {
        this.region = region;
        Start = start;
        this.data = data;
    }
    
    public interface RegionService {
        @GET("/v1/region/{id}")
        Call<Region> getRegion(
                @Path("id") String id
        );

        @POST("/v1/region/")
        Call<Region> createRegion(
                @Body Region region
        );

        @PUT("/v1/region/{id}")
        Call<Region> updateRegion(
                @Path("id") String id,
                @Body Region region
        );

        @DELETE("/v1/region/{id}")
        Call<Void> deleteRegion(
                @Path("id") String id
        );
    }
}
