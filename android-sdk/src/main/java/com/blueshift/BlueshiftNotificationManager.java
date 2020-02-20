package com.blueshift;

import android.content.Context;
import android.os.Build;

import com.blueshift.rich_push.CustomNotificationFactory;
import com.blueshift.rich_push.Message;
import com.blueshift.rich_push.NotificationFactory;

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showNotificationPreAndroidOreo(context, message);
        } else {
            try {
                BlueshiftNotification notification = BlueshiftNotificationFactory.createNotification(context, message);
                boolean status = notification.show();
                BlueshiftLogger.d(LOG_TAG, "Notification " + (status ? "displayed." : "could not be displayed."));
            } catch (Exception e) {
                BlueshiftLogger.e(LOG_TAG, e);
            }
        }
    }

    public void updateNonAnimatedCarousel(Context context, Message message, int id, int index) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            updateNonAnimatedCarouselPreAndroidOreo(context, message, id, index);
        } else {
            try {
                BlueshiftNotification notification = BlueshiftNotificationFactory.getUpdatedNonAnimatedCarouselNotification(context, message, id, index);
                boolean status = notification.show();
                BlueshiftLogger.d(LOG_TAG, "Notification " + (status ? "updated." : "could not be updated."));
            } catch (Exception e) {
                BlueshiftLogger.e(LOG_TAG, e);
            }
        }
    }

    private void showNotificationPreAndroidOreo(Context context, Message message) {
        NotificationFactory.handleMessage(context, message);
    }

    private void updateNonAnimatedCarouselPreAndroidOreo(Context context, Message message, int id, int index) {
        CustomNotificationFactory.getInstance()
                .createAndShowCarousel(context, message, true, index, id);
    }
}
