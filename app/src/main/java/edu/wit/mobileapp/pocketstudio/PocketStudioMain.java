package edu.wit.mobileapp.pocketstudio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.Properties;

import butterknife.ButterKnife;
import butterknife.BindView;


public class PocketStudioMain extends AppCompatActivity {

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
                Intent newProject = new Intent();
                newProject.setClass(PocketStudioMain.this, ProjectEditor.class);
                startActivity(newProject);
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

    public String getUserid() {
        return userid;
    }

    public SharedPreferences getSettings() {
        return settings;
    }
}
