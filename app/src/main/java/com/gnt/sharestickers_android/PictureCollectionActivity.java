package com.gnt.sharestickers_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.gnt.sharestickers_android.databinding.ActivityPictureCollectionBinding;
import com.gnt.sharestickers_android.util.Constant;
import com.gnt.sharestickers_android.util.MediaType;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PictureCollectionActivity extends AppCompatActivity {
    private final String DEBUG_TAG = getClass().getName();

    public static final String EXTRA_MIME_TYPE = "mime";
    public static final String EXTRA_URL = "url";
    public static final int REQUEST_IMAGE_GALLERY = 1;
    public static final int REQUEST_IMAGE_CAMERA = 2;

    private Context mContext;
    private String captureImagePath = "";
    ActivityPictureCollectionBinding binding;

    private ArrayList<Bitmap> mBitmaps;
    private int exportGifWidth, exportGifHeight;
    private String exportGiftPath;
    private GridImageAdapter mGridImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_picture_collection);

        setTitle(R.string.label_gif);

        mContext = this;

        setupActionBar();

        setupGridView();
    }

    private void setupActionBar() {
        //Change Up button to Add (+) menu item
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setHomeAsUpIndicator(getDrawable(R.mipmap.ic_add_black_24dp));
        } else {
            actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.mipmap.ic_add_black_24dp));
        }

    }

    private void setupGridView() {
        mGridImageAdapter = new GridImageAdapter(this);
        binding.pictureGridView.setAdapter(mGridImageAdapter);
        binding.pictureGridView.setOnItemClickListener(new GridPictureItemClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(DEBUG_TAG, "onCreateOptionMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.imagecollection, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export:
                onExportMenuItemClick();
                return true;
            case android.R.id.home:
                onAddMenuItemClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onExportMenuItemClick() {
        final String[] items = {"GIF", "MP4"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Export File");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dialog.dismiss();
                        exportGif();
                        break;
                    case 1:
                        exportMp4();
                        break;
                    default:
                        break;
                }
            }
        });
        alertBuilder.setNegativeButton("Cancel", null);

        AlertDialog pickFromDialog = alertBuilder.create();
        pickFromDialog.show();

//        //TODO Determine what kind of media to preview
//        String mime, url;
//
//        int num = (int) ((Math.random()*100) % 2);
//        if (num == 0) {
//            mime = MediaType.MIME_IMAGE_GIF;
//            url = Constant.URL_GIF;
//        } else {
//            mime = MediaType.MIME_VIDEO_MP4;
//            url = Constant.URL_MP4;
//        }


    }


    private void exportGif() {
        try {
            // Get path for gif export file
            exportGiftPath = saveExportGif();

            mBitmaps = mGridImageAdapter.getBitmaps();

            // Get width = size[0] and height = size[1]
            int[] size = getAndCheckSize(mBitmaps);
            exportGifWidth = size[0];
            exportGifHeight = size[1];

            if (exportGifWidth == -1) { // Empty pictures
                showErrorDialog("Error", "Picture collection is empty.\nPlease try again!");

            } else if (exportGifWidth == -2){ // Pictures's size are not identical
                showErrorDialog("Error", "Size of pictures are not identical. Please try again!");

            } else { // Legal case
                // Start encode gif file
                final ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle("Building GIF file . . .");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgress(0);
                progressDialog.setMax(mBitmaps.size());
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            GifEncoder gifEncoder = new GifEncoder();
                            gifEncoder.init(exportGifWidth, exportGifHeight, exportGiftPath);

                            for (int i = 0; i < mBitmaps.size(); i++) {
                                Bitmap bitmap = mBitmaps.get(i);
                                gifEncoder.encodeFrame(bitmap, 200);
                                progressDialog.setProgress(i+1);
                            }

                            // End encode gif file
                            gifEncoder.close();

                            progressDialog.dismiss();

                            String mime = MediaType.MIME_IMAGE_GIF;
                            String url = exportGiftPath;

                            Intent previewIntent = new Intent(mContext, SharestickersPreviewActivity.class);
                            previewIntent.putExtra(EXTRA_MIME_TYPE, mime);
                            previewIntent.putExtra(EXTRA_URL, url);

                            startActivity(previewIntent);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private int[] getAndCheckSize(ArrayList<Bitmap> bitmaps) {
        int[] size = {0, 0};

        int width, height;
        if (bitmaps.size() > 0) {
            // Get width & height for setting gif exporting file
            Bitmap infoBitmap = bitmaps.get(0);
            width = infoBitmap.getWidth();
            height = infoBitmap.getHeight();

            boolean isDifferenceSize = false;
            for (int i = 1; i < bitmaps.size(); i++) {
                Bitmap bitmap = bitmaps.get(i);
                if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
                    if (BuildConfig.DEBUG) Log.d(DEBUG_TAG, "[getAndCheckSize] - Bitmap at " + i + " order is identical");
                } else {
                    isDifferenceSize = true;
                }
            }

            if (!isDifferenceSize) {
                size[0] = width;
                size[1] = height;
            } else {
                size[0] = -2;
                if (BuildConfig.DEBUG) Log.d(DEBUG_TAG, "[getAndCheckSize] - Bitmaps's size are not identical");
            }
        } else {
            size[0] = -1;
        }

        return size;
    }

    private void showErrorDialog(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }

    private void exportMp4() {
        String mime = MediaType.MIME_VIDEO_MP4;
        String url = Constant.EXPORT_MP4;

        Intent previewIntent = new Intent(this, SharestickersPreviewActivity.class);
        previewIntent.putExtra(EXTRA_MIME_TYPE, mime);
        previewIntent.putExtra(EXTRA_URL, url);

        startActivity(previewIntent);
    }

    private String saveExportGif() throws IOException {
        // Image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Export_Gif_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".gif", storageDir);

        return imageFile.getAbsolutePath();
    }

    private void onAddMenuItemClick() {
        final String[] items = {"Gallery", "Camera"};

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Take Photo From");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        startPickGalleryImage();
                        break;
                    case 1:
                        startPickCameraImage();
                        break;
                    default:
                        break;
                }
            }
        });

        AlertDialog pickFromDialog = alertBuilder.create();
        pickFromDialog.show();
    }

    private void startPickGalleryImage() {
        // Pick image up from Gallery intent
        Intent galleryIntent = new Intent();
        galleryIntent.setType(MediaType.MIME_IMAGE);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private void startPickCameraImage() {
        // Take photo from Camera intent
        File photo = null;
        Intent cameraIntent = null;
        try {
            photo = saveCapturePicture();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photo != null) {
            Uri photoUri = Uri.fromFile(photo);

            cameraIntent = new Intent();
            cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        } else {
            //TODO
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "[onAddMenuItemClick] - saveCapturePicture return NULL");
            }
        } // End check photo is null or not
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAMERA);
    }

    private File saveCapturePicture() throws IOException {
        // Image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Capture_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        captureImagePath = imageFile.getAbsolutePath();

        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY) { // Image from gallery
                receiveGalleryImage(data);
            } else if (requestCode == REQUEST_IMAGE_CAMERA) { // Image from camera capture
                receiveCameraImage();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(DEBUG_TAG, "[onActivityResult] - requestCode is unknown");
                }
            } // End if requestCode
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "[onActivityResult] - resultCode is NOT OK");
            }
        } // End if resultCode

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void receiveGalleryImage(Intent data) {
        Uri receiveUri = data.getData();
        if (receiveUri != null) {
            Log.d(DEBUG_TAG, receiveUri.toString());
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), receiveUri);
                mGridImageAdapter.addImageItem(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "[onActivityResult] - Receive Uri is NULL");
            }
        } // End if receiveUri is null or not
    }

    private void receiveCameraImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(captureImagePath);
        mGridImageAdapter.addImageItem(bitmap);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    class GridPictureItemClickListener implements AdapterView.OnItemClickListener {
        int mPosition;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mPosition = position;

            // Show Delete picture dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Confirm")
                    .setMessage("Delete this picture")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mGridImageAdapter.removeImageItem(mPosition);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        }
    }
}
