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
import android.widget.EditText;
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

public class ChangePassFragment extends Fragment {
    private static String TAG = ChangePassFragment.class.getSimpleName();

    User user;
    ChangePassFragment us;

    public ChangePassFragment() {
    }

    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_changepass) Button _changepassbutton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_changepass,container,false);
        ButterKnife.bind(this, view);
        this.us = this;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCurrentUser();

        _changepassbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    updateUser();
                } else {
                    Toast.makeText(getActivity(), "Passwords didn't match", Toast.LENGTH_LONG).show();
                    _changepassbutton.setEnabled(true);
                }
            }
        });
    }

    private boolean validate() {
        boolean valid = true;

        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();
        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }
        return valid;
    }

    private void updateUser() {
        user.setPassword(_passwordText.getText().toString());
        User.UserService userService = ServiceHelper.createService(User.UserService.class);
        Call<User> call = userService.updateUser(user.id, user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                Toast.makeText(getActivity(), "Profile Updated!",
                        Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("userid", getArguments().getString("userid"));
                Fragment profile = new ProfileFragment();
                profile.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContent, profile)
                        .addToBackStack(null)
                        .commit();
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
                //user.printUser();
                setUser(user);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
    }

}
