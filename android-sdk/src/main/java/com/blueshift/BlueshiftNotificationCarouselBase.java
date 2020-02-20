package com.blueshift;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RemoteViews;

import com.blueshift.rich_push.CarouselElement;
import com.blueshift.rich_push.CarouselElementText;
import com.blueshift.rich_push.Message;

abstract class BlueshiftNotificationCarouselBase extends BlueshiftNotification {
    private static final String TAG = "NotificationCarouselAnimated";

    BlueshiftNotificationCarouselBase(Context context, Message message) {
        super(context, message);
    }

    RemoteViews getOverlayViewWithText(CarouselElement element) {
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
