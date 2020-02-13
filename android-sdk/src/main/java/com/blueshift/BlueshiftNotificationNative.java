package com.blueshift;

import android.app.Notification;
import android.app.PendingIntent;
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
    private static final String TAG = "BlueshiftNotificationNative";

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
            if (mMessage != null) {
                Configuration config = BlueshiftUtils.getConfiguration(mContext);
                if (config != null) {
                    String channelId = NotificationUtils.getNotificationChannelId(mContext, mMessage);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);

                    // BASIC -> TODO: Check with @aswani
                    builder.setDefaults(Notification.DEFAULT_SOUND);
                    builder.setAutoCancel(true);
                    builder.setPriority(NotificationCompat.PRIORITY_MAX);

                    // MANDATORY
                    int smallIconResId = config.getSmallIconResId();
                    if (smallIconResId != 0) builder.setSmallIcon(smallIconResId);

                    int color = config.getNotificationColor();
                    if (color != 0) builder.setColor(color);

                    int bigIconResId = config.getLargeIconResId();
                    if (bigIconResId != 0) {
                        Bitmap bigIcon = BitmapFactory.decodeResource(mContext.getResources(), bigIconResId);
                        if (bigIcon != null) builder.setLargeIcon(bigIcon);
                    }

                    // CONTENT
                    addBasicContent(builder);

                    // STYLE
                    if (mMessage.hasImage()) {
                        enableBigPictureStyle(builder);
                    } else {
                        enableBigTextStyle(builder);
                    }

                    // ACTION
                    addActions(builder);

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

    private void addBasicContent(NotificationCompat.Builder builder) {
        try {
            builder.setContentTitle(mMessage.getContentTitle());
            builder.setContentText(mMessage.getContentText());

            if (!TextUtils.isEmpty(mMessage.getContentSubText())) {
                builder.setSubText(mMessage.getContentSubText());
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void enableBigPictureStyle(NotificationCompat.Builder builder) {
        try {
            URL imageURL = new URL(mMessage.getImageUrl());
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());
            if (bitmap != null) {
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                bigPictureStyle.bigPicture(bitmap);

                if (!TextUtils.isEmpty(mMessage.getBigContentTitle())) {
                    bigPictureStyle.setBigContentTitle(mMessage.getBigContentTitle());
                }

                if (!TextUtils.isEmpty(mMessage.getBigContentSummaryText())) {
                    bigPictureStyle.setSummaryText(mMessage.getBigContentSummaryText());
                }

                builder.setStyle(bigPictureStyle);
            }
        } catch (IOException e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void enableBigTextStyle(NotificationCompat.Builder builder) {
        try {
            // enable big text style
            boolean enableBigStyle = false;

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

            if (!TextUtils.isEmpty(mMessage.getBigContentTitle())) {
                enableBigStyle = true;
                bigTextStyle.setBigContentTitle(mMessage.getBigContentTitle());
            }

            if (!TextUtils.isEmpty(mMessage.getBigContentSummaryText())) {
                enableBigStyle = true;
                bigTextStyle.setSummaryText(mMessage.getBigContentSummaryText());
            }

            if (!TextUtils.isEmpty(mMessage.getContentText())) {
                bigTextStyle.bigText(mMessage.getContentText());
            }

            if (enableBigStyle) {
                builder.setStyle(bigTextStyle);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void addActions(NotificationCompat.Builder builder) {
        try {
            // pending intent that opens the app using MAIN activity.
            PendingIntent openAppPendingIntent = NotificationUtils.getOpenAppPendingIntent(mContext, mMessage, getNotificationId());

            switch (mMessage.getCategory()) {
                case Buy:
                    PendingIntent viewPendingIntent = NotificationUtils.getViewActionPendingIntent(mContext, mMessage, getNotificationId());
                    builder.addAction(0, "View", viewPendingIntent);

                    PendingIntent buyPendingIntent = NotificationUtils.getBuyActionPendingIntent(mContext, mMessage, getNotificationId());
                    builder.addAction(0, "Buy", buyPendingIntent);

                    builder.setContentIntent(openAppPendingIntent);

                    break;

                case ViewCart:
                    PendingIntent openCartPendingIntent = NotificationUtils.getOpenCartPendingIntent(mContext, mMessage, getNotificationId());
                    builder.addAction(0, "Open Cart", openCartPendingIntent);

                    builder.setContentIntent(openAppPendingIntent);

                    break;

                case Promotion:
                    PendingIntent openPromoPendingIntent = NotificationUtils.getOpenPromotionPendingIntent(mContext, mMessage, getNotificationId());
                    builder.setContentIntent(openPromoPendingIntent);

                    break;

                default:
                    /*
                     * Default action is to open app and send all details as extra inside intent
                     */
                    builder.setContentIntent(openAppPendingIntent);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }
}
