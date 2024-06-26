package edu.wit.mobileapp.pocketstudio;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by Matt on 4/3/17.
 */

public class AudioPlayManager implements Runnable {

    private File fileName;
    private InputStream stream;
    private volatile boolean playing;

    public AudioPlayManager() {
        super();
        setPlaying(false);

    }

    public void run() {
        // Get the length of the audio stored in the file (16 bit so 2 bytes per short)
        // and create a short array to store the recorded audio.
        int musicLength = (int) (fileName.length() / 2);
        short[] music = new short[musicLength];

        try {
            // Create a DataInputStream to read the audio data back from the saved file.
            InputStream is = this.stream;
            BufferedInputStream bis = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bis);

            // Read the file into the music array.
            int i = 0;
            while (dis.available() > 0) {
                music[i] = dis.readShort();
                i++;
            }

            // Close the input streams.
            dis.close();

            // Create a new AudioTrack object using the same parameters as the AudioRecord
            // object used to create the file.
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    11025,
                    AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    musicLength,
                    AudioTrack.MODE_STREAM);
            // Start playback
            audioTrack.play();

            // Write the music buffer to the AudioTrack object
            while (playing) {
                audioTrack.write(music, 0, musicLength);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setFileName(File fileName) {
        this.fileName = fileName;
    }

    public File getFileName() {
        return fileName;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}