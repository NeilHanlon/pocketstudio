package edu.wit.mobileapp.pocketstudio.models;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.wit.mobileapp.pocketstudio.models.User.UserService;

/**
 * Created by Neil on 3/21/2017.
 */

public final class PocketAPI {
    public static void main(String... args) throws IOException {
        UserService userService = ServiceHelper.createService(UserService.class);
        User potato = new User("me", "neil@shrug.pw", "potato");
        Call<User> createNeil = userService.createUser(potato);
        createNeil.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                response.body().printUser();
                System.out.println(Arrays.toString(response.body().groups.toArray()));
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
        Call<User> neil = userService.getUserByEmail("neil@shrug.pw");
        neil.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                response.body().printUser();
                System.out.println(Arrays.toString(response.body().groups.toArray()));
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });

    }
}
