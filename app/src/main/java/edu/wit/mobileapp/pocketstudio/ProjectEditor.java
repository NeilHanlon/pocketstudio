package edu.wit.mobileapp.pocketstudio;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.widget.ImageView;
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

    WavRecordService mWavRecordService;
    WavPlayerService mWavPlayerService;
    boolean mBound = false;

    boolean isRecording = false;
    boolean isplaying = false;

    int currentTrack = 1; // selector for currently active track

    //Button references
    private ImageButton playPauseButton;
    private ImageButton backToZeroButton;
    private ImageButton recordButton;
    private MediaPlayer player = null;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), WavRecordService.class);
        Intent intent2 = new Intent(getApplicationContext(), WavPlayerService.class);
        Log.d(TAG_PLAYBACK, "#### WE HERE");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        bindService(intent2, mConnectionPlayer, Context.BIND_AUTO_CREATE);
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

        //Create recorder
        final MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(16);
        mediaRecorder.setAudioSamplingRate(44100);


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                Log.d(TAG_TRANSPORT, "Record Tapped");
                if (hasMic) {
                    if (isRecording) {
                        File file = new File(createTrackFileName());
                        file.delete();
                        mWavRecordService.setFileToRecord(file);
                        mWavRecordService.recordAudio();
                        /*mediaRecorder.setOutputFile(createTrackFileName());
                        try {
                            mediaRecorder.prepare();
                            Log.d(TAG_RECORD, "Recording prepared");
                        } catch (IOException e) {
                            Log.e(TAG_RECORD, "prepare() failed");
                        }
                        mediaRecorder.start();*/
                        recordButton.setBackground(pvStopDrawable);
                        Log.d(TAG_RECORD, "Recording started");
                    } else {
                        Log.d(TAG_TRANSPORT, "Stop Tapped");
                        mWavRecordService.stopRecording(mWavRecordService.getRecServiceHandler(), 0);
                        recordButton.setBackground(pvRecordDrawable);
                        Log.d(TAG_RECORD, "Recording stopped");
                        /*
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        //mediaRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
                        mediaRecorder.setAudioEncodingBitRate(16);
                        mediaRecorder.setAudioSamplingRate(44100);*/
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
                    Log.d(TAG_TRANSPORT, "inside");
                    playPauseButton.setBackground(pvPauseDrawable);
                    File file1 = new File(createTrackFileName("1"));
                    File file2 = new File(createTrackFileName("2"));
                    Handler handler = new Handler();
                    /*WavMixer wm = new WavMixer(handler);
                    wm.setFilesToMix(file1, file2);*/
                    File output = new File(createTrackFileName("mixed"));
                    /*wm.setRawOutputFile(output);
                    wm.run();*/
                    mWavPlayerService.setFilesToPlay(file1, file2);
                    mWavPlayerService.setOutputFile(output);
                    mWavPlayerService.setTempFile(new File(createTrackFileName("raw")));
                    //mWavPlayerService.setFileToPlay(output);
                    mWavPlayerService.playAudio();
                } else {
                    playPauseButton.setBackground(pvPlayDrawable);
                }
            }
        });

        backToZeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //String filename = "android.resource://" + this.getPackageName() + "/raw/peppers1";
    }

    private Drawable makeDrawable(String resourceURI) {
        int resourceName = getResources().getIdentifier(resourceURI, null, getPackageName());
        return getResources().getDrawable(resourceName);
    }

    private boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return (checkPermission == PackageManager.PERMISSION_GRANTED);
    }

    private String createTrackFileName(){
        return recordFileName + "track_file" + String.valueOf(currentTrack) + ".wav";
    }

    private String createTrackFileName(String trackNum) {
        return recordFileName + "track_file" + trackNum + ".wav";
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
        ImageView trackSettings = (ImageView) findViewById(R.id.trackSettings1);
        Log.d(TAG_PLAYBACK, "Track set to 1");
    }

    public void clickableTrack2(View view) {
        currentTrack = 2;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track2Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        ImageView trackSettings = (ImageView) findViewById(R.id.trackSettings2);
        Log.d(TAG_PLAYBACK, "Track set to 2");

    }

    public void clickableTrack3(View view) {
        currentTrack = 3;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track3Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        ImageView trackSettings = (ImageView) findViewById(R.id.trackSettings3);
        Log.d(TAG_PLAYBACK, "Track set to 3");

    }

    public void clickableTrack4(View view) {
        currentTrack = 4;
        removeTrackStyling();
        view.setBackgroundResource(R.color.colorAccent);
        TextView textView = (TextView) findViewById(R.id.track4Text);
        textView.setTextColor(getResources().getColor(R.color.white));
        ImageView trackSettings = (ImageView) findViewById(R.id.trackSettings4);
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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WavRecordService.RecLocalBinder binder = (WavRecordService.RecLocalBinder) service;
            mWavRecordService = binder.getService();
            Log.d(TAG_PLAYBACK, "#### WE HERE2222");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private boolean mBoundPlayer;
    private ServiceConnection mConnectionPlayer = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WavPlayerService.PlayLocalBinder binder = (WavPlayerService.PlayLocalBinder) service;
            mWavPlayerService = binder.getService();
            Log.d(TAG_PLAYBACK, "#### WE HERE2222");
            mBoundPlayer = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundPlayer = false;
        }
    };
    
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (mBoundPlayer) {
            unbindService(mConnectionPlayer);
            mBoundPlayer = false;
        }
    }
}
