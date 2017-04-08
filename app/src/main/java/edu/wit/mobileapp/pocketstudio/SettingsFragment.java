package edu.wit.mobileapp.pocketstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

/**
 * Created by Neil on 4/8/2017.
 */

public class SettingsFragment extends Fragment {

    RelativeLayout mMainContent;
    FragmentTransaction ft;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings,container,false);
        RelativeLayout notiRow = (RelativeLayout) view.findViewById(R.id.notificationLayoutBox);

        /***
         * @TODO Implement onclick for Fix Latency
         * @TODO Make up storage usage and set textview using that value
         */
        notiRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainContent = (RelativeLayout) view.findViewById(R.id.mainContent);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContent, new NotificationPrefFragment())
                .commit();
            }
        });
        return view;
    }
}
