package edu.wit.mobileapp.pocketstudio.models;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Neil on 4/8/2017.
 */

public class ModelFactory {

    Class<?> ourClass;

    /*public <S> S createService(Class<S> modelClass, String id) {
        final S[] potato = new S[1];
        try {
            Call<S> NewCall = (S)ServiceHelper.createService(modelClass) .getOne(id);
            NewCall.enqueue(new Callback<S>() {
                @Override
                public void onResponse(Call<S> call, Response<S> response) {
                    // The network call was a success and we got a response
                    potato[0] = response.body();
                }
                @Override
                public void onFailure(Call<S> call, Throwable t) {
                    System.err.println(t.getStackTrace());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return potato[0];
    }*/

}
