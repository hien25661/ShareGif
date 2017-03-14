package com.gnt.sharestickers_android.preview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.gnt.sharestickers_android.BuildConfig;
import com.gnt.sharestickers_android.util.MediaType;

/**
 * Created by tuan.tn on 3/10/2017.
 */

public class Mp4Preview extends MediaPreview {
    //TODO remove this String
    public final String DEBUG_TAG = getClass().getName();

    private VideoView videoPreviewHolder;
    private Context context;
    private String url;

    private boolean isReady = false;

    public Mp4Preview(String url) {
        this.url = url;
    }

    @Override
    public void play() {
        if (isReady) {
            videoPreviewHolder.start();
        }
    }

    private boolean isReady() {
        return isReady;
    }

    @Override
    public void pause() {
        if (isReady) {
            videoPreviewHolder.pause();
        }
    }

    @Override
    public void share() {
        //TODO need understand URL and URI
        Uri mp4Uri = Uri.parse("file://" + url);

        Intent shareMp4Intent = new Intent();
        shareMp4Intent.setAction(Intent.ACTION_SEND);
        shareMp4Intent.putExtra(Intent.EXTRA_STREAM, mp4Uri);
        shareMp4Intent.setType(getMIMEType());

        Activity previewActivity = (Activity) context;
        previewActivity.startActivity(shareMp4Intent);
    }

    @Override
    public boolean isRunning() {
        if (isReady) {
            return videoPreviewHolder.isPlaying();
        }else {
            return false;
        }
    }

    @Override
    public void attachToLayout(Context context, ViewGroup layout) {
        this.context = context;

        if (layout instanceof LinearLayout) {
            // Clean children view
            layout = (LinearLayout) layout;
            layout.removeAllViews();

            // Initialize and setup properties for VideoView
            setupVideoPreviewHolder(context);
            layout.addView(videoPreviewHolder);

            if (videoPreviewHolder != null && context != null) {
                loadVideo();
                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "videoPreviewHolder != null");
                }
            } else {
                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "videoPreviewHolder is null");
                }
            } // end if else Check null videoPreviewHolder & context


        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "layout is NOT instance of LinearLayout");
            }
        } // end if else layout isInstanceOf LinearLayout
    }

    private void setupVideoPreviewHolder(Context context) {
        videoPreviewHolder = new VideoView(context);

        // Set params for VideoView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        videoPreviewHolder.setLayoutParams(params);
    }

    private void loadVideo() {
        if (videoPreviewHolder != null) {
            videoPreviewHolder.setVideoPath(url);

            //Load video
            videoPreviewHolder.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    isReady = true;
                }
            });
        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "videoPreviewHolder is null");
            }
        }
    }
    public View getGifPreviewHolder() {
        return videoPreviewHolder;
    }

    @Override
    public String getMIMEType() {
        return MediaType.MIME_VIDEO_MP4;
    }

    @Override
    public String getURL() {
        return url;
    }
}
