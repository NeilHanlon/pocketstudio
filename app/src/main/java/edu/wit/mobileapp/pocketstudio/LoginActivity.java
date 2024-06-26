package edu.wit.mobileapp.pocketstudio;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Properties;

import butterknife.ButterKnife;
import butterknife.BindView;
import edu.wit.mobileapp.pocketstudio.models.ServiceHelper;
import edu.wit.mobileapp.pocketstudio.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    Properties prop = new Properties();

    @BindView(R.id.email) EditText _emailText;
    @BindView(R.id.password) EditText _passwordText;
    @BindView(R.id.login_button) Button _loginButton;
    @BindView(R.id.signup_button) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        getSupportActionBar().setElevation(0);


        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        final User[] user = new User[1];
                        try {
                            User.UserService userService = ServiceHelper.createService(User.UserService.class);
                            Call<User> UserCall = userService.getUserByEmail(email);
                            UserCall.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    // The network call was a success and we got a response
                                    user[0] = response.body();
                                    if (user[0].authenticate(password)) {
                                        onLoginSuccess(user[0]);
                                    } else {
                                        onLoginFailed();
                                    }
                                }
                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    onLoginFailed();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            onLoginFailed();
                        } finally {
                            progressDialog.dismiss();
                        }
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(User user) {
        try {
            prop.load(getBaseContext().getAssets().open("pocketstudio.properties"));
            String prefsname = prop.getProperty("pocketstudio.prefs_name", "pocketstudioprefs");
            SharedPreferences settings = getSharedPreferences(prefsname, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("userid", user.id);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _loginButton.setEnabled(true);
        setResult(RESULT_OK);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}