package com.blueshift;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.blueshift.rich_push.CarouselElement;
import com.blueshift.rich_push.Message;
import com.blueshift.util.NotificationUtils;

import java.io.IOException;
import java.net.URL;

class BlueshiftNotificationCarouselAnimated extends BlueshiftNotificationCarouselBase {
    private static final String TAG = "NotificationCarouselAnimated";

    private int id = NotificationUtils.getNotificationId();

    BlueshiftNotificationCarouselAnimated(Context context, Message message) {
        super(context, message);
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
            RemoteViews root = new RemoteViews(getContext().getPackageName(), R.layout.carousel_big_content);

            Message message = getMessage();
            if (message != null) {
                root.setTextViewText(R.id.carousel_content_title, message.getBigContentTitle());
                root.setTextViewText(R.id.carousel_content_line2, message.getBigContentSummaryText());
            }

            root.addView(R.id.carousel_content_root, getViewFlipper());

            return root;
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return null;
    }

    private RemoteViews getViewFlipper() {
        try {
            // create/inflate ViewFlipper layout.
            String pkgName = getContext().getPackageName();

            RemoteViews viewFlipper = new RemoteViews(pkgName, R.layout.carousel_view_flipper);

            // loop through the carousel elements and add the image into ViewFlipper.
            CarouselElement[] elements = getMessage().getCarouselElements();
            if (elements != null) {
                for (CarouselElement element : elements) {
                    try {
                        // Load image using remote URL.
                        URL imageURL = new URL(element.getImageUrl());
                        Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());

                        // Set the image into the view.
                        RemoteViews viewFlipperEntry = new RemoteViews(pkgName, R.layout.carousel_view_flipper_entry);
                        viewFlipperEntry.setImageViewBitmap(R.id.carousel_image_view, bitmap);

                        // Attach an onClick pending intent.
                        viewFlipperEntry.setOnClickPendingIntent(
                                R.id.carousel_image_view,
                                NotificationUtils.getCarouselImageClickPendingIntent(
                                        getContext(), getMessage(), element, getNotificationId())
                        );

                        // remove any view found on the overlay container
                        viewFlipperEntry.removeAllViews(R.id.carousel_overlay_container);

                        // look for overlay content and add it in the container layout (if found)
                        RemoteViews overlay = getOverlayViewWithText(element);
                        if (overlay != null) {
                            viewFlipperEntry.addView(R.id.carousel_overlay_container, overlay);
                        }

                        // Add the image into ViewFlipper.
                        viewFlipper.addView(R.id.view_flipper, viewFlipperEntry);
                    } catch (IOException e) {
                        BlueshiftLogger.e(TAG, e);
                    }
                }
            }

            return viewFlipper;
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }

        return null;
    }
}
