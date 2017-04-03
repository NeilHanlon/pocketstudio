package edu.wit.mobileapp.pocketstudio;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;


public class ProjectEditor extends AppCompatActivity {
    private static final String TAG = "Transport";
    boolean isRecording= false;
    boolean isplaying = false;

    private ImageButton playPauseButton;
    private ImageButton backToZeroButton;
    private ImageButton recordButton;
    private MediaPlayer track1, track2, track3, track4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_editor);

        playPauseButton = (ImageButton) findViewById(R.id.buttonPlayPause);
        backToZeroButton = (ImageButton) findViewById(R.id.buttonBackToZero);
        recordButton = (ImageButton) findViewById(R.id.buttonRecord);

        String pvPauseURI = "@drawable/pvpausebutton";
        final int pvPauseResource = getResources().getIdentifier(pvPauseURI, null, getPackageName());
        final Drawable pvPauseDrawable = getResources().getDrawable(pvPauseResource);

        String pvPlayURI = "@drawable/pvplaybutton";
        final int pvPlayResource = getResources().getIdentifier(pvPlayURI, null, getPackageName());
        final Drawable pvPlayDrawable = getResources().getDrawable(pvPlayResource);


        final MediaPlayer track1 = MediaPlayer.create(this, R.raw.peppers1);
        final MediaPlayer track2 = MediaPlayer.create(this, R.raw.peppers2);
        final MediaPlayer track3 = MediaPlayer.create(this, R.raw.letthemtalk);
        //track1.prepareAsync();
        //track2.prepareAsync();
        //track3.prepareAsync();


        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "PlayPause pressed");
                if (isplaying == false){
                    playPauseButton.setBackground(pvPauseDrawable);
                    track1.start();
                    //track2.start();
                    //track3.start();
                }
                else {
                    playPauseButton.setBackground(pvPlayDrawable);
                    track1.pause();
                    //track2.pause();
                    //track3.pause();
                }
                isplaying = !isplaying;
            }
        });

        //String filename = "android.resource://" + this.getPackageName() + "/raw/peppers1";
    }

    public void onClickBackToZero(View button) {

    }


    public void onClickRecord(View button) {

    }


    public void play(){

    }

    public void pause(){

    }



    //C++ native functions:
    private native void onPlayPause(boolean play);

    static {
        System.loadLibrary("SuperpoweredExample");
    }

}