package com.example.administrator.myapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent mServiceIntent = new Intent(this, NoiseWindowService.class);
//        startService(mServiceIntent);
//
//        NoiseSleepRunable runnable=new NoiseSleepRunable(this.getApplicationContext());
//        Thread thread=new Thread(runnable);
//        thread.start();
        NoiseAwakeRunnable runnable=new NoiseAwakeRunnable(this.getApplicationContext());
        Thread thread=new Thread(runnable);
        thread.start();

        IntentFilter alterIntentFilter=new IntentFilter(Constants.NOISE_ALERT);
        AlertReceiver alertReceiver=new AlertReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(alertReceiver,alterIntentFilter);
        try {
                Thread.sleep(20000);
                runnable.requestRecord(System.currentTimeMillis()-7000);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        TimerBackGroundTask backGroundTask = new TimerBackGroundTask();
//        backGroundTask.execute(mServiceIntent);

    }

//    private class TimerBackGroundTask extends AsyncTask <Intent,Void, String>{
//
//        @Override
//        protected String doInBackground(Intent... params) {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            startService(params[0]);
//            Log.d("dddddddddd","dddddddddddd");
//
//            return null;
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
