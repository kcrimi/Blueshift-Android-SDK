package com.blueshift;

import android.content.Context;

import com.blueshift.rich_push.Message;

class BlueshiftNotificationFactory {
    static BlueshiftNotification createNotification(Context context, Message message) {
        BlueshiftNotification blueshiftNotification = null;

        if (message.isNative()) {
            blueshiftNotification = BlueshiftNotificationNative.newInstance(context, message);
        } else if (message.isCarousel()) {
            blueshiftNotification = new BlueshiftNotificationCarousel(context, message);
        } else if (message.isAnimatedCarousel()) {
            blueshiftNotification = new BlueshiftNotificationCarouselAnimated(context, message);
        }

        return blueshiftNotification;
    }
}
