package edu.wit.mobileapp.pocketstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Neil on 4/7/2017.
 */

public class PreferencesActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        int pos = i.getIntExtra("position", 0);

        switch (pos) {
            case 0:
                //Home, we should never go here
                break;
            case 1:
                //Preferences
                setTitle("Preferences");
                break;
            case 2:
                //About
                setTitle("About");
                break;
            case 3:
                //Profile
                setTitle("Profile");
                break;
            default:
                break;
        }
    }


}