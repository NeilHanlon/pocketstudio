package edu.wit.mobileapp.pocketstudio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Neil on 4/8/2017.
 */

public class AboutFragment extends Fragment {

    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHash;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about,container,false);

        /*WebView wv = (WebView) view.findViewById(R.id.about_webview);
        wv.loadUrl("https://shrug.pw/");*/

        listView = (ExpandableListView)view.findViewById(R.id.lvExp);
        initData();
        listAdapter = new ExpandableListAdapter(getContext(),listDataHeader,listHash);
        listView.setAdapter(listAdapter);

        return view;
    }

    private void initData() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        listDataHeader.add("Creating a project");
        listDataHeader.add("Record audio");
        listDataHeader.add("Add effects");
        listDataHeader.add("Creating/editing a group");
        listDataHeader.add("Logout");
        listDataHeader.add("Edit account");
        listDataHeader.add("Change notification settings");


        List<String> createAProject = new ArrayList<>();
        createAProject.add("1) If you haven’t, sign into the app");
        createAProject.add("2) Under the projects tab, press the plus button in the orange circle in the bottom right of the screen");
        createAProject.add("3) The new project window will open with 4 tracks available to record and edit");

        List<String> createAGroup = new ArrayList<>();
        createAGroup.add("1) If you haven’t, sign into the app");
        createAGroup.add("2) On the project/home screen, swipe to the left and it will bring you to the groups page");
        createAGroup.add("3) Click the orange circle in the bottom right of the screen, and you will be prompted to name the group");
        createAGroup.add("4) To edit or delete the group, hold your finger on the group you wish to edit/delete and you will be prompted with a menu with each option");

        List<String> logout = new ArrayList<>();
        logout.add("1) From any screen within the application, tap the navigation drawer in the top left");
        logout.add("2) Tap Logout, and you will be returned to the login screen");

        List<String> recordAudio = new ArrayList<>();
        recordAudio.add("1) In the project editor view, tap the red circle near the bottom right of the screen and begin to play an instrument/sing/etc.");
        recordAudio.add("2) When you want to finish recording, tap the white square that has replaced the previous red circle\n");

        List<String> addEffects = new ArrayList<>();
        addEffects.add("1) Ensure you are currently in a new or existing project.");
        addEffects.add("2) By pressing the settings button (gear shaped icon) you’re able to choose from a list of effects to manipulate the track you have selected");

        List<String> editAccount = new ArrayList<>();
        editAccount.add("1) On the pocketStudio home page, swipe out the navigation drawer.");
        editAccount.add("2) Tap on the orange bar at the top of the bar labeled by your username");
        editAccount.add("3) Here, you have access to change your alias, email address, and password. You may also delete your account here.");

        List<String> changeNotifications = new ArrayList<>();
        changeNotifications.add("1) On the pocketStudio home page, swipe out the navigation drawer.");
        changeNotifications.add("2) Tap the \"Preferences\" pane.");
        changeNotifications.add("3) Tap \"Notification settings\".");
        changeNotifications.add("4) Here you will find options to turn off notifications for messages, change your notification sound, vibrate on notification, and change the volume of incoming notification.");

        listHash.put(listDataHeader.get(0),createAProject);
        listHash.put(listDataHeader.get(1),recordAudio);
        listHash.put(listDataHeader.get(2),addEffects);
        listHash.put(listDataHeader.get(3),createAGroup);
        listHash.put(listDataHeader.get(4),logout);
        listHash.put(listDataHeader.get(5),editAccount);
        listHash.put(listDataHeader.get(6),changeNotifications);


    }
}
