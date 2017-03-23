package com.coste.syncorg.gui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

import com.coste.syncorg.MainActivity;
import com.coste.syncorg.R;

public class SynchronizerNotificationCompat {
    private NotificationManager notificationManager;
    private Notification notification;
    private int notifyRef = 1;
    private Context context;
    private RemoteViews contentView;

    public SynchronizerNotificationCompat(Context context) {
        this.context = context;
    }

    public void errorNotification(String errorMsg) {
        this.notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, 0);

        Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("Synchronization failed");

        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.sync_notification);

        remoteViews.setImageViewResource(R.id.status_icon,
                R.drawable.icon);
        remoteViews.setTextViewText(R.id.status_text, errorMsg);
        remoteViews.setProgressBar(R.id.status_progress, 100, 100,
                false);

        builder.setCustomBigContentView(remoteViews);
        notificationManager.notify(notifyRef, builder.build());
    }

    public void setupNotification() {
        this.notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, 0);

        Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.icon);
        builder.setOngoing(true);
        builder.setContentTitle("Started synchronization");
        builder.setContentText("Started synchronization");

        contentView = new RemoteViews(context.getPackageName(),
                R.layout.sync_notification);

        contentView.setImageViewResource(R.id.status_icon,
                R.drawable.icon);
        contentView.setTextViewText(R.id.status_text,
                context.getString(R.string.sync_synchronizing_changes));
        contentView.setProgressBar(R.id.status_progress, 100, 0,
                true);
        builder.setCustomContentView(contentView);

        notificationManager.notify(notifyRef, builder.build());
    }


    public void updateNotification(int progress) {
        updateNotification(progress, null);
    }

    public void updateNotification(int progress, String message) {
        if (notification == null)
            return;

        if (message != null)
            notification.contentView.setTextViewText(R.id.status_text, message);

        notification.contentView.setProgressBar(R.id.status_progress, 100,
                progress, false);
        notificationManager.notify(notifyRef, notification);
    }

    public void finalizeNotification() {
        notificationManager.cancel(notifyRef);
    }

}
