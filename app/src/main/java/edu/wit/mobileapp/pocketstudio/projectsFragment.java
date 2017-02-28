package edu.wit.mobileapp.pocketstudio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Matt on 2/23/17.
 */


public class ProjectsFragment extends Fragment {

    public static boolean isMyProjectsHidden = false;
    public static boolean isSharedProjectsHidden = false;
    private static final String TAG = ProjectsFragment.class.getName();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View myInflatedView = inflater.inflate(R.layout.projects_fragment,container,false);

        //MY PROJECTS SECTION
        ListView projectsListView = (ListView) myInflatedView.findViewById(R.id.myProjectsListview);
        TextView myProjEmptyText = (TextView) myInflatedView.findViewById(R.id.myProjEmptyText);
        final RelativeLayout projectExpandCollapse = (RelativeLayout) myInflatedView.findViewById(R.id.projectsExpandCollapse);
        final ImageView projectsExpandCollapseImage = (ImageView) myInflatedView.findViewById(R.id.projectsExpandCollapseImage);
        final LinearLayout myProjectsContent = (LinearLayout) myInflatedView.findViewById(R.id.myProjectsContent);

        final int myProjectsHeight = (int) myProjectsContent.getMeasuredHeight();

        projectExpandCollapse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isMyProjectsHidden) {
                    projectsExpandCollapseImage.animate().rotation(0).start();
                    myProjectsContent.setVisibility(View.VISIBLE);
                }
                else {
                    projectsExpandCollapseImage.animate().rotation(180).start();
                    myProjectsContent.setVisibility(View.GONE);
                }
                isMyProjectsHidden = !isMyProjectsHidden;
            }
        });

        //SHARED PROJECTS SECTION
        ListView sharedListView = (ListView) myInflatedView.findViewById(R.id.sharedProjectsListview);
        TextView sharedProjEmptyText = (TextView) myInflatedView.findViewById(R.id.sharedProjEmptyText);
        final RelativeLayout sharedExpandCollapse = (RelativeLayout) myInflatedView.findViewById(R.id.sharedExpandCollapse);
        final ImageView sharedExpandCollapseImage = (ImageView) myInflatedView.findViewById(R.id.sharedExpandCollapseImage);
        final LinearLayout sharedProjectsContent = (LinearLayout) myInflatedView.findViewById(R.id.sharedProjectsContent);

        final int sharedProjectsHeight = (int) sharedProjectsContent.getMeasuredHeight();

        sharedExpandCollapse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isSharedProjectsHidden) {
                    sharedExpandCollapseImage.animate().rotation(0).start();
                    //sharedExpandCollapseImage.setRotation(0);
                    sharedProjectsContent.setVisibility(View.VISIBLE);


                }
                else {
                    sharedExpandCollapseImage.animate().rotation(180).start();
                    //sharedExpandCollapseImage.setRotation(180);
                    sharedProjectsContent.setVisibility(View.GONE);

                }
                isSharedProjectsHidden = !isSharedProjectsHidden;
            }
        });

        return myInflatedView;
    }
}
