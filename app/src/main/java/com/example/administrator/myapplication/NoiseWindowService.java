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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WeiHuang on 2/28/2015.
 */
public class NoiseWindowService extends IntentService {
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 16000;
    static final short THRESHOLD=4000;
    static final int NUM_OVER_THRESHOLD=1000;
    static final int WINDOW_WIDTH=5;//second
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    //AutomaticGainControl ACG;
    Object mLock;

    public NoiseWindowService(){
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
        boolean isGetAudio=true;
        AudioWindow audioWindow=new AudioWindow(THRESHOLD,WINDOW_WIDTH,SAMPLE_RATE_IN_HZ);
        while (isGetAudio) {
            int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
            long approximateTime=System.currentTimeMillis();
            for (int i = 0; i < r; i++) {
                audioWindow.push(buffer[i],approximateTime);
            }
            Log.d("Num over threshold",Integer.toString(audioWindow.num_over_threshold));
            if(audioWindow.num_over_threshold>NUM_OVER_THRESHOLD && audioWindow.isFull()){
                Intent localIntent = new Intent(Constants.NOISE_ALERT);
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                Log.d("Start Time",Long.toString(audioWindow.getTimeStamp()));
                isGetAudio=false;
            }
            synchronized (mLock) {
                try {
                    mLock.wait(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("report","finished");
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;

        short[] pcmdata=new short[audioWindow.num_of_nodes];
        audioWindow.setIterator();
        int idx_pcmdata=0;
        while (audioWindow.hasNext()){
            pcmdata[idx_pcmdata++]=audioWindow.getNext();
        }
        try {
            PCMtoFile(pcmdata,SAMPLE_RATE_IN_HZ);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String[]> listStr=new ArrayList<>();
        audioWindow.setIterator();
        while (audioWindow.hasNext()){
            String[] str={Short.toString(audioWindow.getNext())};
            listStr.add(str);
        }
        try {
            exportToCSV(listStr);
        } catch (IOException e){
            Log.d("exportToCSV","fail");
        }
    }

    public byte[] get16BitPcm(short[] samples) {
        byte[] generatedSound = new byte[2 * samples.length];
        int index = 0;
        for (short sample : samples) {
            generatedSound[index++] = (byte) (sample & 0x00ff);
            generatedSound[index++] = (byte) ((sample & 0xff00) >> 8);
        }
        return generatedSound;
    }

    public void PCMtoFile(short[] pcmdata, int srate) throws IOException {
        File pathfile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "csvData");
        if (!pathfile.isDirectory()) {
            pathfile.mkdir();
        }
        FileOutputStream os=new FileOutputStream(pathfile+File.separator+"results.wav");
        byte[] data = get16BitPcm(pcmdata);
        WaveHeader header = new WaveHeader(data.length,srate);
        byte[] waveHeaderBytes = header.getHeader();

        os.write(waveHeaderBytes);
        os.write(data);
        os.close();
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
