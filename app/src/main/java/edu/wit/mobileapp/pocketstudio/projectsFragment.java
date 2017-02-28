package edu.wit.mobileapp.pocketstudio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

        projectExpandCollapse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isMyProjectsHidden) {
                    projectsExpandCollapseImage.animate().rotation(0).start();
                    //projectsExpandCollapseImage.setRotation(0);
                    myProjectsContent.setVisibility(View.VISIBLE);


                }
                else {
                    projectsExpandCollapseImage.animate().rotation(180).start();
                    //projectsExpandCollapseImage.setRotation(180);
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

        private void collapse(final View view) {
            view.setPivotY(0);
            view.animate().scaleY(1/view.getHeight()).setDuration(1000).withEndAction(new Runnable() {
                @Override public void run() {
                    view.setVisibility(View.GONE);
                }
            });
        }

        private void expand(View view, int height) {
            float scaleFactor = height / view.getHeight();

            view.setVisibility(View.VISIBLE);
            view.setPivotY(0);
            view.animate().scaleY(scaleFactor).setDuration(1000);
        }

        return myInflatedView;
    }
}
