package edu.wit.mobileapp.pocketstudio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import butterknife.ButterKnife;
import butterknife.BindView;


public class PocketStudioMain extends AppCompatActivity {
    private static final String TAG_PERMISSION = "Permissions";
    private static final String TAG_FILEIO = "FileIO";

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int READ_REQUEST_CODE = 1;
    private static final int WRITE_REQUEST_CODE = 2;

    private static String TAG = PocketStudioMain.class.getSimpleName();

    TabLayout tabLayout;
    ViewPager viewPager;
    String userid;
    Properties prop = new Properties();
    SharedPreferences settings;

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

    @BindView(R.id.desc) TextView _profile_description;

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

        if(settings.getString("userid", null) == null) {
            Intent gimmegimmesomelovin = new Intent();
            gimmegimmesomelovin.setClass(this, LoginActivity.class);
            startActivity(gimmegimmesomelovin);
        }

        System.out.printf("User ID: %s", userid);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocket_studio_main);

        String pocketStudioDirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pocketStudio/";
        File pocketStudioDirectory = new File(pocketStudioDirName);
        Log.d(TAG_FILEIO, pocketStudioDirectory.toString());
        if (!pocketStudioDirectory.exists()) {
            pocketStudioDirectory.mkdirs();
            Log.d(TAG_FILEIO, "PocketstudioDir created");
        }

        /*
        File f = new File(pocketStudioDir);
        File contents[] = f.listFiles();
        int minExistingProject = 1;
        int maxExistingProject = 1;
        for (int i=0; i < contents.length; i++) {
            Log.d(TAG_FILEIO, "File found: " + contents[i].getName());
            if (contents[i].toString() == "Project " + (i+1)){
                Log.d(TAG_FILEIO, "Project Standard found: " + contents[i]);
            }

        }*/


        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        mNavItems.add(new NavItem("Home", "Meetup destination", R.drawable.ic_guitarshilouette));
        mNavItems.add(new NavItem("Preferences", "Change your preferences", R.drawable.ic_track_settings));
        mNavItems.add(new NavItem("About", "Get to know about us", R.drawable.ic_add));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        RelativeLayout mProfileBox = (RelativeLayout) findViewById(R.id.profileBox);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: " + getTitle());

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mProfileBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile = new Intent();
                profile.setClass(PocketStudioMain.this, PreferencesActivity.class);
                profile.setAction(Intent.ACTION_MAIN);
                profile.addCategory(Intent.CATEGORY_LAUNCHER);
                profile.putExtra("position", 3);
                mDrawerLayout.closeDrawer(mDrawerPane);
                startActivity(profile);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pagerMain + ":"+viewPager.getCurrentItem());
        if (fragment != null) // could be null if not instantiated yet
        {
            if (fragment.getView() != null) {
                // Pop the backstack on the ChildManager if there is any. If not, close this activity as normal.
                if (!fragment.getChildFragmentManager().popBackStackImmediate()) {
                    finish();
                }
            }
        }
    }
    /*
    * Called when a particular item from the navigation drawer
    * is selected.
    * */
    private void selectItemFromDrawer(int position) {
        Intent travel = null;
        if (position != 0) {
            travel = new Intent(this, PreferencesActivity.class);
            travel.setAction(Intent.ACTION_MAIN);
            travel.addCategory(Intent.CATEGORY_LAUNCHER);
            travel.putExtra("position", position);
        }
        /*
        Fragment fragment = new PreferencesFragment();
        ((CustomAdapter)viewPager.getAdapter()).getFragmentManager().beginTransaction()
                .replace(R.id.pagerMain, fragment)
                .commit();*/

        mDrawerList.setItemChecked(position, true);

        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
        if (position != 0) {
            startActivity(travel);
        }
    }

    // Called when invalidateOptionsMenu() is invoked
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        //return super.onPrepareOptionsMenu(menu);
        return true;
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private String fragments [] = {"Projects", "Groups"};
        private Context context;
        private FragmentManager fragmentManager;
        public CustomAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
            super(supportFragmentManager);
            this.context = applicationContext;
            this.fragmentManager = supportFragmentManager;
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

        public FragmentManager getFragmentManager() {
            return fragmentManager;
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

    private class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }
}
