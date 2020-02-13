package com.blueshift;

import android.content.Context;

import com.blueshift.rich_push.Message;

public class BlueshiftNotificationManager {
    private static final String LOG_TAG = "BlueshiftNotificationManager";

    private static BlueshiftNotificationManager sInstance = null;

    public static BlueshiftNotificationManager getInstance() {
        if (sInstance == null) {
            sInstance = new BlueshiftNotificationManager();
        }

        return sInstance;
    }

    public void showNotification(Context context, Message message) {
        try {
            BlueshiftNotification notification = BlueshiftNotificationFactory.createNotification(context, message);
            boolean status = notification.show();
            BlueshiftLogger.d(LOG_TAG, "Notification " + (status ? "displayed." : "could not be displayed."));
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }
    }
}
