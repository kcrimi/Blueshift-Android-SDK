package com.blueshift.rich_push;

import android.support.annotation.NonNull;

/**
 * This class holds the meta data of the GIF frame stored locally
 *
 * @author Rahul Raveendran 24/9/16 @ 4:52 PM
 */

public class GifFrameMetaData implements Comparable<GifFrameMetaData> {
    private Integer mIndex;
    private int mDelay;
    private String mFileName;

    public GifFrameMetaData(int index, int delay, String fileName) {
        mIndex = index;
        mDelay = delay;
        mFileName = fileName;
    }

    public int getDelay() {
        return mDelay;
    }

    public String getFileName() {
        return mFileName;
    }

    @Override
    public int compareTo(@NonNull GifFrameMetaData another) {
        return this.mIndex.compareTo(another.mIndex);
    }
}
