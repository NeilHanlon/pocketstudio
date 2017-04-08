package edu.wit.mobileapp.pocketstudio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ProjectEditor extends AppCompatActivity {
    private static final String TAG_TRANSPORT = "Transport";
    private static final String TAG_RECORD = "Record Audio Test";
    private static final String TAG_PERMISSIONS = "Permissions";
    private static final String TAG_PLAYBACK = "Playback";
    private static final String TAG_STORAGE = "Storage";

    static String recordFileName = null;

    boolean isRecording= false;
    boolean isplaying = false;

    //Track file storage
    File track1 = new File(Environment.getExternalStorageDirectory() + File.separator + "test1.wav"); //sorry
    File track2 = new File(Environment.getExternalStorageDirectory() + File.separator + "test2.wav");
    File track3 = new File(Environment.getExternalStorageDirectory() + File.separator + "test3.wav");
    File track4 = new File(Environment.getExternalStorageDirectory() + File.separator + "test4.wav");
    File currentTrack = track1;

    //Button references
    private ImageButton playPauseButton;
    private ImageButton backToZeroButton;
    private ImageButton recordButton;
    private MediaPlayer player = null;

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(recordFileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG_PLAYBACK, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private Drawable makeDrawable (String resourceURI){
        int resourceName = getResources().getIdentifier(resourceURI, null, getPackageName());
        return getResources().getDrawable(resourceName);
    }

    private boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return (checkPermission == PackageManager.PERMISSION_GRANTED);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_editor);

        customCheckPermissions();
        File pocketStudioDirectory = getApplicationContext().getDir("pocketStudio", Context.MODE_PRIVATE)
        pocketStudioDirectory.mkdirs();
        Log.d(TAG_STORAGE, "directory checked and created");

        //buttons
        playPauseButton = (ImageButton) findViewById(R.id.buttonPlayPause);
        backToZeroButton = (ImageButton) findViewById(R.id.buttonBackToZero);
        recordButton = (ImageButton) findViewById(R.id.buttonRecord);
        final Context context = getApplicationContext();

        //button resource declarations for icon changes, use drawables as sources
        //String pvPauseURI = "@drawable/pvpausebutton";
        //final int pvPauseResource = getResources().getIdentifier(pvPauseURI, null, getPackageName());
        //final Drawable pvPauseDrawable = getResources().getDrawable(pvPauseResource);
        final Drawable pvPauseDrawable = makeDrawable("@drawable/pvpausebutton");
        final Drawable pvPlayDrawable = makeDrawable("@drawable/pvplaybutton");
        final Drawable pvStopDrawable = makeDrawable("@drawable/pvstoprecording");
        final Drawable pvRecordDrawable = makeDrawable("@drawable/pvrecordbutton");

        // Record to this file:
        final int trackNumber = 0;

        PackageManager pmanager = this.getPackageManager();
        final boolean hasMic = (pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE));

        recordFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        recordFileName += "/audiorecordtest.wav";

        //Create recorder
        final MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //mediaRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
        mediaRecorder.setAudioEncodingBitRate(32);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(recordFileName);
        try {
            mediaRecorder.prepare();
            Log.d(TAG_RECORD, "Recording prepared");
        } catch (IOException e) {
            Log.e(TAG_RECORD, "prepare() failed");
        }

        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(recordFileName);
        }
        catch (IOException e) {
            Log.e(TAG_PLAYBACK, "Failed to set data source");
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                if (hasMic) {
                    if (isRecording) {
                        mediaRecorder.start();
                        recordButton.setBackground(pvStopDrawable);
                        Log.d(TAG_RECORD, "Recording started");
                    } else {
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();
                        recordButton.setBackground(pvRecordDrawable);
                        Log.d(TAG_RECORD, "Recording stopped");
                    }
                }
                else {
                    Toast.makeText(context, "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
                }
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_TRANSPORT, "PlayPause pressed");
                isplaying = !isplaying;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    playPauseButton.setBackground(pvPauseDrawable);
                } else {
                    playPauseButton.setBackground(pvPlayDrawable);
                }
            }
        });

        //String filename = "android.resource://" + this.getPackageName() + "/raw/peppers1";
    }

    private void customCheckPermissions(){
        int recordPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d(TAG_PERMISSIONS, "Record Permission: " + recordPermission);
        Log.d(TAG_PERMISSIONS, "Read Permission: " + readPermission);
        Log.d(TAG_PERMISSIONS, "Write Permission: " + writePermission);
    }

    public void clickableTrack1(View view) {
        currentTrack = track1;
    }

    public void clickableTrack2(View view) {
        currentTrack = track2;
    }

    public void clickableTrack3(View view) {
        currentTrack = track3;
    }

    public void clickableTrack4(View view) {
        currentTrack = track4;
    }
}