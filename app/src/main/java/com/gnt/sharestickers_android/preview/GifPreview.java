package com.gnt.sharestickers_android.preview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gnt.sharestickers_android.BuildConfig;
import com.gnt.sharestickers_android.util.MediaType;

/**
 * Created by tuan.tn on 3/9/2017.
 */

public class GifPreview extends MediaPreview {
    //TODO remove this String
    public final String DEBUG_TAG = getClass().getName();

    private ImageView gifPreviewHolder;
    private String url;
    private Context context;
    private boolean isRunning = false;

    public GifPreview(String url) {
        this.url = url;
    }

    @Override
    public void play() {
        if (gifPreviewHolder != null && context != null) {
            Glide.with(context)
                    .load(url)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(gifPreviewHolder);

            isRunning = true;

            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "gifPreviewHolder != null");
            }
        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "gifPreviewHolder is null");
            }
        }
    }

    @Override
    public void pause() {
        if (gifPreviewHolder != null && context != null) {
            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(gifPreviewHolder);

            isRunning = false;

            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "gifPreviewHolder != null");
            }
        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "gifPreviewHolder is null");
            }
        }
    }

    @Override
    public void share() {
        //TODO need understand URL and URI
        Uri gifUri = Uri.parse("file://" + url);

        Intent shareGifIntent = new Intent();
        shareGifIntent.setAction(Intent.ACTION_SEND);
        shareGifIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
        shareGifIntent.setType(getMIMEType());

        Activity previewActivity = (Activity) context;
        previewActivity.startActivity(shareGifIntent);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void attachToLayout(Context context, ViewGroup layout) {
        this.context = context;

        if (layout instanceof LinearLayout) {
            // Clean children view
            layout = (LinearLayout) layout;
            layout.removeAllViews();

            // Initialize and setup properties for ImageView
            setupPreviewHolder(context);
            layout.addView(gifPreviewHolder);

            //TODO Need optimize
            if (gifPreviewHolder != null && context != null) {
                Glide.with(context)
                        .load(url)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(gifPreviewHolder);

                isRunning = false;

                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "gifPreviewHolder != null");
                }
            } else {
                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "gifPreviewHolder is null");
                }
            }


        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "layout is NOT instance of LinearLayout");
            }
        }// end if else layout isInstanceOf LinearLayout
    }

    private void setupPreviewHolder(Context context) {
        gifPreviewHolder = new ImageView(context);

        // Set params for imageview
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        gifPreviewHolder.setLayoutParams(params);
        gifPreviewHolder.setAdjustViewBounds(true);
    }

    public View getGifPreviewHolder() {
        return gifPreviewHolder;
    }

    @Override
    public String getMIMEType() {
        return MediaType.MIME_IMAGE_GIF;
    }

    @Override
    public String getURL() {
        return url;
    }
}
