package com.blueshift.inappmessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.blueshift.Blueshift;
import com.blueshift.BlueshiftConstants;
import com.blueshift.BlueshiftExecutor;
import com.blueshift.BlueshiftLogger;
import com.blueshift.R;
import com.blueshift.httpmanager.HTTPManager;
import com.blueshift.httpmanager.Response;
import com.blueshift.model.Configuration;
import com.blueshift.model.UserInfo;
import com.blueshift.rich_push.Message;
import com.blueshift.util.BlueshiftUtils;
import com.blueshift.util.CommonUtils;
import com.blueshift.util.DeviceUtils;
import com.blueshift.util.InAppUtils;
import com.blueshift.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class InAppManager {
    private static final String LOG_TAG = InAppManager.class.getSimpleName();

    @SuppressLint("StaticFieldLeak") // cleanup happens when unregisterForInAppMessages() is called.
    private static Activity mActivity = null;
    private static AlertDialog mDialog = null;
    private static InAppActionCallback mActionCallback = null;

    /**
     * Calling this method makes the activity eligible for displaying InAppMessage
     *
     * @param activity valid Activity object.
     */
    public static void registerForInAppMessages(Activity activity) {
        if (mActivity != null) {
            // Log.w(LOG_TAG, "Possible memory leak detected! Cleaning up. ");
            // do the clean up for old activity to avoid mem leak
            unregisterForInAppMessages(mActivity);
        }

        mActivity = activity;

        invokeTriggerWithinSdk();
    }

    /**
     * Calling this method will clean up the memory to avoid memory leak.
     *
     * @param activity valid Activity object.
     */
    public static void unregisterForInAppMessages(Activity activity) {
        // if unregister is called with old activity when new activity is started,
        // we need to skip this call. because the clean up would already been done
        // during the registration call.
        if (activity != null && mActivity != null) {
            String oldName = mActivity.getLocalClassName();
            String newName = activity.getLocalClassName();
            if (!oldName.equals(newName)) {
                return;
            }
        }

        // clean up the dialog and activity
        dismissAndCleanupDialog();

        mDialog = null;
        mActivity = null;
    }

    public static InAppActionCallback getActionCallback() {
        return mActionCallback;
    }

    public static void setActionCallback(InAppActionCallback callback) {
        mActionCallback = callback;
    }

    /**
     * Creates a Handler with current looper.
     *
     * @param callback callback to invoke after API call
     * @return Handler object to run the callback
     */
    private static Handler getCallbackHandler(InAppApiCallback callback) {
        Handler handler = null;
        if (callback != null) {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                handler = new Handler(looper);
            }
        }

        return handler;
    }

    public static void fetchInAppFromServer(final Context context, final InAppApiCallback callback) {
        boolean isEnabled = BlueshiftUtils.isInAppEnabled(context);
        if (isEnabled) {
            final Handler callbackHandler = getCallbackHandler(callback);
            BlueshiftExecutor.getInstance().runOnNetworkThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject params = new JSONObject();

                                // api key
                                String apiKey = BlueshiftUtils.getApiKey(context);
                                params.put(BlueshiftConstants.KEY_API_KEY, apiKey != null ? apiKey : "");

                                // device id
                                String deviceId = DeviceUtils.getDeviceId(context);
                                params.put(BlueshiftConstants.KEY_DEVICE_IDENTIFIER, deviceId != null ? deviceId : "");

                                // email
                                String email = UserInfo.getInstance(context).getEmail();
                                params.put(BlueshiftConstants.KEY_EMAIL, email != null ? email : "");

                                String messageUuid = null;
                                String lastTimestamp = null;

                                InAppMessage inAppMessage = InAppMessageStore.getInstance(context).getLastInAppMessage();
                                if (inAppMessage != null) {
                                    messageUuid = inAppMessage.getMessageUuid();
                                    lastTimestamp = inAppMessage.getTimestamp();
                                }

                                // message uuid
                                params.put(Message.EXTRA_BSFT_MESSAGE_UUID, messageUuid != null ? messageUuid : "");

                                // lastTimestamp
                                params.put(BlueshiftConstants.KEY_LAST_TIMESTAMP, lastTimestamp != null ? lastTimestamp : 0);

                                HTTPManager httpManager = new HTTPManager(BlueshiftConstants.IN_APP_API_URL);

                                if (apiKey != null) {
                                    httpManager.addBasicAuthentication(apiKey, "");
                                }

                                String json = params.toString();
                                BlueshiftLogger.d(LOG_TAG, "In-App API Req Params: " + json);

                                Response response = httpManager.post(json);
                                int statusCode = response.getStatusCode();
                                String responseBody = response.getResponseBody();

                                if (statusCode == 200) {
                                    if (!TextUtils.isEmpty(responseBody)) {
                                        BlueshiftLogger.d(LOG_TAG, "In-App API Response: " + responseBody);
                                        try {
                                            JSONArray inAppJsonArray = decodeResponse(responseBody);
                                            InAppManager.onInAppMessageArrayReceived(context, inAppJsonArray);
                                        } catch (Exception e) {
                                            BlueshiftLogger.e(LOG_TAG, e);
                                        }
                                    }

                                    invokeApiSuccessCallback(callbackHandler, callback);
                                } else {
                                    invokeApiFailureCallback(callbackHandler, callback, statusCode, responseBody);
                                }
                            } catch (Exception e) {
                                BlueshiftLogger.e(LOG_TAG, e);

                                invokeApiFailureCallback(callbackHandler, callback, 0, e.getMessage());
                            }
                        }
                    }
            );
        }
    }

    private static void invokeApiSuccessCallback(Handler handler, final InAppApiCallback callback) {
        if (handler != null && callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess();
                }
            });
        }
    }

    private static void invokeApiFailureCallback(Handler handler, final InAppApiCallback callback,
                                                 final int errorCode, final String errorMessage) {
        if (handler != null && callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFailure(errorCode, errorMessage);
                }
            });
        }
    }

    private static JSONArray decodeResponse(String response) {
        JSONArray jsonArray = null;

        try {
            if (response != null) {
                JSONObject responseJson = new JSONObject(response);
                jsonArray = responseJson.optJSONArray("content");
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return jsonArray;
    }

    private static void onInAppMessageArrayReceived(Context context, JSONArray inAppContentArray) {
        try {
            if (inAppContentArray != null) {
                int len = inAppContentArray.length();
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        JSONObject content = inAppContentArray.getJSONObject(i);
                        if (content != null) {
                            JSONObject inApp = content.optJSONObject("data");
                            if (inApp != null) {
                                InAppMessage message = InAppMessage.getInstance(inApp);
                                InAppManager.onInAppMessageReceived(context, message);
                            }
                        }
                    }

                    InAppManager.invokeTriggerWithinSdk();
                } else {
                    Log.d(LOG_TAG, "No items found inside 'content'.");
                }
            } else {
                Log.d(LOG_TAG, "'content' is NULL.");
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }
    }

    public static void onInAppMessageReceived(Context context, InAppMessage inAppMessage) {
        boolean isEnabled = BlueshiftUtils.isInAppEnabled(context);
        if (isEnabled) {
            Log.d(LOG_TAG, "In-app message received. Message UUID: " + (inAppMessage != null ? inAppMessage.getMessageUuid() : null));

            if (inAppMessage != null) {
                Blueshift.getInstance(context).trackInAppMessageDelivered(inAppMessage);

                if (!inAppMessage.isExpired()) {
                    boolean inserted = InAppMessageStore.getInstance(context).insert(inAppMessage);
                    if (inserted) {
                        InAppManager.cacheAssets(inAppMessage, context);
                    } else {
                        Log.d(LOG_TAG, "Possible duplicate in-app received. Skipping! Message UUID: " + inAppMessage.getMessageUuid());
                    }
                } else {
                    Log.d(LOG_TAG, "Expired in-app received. Message UUID: " + inAppMessage.getMessageUuid());
                }
            }
        }
    }

    /**
     * This method checks if the dev has enabled the manual triggering of in-app
     * and then decides if we should call the invokeTrigger or not based on that
     */
    public static void invokeTriggerWithinSdk() {
        if (mActivity != null) {
            Configuration config = BlueshiftUtils.getConfiguration(mActivity);
            if (config != null && !config.isInAppManualTriggerEnabled()) {
                invokeTriggers();
            }
        }
    }

    public static void invokeTriggers() {
        if (mActivity == null) {
            Log.d(LOG_TAG, "App isn't running with an eligible Activity to display InAppMessage.");
            return;
        }

        boolean isEnabled = BlueshiftUtils.isInAppEnabled(mActivity);
        if (isEnabled) {
            try {
                BlueshiftExecutor.getInstance().runOnDiskIOThread(new Runnable() {
                    @Override
                    public void run() {
                        InAppMessage input = InAppMessageStore.getInstance(mActivity).getInAppMessage(mActivity);

                        if (input == null) {
                            Log.d(LOG_TAG, "No pending in-app messages found.");
                            return;
                        }

                        if (!validate(input)) {
                            Log.d(LOG_TAG, "Invalid in-app messages found. Message UUID: " + input.getMessageUuid());
                            return;
                        }

                        displayInAppMessage(input);
                    }
                });
            } catch (Exception e) {
                BlueshiftLogger.e(LOG_TAG, e);
            }
        }
    }

    private static void scheduleNextInAppMessage(final Context context) {
        try {
            Configuration config = BlueshiftUtils.getConfiguration(context);
            if (config != null) {
                long delay = config.getInAppInterval();
                Log.d(LOG_TAG, "Scheduling next in-app in " + delay + "ms");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InAppManager.invokeTriggerWithinSdk();
                    }
                }, delay);
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }
    }

    private static void displayInAppMessage(final InAppMessage input) {
        if (input != null) {
            BlueshiftExecutor.getInstance().runOnMainThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (mActivity != null) {
                                boolean isSuccess = buildAndShowInAppMessage(mActivity, input);
                                if (isSuccess) {
                                    markAsDisplayed(input);
                                }

                                if (isSuccess) {
                                    BlueshiftLogger.d(LOG_TAG, "InApp message displayed successfully!");
                                } else {
                                    BlueshiftLogger.e(LOG_TAG, "InApp message display failed!");
                                }
                            } else {
                                BlueshiftLogger.e(LOG_TAG, "No activity is running, skipping in-app display.");
                            }
                        }
                    }
            );
        } else {
            BlueshiftLogger.e(LOG_TAG, "InApp message is null!");
        }
    }

    private static void markAsDisplayed(final InAppMessage input) {
        BlueshiftExecutor.getInstance().runOnDiskIOThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (input != null && mActivity != null) {
                            input.setDisplayedAt(System.currentTimeMillis());
                            InAppMessageStore.getInstance(mActivity).update(input);
                        }
                    }
                }
        );
    }

    private static boolean validate(InAppMessage inAppMessage) {
        return checkIsInstantMessage(inAppMessage) || (checkInterval() && checkDisplayTimeInterval(inAppMessage));
    }

    private static boolean checkDisplayTimeInterval(InAppMessage inAppMessage) {
        return checkDisplayFrom(inAppMessage) && checkExpiry(inAppMessage);
    }

    private static boolean checkDisplayFrom(InAppMessage inAppMessage) {
        try {
            if (inAppMessage != null) {
                long currentTime = System.currentTimeMillis();
                long displayFrom = inAppMessage.getDisplayFromMillis();
                return currentTime > displayFrom;
            }
        } catch (Exception e) {
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return false;
    }

    private static boolean checkIsInstantMessage(InAppMessage inAppMessage) {
        return inAppMessage != null && inAppMessage.shouldShowNow();
    }

    private static boolean checkInterval() {
        boolean result = false;

        Configuration config = BlueshiftUtils.getConfiguration(mActivity);
        if (config != null) {
            long intervalMs = config.getInAppInterval();
            long lastDisplayedAt = InAppMessageStore.getInstance(mActivity).getLastDisplayedAt();

            BlueshiftLogger.d(LOG_TAG, "Last In App Message was displayed at " + CommonUtils.formatMilliseconds(lastDisplayedAt));

            long diffMs = System.currentTimeMillis() - lastDisplayedAt;
            result = diffMs >= intervalMs;

            if (!result) {
                BlueshiftLogger.d(LOG_TAG, "Interval between In App Messages should be " + intervalMs / 1000 + " seconds.");
            }
        }

        return result;
    }

    private static boolean checkExpiry(InAppMessage inAppMessage) {
        boolean result = false;

        if (inAppMessage != null) {
            long currentTimeMs = System.currentTimeMillis();
            long expiresAtMs = inAppMessage.getExpiresAt() * 1000;
            result = currentTimeMs < expiresAtMs;

            if (!result) {
                BlueshiftLogger.d(LOG_TAG, "In App Message expired at " + expiresAtMs);
            }
        }

        return result;
    }

    private static boolean buildAndShowInAppMessage(Context context, InAppMessage inAppMessage) {
        if (inAppMessage != null) {
            InAppTemplate inAppTemplate = inAppMessage.getTemplate();
            if (inAppTemplate != null) {
                switch (inAppTemplate) {
                    case HTML:
                        return buildAndShowHtmlInAppMessage(context, inAppMessage);
                    case MODAL:
                        return buildAndShowCenterPopupInAppMessage(context, inAppMessage);
                    case SLIDE_IN_BANNER:
                        return buildAndShowSlidingBannerInAppMessage(context, inAppMessage);
                    case RATING:
                        return buildAndShowRatingInAppMessage(context, inAppMessage);
                }
            }
        }

        return false;
    }

    private static boolean buildAndShowCenterPopupInAppMessage(Context context, InAppMessage inAppMessage) {
        if (context != null && inAppMessage != null) {
            InAppMessageViewModal inAppMessageViewModal = new InAppMessageViewModal(context, inAppMessage) {
                public void onDismiss(InAppMessage inAppMessage, String elementName) {
                    invokeDismissButtonClick(inAppMessage, elementName);
                }
            };

            return displayInAppDialogModal(context, inAppMessageViewModal, inAppMessage);
        }

        return false;
    }

    private static boolean buildAndShowRatingInAppMessage(Context context, InAppMessage inAppMessage) {
        if (context != null && inAppMessage != null) {
            InAppMessageViewRating inAppMessageViewRating = new InAppMessageViewRating(context, inAppMessage) {
                public void onDismiss(InAppMessage inAppMessage, String elementName) {
                    invokeDismissButtonClick(inAppMessage, elementName);
                }
            };

            return displayInAppDialogModal(context, inAppMessageViewRating, inAppMessage);
        }

        return false;
    }

    private static boolean buildAndShowHtmlInAppMessage(final Context context, final InAppMessage inAppMessage) {
        if (context != null && inAppMessage != null) {
            InAppMessageViewHTML inAppMessageViewHTML = new InAppMessageViewHTML(context, inAppMessage) {
                @Override
                public void onDismiss(InAppMessage inAppMessage, String elementName) {
                    invokeDismissButtonClick(inAppMessage, elementName);
                }
            };

            return displayInAppDialogModal(context, inAppMessageViewHTML, inAppMessage);
        }

        return false;
    }

    private static boolean buildAndShowSlidingBannerInAppMessage(Context context, InAppMessage inAppMessage) {
        if (context != null && inAppMessage != null) {
            InAppMessageViewBanner inAppMessageViewBanner = new InAppMessageViewBanner(context, inAppMessage) {
                public void onDismiss(InAppMessage inAppMessage, String elementName) {
                    invokeDismissButtonClick(inAppMessage, elementName);
                }
            };

            return displayInAppDialogAnimated(context, inAppMessageViewBanner, inAppMessage);
        }

        return false;
    }

    private static boolean displayInAppDialogModal(final Context context, final View customView, final InAppMessage inAppMessage) {
        float dimAmount = (float) InAppUtils.getTemplateBackgroundDimAmount(context, inAppMessage, 0.5);
        return buildAndShowAlertDialog(context, inAppMessage, customView, R.style.dialogStyleInApp, dimAmount);
    }

    private static boolean displayInAppDialogAnimated(final Context context, final View customView, final InAppMessage inAppMessage) {
        float dimAmount = (float) InAppUtils.getTemplateBackgroundDimAmount(context, inAppMessage, 0.0);
        return buildAndShowAlertDialog(context, inAppMessage, customView, R.style.inAppSlideFromLeft, dimAmount);
    }

    private static void invokeDismissButtonClick(InAppMessage inAppMessage, String elementName) {
        // dismiss the dialog and cleanup memory
        dismissAndCleanupDialog();
        // log the click event
        Blueshift.getInstance(mActivity).trackInAppMessageClick(inAppMessage, elementName);
    }

    private static void invokeOnInAppViewed(InAppMessage inAppMessage) {
        // send stats
        Blueshift.getInstance(mActivity).trackInAppMessageView(inAppMessage);
        // update with displayed at timing
        inAppMessage.setDisplayedAt(System.currentTimeMillis());
        InAppMessageStore.getInstance(mActivity).update(inAppMessage);
    }

    private static void dismissAndCleanupDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private static boolean buildAndShowAlertDialog(
            final Context context, final InAppMessage inAppMessage, final View content, final int theme, final float dimAmount) {
        if (mActivity != null && !mActivity.isFinishing()) {
            if (mDialog == null || !mDialog.isShowing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, theme);
                builder.setView(content);
                mDialog = builder.create();
                mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        BlueshiftExecutor.getInstance().runOnDiskIOThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        InAppManager.clearCachedAssets(inAppMessage, context);
                                        InAppManager.scheduleNextInAppMessage(context);
                                    }
                                }
                        );
                    }
                });
                mDialog.show();

                Window window = mDialog.getWindow();
                if (window != null) {
                    window.setGravity(InAppUtils.getTemplateGravity(context, inAppMessage));
                    window.setDimAmount(dimAmount);

                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                    if (InAppUtils.isTemplateFullScreen(context, inAppMessage)) {
                        height = LinearLayout.LayoutParams.MATCH_PARENT;
                        width = LinearLayout.LayoutParams.MATCH_PARENT;
                    }

                    window.setLayout(width, height);
                }

                BlueshiftExecutor.getInstance().runOnDiskIOThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                InAppManager.invokeOnInAppViewed(inAppMessage);
                            }
                        }
                );

                return true;
            } else {
                Log.d(LOG_TAG, "Already an in-app is in display.");
            }
        } else {
            Log.d(LOG_TAG, "App is not running in foreground.");
        }

        return false;
    }

    private static void clearCachedAssets(InAppMessage inAppMessage, Context context) {
        if (inAppMessage != null && context != null) {
            //noinspection StatementWithEmptyBody
            if (InAppConstants.HTML.equals(inAppMessage.getType())) {
                // do nothing. cache is managed by WebView
            } else {
                // modal with banner
                String bannerImage = inAppMessage.getContentString(InAppConstants.BANNER);
                if (!TextUtils.isEmpty(bannerImage)) {
                    deleteCachedImage(context, bannerImage);
                }
                // icon image of slide-in
                String iconImage = inAppMessage.getContentString(InAppConstants.ICON_IMAGE);
                if (!TextUtils.isEmpty(iconImage)) {
                    deleteCachedImage(context, iconImage);
                }
            }
        }
    }

    private static void cacheAssets(final InAppMessage inAppMessage, final Context context) {
        if (inAppMessage != null) {
            if (InAppConstants.HTML.equals(inAppMessage.getType())) {
                // html, cache inside WebView
                BlueshiftExecutor.getInstance().runOnMainThread(new Runnable() {
                    @SuppressLint("SetJavaScriptEnabled")
                    @Override
                    public void run() {
                        String htmlContent = inAppMessage.getContentString(InAppConstants.HTML);
                        if (!TextUtils.isEmpty(htmlContent)) {
                            WebView webView = new WebView(context);

                            // taking consent from dev to enable js
                            Configuration config = BlueshiftUtils.getConfiguration(context);
                            if (config != null && config.isJavaScriptForInAppWebViewEnabled()) {
                                webView.getSettings().setJavaScriptEnabled(true);
                            }

                            webView.loadData(CommonUtils.getBase64(htmlContent), "text/html; charset=UTF-8", "base64");
                        }
                    }
                });
            } else {
                // cache for modals and other templates

                // modal with banner
                String bannerImage = inAppMessage.getContentString(InAppConstants.BANNER);
                if (!TextUtils.isEmpty(bannerImage)) {
                    cacheImage(context, bannerImage);
                }
                // icon image of slide-in
                String iconImage = inAppMessage.getContentString(InAppConstants.ICON_IMAGE);
                if (!TextUtils.isEmpty(iconImage)) {
                    cacheImage(context, iconImage);
                }
            }
        }
    }

    private static void deleteCachedImage(Context context, String url) {
        if (context != null && url != null) {
            File imgFile = InAppUtils.getCachedImageFile(context, url);
            if (imgFile != null && imgFile.exists()) {
                Log.d(LOG_TAG, "Image delete " + (imgFile.delete() ? "success. " : "failed. ")
                        + imgFile.getAbsolutePath());
            }
        }
    }

    private static void cacheImage(final Context context, final String url) {
        final File file = InAppUtils.getCachedImageFile(context, url);

        if (file != null) {
            BlueshiftExecutor.getInstance().runOnNetworkThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean success = NetworkUtils.downloadFile(url, file.getAbsolutePath());
                        Log.d(LOG_TAG, "Download " + (success ? "success!" : "failed!"));
                    } catch (Exception e) {
                        BlueshiftLogger.e(LOG_TAG, e);
                    }
                }
            });
        }
    }
}
