package com.nuron.justdoit.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by nuron on 07/12/15.
 */
public class NotificationRestart extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        Log.d("1", "Notification ID : " + id);
        notificationManager.notify(id, notification);

//        Intent service = new Intent(context, AlarmService.class);
//        service.setAction(AlarmService.CREATE);
//        context.startService(service);
    }
}