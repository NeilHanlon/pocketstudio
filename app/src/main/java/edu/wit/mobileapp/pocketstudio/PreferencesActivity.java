package edu.wit.mobileapp.pocketstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Neil on 4/7/2017.
 */

public class PreferencesActivity extends AppCompatActivity {

    RelativeLayout mMainContent;
    FragmentTransaction ft;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMainContent = (RelativeLayout) findViewById(R.id.mainContent);
        ft = getSupportFragmentManager().beginTransaction();

        Intent i = getIntent();
        int pos = i.getIntExtra("position", 0);

        switch (pos) {
            case 0:
                //Home, we should never go here
                break;
            case 1:
                //Preferences
                setTitle("Preferences");
                ft.replace(R.id.mainContent, new SettingsFragment()).commit();
                break;
            case 2:
                //About
                setTitle("About");
                ft.replace(R.id.mainContent, new AboutFragment()).commit();
                break;
            case 3:
                //Profile
                setTitle("Profile");
                ft.replace(R.id.mainContent, new ProfileFragment()).commit();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}