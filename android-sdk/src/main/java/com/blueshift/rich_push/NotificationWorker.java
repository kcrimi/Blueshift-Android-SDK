package com.blueshift.rich_push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blueshift.util.NotificationUtils;

/**
 * @author Rahul Raveendran V P
 *         Created on 20/9/16 @ 12:00 PM @ 11:40 AM
 *         https://github.com/rahulrvp
 */
public class NotificationWorker extends IntentService {

    public static final String ACTION_CAROUSEL_IMG_CHANGE = "carousel_image_change";
    public static final String ACTION_START_GIF_PLAYBACK = "start_gif_playback";
    public static final String ACTION_DELETE_GIF_NOTIFICATION = "delete_gif_notification";
    public static final String ACTION_DELETE_CAROUSEL_NOTIFICATION = "delete_carousel_notification";

    private static final String LOG_TAG = "NotificationWorker";

    private static boolean mShouldPlayGif = false;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationWorker(String name) {
        super(name);
    }

    public NotificationWorker() {
        super("NotificationIntentService");
    }

    public synchronized static void enableGifPlayback() {
        mShouldPlayGif = true;
    }

    public synchronized static void disableGifPlayback() {
        mShouldPlayGif = false;
    }

    private synchronized static boolean shouldPlayGif() {
        return mShouldPlayGif;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        if (action == null) return;

        Message message = (Message) intent.getSerializableExtra(RichPushConstants.EXTRA_MESSAGE);

        switch (action) {
            case ACTION_CAROUSEL_IMG_CHANGE:
                int targetIndex = intent.getIntExtra(RichPushConstants.EXTRA_CAROUSEL_INDEX, 0);

                updateCarouselNotification(this, message, targetIndex);

                break;

            case ACTION_DELETE_CAROUSEL_NOTIFICATION:
                /**
                 * Remove if there is any cached images (used for carousel) found for this notification.
                 */
                NotificationUtils.removeCachedCarouselImages(this, message);

                Log.i(LOG_TAG, "Carousel notification deleted. Id: " + message.getCategory().getNotificationId());

                break;

            case ACTION_START_GIF_PLAYBACK:
                playGifNotification(getApplicationContext(), message);

                break;

            case ACTION_DELETE_GIF_NOTIFICATION:
                /**
                 * Remove cached images and meta data used for GIF notification
                 */
                NotificationUtils.deleteCachedGifFrameBitmaps(this, message);
                NotificationUtils.deleteCachedGifFramesMetaData(this, message);

                Log.i(LOG_TAG, "GIF notification deleted. Id: " + message.getCategory().getNotificationId());

                break;
        }
    }

    private void updateCarouselNotification(Context context, Message message, int newIndex) {
        CustomNotificationFactory
                .getInstance()
                .createAndShowCarousel(context, message, true, newIndex);
    }

    /**
     * This method is responsible for GIF playback.
     *
     * @param context valid {@link Context} object
     * @param message valid {@link Message} object
     */
    private void playGifNotification(Context context, Message message) {
        enableGifPlayback();

        GifFrameMetaData[] gifFrameMetaData = NotificationUtils.getCachedGifFramesMetaData(context, message);

        // exit if there is not cached info available.
        if (gifFrameMetaData == null || gifFrameMetaData.length == 0) {
            return;
        }

        // repeat count hard coded as 2 times for now.
        // since showing GIF notification is a little expensive,
        // lets keep it 2 for better experience and less resource usage.
        for (int i = 0; i < 2; i++) {
            int frameIndex = 0;

            for (GifFrameMetaData frameData : gifFrameMetaData) {
                if (shouldPlayGif()) {
                    // show current frame in Notification
                    CustomNotificationFactory
                            .getInstance()
                            .createAndShowGIFNotification(context, message, true, frameIndex++);

                    // simulate frame delay
                    try {
                        Thread.sleep(frameData.getDelay());
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                } else {
                    // falls in this block when user clears notification
                    // or when notification is clicked and then cancelled.
                    break;
                }
            }
        }

        if (shouldPlayGif()) {
            // Show notification with 0th frame and play button
            CustomNotificationFactory
                    .getInstance()
                    .createAndShowGIFNotification(context, message, true, -1);
        }
    }
}
