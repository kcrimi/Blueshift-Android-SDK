package com.blueshift.rich_push;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Created by rahul on 24/9/16 @ 4:52 PM.
 */

public class GifFrameData implements Comparable<GifFrameData> {
    private Integer mIndex;
    private int mDelay;
    private String mFileName;

    public GifFrameData(int index, int delay, String fileName) {
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
    public int compareTo(@NonNull GifFrameData another) {
        return this.mIndex.compareTo(another.mIndex);
    }
}
