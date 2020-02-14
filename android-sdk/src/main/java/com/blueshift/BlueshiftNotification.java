package com.blueshift;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blueshift.model.Configuration;
import com.blueshift.rich_push.Message;
import com.blueshift.util.BlueshiftUtils;
import com.blueshift.util.NotificationUtils;

abstract class BlueshiftNotification {
    private static final String TAG = "BlueshiftNotification";

    private Context mContext;
    private Message mMessage;
    private NotificationCompat.Builder mBuilder;

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

    public Context getContext() {
        return mContext;
    }

    public Message getMessage() {
        return mMessage;
    }

    private void initNotificationBuilder() {
        try {
            String channelId = NotificationUtils.getNotificationChannelId(mContext, mMessage);

            mBuilder = new NotificationCompat.Builder(mContext, channelId);

            addBasicConfig(mBuilder);
            addMandatoryContent(mBuilder);
            addBasicContent(mBuilder);
            addActions(mBuilder);
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    NotificationCompat.Builder getNotificationBuilder() {
        if (mBuilder == null) {
            initNotificationBuilder();
        }

        return mBuilder;
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

    private void addBasicConfig(NotificationCompat.Builder builder) {
        if (builder != null) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
            builder.setAutoCancel(true);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }
    }

    private void addMandatoryContent(NotificationCompat.Builder builder) {
        try {
            Configuration config = BlueshiftUtils.getConfiguration(mContext);
            if (config != null) {
                int smallIconResId = config.getSmallIconResId();
                if (smallIconResId != 0) builder.setSmallIcon(smallIconResId);

                int color = config.getNotificationColor();
                if (color != 0) builder.setColor(color);

                int bigIconResId = config.getLargeIconResId();
                if (bigIconResId != 0) {
                    Bitmap bigIcon = BitmapFactory.decodeResource(mContext.getResources(), bigIconResId);
                    if (bigIcon != null) builder.setLargeIcon(bigIcon);
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
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

    abstract int getNotificationId();

    abstract Notification getNotification();
}
