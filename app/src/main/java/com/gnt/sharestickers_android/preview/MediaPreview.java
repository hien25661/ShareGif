package com.gnt.sharestickers_android.preview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tuan.tn on 3/9/2017.
 */

public abstract class MediaPreview {

    abstract public void play();
    abstract public void pause();
    abstract public void share();
    abstract public boolean isRunning();

    abstract public String getMIMEType();
    abstract public String getURL();

    abstract public void attachToLayout(Context context, ViewGroup layout);
    abstract public View getGifPreviewHolder();
}
