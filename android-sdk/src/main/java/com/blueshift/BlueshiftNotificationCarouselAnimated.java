package com.blueshift;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.blueshift.rich_push.CarouselElement;
import com.blueshift.rich_push.CarouselElementText;
import com.blueshift.rich_push.Message;
import com.blueshift.util.NotificationUtils;

import java.io.IOException;
import java.net.URL;

class BlueshiftNotificationCarouselAnimated extends BlueshiftNotification {
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
                        RemoteViews overlay = getOverlayView(element);
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

    private RemoteViews getOverlayView(CarouselElement element) {
        if (element != null) {
            RemoteViews container = new RemoteViews(getContext().getPackageName(), R.layout.carousel_big_content_overlay);

            CarouselElementText text = element.getContentText();
            CarouselElementText subtext = element.getContentSubtext();

            if (text != null && subtext != null) {
                // text
                setRemoteViewsTextViewText(container, R.id.carousel_overlay_text, text.getText());
                setRemoteViewsTextViewColor(container, R.id.carousel_overlay_text, text.getTextColor());
                setRemoteViewsTextViewSize(container, R.id.carousel_overlay_text, text.getTextSize());
                setRemoteViewsBackgroundColor(container, R.id.carousel_overlay_text_parent, text.getTextBackgroundColor());

                // subtext
                setRemoteViewsTextViewText(container, R.id.carousel_overlay_subtext, subtext.getText());
                setRemoteViewsTextViewColor(container, R.id.carousel_overlay_subtext, subtext.getTextColor());
                setRemoteViewsTextViewSize(container, R.id.carousel_overlay_subtext, subtext.getTextSize());
                setRemoteViewsBackgroundColor(container, R.id.carousel_overlay_subtext_parent, subtext.getTextBackgroundColor());

                // positioning
                String position = element.getContentLayoutType();
                if (!TextUtils.isEmpty(position)) {
                    int root;
                    int children;

                    switch (position) {
                        case "top_left":
                            root = Gravity.TOP;
                            children = Gravity.START;
                            break;

                        case "top_center":
                            root = Gravity.TOP;
                            children = Gravity.CENTER;
                            break;

                        case "top_right":
                            root = Gravity.TOP;
                            children = Gravity.END;
                            break;

                        case "center_left":
                            root = Gravity.CENTER;
                            children = Gravity.START;
                            break;

                        case "center_right":
                            root = Gravity.CENTER;
                            children = Gravity.END;
                            break;

                        case "bottom_left":
                            root = Gravity.BOTTOM;
                            children = Gravity.START;
                            break;

                        case "bottom_center":
                            root = Gravity.BOTTOM;
                            children = Gravity.CENTER;
                            break;

                        case "bottom_right":
                            root = Gravity.BOTTOM;
                            children = Gravity.END;
                            break;

                        default:
                            root = Gravity.CENTER;
                            children = Gravity.CENTER;
                    }

                    setRemoteViewsGravity(container, R.id.carousel_overlay_root, root);
                    setRemoteViewsGravity(container, R.id.carousel_overlay_text_parent, children);
                    setRemoteViewsGravity(container, R.id.carousel_overlay_subtext_parent, children);
                }
            }

            return container;
        }

        return null;
    }

    private void setRemoteViewsBackgroundColor(RemoteViews container, int id, String colorStr) {
        try {
            if (container != null && id != 0 && !TextUtils.isEmpty(colorStr)) {
                int color = Color.parseColor(colorStr);
                container.setInt(id, "setBackgroundColor", color);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void setRemoteViewsTextViewText(RemoteViews container, int id, String text) {
        try {
            if (container != null && id != 0 && !TextUtils.isEmpty(text)) {
                container.setTextViewText(id, text);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void setRemoteViewsTextViewColor(RemoteViews container, int id, String colorStr) {
        try {
            if (container != null && id != 0 && !TextUtils.isEmpty(colorStr)) {
                int color = Color.parseColor(colorStr);
                container.setTextColor(id, color);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void setRemoteViewsTextViewSize(RemoteViews container, int id, int size) {
        try {
            if (container != null && id != 0 && size > 11) { // recommended min value is 11
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    container.setTextViewTextSize(size, TypedValue.COMPLEX_UNIT_SP, size);
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }

    private void setRemoteViewsGravity(RemoteViews container, int id, int gravity) {
        try {
            if (container != null && id != 0) {
                container.setInt(id, "setGravity", gravity);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(TAG, e);
        }
    }
}
