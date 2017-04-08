package edu.wit.mobileapp.pocketstudio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.parceler.Parcels;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.BindView;
import edu.wit.mobileapp.pocketstudio.models.ServiceHelper;
import edu.wit.mobileapp.pocketstudio.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Neil on 4/8/2017.
 */

public class ProfileFragment extends Fragment {
    private static String TAG = ProfileFragment.class.getSimpleName();

    User user;

    public ProfileFragment() {
    }

    @BindView(R.id.nameText) TextView _nameText;
    @BindView(R.id.emailText) TextView _emailText;
    @BindView(R.id.saveProfileButton) Button _saveButton;
    @BindView(R.id.changePassButton) Button _changePassButton;
    @BindView(R.id.deleteAccountButton) Button _deleteAccountButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCurrentUser();

        _saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUser();
            }
        });
    }

    private void updateUser() {
        user.name = _nameText.getText().toString();
        user.email = _emailText.getText().toString();
        User.UserService userService = ServiceHelper.createService(User.UserService.class);
        Call<User> call = userService.updateUser(user.id, user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                Toast.makeText(getActivity(), "Profile Updated!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "wat", t);
                Toast.makeText(getActivity(), "Something went wrong",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

   public void getCurrentUser(){
        User.UserService userService = ServiceHelper.createService(User.UserService.class);
        Call<User> user = userService.get(this.getArguments().getString("userid"));
        user.enqueue(new Callback<User>() {
            private User user;

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                Log.d(TAG, "In Onresponse");
                User user = response.body();
                user.printUser();
                setUser(user);
                setTexts();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
   }

    private void setTexts() {
        _nameText.setText(user.name);
        _emailText.setText(user.email);
    }
    public void setUser(User user) {
        this.user = user;
    }

}
