package com.blueshift;

import android.app.Notification;
import android.content.Context;

import com.blueshift.rich_push.Message;

public class BlueshiftNotificationCarousel extends BlueshiftNotification {

    BlueshiftNotificationCarousel(Context context, Message message) {
        super(context, message);
    }

    @Override
    int getNotificationId() {
        return 0;
    }

    @Override
    Notification getNotification() {
        return null;
    }
}
