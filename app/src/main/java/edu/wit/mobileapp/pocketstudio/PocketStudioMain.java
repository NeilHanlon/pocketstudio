package edu.wit.mobileapp.pocketstudio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.Properties;

import butterknife.ButterKnife;
import butterknife.BindView;


public class PocketStudioMain extends AppCompatActivity {
    private static final String TAG_PERMISSION = "Permissions";
    private static final int RECORD_REQUEST_CODE = 101;
    private static final int READ_REQUEST_CODE = 1;
    private static final int WRITE_REQUEST_CODE = 2;

    TabLayout tabLayout;
    ViewPager viewPager;
    String userid;
    Properties prop = new Properties();
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            prop.load(getBaseContext().getAssets().open("pocketstudio.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String prefsname = prop.getProperty("pocketstudio.prefs_name", "pocketstudioprefs");
        settings = getSharedPreferences(prefsname, 0);
        userid = settings.getString("userid", null);
        System.out.printf("User ID: %s", userid);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocket_studio_main);

        getSupportActionBar().setElevation(0);

        viewPager = (ViewPager) findViewById(R.id.pagerMain);
        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager(), getApplicationContext()));

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int recordPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO);
                int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (recordPermission != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG_PERMISSION, "Record permissions denied");
                    makeRecordRequest();
                }
                if (readPermission != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG_PERMISSION, "Read permissions denied");
                    makeReadRequest();
                }
                if (writePermission != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG_PERMISSION, "Write permissions denied");
                    makeWriteRequest();
                }
                else {
                    Intent newProject = new Intent();
                    newProject.setClass(PocketStudioMain.this, ProjectEditor.class);
                    startActivity(newProject);
                }
            }
        });
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private String fragments [] = {"Projects", "Groups"};
        public CustomAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
            super(supportFragmentManager);
        }
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new ProjectsFragment();
                case 1:
                    return new GroupsFragment();
                default:
                    return null;
            }
        }
        @Override
        public int getCount() {
            return fragments.length;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }
    }

    protected void makeRecordRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
    }
    protected void makeReadRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_REQUEST_CODE);
    }
    protected void makeWriteRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
    }

    public String getUserid() {
        return userid;
    }

    public SharedPreferences getSettings() {
        return settings;
    }
}
