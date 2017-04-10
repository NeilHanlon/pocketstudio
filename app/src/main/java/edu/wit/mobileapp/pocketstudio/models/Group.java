package edu.wit.mobileapp.pocketstudio.models;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Neil on 3/25/2017.
 */
@Parcel
public class Group {
    @Expose(deserialize = true, serialize = false) public String id;
    @Expose() public String name;
    @Expose() public List<String> users;
    @Expose() public List<String> projects;

    public Group(String name, List<String> users, List<String> projects) {
        this.name = name;
        this.users = users;
        this.projects = projects;
    }

    public Group() {

    }

    public interface GroupService {
        @GET("/v1/group/{id}")
        Call<Group> getGroup(
                @Path("id") String id
        );

        @POST("/v1/group/")
        Call<Group> createGroup(
                @Body Group group
        );

        @PUT("/v1/group/{id}/")
        Call<Group> updateGroup(
                @Path("id") String id,
                @Body Group group
        );

        @DELETE("/v1/group/{id}/")
        Call<Void> deleteGroup(
                @Path("id") String id
        );

        @GET("/v1/group/{id}/users")
        Call<List<User>> getGroupUsers(
                @Path("id") String id
        );

        @PUT("/v1/group/{id}/users")
        Call<List<User>> updateGroupUsers(
                @Path("id") String id,
                @Body List<String> user_ids
        );

        @PUT("/v1/group/{id}/users/{user_id}")
        Call<Group> addUserToGroup(
                @Path("id") String id,
                @Path("user_id") String user_id
        );

        @DELETE("/v1/group/{id}/users/{user_id}")
        Call<Group> removeUserFromGroup(
                @Path("id") String id,
                @Path("user_id") String user_id
        );

        @GET("/v1/group/{id}/projects")
        Call<List<Project>> getGroupProjects(
                @Path("id") String id
        );

        @PUT("/v1/group/{id}/projects/{project_id}")
        Call<Group> addProjectToGroup(
                @Path("id") String id,
                @Path("project_id") String project_id
        );

        @DELETE("/v1/group/{id}/projects/{project_id}")
        Call<Group> removeProjectFromGroup(
                @Path("id") String id,
                @Path("project_id") String project_id
        );
    }
}
