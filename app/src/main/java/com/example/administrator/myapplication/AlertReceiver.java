package com.example.administrator.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2015/2/14 0014.
 */
public class AlertReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("Receiver", "Alert!!!!!!!!!!!");

        String svcName=Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager;
        notificationManager = (NotificationManager) context.getSystemService(svcName);

        int icon=R.drawable.abc_ic_menu_paste_mtrl_am_alpha;
        String tickerText="Noise Alert";
        long when=System.currentTimeMillis();
        Notification.Builder builder=new Notification.Builder(context);
        builder.setSmallIcon(icon)
               .setTicker(tickerText)
               .setContentText("Your roommate is sleeping")
               .setContentTitle("Noise Alert")
               .setWhen(when);
        Notification notification=builder.build();

        int NOTIFICATION_REF=1;
        notificationManager.notify(NOTIFICATION_REF,notification);
    }
}
