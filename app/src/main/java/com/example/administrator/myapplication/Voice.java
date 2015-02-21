package com.example.administrator.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


/**
 * Created by Administrator on 2015/2/14 0014.
 */
public class Voice extends IntentService {
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    //AutomaticGainControl ACG;
    Object mLock;
    boolean isGetVoiceRun;
    public Voice(){
        super("Voice");
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        mLock = new Object();
        //ACG=AutomaticGainControl.create(mAudioRecord.getAudioSessionId());
        //ACG.setEnabled(false);
        isGetVoiceRun = true;
    }
    @Override
    protected void onHandleIntent(Intent workIntent){
        mAudioRecord.startRecording();
        short[] buffer = new short[BUFFER_SIZE];

        while (isGetVoiceRun) {
            int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
            long v = 0;
            for (int i = 0; i < buffer.length; i++) {
                v += buffer[i] * buffer[i];
            }
            double mean = v / (double) r;
            double volume = 10 * Math.log10(mean);
            if(volume>60) {
                Intent localIntent = new Intent(Constants.NOISE_ALERT);
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            }
            Log.d(TAG, "Db:" + volume);
            synchronized (mLock) {
                try {
                    mLock.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isGetVoiceRun = workIntent.getBooleanExtra("runCommand",true);
        }
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
    }


}