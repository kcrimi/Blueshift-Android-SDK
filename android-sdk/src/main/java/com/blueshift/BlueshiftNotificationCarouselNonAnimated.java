package com.blueshift;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.blueshift.model.Configuration;
import com.blueshift.rich_push.CarouselElement;
import com.blueshift.rich_push.Message;
import com.blueshift.util.NotificationUtils;

class BlueshiftNotificationCarouselNonAnimated extends BlueshiftNotificationCarouselBase {
    private static final String TAG = "NotificationCarouselNonAnimated";

    private int id = NotificationUtils.getNotificationId();
    private boolean update = false;
    private int index = 0;

    private BlueshiftNotificationCarouselNonAnimated(Context context, Message message) {
        super(context, message);
    }

    static BlueshiftNotificationCarouselNonAnimated newInstance(Context context, Message message) {
        return new BlueshiftNotificationCarouselNonAnimated(context, message);
    }

    static BlueshiftNotificationCarouselNonAnimated newInstance(Context context, Message message, int id, boolean updating, int carouselIndex) {
        BlueshiftNotificationCarouselNonAnimated notification = new BlueshiftNotificationCarouselNonAnimated(context, message);
        notification.id = id;
        notification.update = updating;
        notification.index = carouselIndex;

        return notification;
    }

    @Override
    int getNotificationId() {
        return id;
    }

    @Override
    Notification getNotification() {
        try {
            NotificationCompat.Builder builder = getNotificationBuilder();
            if (builder != null) {
                // this is to avoid notifying user each time we change the carousel image
                builder.setOnlyAlertOnce(true);

                builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
                builder.setCustomBigContentView(getNotificationLayoutExpanded());

                builder.setDeleteIntent(NotificationUtils.getNotificationDeleteIntent(getContext(), getMessage()));

                return builder.build();
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return null;
    }

    private RemoteViews getNotificationLayoutExpanded() {
        try {
            RemoteViews root = new RemoteViews(getContext().getPackageName(), R.layout.notification_big_content);

            Message message = getMessage();
            if (message != null) {
                root.setTextViewText(R.id.carousel_content_title, message.getBigContentTitle());
                root.setTextViewText(R.id.carousel_content_line2, message.getBigContentSummaryText());
            }

            root.addView(R.id.carousel_content_root, getCarouselElement());

            return root;
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return null;
    }

    private RemoteViews getCarouselElement() {
        try {
            Message message = getMessage();
            Context context = getContext();
            if (message != null && message.getCarouselElements() != null
                    && message.getCarouselElements().length > index) {

                CarouselElement element = getMessage().getCarouselElements()[index];
                if (element != null) {
                    RemoteViews contentView = new RemoteViews(
                            context.getPackageName(), R.layout.notification_carousel_non_animated);

                    String imageFileName = NotificationUtils.getImageFileName(element.getImageUrl());
                    Bitmap bitmap = NotificationUtils.loadImageFromDisc(context, imageFileName);

                    // if the image is null and we are creating the notification for the first time,
                    // it means the cache is empty. so let's try downloading the images.
                    if (bitmap == null && !update) {
                        // image is unavailable. update the image cache.
                        NotificationUtils.downloadCarouselImages(context, message);

                        bitmap = NotificationUtils.loadImageFromDisc(context, imageFileName);
                        if (bitmap == null) {
                            BlueshiftLogger.e(TAG, "Could not load image for carousel.");
                        }
                    }

                    if (bitmap != null) {
                        contentView.setImageViewBitmap(R.id.big_picture, bitmap);
                    } else {
                        // show the app icon as place holder for corrupted image
                        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
                        if (configuration != null) {
                            contentView.setImageViewResource(R.id.big_picture, configuration.getAppIcon());
                        }
                    }

                    // remove any view found on the overlay container
                    contentView.removeAllViews(R.id.carousel_overlay_container);

                    // look for overlay content and add it in the container layout (if found)
                    RemoteViews overlay = getOverlayViewWithText(element);
                    if (overlay != null) {
                        contentView.addView(R.id.carousel_overlay_container, overlay);
                    }

                    contentView.setOnClickPendingIntent(
                            R.id.big_picture,
                            NotificationUtils.getCarouselImageClickPendingIntent(
                                    context, message, element, id)
                    );

                    contentView.setOnClickPendingIntent(
                            R.id.next_button,
                            NotificationUtils.getNavigationPendingIntent(
                                    context, message, message.getNextCarouselIndex(index), id)
                    );

                    contentView.setOnClickPendingIntent(
                            R.id.prev_button,
                            NotificationUtils.getNavigationPendingIntent(
                                    context, message, message.getPrevCarouselIndex(index), id)
                    );

                    return contentView;
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return null;
    }
}
