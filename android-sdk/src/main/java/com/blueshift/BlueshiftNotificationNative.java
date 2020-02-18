package com.blueshift;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.blueshift.model.Configuration;
import com.blueshift.rich_push.Message;
import com.blueshift.util.BlueshiftUtils;
import com.blueshift.util.NotificationUtils;

import java.io.IOException;
import java.net.URL;

class BlueshiftNotificationNative extends BlueshiftNotification {
    private static final String TAG = "NotificationNative";

    private int id = NotificationUtils.getNotificationId();

    private BlueshiftNotificationNative(Context context, Message message) {
        super(context, message);
    }

    static BlueshiftNotificationNative newInstance(Context context, Message message) {
        return new BlueshiftNotificationNative(context, message);
    }

    @Override
    int getNotificationId() {
        return id;
    }

    @Override
    Notification getNotification() {
        try {
            // build the notification here
            if (getMessage() != null) {
                Configuration config = BlueshiftUtils.getConfiguration(getContext());
                if (config != null) {
                    NotificationCompat.Builder builder = getNotificationBuilder();

                    // STYLE
                    if (getMessage().hasImage()) {
                        addBigPictureStyle(builder);
                    } else {
                        addBigTextStyle(builder);
                    }

                    return builder.build();
                } else {
                    BlueshiftLogger.d(TAG, "No configuration found. Skipping notification.");
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return null;
    }

    private void addBigPictureStyle(NotificationCompat.Builder builder) {
        try {
            URL imageURL = new URL(getMessage().getImageUrl());
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());
            if (bitmap != null) {
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                bigPictureStyle.bigPicture(bitmap);

                if (!TextUtils.isEmpty(getMessage().getBigContentTitle())) {
                    bigPictureStyle.setBigContentTitle(getMessage().getBigContentTitle());
                }

                if (!TextUtils.isEmpty(getMessage().getBigContentSummaryText())) {
                    bigPictureStyle.setSummaryText(getMessage().getBigContentSummaryText());
                }

                builder.setStyle(bigPictureStyle);
            }
        } catch (IOException e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void addBigTextStyle(NotificationCompat.Builder builder) {
        try {
            // enable big text style
            boolean enableBigStyle = false;

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

            if (!TextUtils.isEmpty(getMessage().getBigContentTitle())) {
                enableBigStyle = true;
                bigTextStyle.setBigContentTitle(getMessage().getBigContentTitle());
            }

            if (!TextUtils.isEmpty(getMessage().getBigContentSummaryText())) {
                enableBigStyle = true;
                bigTextStyle.setSummaryText(getMessage().getBigContentSummaryText());
            }

            if (!TextUtils.isEmpty(getMessage().getContentText())) {
                bigTextStyle.bigText(getMessage().getContentText());
            }

            if (enableBigStyle) {
                builder.setStyle(bigTextStyle);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }
}
