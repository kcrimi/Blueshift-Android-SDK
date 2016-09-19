package com.blueshift.rich_push;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.blueshift.Blueshift;
import com.blueshift.R;
import com.blueshift.model.Configuration;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rahul on 16/9/16.
 */
public class CustomNotificationFactory {

    private static final String LOG_TAG = "NotificationFactory";

    private static CustomNotificationFactory sInstance;

    private CustomNotificationFactory() {
        // do nothing for now.
    }

    public static CustomNotificationFactory getInstance() {
        if (sInstance == null) {
            sInstance = new CustomNotificationFactory();
        }

        return sInstance;
    }

    public void createAndShowAnimatedCarousel(Context context, Message message) {
        NotificationCompat.Builder builder = createBasicNotification(context, message);
        if (builder != null) {
            Notification notification = builder.build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification.bigContentView = createAnimatedCarousal(context, message);
            }

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(500, notification);
        }
    }

    public void createAndShowCarousel(Context context, Message message) {

    }

    private NotificationCompat.Builder createBasicNotification(Context context, Message message) {
        NotificationCompat.Builder builder = null;

        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null) {
            builder = new NotificationCompat.Builder(context);
            // set basic items
            builder.setSmallIcon(configuration.getAppIcon());
            builder.setContentTitle(message.getTitle());
            builder.setContentText(message.getBody());

            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_basic_layout);
            builder.setContent(contentView);

            String notificationTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));

            contentView.setImageViewResource(R.id.notification_icon, configuration.getAppIcon());
            contentView.setTextViewText(R.id.notification_content_title, message.getTitle());
            contentView.setTextViewText(R.id.notification_content_text, message.getBody());
            contentView.setTextViewText(R.id.notification_time, notificationTime);
        }

        return builder;
    }

    private RemoteViews createAnimatedCarousal(Context context, Message message) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.carousel_layout);
        remoteViews.setViewVisibility(R.id.animated_carousel_view, View.VISIBLE);

        int index = 0;

        CarouselElement[] elements = message.getCarouselElements();
        for (CarouselElement element : elements) {
            try {
                int resId = getImageViewResId(index++);
                if (resId != -1) {
                    URL imageURL = new URL(element.getImageUrl());
                    Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());

                    remoteViews.setImageViewBitmap(resId, bitmap);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not download image" + e.getMessage());
            }
        }

        return remoteViews;
    }

    private int getImageViewResId(int index) {
        switch (index) {
            case 0:
                return R.id.carousel_picture1;

            case 1:
                return R.id.carousel_picture2;

            case 2:
                return R.id.carousel_picture3;

            case 3:
                return R.id.carousel_picture4;
        }

        return -1;
    }
}
