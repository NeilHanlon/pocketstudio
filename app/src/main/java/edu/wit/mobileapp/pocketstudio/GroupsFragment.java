package edu.wit.mobileapp.pocketstudio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.wit.mobileapp.pocketstudio.models.Group;
import edu.wit.mobileapp.pocketstudio.models.ServiceHelper;
import edu.wit.mobileapp.pocketstudio.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Matt on 2/23/17.
 */
public class GroupsFragment extends Fragment {


    private static final String TAG = GroupsFragment.class.getName();
    RelativeLayout mMainContent;
    FragmentTransaction ft;
    Properties prop = new Properties();
    SharedPreferences settings;
    String userid;
    User user;
    List<Group> groups;
    boolean mIsRestoredFromBackstack;

    @BindView(R.id.groupListView) ListView _groupListView;
    @BindView(R.id.groupFragmentContainer) RelativeLayout _groupFragmentContainer;
    View nogroupView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mIsRestoredFromBackstack = false;
        View view = inflater.inflate(R.layout.groups_fragment,container,false);
        ButterKnife.bind(this, view);
        try {
            prop.load(getActivity().getBaseContext().getAssets().open("pocketstudio.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String prefsname = prop.getProperty("pocketstudio.prefs_name", "pocketstudioprefs");
        settings = getActivity().getSharedPreferences(prefsname, 0);
        this.userid = settings.getString("userid", null);
        nogroupView = inflater.inflate(R.layout.group_fragment_none, container, false);

        getCurrentUser();
        getUserGroups();

        registerForContextMenu(_groupListView);

        _groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                           long id) {
                // TODO Auto-generated method stub
                if (view != null) {
                    view.setSelected(true);
                    //view.setBackgroundResource(R.color.colorAccent);
                }
                return false;
            }
        });
        _groupListView.setOnCreateContextMenuListener(new AdapterView.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                GroupsFragment.super.onCreateContextMenu(menu, v, menuInfo);
                ((PocketStudioMain)getActivity()).lastContextMenuButton = v;
                ((PocketStudioMain)getActivity()).groups = groups;
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.group_context, menu);
            }
        });

        _groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("userid", settings.getString("userid", null));
                    bundle.putParcelable("group", Parcels.wrap(groups.get(position)));
                    Fragment frag = new DisplayGroupFragment();
                    frag.setArguments(bundle);
                    frag.setTargetFragment(GroupsFragment.this, 0);
                    _groupFragmentContainer.removeAllViews();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.groupFragmentContainer, frag)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });



        return view;
    }

    @OnClick(R.id.fabAddGroup)
    public void addGroup() {
        Bundle bundle = new Bundle();
        bundle.putString("userid", settings.getString("userid", null));
        Fragment frag = new AddGroupFragment();
        frag.setArguments(bundle);
        frag.setTargetFragment(this, 0);
        _groupFragmentContainer.removeAllViews();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.groupFragmentContainer, frag)
                .addToBackStack(null)
                .commit();
    }

    protected void getUserGroups() {
        User.UserService userService = ServiceHelper.createService(User.UserService.class);
        Call<List<Group>> call = userService.getUserGroups(this.userid);
        call.enqueue(new Callback<List<Group>>() {
            private User user;

            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                // The network call was a success and we got a response
                Log.d(TAG, "In Onresponse");
                List<Group> groups = response.body();
                //user.printUser();
                setGroups(groups);
                if (groups.size() > 0) {
                    refreshGroupView();
                } else if (nogroupView.getParent() == null) {
                    _groupFragmentContainer.addView(nogroupView);
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
    }

    private void getCurrentUser() {
        User.UserService userService = ServiceHelper.createService(User.UserService.class);
        Call<User> user = userService.get(settings.getString("userid", null));
        user.enqueue(new Callback<User>() {
            private User user;

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                User user = response.body();
                setUser(user);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
    }

    private void refreshGroupView() {
        _groupListView.setAdapter(new CustomArrayAdapter(getContext(), groups));
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    private class CustomArrayAdapter extends ArrayAdapter {
        List<Group> groups;
        LayoutInflater  mInflater;
        public CustomArrayAdapter(Context context, List<Group> groups) {
            super(context, 0, groups);
            this.groups = groups;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.group_row,parent,false);
                // inflate custom layout called row
                holder = new ViewHolder();
                holder.name =(TextView) convertView.findViewById(R.id.textView1);
                holder.desc =(TextView) convertView.findViewById(R.id.textView2);
                // initialize textview
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            Group group = (Group) groups.get(position);
            holder.name.setText(group.name);
            holder.desc.setText("i am potato");
            holder.id = group.id;

            return convertView;

        }

        class ViewHolder
        {
            TextView name;
            TextView desc;
            String id;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null && !mIsRestoredFromBackstack) {
            mIsRestoredFromBackstack = true;
            getUserGroups();
        }
        getUserGroups();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mIsRestoredFromBackstack = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            getUserGroups();
        }
        getUserGroups();

    }

    @Override
    public void onResume() {
        super.onResume();
        getUserGroups();
    }
}
