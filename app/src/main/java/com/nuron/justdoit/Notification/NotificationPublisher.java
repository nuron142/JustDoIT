package com.nuron.justdoit.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nuron.justdoit.R;

/**
 * Created by nuron on 06/12/15.
 */
public class NotificationPublisher extends BroadcastReceiver {

    public final static String NOTIFICATION_ID = "notificationId";
    public final static String NOTIFICATION_CONTENT_TITLE = "contentTitle";
    public final static String NOTIFICATION_CONTENT_TEXT = "contentText";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        String contentTitle = intent.getStringExtra(NOTIFICATION_CONTENT_TITLE);
        if (contentTitle != null) {
            builder.setContentTitle(contentTitle);
        } else {
            builder.setContentTitle("Just Do IT");
        }

        String contentText = intent.getStringExtra(NOTIFICATION_CONTENT_TEXT);
        if (contentText != null) {
            builder.setContentText(contentText);
        } else {
            builder.setContentText("Just Do IT");
        }

        builder.setSmallIcon(R.mipmap.ic_launcher);

        Intent dismissButtonIntent = new Intent(context, DismissNotification.class);
        dismissButtonIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 0);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0,
                dismissButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action.Builder dismissAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_clear_black_24dp, "Dismiss", dismissPendingIntent);

        NotificationCompat.Action.Builder snoozeAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_done_white_18dp, "Snooze", null);

        builder.addAction(dismissAction.build());
        builder.addAction(snoozeAction.build());

        PendingIntent emptyPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        builder.setContentIntent(emptyPendingIntent);

        builder.build();

        int id = intent.getIntExtra(NotificationPublisher.NOTIFICATION_ID, 0);
        Log.d("1", "NotificationPublisher ID : " + id);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
