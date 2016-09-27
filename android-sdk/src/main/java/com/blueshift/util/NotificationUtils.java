package com.blueshift.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.blueshift.rich_push.CarouselElement;
import com.blueshift.rich_push.GifDecoder;
import com.blueshift.rich_push.GifFrameMetaData;
import com.blueshift.rich_push.Message;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * A class with helper methods to show custom notification.
 * <p>
 * Created by Rahul on 20/9/16 @ 3:55 PM.
 */
public class NotificationUtils {

    private static final String LOG_TAG = "NotificationUtils";

    /**
     * Extracts the file name from the image file url
     *
     * @param url a valid image url
     * @return the file name of the image the url referring to
     */
    public static String getImageFileName(String url) {
        if (url == null) return null;

        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * This method will first downloads the GIF image and then decode it to get it's individual frames as bitmap.
     * Then the bitmaps are stored locally inside private files location of the host app.
     *
     * @param context valid {@link Context} object
     * @param message valid {@link Message} object
     */
    public static void downloadAndCacheGifFrames(Context context, Message message) {
        if (context != null && message != null) {
            String imageUrl = message.getImage_url();
            if (TextUtils.isEmpty(imageUrl)) {
                Log.e(LOG_TAG, "No image url found. GIF download failed.");
            } else {

                try {
                    URL gifImageURL = new URL(imageUrl);

                    GifDecoder gifDecoder = new GifDecoder();
                    gifDecoder.read(gifImageURL.openStream());

                    String gifImageFileName = getImageFileName(imageUrl);
                    int frameCount = gifDecoder.getFrameCount();

                    // frameIndex is the index of frames stored locally.
                    int frameIndex = 0;

                    ArrayList<GifFrameMetaData> gifFrameMetaData = new ArrayList<>();

                    for (int currentFrameIndex = 0; currentFrameIndex < frameCount; currentFrameIndex++) {
                        Bitmap currentFrame = gifDecoder.getFrame(currentFrameIndex);

                        if (currentFrame != null) {
                            int delayInMillis = gifDecoder.getDelay(currentFrameIndex);

                            if (delayInMillis > 0) {
                                String fileName = System.currentTimeMillis() + "-" + gifImageFileName;
                                saveImageInDisc(context, currentFrame, fileName);

                                // store the meta data of the file stored locally
                                GifFrameMetaData frameData = new GifFrameMetaData(frameIndex, delayInMillis, fileName);
                                gifFrameMetaData.add(frameData);

                                frameIndex++;
                            }
                        }
                    }

                    if (gifFrameMetaData.size() > 0) {
                        // cache the meta data about frames stored locally
                        cacheGifFramesMetaData(context, message, gifFrameMetaData);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Could not decode GIF image. " + e.getMessage());
                }
            }
        }
    }

    private static String PREF_FILE(Context context) {
        return context.getPackageName() + ".GIF_DATA_FILE";
    }

    private static String PREF_KEY(Context context, String fileName) {
        return context.getPackageName() + "." + fileName;
    }

    private static void cacheGifFramesMetaData(Context context, Message message, ArrayList<GifFrameMetaData> gifFrameMetaDataList) {
        if (message != null) {
            String gifFileName = getImageFileName(message.getImage_url());
            if (context != null && !TextUtils.isEmpty(gifFileName)) {
                context.getSharedPreferences(PREF_FILE(context), Context.MODE_PRIVATE)
                        .edit()
                        .putString(PREF_KEY(context, gifFileName), new Gson().toJson(gifFrameMetaDataList))
                        .apply();
            }
        }
    }

    public static void deleteCachedGifFramesMetaData(Context context, Message message) {
        if (message != null) {
            String fileName = getImageFileName(message.getImage_url());
            if (context != null && !TextUtils.isEmpty(fileName)) {
                context.getSharedPreferences(PREF_FILE(context), Context.MODE_PRIVATE)
                        .edit()
                        .remove(PREF_KEY(context, fileName))
                        .apply();
            }
        }
    }

    public static GifFrameMetaData[] getCachedGifFramesMetaData(Context context, Message message) {
        GifFrameMetaData[] gifFrameMetaDataArray = null;

        if (message != null) {
            String fileName = getImageFileName(message.getImage_url());
            if (context != null && !TextUtils.isEmpty(fileName)) {
                String json = context.getSharedPreferences(PREF_FILE(context), Context.MODE_PRIVATE)
                        .getString(PREF_KEY(context, fileName), null);

                if (!TextUtils.isEmpty(json)) {
                    try {
                        gifFrameMetaDataArray = new Gson().fromJson(json, GifFrameMetaData[].class);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Could not decode JSON. " + e.getMessage());
                    }
                }
            }
        }

        return gifFrameMetaDataArray;
    }

    /**
     * Load bitmap from private files with the filename mentioned inside metadata
     *
     * @param context  valid {@link Context} object to get private files location
     * @param metaData the meta data of the stored bitmap frame
     * @return valid bitmap image if available, else null
     */
    public static Bitmap getCachedGifFrameBitmap(Context context, GifFrameMetaData metaData) {
        Bitmap bitmap = null;

        if (context != null && metaData != null) {
            bitmap = loadImageFromDisc(context, metaData.getFileName());
        }

        return bitmap;
    }

    /**
     * Delete all cached bitmap images for the GIF mentioned inside {@link Message} object
     *
     * @param context valid {@link Context} object to get private files location
     * @param message valid {@link Message} object to get GIF file URL
     */
    public static void deleteCachedGifFrameBitmaps(Context context, Message message) {
        GifFrameMetaData[] gifFrameMetaData = getCachedGifFramesMetaData(context, message);

        if (gifFrameMetaData != null) {
            for (GifFrameMetaData frameData : gifFrameMetaData) {
                removeImageFromDisc(context, frameData.getFileName());
            }
        }
    }

    /**
     * Downloads all carousel images and stores them inside app's private file location after compressing them.
     *
     * @param context valid context object
     * @param message message object with valid carousel elements
     */
    public static void downloadCarouselImages(Context context, Message message) {
        if (context != null && message != null) {
            CarouselElement[] carouselElements = message.getCarouselElements();
            if (carouselElements != null) {
                for (CarouselElement element : carouselElements) {
                    try {
                        // download image
                        URL imageURL = new URL(element.getImageUrl());
                        Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());

                        // resize image
                        bitmap = resizeImageForDevice(context, bitmap);

                        // save image
                        String imageUrl = element.getImageUrl();
                        String fileName = getImageFileName(imageUrl);

                        saveImageInDisc(context, bitmap, fileName);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Could not decode image for carousel. " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * This method re-sizes the bitmap to have aspect ration 2:1 based on the device's dimension.
     *
     * @param context      valid context object
     * @param sourceBitmap the image to resize
     * @return resized image
     */
    public static Bitmap resizeImageForDevice(Context context, Bitmap sourceBitmap) {
        Bitmap resizedBitmap = null;

        if (sourceBitmap != null) {
            if (sourceBitmap.getWidth() > sourceBitmap.getHeight()) {
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

                // ideal image aspect ratio for notification is 2:1
                int newWidth = displayMetrics.widthPixels;
                int newHeight = newWidth / 2;

                resizedBitmap = Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true);
            }
        }

        return resizedBitmap;
    }

    /**
     * This method saves a given bitmap file with a given name inside private files area of the host app.
     *
     * @param context  {@link Context} object to get private file location
     * @param bitmap   {@link Bitmap} image to be stored
     * @param fileName filename for storing the bitmap file
     */
    public static void saveImageInDisc(Context context, Bitmap bitmap, String fileName) {
        if (context != null && bitmap != null && !TextUtils.isEmpty(fileName)) {
            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                Log.i(LOG_TAG, "Saved file: " + fileName);
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "File not found. " + e.getMessage());
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Could not close fileOutputStream. " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Loads the image stored inside app's private file location
     *
     * @param context  valid context object
     * @param fileName name of the image to be retrieved
     * @return bitmap image with given filename (if exists)
     */
    public static Bitmap loadImageFromDisc(Context context, String fileName) {
        Bitmap bitmap = null;

        File imageFile = context.getFileStreamPath(fileName);
        if (imageFile.exists()) {
            try {
                InputStream inputStream = context.openFileInput(fileName);
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "File not found. " + e.getMessage());
            }
        }

        return bitmap;
    }

    /**
     * Remove file with given name from app's private files directory.
     *
     * @param context  valid context object
     * @param fileName the name of the file to be removed.
     */
    public static void removeImageFromDisc(Context context, String fileName) {
        if (context != null && !TextUtils.isEmpty(fileName)) {
            if (context.deleteFile(fileName)) {
                Log.i(LOG_TAG, "Deleted file: " + fileName);
            }
        }
    }

    /**
     * Method to remove all carousel images cached.
     *
     * @param context valid context object
     * @param message message with valid carousel elements
     */
    public static void removeCachedCarouselImages(Context context, Message message) {
        if (context != null && message != null) {
            CarouselElement[] carouselElements = message.getCarouselElements();
            if (carouselElements != null && carouselElements.length > 0) {
                for (CarouselElement element : carouselElements) {
                    String fileName = getImageFileName(element.getImageUrl());
                    removeImageFromDisc(context, fileName);
                }
            }
        }
    }
}
