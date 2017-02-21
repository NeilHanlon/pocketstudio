package edu.wit.mobileapp.pocketstudio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.roughike.bottombar.*;

public class ProjectEditor extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;
    private String AUDIO_FILE;
    private String currentToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_viewer);

        AUDIO_FILE= Environment.getExternalStorageDirectory()+"/audiorecorder.3gpp";

        Toast currentToast;
        setupBottomBar();

    }


    private void setupBottomBar(){
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId){
                if (tabId == R.id.tab_record){
                    Log.i("recordTap", "Record button tapped");

                    currentToast = null;
                    Toast currentToast = Toast.makeText(ProjectEditor.this, "Recording begun...", Toast.LENGTH_SHORT
                    );
                    currentToast.show();
                }
                 else if (tabId == R.id.tab_play){
                    Log.i("playTap", "Play button tapped");
                }
                else if (tabId == R.id.tab_backzero){
                    Log.i("backZeroTap", "Back to zero");
                }
            }
        });
    }

    public void buttonTapped(View view){

    }
}
