/*
 * Copyright (C) 2014-2015 Denis Crowdy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.wit.mobileapp.pocketstudio;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.List;

/**
 * The main player class for Twotrack. It plays appropriate tracks as
 * needed by Twotrack - whether that's just track 1 (stereo), just track 2 (mono) 
 * or both mixed together
 * 
 * @author dcrowdy
 *
 */
public class WavPlayerService extends Service implements Runnable {
	private static final String APP_NAME = "WavPlayerService";
	public static final int IS_PLAYING = 3;
	public static final int IS_PAUSED = 2;
	public static final int IS_STOPPED = 1;
	public static final int END_FILE_REACHED = 4;

	private static final int BUFFER_SIZE = 1024 * 8;

	private Handler playBackPosHandler;
	private Thread play_thread;
    private File monoFile;
    private File stereoFile;
    private File file3;
    private File file4;
	private AudioTrack mAudioTrack;

	private String outputDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pocketStudio/";

	private int bufferSize;
	private float monoVolL = (float)0.8;  
	private float monoVolR = (float)0.8;
	private float stereoVolL = (float)0.8;
    private float stereoVolR = (float)0.8;
    private float file3VolL = (float)0.8;
    private float file3VolR = (float)0.8;
    private float file4VolL = (float)0.8;
    private float file4VolR = (float)0.8;


	private int frameOffset = 0;



	private int nudgeFrames = 0;
	private long size;  // Size of file in bytes


	private boolean playStereo;
	private boolean playMono;
	private boolean playMix;
	private boolean shouldMixDownSteroMono;
	private boolean stereoFileLonger;
	private boolean shouldPlay;

	private FileInputStream inStream;
	private byte[] inBytes;
    private int readBufferSize;
	private short[] outArray;

	private List<File> monoFilesToMix;

	private long playStartTime;

	private File outputFile;

	FileOutputStream fos = null;

	// Binder given to clients
	private final IBinder mBinder = new PlayLocalBinder();
    private File tempFile;
	public ProgressDialog progressDialog;
	private ImageView playPauseButton;
	private Drawable pvPlayDrawable;


	public void setHandler (Handler h) {
		playBackPosHandler = h;    	
	}

