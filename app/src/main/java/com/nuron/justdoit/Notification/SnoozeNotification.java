package com.nuron.justdoit.Notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by nuron on 08/12/15.
 */
public class SnoozeNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int delay = 20 * 100;

        int notificationId = intent.getIntExtra(NotificationPublisher.NOTIFICATION_ID, 0);
        Log.d("1", "SnoozeNotification ID : " + notificationId);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CONTENT_TITLE,
                intent.getStringExtra(NotificationPublisher.NOTIFICATION_CONTENT_TITLE));

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CONTENT_TEXT,
                intent.getStringExtra(NotificationPublisher.NOTIFICATION_CONTENT_TEXT));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }
}
