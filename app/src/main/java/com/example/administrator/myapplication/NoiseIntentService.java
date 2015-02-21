package com.example.administrator.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WeiHuang on 2/21/2015.
 */
public class NoiseIntentService extends IntentService {
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    //AutomaticGainControl ACG;
    Object mLock;

    public NoiseIntentService(){
        super("NoiseIntentService");
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        mLock = new Object();
        //ACG=AutomaticGainControl.create(mAudioRecord.getAudioSessionId());
        //ACG.setEnabled(false);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        mAudioRecord.startRecording();
        short[] buffer = new short[BUFFER_SIZE];

        List<String[]> listStr=new ArrayList<String[]>();
        long startTime=System.currentTimeMillis();
        while (System.currentTimeMillis()-startTime<10000) {
            int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
            long v = 0;
            for (int i = 0; i < buffer.length; i++) {
                String[] strings={Double.toString(buffer[i])};
                listStr.add(strings);
                v += buffer[i] * buffer[i];
            }
            double mean = v / (double) r;
            double volume = 10 * Math.log10(mean);

//            String[] strings={Double.toString(volume)};
//            listStr.add(strings);
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
        }
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        try {
            exportToCSV(listStr);
        } catch (IOException e){
            Log.d("exportToCSV","fail");
        }
    }

    public void exportToCSV(final List<String[]> lst) throws IOException {

        File pathfile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "csvData");
        if (!pathfile.isDirectory()) {
            pathfile.mkdir();
        }

        File file = new File(pathfile,
                File.separator + "csvDataFile.csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final boolean csvThreadRunning=true;
        final CSVWriter writer = new CSVWriter(new FileWriter(file));
        new Thread() {
            public void run() {
                if (csvThreadRunning) {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            //String[] df = {"1", "2", "3"};
                            for(String[] strs : lst)
                                writer.writeNext(strs);
                            writer.close();


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

    }
}
