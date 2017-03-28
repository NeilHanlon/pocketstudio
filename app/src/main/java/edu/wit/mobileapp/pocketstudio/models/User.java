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

public class User{
    public String id;
    public String name;
    private String password;
    public String email;
    public List<String> groups;
    public List<String> projects;

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }


    public void printUser()
    {
        System.out.printf("ID: %s\nName: %s\nPassword: %s\nEmail: %s\n", this.id, this.name, this.password, this.email);
    }

    public interface UserService {
        @GET("/v1/user/{email}")
        Call<User> getUserByEmail(
                @Path("email") String email
        );

        @POST("/v1/user/")
        Call<User> createUser(
                @Body User user
        );

        @PUT("/v1/user/{id}")
        Call<User> createUser(
                @Path("id") String id,
                @Body User user
        );

        @DELETE("/v1/user/{id}")
        Call<Void> deleteUser(
                @Path("id") String id
        );

        @GET("/v1/user/{id}/groups")
        Call<List<Group>> getUserGroups(
                @Path("id") String id
        );

        @GET("/v1/user/{id}/projects")
        Call<List<Project>> getUserProjects(
                @Path("id") String id
        );
    }
}
