package com.gnt.sharestickers_android;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.gnt.sharestickers_android.databinding.ActivitySharestickersPreviewBinding;
import com.gnt.sharestickers_android.preview.GifPreview;
import com.gnt.sharestickers_android.preview.MediaPreview;
import com.gnt.sharestickers_android.preview.Mp4Preview;
import com.gnt.sharestickers_android.util.MediaType;

public class SharestickersPreviewActivity extends AppCompatActivity {

    //TODO remove this String
    public final String DEBUG_TAG = getClass().getName();

    private ActivitySharestickersPreviewBinding binding;

    private LinearLayout previewLayout;
    private ImageButton playButton;
    private MediaPreview mediaPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sharestickers_preview);

        setupActionBar();

        // Get all necessary view
        previewLayout = binding.previewLayout;
        playButton = binding.playButton;

        // Register action listener for play button
        playButton.setOnTouchListener(new TouchDemo());

        // Determine what kind of media to preview and share
        prepareMediaPreview();

    }

    private void setupActionBar() {
        //Enable the Up button (Back arrow)
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void prepareMediaPreview() {
        //TODO Check if Gif or Mp4 preview
        Intent previewIntent = getIntent();
        if (previewIntent != null) {
            String mimeType = previewIntent.getStringExtra(PictureCollectionActivity.EXTRA_MIME_TYPE);
            String url = previewIntent.getStringExtra(PictureCollectionActivity.EXTRA_URL);

            if (mimeType.equals(MediaType.MIME_IMAGE_GIF)) {
                mediaPreview = new GifPreview(url);
            } else if (mimeType.equals(MediaType.MIME_VIDEO_MP4)) {
                mediaPreview = new Mp4Preview(url);
            } else {
                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "mime type is not supported");
                }
            } // End if check mimeType

            if (mediaPreview != null) {
                mediaPreview.attachToLayout(this, previewLayout);
            } else {
                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "mediaPreview is null");
                }
            } // End if mediaPreview is null or not
        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "previewIntent is null");
            }
        }// End if previewIntent is null or not
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareMedia();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareMedia() {
        if (mediaPreview != null) {
            mediaPreview.share();
        } else {
            if (BuildConfig.DEBUG ) {
                Log.d(DEBUG_TAG, "mediaPreview is null");
            }
        }
    }

    class TouchDemo implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // This is preview's play button
            ImageButton imageButton = (ImageButton) v;

            // Change image of button (Play - Pause) and associated action
            if (mediaPreview != null) {
                boolean isPlaying = mediaPreview.isRunning();
                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN) {
                    if (isPlaying) {
                        imageButton.setImageResource(R.mipmap.ic_pause_circle_outline_gray_48dp);
                    } else {
                        imageButton.setImageResource(R.mipmap.ic_play_circle_outline_gray_48dp);
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (isPlaying) {
                        imageButton.setImageResource(R.mipmap.ic_play_circle_outline_black_48dp);
                        mediaPreview.pause();
                    } else {
                        imageButton.setImageResource(R.mipmap.ic_pause_circle_outline_black_48dp);
                        mediaPreview.play();
                    }
                } else {
                    if (BuildConfig.DEBUG ) {
                        Log.d(DEBUG_TAG, "This touch action hasn't processed: " + action);
                    }
                }

                return true;
            } else {
                if (BuildConfig.DEBUG ) {
                    Log.d(DEBUG_TAG, "mediaPreview is null");
                }
                return false;
            }

        }
    }
}
