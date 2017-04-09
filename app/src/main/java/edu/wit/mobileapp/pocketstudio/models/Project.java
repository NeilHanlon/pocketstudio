package edu.wit.mobileapp.pocketstudio.models;

import com.google.gson.annotations.Expose;

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
public class Project {
    @Expose(deserialize = true, serialize = false) public String id;
    @Expose() public String name;
    @Expose() public int measures;
    @Expose() public int bpm;
    @Expose() public User owner;
    @Expose() public List<Track> tracks;

    public Project(String name, User owner, List<Track> tracks) {
        this.name = name;
        this.owner = owner;
        this.tracks = tracks;
    }

    public interface ProjectService {
        @GET("/v1/project/{id}")
        Call<Project> getProject(
                @Path("id") String id
        );

        @POST("/v1/project/")
        Call<Project> createProject(
                @Body Project project
        );

        @PUT("/v1/project/{id}")
        Call<Project> updateProject(
                @Path("id") String id,
                @Body Project project
        );

        @DELETE("/v1/project/{id}")
        Call<Void> deleteProject(
                @Path("id") String id
        );

        @GET("/v1/project/{id}/groups")
        Call<List<Group>> getProjectGroups(
                @Path("id") String id
        );

        @PUT("/v1/project/{id}/groups/{group_id}")
        Call<List<Group>> addGroupToProject(
                @Path("id") String id,
                @Path("group_id") String group_id
        );

        @DELETE("/v1/project/{id}/groups/{group_id}")
        Call<List<Group>> removeGroupFromProject(
                @Path("id") String id,
                @Path("group_id") String group_id
        );

        @GET("/v1/project/{id]/tracks")
        Call<List<Track>> getProjectTracks(
                @Path("id") String id
        );

        @PUT("/v1/project/{id}/track/{track_id}")
        Call<List<Track>> addTrackToProject(
                @Path("id") String id,
                @Path("track_id") String track_id
        );

        @DELETE("/v1/project/{id}/track/{track_id}")
        Call<List<Track>> removeTrackFromProject(
                @Path("id") String id,
                @Path("track_id") String track_id
        );

    }
}
