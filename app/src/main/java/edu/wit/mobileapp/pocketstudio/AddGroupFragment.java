package edu.wit.mobileapp.pocketstudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * Created by Neil on 4/8/2017.
 */

public class AddGroupFragment extends Fragment {

    @BindView(R.id.btn_creategroup) AppCompatButton _btn_createGroup;
    @BindView(R.id.group_name) EditText _group_name;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addgroup,container,false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btn_creategroup)
    public void createGroup(){
        String userid = getArguments().getString("userid");
        String groupname = _group_name.getText().toString();
        Group.GroupService groupService = ServiceHelper.createService(Group.GroupService.class);
        List<String> users = new ArrayList<String>();
        users.add(userid);
        Group group = new Group(groupname, users, new ArrayList<String>());
        Call<Group> call = groupService.createGroup(group);
        call.enqueue(new Callback<Group>() {
            private User user;

            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                // The network call was a success and we got a response
                Group group = response.body();
                Toast.makeText(getActivity(), "Group added!",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("result", "Ok");
                getTargetFragment().onActivityResult(0, Activity.RESULT_OK, intent);
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
            }
        });
    }
}
