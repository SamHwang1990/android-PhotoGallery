package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by sam on 16/8/27.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private final static String TAG = "Notification Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

        int requestCode = intent.getIntExtra(PollService.EXTRA_REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(PollService.EXTRA_NOTIFICATION);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, notification);

    }
}
