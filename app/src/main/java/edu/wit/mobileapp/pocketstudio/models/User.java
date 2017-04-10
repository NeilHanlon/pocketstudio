package edu.wit.mobileapp.pocketstudio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import org.parceler.Transient;

import java.util.List;
import java.util.Objects;

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

@org.parceler.Parcel
public class User{
    @Expose(deserialize = true, serialize = false) public String id;
    @Expose() public String name;
    @Expose() private String password;
    @Expose() public String email;
    @Expose() public List<String> groups;
    @Expose() public List<String> projects;

    public User(String name, String email, String password) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public User() {
    }

    public void printUser()
    {
        System.out.printf("ID: %s\nName: %s\nPassword: %s\nEmail: %s\n", this.id, this.name, this.password, this.email);
    }

    public boolean authenticate(String mPassword) {
        return this.password.equals(mPassword);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public interface UserService {

        @GET("/v1/user/{id}")
        Call<User> get(
                @Path("id") String id
        );

        @GET("/v1/user/{email}")
        Call<User> getUserByEmail(
                @Path("email") String email
        );

        @POST("/v1/user/")
        Call<User> createUser(
                @Body User user
        );

        @PUT("/v1/user/{id}/")
        Call<User> updateUser(
                @Path("id") String id,
                @Body User user
        );

        @DELETE("/v1/user/{id}/")
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
