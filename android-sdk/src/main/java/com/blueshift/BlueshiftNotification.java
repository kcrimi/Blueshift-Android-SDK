package com.blueshift;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.blueshift.rich_push.Message;
import com.blueshift.util.NotificationUtils;

abstract class BlueshiftNotification {
    private static final String TAG = "BlueshiftNotification";

    Context mContext;
    Message mMessage;

    BlueshiftNotification(Context context, Message message) {
        this.mContext = context;
        this.mMessage = message;
    }

    boolean show() {
        boolean status = false;

        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(mContext);
            createChannel(manager);
            manager.notify(getNotificationId(), getNotification());
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return status;
    }

    private void createChannel(NotificationManagerCompat manager) {
        try {
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel =
                            NotificationUtils.createNotificationChannel(mContext, mMessage);
                    if (channel != null) {
                        manager.createNotificationChannel(channel);
                    }
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    abstract int getNotificationId();

    abstract Notification getNotification();
}
