package com.example.administrator.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WeiHuang on 3/6/2015.
 */
public class NoiseAwakeRunnable implements Runnable {
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 16000;
    static final short THRESHOLD = 4000;
    static final int WINDOW_WIDTH = 5;//second
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetAudio = true;
    boolean isOnRequest = false;
    //AutomaticGainControl ACG;
    Object mLock;
    Context context;
    List<long[]> recordList=new ArrayList<>();
    static final int LISTSIZE=(SAMPLE_RATE_IN_HZ/BUFFER_SIZE+1)*30;//30 seconds

    public  NoiseAwakeRunnable(Context ctxt){
        context=ctxt;
    }
    long requestTime=0;
    public void terminate(){
        isGetAudio=false;
    }
    public void requestRecord(long time){
        requestTime=time;
        isOnRequest=true;
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
            Log.d("Num over threshold", Long.toString(audioWindow.getNumOverThreshold()));
            Log.d("Average",Long.toString(audioWindow.getAverageAmplitude()));
            if (audioWindow.isFull()) {
                long startTime=audioWindow.getTimeStamp();
                long numOverThreshold=audioWindow.getNumOverThreshold();
                long average=audioWindow.getAverageAmplitude();
                long[] strs={startTime,numOverThreshold,average};
                recordList.add(strs);
                if(recordList.size()>LISTSIZE)
                    recordList.remove(0);
            }
            if(isOnRequest){
                long startTime=0;
                long numOverThreshold=0;
                long average=0;
                for(long[] strs:recordList){
                    if(Math.abs(startTime-requestTime)>Math.abs(strs[0]-requestTime)){
                        startTime=strs[0];
                        numOverThreshold=strs[1];
                        average=strs[2];
                    }
                }
                Log.d("Start time",Long.toString(startTime));
                Log.d("Num over Threshold",Long.toString(numOverThreshold));
                Log.d("Average",Long.toString(average));
                isOnRequest=false;
                isGetAudio=false;// Temporary Test
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
