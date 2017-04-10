package edu.wit.mobileapp.pocketstudio;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.wit.mobileapp.pocketstudio.models.Group;
import edu.wit.mobileapp.pocketstudio.models.Project;
import edu.wit.mobileapp.pocketstudio.models.ServiceHelper;
import edu.wit.mobileapp.pocketstudio.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DisplayGroupFragment extends Fragment {

    private static final String TAG = DisplayGroupFragment.class.getName();

    Group group;
    List<User> users;
    List<Project> projects;
    User user;
    String userid;

    @BindView(R.id.projectsListView) ListView _projectsListView;
    @BindView(R.id.usersListView) ListView _usersListView;
    @BindView(R.id.projectsListViewContainer) LinearLayout _projectsListViewContainer;
    @BindView(R.id.usersListViewContainer) LinearLayout _usersListViewContainer;

    View noUsersView;
    View noProjectsView;

    public DisplayGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            group = Parcels.unwrap(getArguments().getParcelable("group"));
            userid = getArguments().getString("userid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_group, container, false);
        ButterKnife.bind(this, view);
        noUsersView = inflater.inflate(R.layout.fragment_nocontent, container, false);
        noProjectsView = inflater.inflate(R.layout.fragment_nocontent, container, false);

        getGroupUsers();
        getGroupProjects();

        registerForContextMenu(_projectsListView);
        registerForContextMenu(_usersListView);

        _usersListView.setOnCreateContextMenuListener(new AdapterView.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                DisplayGroupFragment.super.onCreateContextMenu(menu, v, menuInfo);
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                if (users.get(info.position).id.equals(userid)) {
                    return;
                }
                ((PocketStudioMain)getActivity()).lastContextMenuButton = v;
                ((PocketStudioMain)getActivity()).users = users;
                ((PocketStudioMain)getActivity()).currentGroup = group;
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.group_user_context, menu);
            }
        });

        _projectsListView.setOnCreateContextMenuListener(new AdapterView.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                DisplayGroupFragment.super.onCreateContextMenu(menu, v, menuInfo);
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                ((PocketStudioMain)getActivity()).lastContextMenuButton = v;
                ((PocketStudioMain)getActivity()).users = users;
                ((PocketStudioMain)getActivity()).currentGroup = group;
                ((PocketStudioMain)getActivity()).projects = projects;
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.group_project_context, menu);
            }
        });

        /**
         * @TODO Add ability to add project to group
         * @TODO Add ability to add user to group
         */

        return view;
    }

    protected void getGroupUsers() {
        Group.GroupService groupService = ServiceHelper.createService(Group.GroupService.class);
        Call<List<User>> call = groupService.getGroupUsers(this.group.id);
        call.enqueue(new Callback<List<User>>() {
            private User user;

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                // The network call was a success and we got a response
                Log.d(TAG, "In Onresponse");
                List<User> users = response.body();
                //user.printUser();
                setUsers(users);
                if (users.size() > 0) {
                    refreshGroupUsersView();
                } else if (noUsersView.getParent() == null) {
                    _usersListViewContainer.addView(noUsersView);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
    }

    protected void getGroupProjects() {
        Group.GroupService groupService = ServiceHelper.createService(Group.GroupService.class);
        Call<List<Project>> call = groupService.getGroupProjects(this.group.id);
        call.enqueue(new Callback<List<Project>>() {
            private User user;

            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                // The network call was a success and we got a response
                Log.d(TAG, "In Onresponse");
                List<Project> projects = response.body();
                //user.printUser();
                setProjects(projects);
                if (projects.size() > 0) {
                    refreshGroupProjectView();
                } else if (noProjectsView.getParent() == null) {
                    _projectsListViewContainer.addView(noProjectsView);
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
    }

    private void refreshGroupProjectView() {
        _projectsListView.setAdapter(new GroupProjectsArrayAdapter(getContext(), projects));
    }

    private void refreshGroupUsersView() {
        _usersListView.setAdapter(new GroupUsersArrayAdapter(getContext(), users));
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    private class GroupProjectsArrayAdapter extends ArrayAdapter {
        List<Project> projects;
        LayoutInflater  mInflater;
        public GroupProjectsArrayAdapter(Context context, List<Project> projects) {
            super(context, 0, projects);
            this.projects = projects;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupProjectsArrayAdapter.ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.group_row,parent,false);
                // inflate custom layout called row
                holder = new GroupProjectsArrayAdapter.ViewHolder();
                holder.name =(TextView) convertView.findViewById(R.id.textView1);
                holder.desc =(TextView) convertView.findViewById(R.id.textView2);
                // initialize textview
                convertView.setTag(holder);
            }
            else
            {
                holder = (GroupProjectsArrayAdapter.ViewHolder)convertView.getTag();
            }
            Project project = (Project) projects.get(position);
            holder.name.setText(project.name);
            holder.desc.setText("i am potato");
            holder.id = project.id;

            return convertView;

        }

        class ViewHolder
        {
            TextView name;
            TextView desc;
            String id;
        }
    }

    private class GroupUsersArrayAdapter extends ArrayAdapter {
        List<User> users;
        LayoutInflater  mInflater;
        public GroupUsersArrayAdapter(Context context, List<User> users) {
            super(context, 0, users);
            this.users = users;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupUsersArrayAdapter.ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.group_row,parent,false);
                // inflate custom layout called row
                holder = new GroupUsersArrayAdapter.ViewHolder();
                holder.name =(TextView) convertView.findViewById(R.id.textView1);
                holder.desc =(TextView) convertView.findViewById(R.id.textView2);
                // initialize textview
                convertView.setTag(holder);
            }
            else
            {
                holder = (GroupUsersArrayAdapter.ViewHolder)convertView.getTag();
            }
            User user = (User) users.get(position);
            holder.name.setText(user.name);
            holder.desc.setText("i am potato");
            holder.id = user.id;

            return convertView;

        }

        class ViewHolder
        {
            TextView name;
            TextView desc;
            String id;
        }
    }
}