	public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
	}

    public void setTempFile(File tempFile) {
        this.tempFile = tempFile;
        try {
            this.fos = new FileOutputStream(this.tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public void setPlayButtonView(ImageView playPauseButton, Drawable pvPlayDrawable) {
		this.playPauseButton = playPauseButton;
		this.pvPlayDrawable = pvPlayDrawable;
	}

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class PlayLocalBinder extends Binder {
		public WavPlayerService getService() {
			// Return this instance of LocalService so clients can call public methods
			return WavPlayerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}


	/** 
	 * Prepares to play back two files, the first a mono file (track 2 in
	 * Twotrack) and the second a stereo file (track 1 in Twotrack).
	 * 
	 * @param file1
	 * @param file2
	 */
	public void setFilesToPlay(File file1, File file2) {
		//		FIXME just assuming file2 is mono and file2 stereo at the moment...
		//Log.i(APP_NAME, "In setFilesToPlay");
		monoFile = file2;
		stereoFile = file1;

		/*if (monoFile.length() * 2 > stereoFile.length()) {
			size = monoFile.length() * 2; // so the size of a stereo file of that length
			//Log.i(APP_NAME, "monoFile size " + size);
		}*/
        if (monoFile.length() > stereoFile.length()) {
            size = monoFile.length(); // so the size of a stereo file of that length
            //Log.i(APP_NAME, "monoFile size " + size);
        }
		else {
			size = stereoFile.length();
			//Log.i(APP_NAME, "stereoFile size " + size);
			stereoFileLonger = true;
		}

		playMix = true;
		playStereo = false;
		playMono = false;
		initialise();

	}

	public void setFilesToMix(File file1, File file2) {
		monoFile = file1;
		stereoFile = file2;

		Log.d(APP_NAME + "SF", "monofile: " + String.valueOf(monoFile.length()));
		Log.d(APP_NAME + "SF", "stereofile: " + String.valueOf(stereoFile.length()));

		if (monoFile.length()*2 > stereoFile.length()) {
			size = monoFile.length()*2; // so the size of a stereo file of that length
			//Log.i(APP_NAME, "monoFile size " + size);
		}
		else {
			size = stereoFile.length();
			//Log.i(APP_NAME, "stereoFile size " + size);
			stereoFileLonger = true;
		}

		shouldMixDownSteroMono = true;
		playMix = false;
		playStereo = false;
		playMono = false;
		initialise();
	}

	public void setFilesToMix(List<File> files) {

		monoFilesToMix = files;
		File mix1 = null;
		File mix2 = null;
		File finaloutput = null;
		Log.d(APP_NAME, "Starting...");
		try {
			/***
			 * We will have a max of 2 intermediary files and a final......
			 */
			mix1 = File.createTempFile("mix1", ".wav", new File(outputDir)); // file 1 and 2
			mix2 = File.createTempFile("mix2", ".wav", new File(outputDir)); // add file 3
			finaloutput = new File(outputDir + "mixed_final.wav"); // final mix
			File mixfile = null;

			if (files.size() <= 0) {
				return;
			}
			if (files.size() == 1) {
				this.setFileToPlay(files.remove(0));
				this.playAudio();
				return;
			}
			if (files.size() == 2) {
				this.setFilesToPlay(files.remove(1), files.remove(0));
				this.setOutputFile(finaloutput);
				this.playAudio();
			} else {
				for (int i = 0; i < files.size(); i++) {
					if (i + 1 >= files.size()) {
						//last file, go to output
						this.setOutputFile(finaloutput);
						this.setFilesToMix(files.get(i), mixfile);
					} else if (i == 0) {
						//first file, mix 1 and 2 as mono
						this.setFilesToPlay(files.get(i), files.get(i + 1));
						this.setOutputFile(mix1);
						mixfile = mix1;
					} else {
						// only happens on i = 2
						this.setFilesToMix(files.get(i + 1), mixfile);
						this.setOutputFile(mix2);
						mixfile = mix2;
					}
					shouldPlay = true;
					play_thread.start();
					synchronized (play_thread) {
						play_thread.join();
					}
				}
			}
			this.progressDialog.dismiss();
			this.setFileToPlay(finaloutput);
			this.playAudio();
			synchronized (play_thread) {
				play_thread.join();
			}
			this.playPauseButton.setBackground(pvPlayDrawable);
			/*this.setFilesToPlay(monoFilesToMix.get(0), monoFilesToMix.get(1));
			this.setOutputFile(mix1); //creates stereo mix1
			shouldPlay = true;
			Log.d(APP_NAME, "Starting mix 1 and 2");
			play_thread.start();
			synchronized (play_thread) {
				play_thread.join();
			}
			Log.d(APP_NAME, "finishd mix 1 and 2");

			shouldPlay = false;
			playMix = false;
			shouldMixDownSteroMono = true;
			this.setFilesToMix(monoFilesToMix.get(2), mix1); // mix down third track and mixed 1 and 2
			this.setOutputFile(mix2); //creates stereo mix2 of tracks 1 2 3
			shouldPlay = true;
			Log.d(APP_NAME, "Starting mix 12 and 3");
			play_thread.start();
			synchronized (play_thread) {
				play_thread.join();
			}
			Log.d(APP_NAME, "stopping mix 12 and 3");


			this.setFilesToMix(monoFilesToMix.get(3), mix2); // mix down fourth track and mixed 1 and 2 and 3
			this.setOutputFile(finaloutput);
			shouldPlay = true;
			Log.d(APP_NAME, "Starting final mix");
			play_thread.start();
			synchronized (play_thread) {
				play_thread.join();
			}*/
			Log.d(APP_NAME, "Completed");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Prepares to play back a single file. Takes either a mono file
	 * (so track 2 from Twotrack) or a stereo file (track 1 from Twotrack)
	 * and gets everything ready.
	 * 
	 * @param file
	 */
	public void setFileToPlay(File file) {
		//Log.i(APP_NAME, "numchannels is " + WavUtils.getNumChannels(file));
		if (WavUtils.getNumChannels(file) == 1) {
			//Log.i(APP_NAME, "in setFileToPlay and numChannels is 1");
			monoFile = file;
			playMono = true;
			playStereo = false;
			playMix = false;
			size = monoFile.length() * 2; 
		}
		if (WavUtils.getNumChannels(file) == 2) {
			//Log.i(APP_NAME, "in setFileToPlay and numChannels is 2");
			stereoFile = file;
			playStereo = true;
			playMono = false;
			playMix = false;
			size = stereoFile.length();
		}

		initialise();

	}

	private void initialise(){
		bufferSize = AudioTrack.getMinBufferSize(44100,
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);

		//bufferSize = bufferSize * 8;

		Log.i(APP_NAME, "Buffer size of wavmixplayer is " + bufferSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

		mAudioTrack.setPositionNotificationPeriod(44100/10); //FIXME should probably be adjustable?

		int frameSize = 4;

		/* if playback is paused mid-stream, then a frame offset is set, so we need to take that into account
		 * when calculating the marker for letting the UI know that playback has finished. Well, almost finished in this case, 
		 * as the marker is set about a fifth of a second before playback ends as it was the only way I could get this
		 * working reliably.
		 * 
		 * All of this should be byte-independent here because we deal with both mono and stereo files...
		 * 
		 */

		//int endFrame = (int) ((size/frameSize) - byteOffset/4 - (44100/10 * 2));
		int endFrame = (int) ((size/frameSize) - frameOffset - (44100/10 * 2));  //sending message 2 periods before very end - more reliable for some reason
		mAudioTrack.setNotificationMarkerPosition(endFrame);

		/*mAudioTrack.setPlaybackPositionUpdateListener( new AudioTrack.OnPlaybackPositionUpdateListener() {

			@Override
			public void onPeriodicNotification(AudioTrack track) {
				//Log.i(APP_NAME,"sending out a message");

				//Message msg = playBackPosHandler.obtainMessage();
				// Below, shouldn't this be size / 2 for mono and size / 4 for stereo (so size * frameSize)?
				//msg.arg1 = (int)(mAudioTrack.getPlaybackHeadPosition()/(double)(size/frameSize));	// Isn't size in bytes and getPlaybackHeadPos in frames?	
				//playBackPosHandler.sendMessage(msg);
				//}
			}

			@Override
			public void onMarkerReached(AudioTrack track) {
				//Log.i(APP_NAME,"sending out a message from WavPlayer - marker reached");
				// TODO Auto-generated method stub

				//Message msg = playBackPosHandler.obtainMessage();
				//msg.what = 	END_FILE_REACHED;
				//msg.arg1 = 1;
				//playBackPosHandler.sendMessage(msg);
				//Log.i(APP_NAME, "Message was actually sent");

			}
		}, playBackPosHandler);*/

		// Stream preparation code taken from beginning of playFile to try and improve latency issue:
		if (playMono) {
			//Log.i(APP_NAME, "About to prepare for mono playback");
			try {
				inStream = new FileInputStream(monoFile);
				//BufferedInputStream inBuffer = new BufferedInputStream(inStream, bufferSize/2);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				inStream.skip(frameOffset * 2 + 44);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inBytes = new byte[bufferSize/2];
			readBufferSize = bufferSize/2;
		}
		else if (playStereo) {
			//Log.i(APP_NAME, "About to prepare for stereo playback");
			try {
				inStream = new FileInputStream(stereoFile);
				//BufferedInputStream inBuffer = new BufferedInputStream(inStream, bufferSize);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				inStream.skip(frameOffset * 4 + 44); // skip the header plus the number of bytes in the frame offset
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inBytes = new byte[bufferSize];
			readBufferSize = bufferSize;
		}

		outArray = new short[bufferSize/2];

		play_thread = new Thread(this);
		//Log.i(APP_NAME, "Play thread priority is " + play_thread.getPriority());
		play_thread.setPriority(Thread.MAX_PRIORITY);
		//Log.i(APP_NAME, "Play thread priority is now " + play_thread.getPriority());
	}

	public void playAudio() {
		shouldPlay = true;
		play_thread.start();
	}

	public void stopPlaying(){
		if (mAudioTrack != null) {
			shouldPlay = false;
			mAudioTrack.stop();
			//mAudioTrack.release();

		}
	}

	public void setStereoVolume(Float LVol, float RVol) {
		//		if (mAudioTrack != null) {
		//			mAudioTrack.setStereoVolume(LVol, RVol);
		//		}
		stereoVolL = LVol;
		//Log.i(APP_NAME, "Stereo vol is " + stereoVolL);
		stereoVolR = RVol;
	}

	public void setMonoVolume(Float LVol, float RVol) {
		monoVolL = LVol;
		monoVolR = RVol;
	}



	/**
	 * Allows for playback to occur from a certain number of frames
	 * in to the wav file
	 * 
	 * @param offset
	 */
	public void setFrameOffset(int offset){
		frameOffset = offset;		

	}

	public void setNudgeFrames(int nudgeFrames) {
		this.nudgeFrames = nudgeFrames;
	}

	@Override
	public void run() {
		//playStartTime = System.currentTimeMillis();
		//Log.i(APP_NAME, "Sys time is " + playStartTime);
		if (playMix && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
			mixAndPlay();
		}

		if (shouldMixDownSteroMono && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
			mixDownSteroMono();
		}

		if ((playStereo || playMono) && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED){
			playFile();
		}
		//Log.i(APP_NAME, "AudioTrack playState is " + mAudioTrack.getPlayState());
		//Log.i(APP_NAME, "AudioTrack State is " + mAudioTrack.getState());

		//Log.i(APP_NAME, "About to stop and release mAudioTrack");
		mAudioTrack.stop();

	}

	/**
	 * The working method for playback of a single file
	 * 
	 */
	private void playFile() {

		@SuppressWarnings("unused")
		int numBytesRead = 0;

		short sample;
		//short sampleR;

		int outL;
		int outR;

		//short stereoSampleL;
		//short stereoSampleR;
		int channels = 1;
		if (playStereo) channels =2;

		mAudioTrack.play();
		playStartTime = System.currentTimeMillis();
		//inShortsBuff = ByteBuffer.wrap(inBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		//Log.i(APP_NAME, "in playFile and Sys time is " + playStartTime);
		try {
			while ((numBytesRead = inStream.read(inBytes, 0, readBufferSize)) != -1) {

				// wrap this buffer as a shortbuffer
                ShortBuffer inShortsBuff = ByteBuffer.wrap(inBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
				int i, n;

				if (playStereo) {
					for (i=0, n = inShortsBuff.remaining(); i < (n/channels); i++) {

						//outL = inShortsBuff.get();
						outL = (int) (inShortsBuff.get() * stereoVolL);
						//Log.i(APP_NAME, "In run and stereo vol is " + stereoVolL);
						//outR = inShortsBuff.get();
						outR = (int) (inShortsBuff.get() * stereoVolR);

						outArray[i * 2] = (short)outL;
						outArray[i * 2 + 1] = (short)outR;
						//Log.i(APP_NAME, "n , i " + n + i);
					}
				}

				if (playMono) {
					for (i=0, n = inShortsBuff.remaining(); i < (n/channels); i++) {

						// pull shorts for mixing

						sample = inShortsBuff.get();
						outL = (int) (sample * monoVolL);
						outR = (int) (sample * monoVolR);

						outArray[i * 2] = (short)outL;
						outArray[i * 2 + 1] = (short)outR;
						//Log.i(APP_NAME, "n , i " + n + i);
					}
				}
				//Log.i(APP_NAME, "play file...");

				// write to the AudioTrack
				//ByteBuffer.wrap(outByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outArray);
				if (shouldPlay) {
					mAudioTrack.write(outArray, 0, outArray.length);
				} else {break;}  // so shouldPlay is false and we need to break from the loop

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * The working method for mixing two files and playing them back
	 * 
	 */
	private void mixAndPlay() {

		// This doesn't have to be fast or predictable in terms of latency as recording is not happening with this

		FileInputStream monoStream = null;
        FileInputStream stereoStream = null;
		int monoByteSkip = 0;
        int stereoByteSkip = 0;

		byte[] mono;
		byte[] stereo;
		ShortBuffer monoShortsBuff;
        ShortBuffer stereoShortsBuff;

		try {
			monoStream = new FileInputStream(monoFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			stereoStream = new FileInputStream(stereoFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        /*try {
            stream3 = new FileInputStream(file3);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            stream4 = new FileInputStream(file4);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

		/*if (nudgeFrames < 0) {    // delay mono track
            monoByteSkip = Math.abs(frameOffset * 2 + nudgeFrames * 2 + 44);
			// FIXME shouldn't it be frameoffset * 2 + Math.abs(nudgeFrames) * 2 + 44; ??
            stereoByteSkip = frameOffset * 4 + 44;
        }
        if (nudgeFrames >= 0) {    // delay stereo track
            monoByteSkip = frameOffset * 2 + 44;
            stereoByteSkip = frameOffset * 4 + nudgeFrames * 4 + 44;
        }*/

		monoByteSkip = frameOffset * 2 + 44;

		// If the user is seeking into the audio, skip the appropriate number of frames
		try {
			if (monoStream != null) {
				monoStream.skip(monoByteSkip); // 2 bytes per mono frame + header
			}
			if (stereoStream != null) {
				stereoStream.skip(monoByteSkip); // 4 bytes per stereo frame
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		mono = new byte[bufferSize/2];
		stereo = new byte[bufferSize/2];
		// Loop through mono stream
		int monoBytesRead = 0;
		int stereoBytesRead = 0;
		byte[] outByteArray = new byte[bufferSize];
		short[] outArray;
		//short[] stereoOut;
		short monoSample;
		short stereoSampleL;
		short stereoSampleR;
		outArray = new short[bufferSize/2];
		//stereoOut = new short[bufferSize];
        if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            mAudioTrack.play();
       }
		playStartTime = System.currentTimeMillis();
		//Log.i(APP_NAME, "in mixAndPlay and Sys time is " + playStartTime);
		//monoShortsBuff = ByteBuffer.wrap(mono).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		//stereoShortsBuff = ByteBuffer.wrap(stereo).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		int totalBytes = 0;
		//Log.d("PLAYER", "stereo file length be: " + String.valueOf(stereoFile.length()));
		try {
			BufferedOutputStream out = new BufferedOutputStream(this.fos, bufferSize);
            if (monoStream != null)
                while ((monoBytesRead = monoStream.read(mono, 0, bufferSize / 2)) != -1) {
					totalBytes += monoBytesRead;
					//Log.i("PLAYER", "we r starting a lope " + String.valueOf(monoBytesRead));
					//Log.i("PLAYER", "total bytes:  " + String.valueOf(totalBytes));

                    // wrap this buffer as a shortbuffer
                    monoShortsBuff = ByteBuffer.wrap(mono).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                    // read from the stereo file
                    if (stereoStream != null && totalBytes < stereoFile.length()) {
                        stereoBytesRead = stereoStream.read(stereo, 0, bufferSize / 2);
                    } else {
						//Log.i("PLAYER", "thisshould happen sometimes");
					}
                    // wrap as shortbuffer

                    stereoShortsBuff = ByteBuffer.wrap(stereo).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                    int i, n;

                    for (i = 0, n = monoShortsBuff.remaining(); i < n; i++) {   //WTF?

                        // pull shorts for mixing
                        monoSample = monoShortsBuff.get();
						if ( totalBytes < stereoFile.length()) {
							stereoSampleL = stereoShortsBuff.get();
						} else {
							stereoSampleL = 0;
						}
                        //stereoSampleR = stereoShortsBuff.get();
                        // mix for the Audiotrack
                        int outL = (int) ((monoSample * monoVolL) + (stereoSampleL * stereoVolL));
                        int outR = (int) ((monoSample * monoVolR) + (stereoSampleL * stereoVolR));
						//Log.i(APP_NAME, "In mixAndPlay and stereo sample is " + String.valueOf(stereoSampleL));
						//Log.i(APP_NAME, "In mixAndPlay and mono sample is " + String.valueOf(monoSample));
                        // put back into a buffer for output to the AudioTrack
                        outArray[i * 2] = (short) outL;
                        outArray[i * 2 + 1] = (short) outR;

                    }
                    //Log.i(APP_NAME, "mix and play...");
                    ByteBuffer.wrap(outByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outArray);

                    // write to the AudioTrack
                    if (shouldPlay) {
                        //mAudioTrack.write(outArray, 0, outArray.length);
						out.write(outByteArray, 0, outByteArray.length);
                    }
                    else {
                        break;
                    }

                }

            if (shouldPlay && stereoFileLonger) {
				Log.d("PLAYER", "POTATO2ssss");
                if (stereoStream != null) {
                    while ((stereoBytesRead = stereoStream.read(stereo, 0, bufferSize/2)) != -1) {

                        stereoShortsBuff = ByteBuffer.wrap(stereo).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

                        int i, n;
                        for (i = 0, n = stereoShortsBuff.remaining(); i < n/2; i++) {

                            stereoSampleL = stereoShortsBuff.get();
                            //stereoSampleR = stereoShortsBuff.get();

                            int  outL = (int)(stereoSampleL * stereoVolL);
                            int  outR = (int)(stereoSampleL * stereoVolR);

                            outArray[i * 2] = (short)outL;
                            outArray[i * 2 + 1] = (short)outR;
                            //Log.i(APP_NAME, "n is " + n + "and i is " + i);
                        }
						ByteBuffer.wrap(outByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outArray);
                        if (shouldPlay) {
                            //mAudioTrack.write(outArray, 0, outArray.length);
							out.write(outByteArray, 0, outByteArray.length);
                        } else {break;}
                    }
                }

            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		copyWaveFile(tempFile, outputFile);

	}

	public int getPosition() {
		/* Returns the current playback position in the file in frames
		 * 
		 */
		if (mAudioTrack != null) {
			// We might have started somewhere other than the beginning
			// but need to know overall position relative to the file, hence framOffset:
			int overallpos = mAudioTrack.getPlaybackHeadPosition() + frameOffset;
			//Log.i(APP_NAME, "Overall pos from player is " + overallpos);
			//return  (myAT.getPlaybackHeadPosition() + getFrameOffset());  
			return  (overallpos);  
		}
		return 0;

	}

	/*private int getFrameOffset(){
		/* Converts the current byteOffset value into a frame length
	 * 
	 *
		int frameOffset;
		frameOffset = byteOffset/4;
		return frameOffset;
	}*/

	public int getStatus(){
		// method to see whether we are playing or not 
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				//Log.i(APP_NAME,"about to return 3 which is playing");
				return IS_PLAYING;	
			}
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
				//Log.i(APP_NAME,"about to return 2 which is paused");
				return IS_PAUSED;	
			}
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
				//Log.i(APP_NAME,"about to return 1 which is stopped");
				return IS_STOPPED;	
			}		

			else {
				//Log.i(APP_NAME,"about to return 0");
				return 0;
			}
		}
		//Log.i(APP_NAME,"mAudioTrack is null...");
		return -1;
	}

	public long getLength(){
		// Returns the length in stereo frames of the current material being played
		//int length;

		return (size/4); // 4 bytes per frame (2 bytes per channel)

	}


	public long getPlayStartTime() {
		return playStartTime;
	}


	public int getBufferSize() {
		return bufferSize;
	}

	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = 16;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	private void copyWaveFile(File inFile,File outFile){
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = 44100;
		int channels = 2;
		long byteRate = 16 * 44100 * channels/8;
		//	int bufferSize = 1024 * 512;
		int bufferSize = 1024 * 16;

		byte[] data = new byte[bufferSize];
		//ByteBuffer dataBuffer = ByteBuffer.wrap(array);

		try {
			in = new FileInputStream(inFile);
			out = new FileOutputStream(outFile);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			//in.skip(latencyAdjust);
			while(in.read(data) != -1){
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void mixDownSteroMono() {

		// This doesn't have to be fast or predictable in terms of latency as recording is not happening with this

		FileInputStream monoStream = null;
		FileInputStream stereoStream = null;
		int monoByteSkip = 0;
		int stereoByteSkip = 0;

		byte[] mono;
		byte[] stereo;
		ShortBuffer monoShortsBuff;
		ShortBuffer stereoShortsBuff;

		try {
			monoStream = new FileInputStream(monoFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			stereoStream = new FileInputStream(stereoFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nudgeFrames = 0;
		if (nudgeFrames < 0) {    // delay mono track
			monoByteSkip = Math.abs(frameOffset * 2 + nudgeFrames * 2 + 44);
			// FIXME shouldn't it be frameoffset * 2 + Math.abs(nudgeFrames) * 2 + 44; ??
			stereoByteSkip = frameOffset * 4 + 44;
		}
		if (nudgeFrames >= 0) {    // delay stereo track
			monoByteSkip = frameOffset * 2 + 44;
			stereoByteSkip = frameOffset * 4 + nudgeFrames * 4 + 44;
		}


		// If the user is seeking into the audio, skip the appropriate number of frames
		try {
			if (monoStream != null) {
				monoStream.skip(monoByteSkip); // 2 bytes per mono frame + header
			}
			if (stereoStream != null) {
				stereoStream.skip(stereoByteSkip); // 4 bytes per stereo frame
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		mono = new byte[bufferSize/2];
		stereo = new byte[bufferSize];
		// Loop through mono stream
		int monoBytesRead = 0;
		int stereoBytesRead = 0;
		byte[] outByteArray = new byte[bufferSize];
		short[] outArray;
		//short[] stereoOut;
		short monoSample;
		short stereoSampleL;
		short stereoSampleR;
		outArray = new short[bufferSize/2];
		//stereoOut = new short[bufferSize];
		if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
			mAudioTrack.play();
		}
		playStartTime = System.currentTimeMillis();
		//Log.i(APP_NAME, "in mixAndPlay and Sys time is " + playStartTime);
		//monoShortsBuff = ByteBuffer.wrap(mono).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		//stereoShortsBuff = ByteBuffer.wrap(stereo).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		int totalBytes = 0;
		try {
			this.fos = new FileOutputStream(tempFile);
			BufferedOutputStream out = new BufferedOutputStream(this.fos, bufferSize);
			if (monoStream != null)
				while ((monoBytesRead = monoStream.read(mono, 0, bufferSize / 2)) != -1) {
					totalBytes += monoBytesRead;
					//Log.d(APP_NAME, "totalbytes: " + totalBytes);
					// wrap this buffer as a shortbuffer
					monoShortsBuff = ByteBuffer.wrap(mono).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
					// read from the stereo file
					if (stereoStream != null && totalBytes * 2 < stereoFile.length()) {
						stereoBytesRead = stereoStream.read(stereo, 0, bufferSize);
					}
					// wrap as shortbuffer

					stereoShortsBuff = ByteBuffer.wrap(stereo).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
					int i, n;

					for (i = 0, n = monoShortsBuff.remaining(); i < n; i++) {   //WTF?

						// pull shorts for mixing
						monoSample = monoShortsBuff.get();

						if ( totalBytes * 2 < stereoFile.length()) {
							//Log.d(APP_NAME, "setting stereosample to something");
							stereoSampleL = stereoShortsBuff.get();
							stereoSampleR = stereoShortsBuff.get();
						} else {
							//Log.d(APP_NAME, "setting stereosample to 0");
							stereoSampleL = 0;
							stereoSampleR = 0;
						}
						// mix for the Audiotrack
						int outL = (int) ((monoSample * monoVolL) + (stereoSampleL * stereoVolL));
						int outR = (int) ((monoSample * monoVolR) + (stereoSampleR * stereoVolR));
						//Log.i(APP_NAME, "In mixAndPlay and stereo vol is " + stereoVolL);
						// put back into a buffer for output to the AudioTrack
						outArray[i * 2] = (short) outL;
						outArray[i * 2 + 1] = (short) outR;

					}
					//Log.i(APP_NAME, "mix and play...");
					//ByteBuffer.wrap(outByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outArray);

					// write to the AudioTrack
					ByteBuffer.wrap(outByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outArray);
					if (shouldPlay) {
						//mAudioTrack.write(outArray, 0, outArray.length);
						out.write(outByteArray, 0, outByteArray.length);
					}
					else {
						break;
					}

				}

			if (shouldPlay && stereoFileLonger) {
				Log.d(APP_NAME, "MIXDOWN Stereo is longer");
				if (stereoStream != null) {
					while ((stereoBytesRead = stereoStream.read(stereo, 0, bufferSize)) != -1) {

						stereoShortsBuff = ByteBuffer.wrap(stereo).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

						int i, n;
						for (i = 0, n = stereoShortsBuff.remaining(); i < n/2; i++) {

							stereoSampleL = stereoShortsBuff.get();
							stereoSampleR = stereoShortsBuff.get();

							int  outL = (int)(stereoSampleL * stereoVolL);
							int  outR = (int)(stereoSampleR * stereoVolR);

							outArray[i * 2] = (short)outL;
							outArray[i * 2 + 1] = (short)outR;
							//Log.i(APP_NAME, "n is " + n + "and i is " + i);
						}
						ByteBuffer.wrap(outByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(outArray);
						if (shouldPlay) {
							//mAudioTrack.write(outArray, 0, outArray.length);
							out.write(outByteArray, 0, outByteArray.length);
						}else {break;}
					}
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		copyWaveFile(tempFile, outputFile);

	}

}
