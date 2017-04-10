package edu.wit.mobileapp.pocketstudio;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.TextView;
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
    private static final String TAG_FILEIO = "FileIO";


    static String recordFileName = null;
    static String projectName = null;

    boolean isRecording = false;
    boolean isplaying = false;

    //Track file storage

    File track1 = new File(Environment.getExternalStorageDirectory() + File.separator + "test1.wav"); //sorry
    File track2 = new File(Environment.getExternalStorageDirectory() + File.separator + "test2.wav");
    File track3 = new File(Environment.getExternalStorageDirectory() + File.separator + "test3.wav");
    File track4 = new File(Environment.getExternalStorageDirectory() + File.separator + "test4.wav");

    SoundPool trackPool;
    int currentTrack = 1; // selector for currently active track


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

    private Drawable makeDrawable(String resourceURI) {
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

        String pocketStudioDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pocketStudio";

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

        File f = new File(pocketStudioDir);
        File contents[] = f.listFiles();
        int minExistingProject = 1;
        int maxExistingProject = 1;
        for (int i=0; i < contents.length; i++) {
            Log.d(TAG_FILEIO, "File found: " + contents[i].getName());
            //Log.d(TAG_FILEIO, "Project " + (i+1));
            if (contents[i].getName().startsWith("Project " + (i+1))){
                Log.d(TAG_FILEIO, "Project Standard found: " + contents[i].toString());
            }

        }

        recordFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pocketStudio/";

        Log.d(TAG_STORAGE, recordFileName);

        File projectDirectory = new File(pocketStudioDir);
        if (!projectDirectory.exists()) {
            projectDirectory.mkdirs();
        }

        Log.d(TAG_STORAGE, projectDirectory.toString());

        //Create recorder
        final MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(16);
        mediaRecorder.setAudioSamplingRate(44100);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            trackPool = new SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        else {
            trackPool = new SoundPool(4, AudioManager.STREAM_MUSIC,1);
        }


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                Log.d(TAG_TRANSPORT, "Record Tapped");
                if (hasMic) {
                    if (isRecording) {
                        File file = new File(createTrackFileName());
                        file.delete();
                        mediaRecorder.setOutputFile(createTrackFileName());
                        try {
                            mediaRecorder.prepare();
                            Log.d(TAG_RECORD, "Recording prepared");
                        } catch (IOException e) {
                            Log.e(TAG_RECORD, "prepare() failed");
                        }
                        mediaRecorder.start();
                        recordButton.setBackground(pvStopDrawable);
                        Log.d(TAG_RECORD, "Recording started");
                    } else {
                        Log.d(TAG_TRANSPORT, "Stop Tapped");
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        //mediaRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
                        mediaRecorder.setAudioEncodingBitRate(16);
                        mediaRecorder.setAudioSamplingRate(44100);
                        recordButton.setBackground(pvRecordDrawable);
                        Log.d(TAG_RECORD, "Recording stopped");
                    }
                } else {
                    Toast.makeText(context, "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
                }
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_TRANSPORT, "PlayPause pressed");
                isplaying = !isplaying;
                if (isplaying) {
                    playPauseButton.setBackground(pvPauseDrawable);
                } else {
                    playPauseButton.setBackground(pvPlayDrawable);
                }
            }
        });

        //String filename = "android.resource://" + this.getPackageName() + "/raw/peppers1";
    }

    private String createTrackFileName(){
        return recordFileName + "track_file" + String.valueOf(currentTrack) + ".wav";
    }

    private void customCheckPermissions() {
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
        currentTrack = 1;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track1Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        Log.d(TAG_PLAYBACK, "Track set to 1");
    }

    public void clickableTrack2(View view) {
        currentTrack = 2;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track2Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        Log.d(TAG_PLAYBACK, "Track set to 2");

    }

    public void clickableTrack3(View view) {
        currentTrack = 3;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track3Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        Log.d(TAG_PLAYBACK, "Track set to 3");

    }

    public void clickableTrack4(View view) {
        currentTrack = 4;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track4Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        Log.d(TAG_PLAYBACK, "Track set to 4");

    }
    private void removeTrackStyling() {
        View t1 = (View) findViewById(R.id.track1);
        View t2 = (View) findViewById(R.id.track2);
        View t3 = (View) findViewById(R.id.track3);
        View t4 = (View) findViewById(R.id.track4);
        TextView tx1 = (TextView) findViewById(R.id.track1Text);
        TextView tx2 = (TextView) findViewById(R.id.track2Text);
        TextView tx3 = (TextView) findViewById(R.id.track3Text);
        TextView tx4 = (TextView) findViewById(R.id.track4Text);
        t1.setBackgroundResource(R.color.content_white);
        t2.setBackgroundResource(R.color.white);
        t3.setBackgroundResource(R.color.content_white);
        t4.setBackgroundResource(R.color.white);
        tx1.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tx2.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tx3.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        tx4.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

    }
}
