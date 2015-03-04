package com.example.administrator.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by WeiHuang on 3/3/2015.
 */
public class NoiseSleepRunable implements Runnable {
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 16000;
    static final short THRESHOLD = 4000;
    static final int NUM_OVER_THRESHOLD = 1000;
    static final int WINDOW_WIDTH = 5;//second
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetAudio = true;
    //AutomaticGainControl ACG;
    Object mLock;
    Context context;
    public  NoiseSleepRunable(Context ctxt){
        context=ctxt;
    }

    public void terminate(){
        isGetAudio=false;
    }
    @Override
    public void run(){
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        mLock = new Object();

        mAudioRecord.startRecording();
        short[] buffer = new short[BUFFER_SIZE];

        AudioWindow audioWindow = new AudioWindow(THRESHOLD, WINDOW_WIDTH, SAMPLE_RATE_IN_HZ);
        while (isGetAudio) {
            int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
            long approximateTime = System.currentTimeMillis();
            for (int i = 0; i < r; i++) {
                audioWindow.push(buffer[i], approximateTime);
            }
            Log.d("Num over threshold", Integer.toString(audioWindow.num_over_threshold));
            if (audioWindow.num_over_threshold > NUM_OVER_THRESHOLD && audioWindow.isFull()) {
                Intent localIntent = new Intent(Constants.NOISE_ALERT);
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                Log.d("Start Time", Long.toString(audioWindow.getTimeStamp()));
                isGetAudio = false;
            }
            synchronized (mLock) {
                try {
                    mLock.wait(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("report", "finished");
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;

    }
}
