package com.blueshift.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blueshift.BlueshiftExecutor;
import com.blueshift.BlueshiftLogger;
import com.blueshift.inappmessage.InAppConstants;
import com.blueshift.inappmessage.InAppMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class InAppUtils {

    private static final String LOG_TAG = "InAppUtils";

    public static String readSamplePayload(Context context, String fileName) {
        String result = null;

        if (context != null && !TextUtils.isEmpty(fileName)) {
            BufferedReader reader = null;
            try {
                StringBuilder stringBuilder = new StringBuilder();
                InputStreamReader inputStreamReader = new InputStreamReader(context.getAssets().open(fileName));
                reader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                result = stringBuilder.toString();
            } catch (IOException e) {
                BlueshiftLogger.e(LOG_TAG, e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        BlueshiftLogger.e(LOG_TAG, e);
                    }
                }
            }
        }

        return result;

    }

    public static String getStringFromJSONObject(JSONObject jsonObject, String key) {
        try {
            if (jsonObject != null && !TextUtils.isEmpty(key)) {
                return jsonObject.optString(key);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static int getIntFromJSONObject(JSONObject jsonObject, String key, int fallback) {
        try {
            if (jsonObject != null && !TextUtils.isEmpty(key)) {
                return jsonObject.optInt(key, fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    public static double getDoubleFromJSONObject(JSONObject jsonObject, String key, double fallback) {
        try {
            if (jsonObject != null && !TextUtils.isEmpty(key)) {
                return jsonObject.optDouble(key, fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    public static Rect getRectFromJSONObject(JSONObject jsonObject, String key) {
        try {
            if (jsonObject != null && !TextUtils.isEmpty(key)) {
                String json = jsonObject.optString(key);
                return new Gson().fromJson(json, Rect.class);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static boolean validateColorString(String colorString) {
        try {
            int len = colorString != null ? colorString.length() : 0;
            return len > 0 && colorString.startsWith("#") && (len == 4 || len == 7 || len == 9);
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return false;
    }

    private static boolean isDarkModeEnabled(Context context) {
        boolean isDarkModeEnabled = false;

        if (context != null) {
            try {
                int flag = context.getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;

                isDarkModeEnabled = Configuration.UI_MODE_NIGHT_YES == flag;
            } catch (Exception e) {
                BlueshiftLogger.e(LOG_TAG, e);
            }
        }

        return isDarkModeEnabled;
    }

    private static JSONObject getContentStyle(Context context, InAppMessage inAppMessage, String contentName) {
        if (inAppMessage != null) {
            try {
                if (isDarkModeEnabled(context) && contentName != null) {
                    /*
                    * This check is to safely fallback to the default config if the key
                    * is absent in dark theme config.
                    */
                    JSONObject darkThemeConfig = inAppMessage.getContentStyleDark();
                    if (darkThemeConfig != null && darkThemeConfig.has(contentName)) {
                        return darkThemeConfig;
                    }
                }
            } catch (Exception e) {
                BlueshiftLogger.e(LOG_TAG, e);
            }

            return inAppMessage.getContentStyle();
        }

        return null;
    }

    private static JSONObject getTemplateStyle(Context context, InAppMessage inAppMessage, String contentName) {
        if (inAppMessage != null) {
            try {
                if (isDarkModeEnabled(context) && contentName != null) {
                    /*
                     * This check is to safely fallback to the default config if the key
                     * is absent in dark theme config.
                     */
                    JSONObject darkThemeConfig = inAppMessage.getTemplateStyleDark();
                    if (darkThemeConfig != null && darkThemeConfig.has(contentName)) {
                        return darkThemeConfig;
                    }
                }
            } catch (Exception e) {
                BlueshiftLogger.e(LOG_TAG, e);
            }

            return inAppMessage.getTemplateStyle();
        }

        return null;
    }

    public static String getContentString(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null) {
                JSONObject contentStyle = getContentStyle(context, inAppMessage, contentName);
                String stringValue = getStringFromJSONObject(contentStyle, contentName);
                return TextUtils.isEmpty(stringValue) ? null : stringValue;
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static int getContentInt(Context context, InAppMessage inAppMessage, String contentName, int fallback) {
        try {
            if (inAppMessage != null && contentName != null) {
                JSONObject contentStyle = getContentStyle(context, inAppMessage, contentName);
                return getIntFromJSONObject(contentStyle, contentName, fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    public static Rect getContentRect(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                JSONObject contentStyle = getContentStyle(context, inAppMessage, contentName);
                return getRectFromJSONObject(contentStyle, contentName);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static String getTemplateString(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                JSONObject templateStyle = getTemplateStyle(context, inAppMessage, contentName);
                String stringValue = getStringFromJSONObject(templateStyle, contentName);
                return TextUtils.isEmpty(stringValue) ? null : stringValue;
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static int getTemplateInt(Context context, InAppMessage inAppMessage, String contentName, int fallback) {
        try {
            if (inAppMessage != null && contentName != null) {
                JSONObject templateStyle = getTemplateStyle(context, inAppMessage, contentName);
                return getIntFromJSONObject(templateStyle, contentName, fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    public static double getTemplateDouble(Context context, InAppMessage inAppMessage, String contentName, double fallback) {
        try {
            if (inAppMessage != null) {
                JSONObject templateStyle = getTemplateStyle(context, inAppMessage, contentName);
                return getDoubleFromJSONObject(templateStyle, contentName, fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    public static double getTemplateBackgroundDimAmount(Context context, InAppMessage inAppMessage, double fallback) {
        return getTemplateDouble(context, inAppMessage, InAppConstants.BACKGROUND_DIM_AMOUNT, fallback);
    }

    public static int getContentOrientation(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            int orientation = getContentInt(context, inAppMessage, InAppConstants.ORIENTATION(contentName), LinearLayout.HORIZONTAL);
            if (orientation == LinearLayout.HORIZONTAL || orientation == LinearLayout.VERTICAL) {
                return orientation;
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return LinearLayout.HORIZONTAL;
    }

    public static String getContentColor(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                return getContentString(context, inAppMessage, InAppConstants.COLOR(contentName));
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static String getContentBackgroundColor(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                return getContentString(context, inAppMessage, InAppConstants.BACKGROUND_COLOR(contentName));
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static int getContentBackgroundRadius(Context context, InAppMessage inAppMessage, String contentName, int fallback) {
        try {
            if (inAppMessage != null && contentName != null) {
                return getContentInt(context, inAppMessage, InAppConstants.BACKGROUND_RADIUS(contentName), fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    public static GradientDrawable getContentBackgroundDrawable(Context context, InAppMessage inAppMessage, String contentName) {
        GradientDrawable shape = new GradientDrawable();
        try {
            String colorVal = getContentBackgroundColor(context, inAppMessage, contentName);
            if (validateColorString(colorVal)) {
                int color = Color.parseColor(colorVal);
                shape.setColor(color);
            }

            int radius = getContentBackgroundRadius(context, inAppMessage, contentName, 0);
            if (radius != 0) {
                shape.setCornerRadius(radius);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return shape;
    }

    public static int getContentSize(Context context, InAppMessage inAppMessage, String contentName, int fallback) {
        try {
            if (inAppMessage != null && contentName != null) {
                return getContentInt(context, inAppMessage, InAppConstants.SIZE(contentName), fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return fallback;
    }

    private static int parseGravityString(String gravityString) {
        int gravity = Gravity.CENTER;

        if (gravityString != null) {
            switch (gravityString) {
                case "start":
                    gravity = Gravity.START;
                    break;

                case "end":
                    gravity = Gravity.END;
                    break;

                case "top":
                    gravity = Gravity.TOP;
                    break;

                case "bottom":
                    gravity = Gravity.BOTTOM;
                    break;

                case "center":
                    gravity = Gravity.CENTER;
                    break;
            }
        }

        return gravity;
    }

    public static int getContentGravity(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                String gravity = getContentString(context, inAppMessage, InAppConstants.GRAVITY(contentName));
                return parseGravityString(gravity);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return Gravity.START;
    }

    public static int getContentLayoutGravity(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                String gravity = getContentString(context, inAppMessage, InAppConstants.LAYOUT_GRAVITY(contentName));
                return parseGravityString(gravity);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return Gravity.START;
    }

    public static Rect getContentPadding(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                return getContentRect(context, inAppMessage, InAppConstants.PADDING(contentName));
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static Rect getContentMargin(Context context, InAppMessage inAppMessage, String contentName) {
        try {
            if (inAppMessage != null && contentName != null) {
                return getContentRect(context, inAppMessage, InAppConstants.MARGIN(contentName));
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return null;
    }

    public static boolean isHeightSet(Context context, InAppMessage inAppMessage) {
        boolean isSet = false;

        try {
            int height = getTemplateInt(context, inAppMessage, InAppConstants.HEIGHT, -1);
            isSet = height > 0;
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return isSet;
    }

    public static boolean isTemplateFullScreen(Context context, InAppMessage inAppMessage) {
        boolean isFullscreen = false;

        try {
            int width = getTemplateInt(context, inAppMessage, InAppConstants.WIDTH, -1);
            int height = getTemplateInt(context, inAppMessage, InAppConstants.HEIGHT, -1);
            isFullscreen = (width == 100) && (height == 100);
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return isFullscreen;
    }

    public static int getTemplateGravity(Context context, InAppMessage inAppMessage) {
        String position = getTemplateString(context, inAppMessage, InAppConstants.POSITION);
        return parseGravityString(position);
    }

    public static void loadImageAsync(final ImageView imageView, final String path) {
        if (imageView != null && path != null) {
            final Context context = imageView.getContext();
            BlueshiftExecutor.getInstance().runOnDiskIOThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bitmap bitmap = loadFromDisk(context, path);
                                if (bitmap == null) {
                                    bitmap = loadFromNetwork(path);
                                }

                                if (bitmap != null) {
                                    final Bitmap finalBitmap = bitmap;
                                    BlueshiftExecutor.getInstance().runOnMainThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        imageView.setImageBitmap(finalBitmap);
                                                    } catch (Exception e) {
                                                        BlueshiftLogger.e(LOG_TAG, e);
                                                    }
                                                }
                                            }
                                    );
                                } else {
                                    BlueshiftLogger.e(LOG_TAG, "Could not load the image from " + path);
                                }
                            } catch (Exception e) {
                                BlueshiftLogger.e(LOG_TAG, e);
                            }
                        }
                    }
            );
        }
    }

    private static Bitmap loadFromDisk(Context context, String url) {
        Bitmap bitmap = null;

        try {
            File imgFile = getCachedImageFile(context, url);
            if (imgFile != null && imgFile.exists()) {
                try {
                    bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                } catch (Exception e) {
                    BlueshiftLogger.e(LOG_TAG, e);
                }

                if (bitmap != null) {
                    Log.d(LOG_TAG, "Using cached image. " + imgFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return bitmap;
    }

    private static Bitmap loadFromNetwork(String url) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            if (!TextUtils.isEmpty(url)) {
                inputStream = new URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (bitmap != null) {
                    Log.d(LOG_TAG, "Using remote image. " + url);
                }
            }
        } catch (Exception e) {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                BlueshiftLogger.e(LOG_TAG, e);
            }

            BlueshiftLogger.e(LOG_TAG, e);
        }

        return bitmap;
    }

    public static JSONObject getActionFromName(InAppMessage inAppMessage, String actionName) {
        JSONObject action = null;

        try {
            if (inAppMessage != null && inAppMessage.getActionsJSONArray() != null
                    && !TextUtils.isEmpty(actionName)) {
                JSONArray actions = inAppMessage.getActionsJSONArray();
                for (int i = 0; i < actions.length(); i++) {
                    try {
                        JSONObject object = actions.getJSONObject(i);
                        if (actionName.equals(object.optString(InAppConstants.ACTION_TYPE))) {
                            return object;
                        }
                    } catch (Exception e) {
                        BlueshiftLogger.e(LOG_TAG, e);
                    }
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return action;
    }

    public static Rect getActionRect(InAppMessage inAppMessage, String actionName, String argName) {
        Rect value = null;

        try {
            JSONObject action = getActionFromName(inAppMessage, actionName);
            if (action != null) {
                String json = action.optString(argName);
                value = new Gson().fromJson(json, Rect.class);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return value;
    }

    public static String getActionString(InAppMessage inAppMessage, String actionName, String argName) {
        String value = null;

        try {
            JSONObject action = getActionFromName(inAppMessage, actionName);
            if (action != null) {
                value = action.optString(argName);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return value;
    }

    public static int getActionInt(InAppMessage inAppMessage, String actionName, String argName, int fallback) {
        int value = fallback;

        try {
            JSONObject action = getActionFromName(inAppMessage, actionName);
            if (action != null) {
                value = action.optInt(argName, fallback);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return value;
    }

    public static String getActionText(InAppMessage inAppMessage, String actionName) {
        return getActionString(inAppMessage, actionName, InAppConstants.TEXT);
    }

    public static Button getActionButtonDefault(Context context) {
        if (context != null) {
            Button button = new Button(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TypedValue typedValue = new TypedValue();
                boolean isResolved = context
                        .getTheme()
                        .resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);

                if (isResolved) {
                    button.setForeground(ContextCompat.getDrawable(context, typedValue.resourceId));
                }
            }

            button.setAllCaps(false);

            Drawable bg = getActionBackgroundDrawableDefault();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                button.setBackground(bg);
            } else {
                button.setBackgroundDrawable(bg);
            }

            button.setTextColor(Color.WHITE);

            return button;
        }

        return null;
    }

    public static Drawable getActionBackgroundDrawableDefault() {
        GradientDrawable shape = new GradientDrawable();

        try {
            // blue
            shape.setColor(Color.parseColor("#2196f3"));
            shape.setCornerRadius(3);
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return shape;
    }

    public static Drawable getActionBackgroundDrawable(JSONObject actionJson) {
        GradientDrawable shape = new GradientDrawable();

        try {
            int bgColor = getActionBackgroundColor(actionJson);
            if (bgColor != 0) {
                shape.setColor(bgColor);
            }

            int bgRadius = getActionBackgroundRadius(actionJson);
            shape.setCornerRadius(bgRadius);
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return shape;
    }

    public static int getActionBackgroundColor(JSONObject actionJson) {
        int color = 0;

        try {
            String colorStr = getStringFromJSONObject(actionJson, InAppConstants.BACKGROUND_COLOR);
            if (validateColorString(colorStr)) {
                return Color.parseColor(colorStr);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return color;
    }

    public static int getActionBackgroundRadius(JSONObject actionJson) {
        return getIntFromJSONObject(actionJson, InAppConstants.BACKGROUND_RADIUS, 0);
    }

    public static Rect getActionMargin(InAppMessage inAppMessage, String actionName) {
        Rect margins = getActionRect(inAppMessage, actionName, InAppConstants.MARGIN);
        return margins != null ? margins : new Rect(0, 0, 0, 0);
    }

    public static Rect getActionPadding(InAppMessage inAppMessage, String actionName) {
        Rect padding = getActionRect(inAppMessage, actionName, InAppConstants.PADDING);
        return padding != null ? padding : new Rect(0, 0, 0, 0);
    }

    public static int getActionOrientation(Context context, InAppMessage inAppMessage) {
        return getContentOrientation(context, inAppMessage, InAppConstants.ACTIONS);
    }

    public static void setContentImageView(ImageView imageView, InAppMessage inAppMessage, String contentName) {
        if (imageView != null && inAppMessage != null && !TextUtils.isEmpty(contentName)) {
            Context context = imageView.getContext();

            // IMAGE
            InAppUtils.loadImageAsync(imageView, inAppMessage.getContentString(contentName));

            // BACKGROUND
            Drawable background = InAppUtils.getContentBackgroundDrawable(context, inAppMessage, contentName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView.setBackground(background);
            } else {
                imageView.setBackgroundDrawable(background);
            }

            // PADDING (DEF: 4dp)
            Rect padding = InAppUtils.getContentPadding(context, inAppMessage, contentName);
            if (padding != null) {
                imageView.setPadding(
                        CommonUtils.dpToPx(padding.left, context),
                        CommonUtils.dpToPx(padding.top, context),
                        CommonUtils.dpToPx(padding.right, context),
                        CommonUtils.dpToPx(padding.bottom, context)
                );
            }
        }
    }

    public static void setContentTextView(TextView textView, InAppMessage inAppMessage, String contentName) {
        if (textView != null && inAppMessage != null && !TextUtils.isEmpty(contentName)) {
            Context context = textView.getContext();

            // TEXT
            textView.setText(inAppMessage.getContentString(contentName));

            // TEXT COLOR
            String colorHashCode = InAppUtils.getContentColor(context, inAppMessage, contentName);
            if (InAppUtils.validateColorString(colorHashCode)) {
                try {
                    textView.setTextColor(Color.parseColor(colorHashCode));
                } catch (Exception e) {
                    BlueshiftLogger.e(LOG_TAG, e);
                }
            }

            // TEXT SIZE
            int val = InAppUtils.getContentSize(context, inAppMessage, contentName, 14);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, val);

            // TEXT GRAVITY (DEF: CENTER)
            int contentGravity = InAppUtils.getContentGravity(context, inAppMessage, contentName);
            textView.setGravity(contentGravity);

            // BACKGROUND
            Drawable background = InAppUtils.getContentBackgroundDrawable(context, inAppMessage, contentName);
            if (background != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    textView.setBackground(background);
                } else {
                    textView.setBackgroundDrawable(background);
                }
            }

            // PADDING (DEF: 4dp)
            Rect padding = InAppUtils.getContentPadding(context, inAppMessage, contentName);
            if (padding != null) {
                textView.setPadding(
                        CommonUtils.dpToPx(padding.left, context),
                        CommonUtils.dpToPx(padding.top, context),
                        CommonUtils.dpToPx(padding.right, context),
                        CommonUtils.dpToPx(padding.bottom, context)
                );
            }
        }
    }

    public static void setActionTextView(TextView textView, JSONObject actionJson) {
        if (textView != null && actionJson != null) {
            // TEXT
            textView.setText(InAppUtils.getStringFromJSONObject(actionJson, InAppConstants.TEXT));

            // TEXT COLOR
            String colorHashCode = InAppUtils.getStringFromJSONObject(actionJson, InAppConstants.COLOR(InAppConstants.TEXT));
            if (InAppUtils.validateColorString(colorHashCode)) {
                try {
                    textView.setTextColor(Color.parseColor(colorHashCode));
                } catch (Exception e) {
                    BlueshiftLogger.e(LOG_TAG, e);
                }
            }

            // TEXT SIZE
            int val = InAppUtils.getIntFromJSONObject(actionJson, InAppConstants.SIZE(InAppConstants.TEXT), 14);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, val);

            // TEXT GRAVITY (DEF: CENTER)
            int contentGravity = InAppUtils.getIntFromJSONObject(actionJson, InAppConstants.GRAVITY(InAppConstants.TEXT), Gravity.CENTER);
            textView.setGravity(contentGravity);

            // BACKGROUND
            Drawable background = InAppUtils.getActionBackgroundDrawable(actionJson);
            if (background != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    textView.setBackground(background);
                } else {
                    textView.setBackgroundDrawable(background);
                }
            }

            // PADDING
            Rect padding = InAppUtils.getRectFromJSONObject(actionJson, InAppConstants.PADDING);
            if (padding != null) {
                Context context = textView.getContext();
                textView.setPadding(
                        CommonUtils.dpToPx(padding.left, context),
                        CommonUtils.dpToPx(padding.top, context),
                        CommonUtils.dpToPx(padding.right, context),
                        CommonUtils.dpToPx(padding.bottom, context)
                );
            }
        }
    }

    public static File getCachedImageFile(Context context, String url) {
        File imageCacheDir = getImageCacheDir(context);
        if (imageCacheDir != null && imageCacheDir.exists()) {
            String fileName = getCachedImageFileName(url);
            File imgFile = new File(imageCacheDir, fileName);
            Log.d(LOG_TAG, "Image file name. Remote: " + url + ", Local: " + imgFile.getAbsolutePath());
            return imgFile;
        } else {
            return null;
        }
    }

    private static File getImageCacheDir(Context context) {
        File imagesDir = null;

        if (context != null) {
            File filesDir = context.getFilesDir();
            imagesDir = new File(filesDir, "images");
            if (!imagesDir.exists()) {
                boolean val = imagesDir.mkdirs();
                if (val) {
                    Log.d(LOG_TAG, "Directory created! " + imagesDir.getAbsolutePath());
                } else {
                    Log.d(LOG_TAG, "Could not create dir.");
                }
            }
        }

        return imagesDir;
    }

    private static String getCachedImageFileName(String url) {
        String md5Hash = "";

        if (!TextUtils.isEmpty(url)) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(url.getBytes());
                byte[] byteArray = md5.digest();

                StringBuilder sb = new StringBuilder();
                for (byte data : byteArray) {
                    sb.append(Integer.toString((data & 0xff) + 0x100, 16).substring(1));
                }

                md5Hash = sb.toString();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return md5Hash;
    }

    public static long timestampToEpochSeconds(String srcTimestamp) {
        long epoch = 0;

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant instant = Instant.parse(srcTimestamp);
                epoch = instant.getEpochSecond();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSSSS'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(srcTimestamp);
                if (date != null) {
                    epoch = date.getTime() / 1000;
                }
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return epoch;
    }
}
