package com.gnt.sharestickers_android;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;

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

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class PictureCollectionActivity extends AppCompatActivity {
    private final String DEBUG_TAG = getClass().getName();

    public static final String EXTRA_MIME_TYPE = "mime";
    public static final String EXTRA_URL = "url";
    public static final int REQUEST_IMAGE_GALLERY = 1;
    public static final int REQUEST_IMAGE_CAMERA = 2;

    private ActivityPictureCollectionBinding binding;
    private Context mContext;
    private String captureImagePath = "";

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
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void exportGif() {
        try {
            // Get path for gif export file
            exportGiftPath = saveExportGif();

            exportGifWidth = mGridImageAdapter.getConstrainWidth();
            exportGifHeight = mGridImageAdapter.getConstrainHeight();

            mBitmaps = mGridImageAdapter.getBitmaps();
            if (mBitmaps.size() < 1) { // Empty pictures
                showErrorDialog("Error", "Picture collection is empty.\nPlease try again!");

            } else { // Legal case
                ExportGifAsyncTask exportGifAsyncTask = new ExportGifAsyncTask();
                exportGifAsyncTask.execute();
            } // End check mBitmaps size

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onPermissionWriteDenied() {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
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

        Intent previewIntent = new Intent(this, ShareStickersPreviewActivity.class);
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

    private class GridPictureItemClickListener implements AdapterView.OnItemClickListener {
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

    private class ExportGifAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Init progressDialog
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("Building GIF file . . .");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(mBitmaps.size());
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                GifEncoder gifEncoder = new GifEncoder();
                gifEncoder.init(exportGifWidth, exportGifHeight, exportGiftPath);

                // Start encode gif file
                for (int i = 0; i < mBitmaps.size(); i++) {
                    Bitmap bitmap = mBitmaps.get(i);
                    gifEncoder.encodeFrame(bitmap, 200);
                    progressDialog.setProgress(i+1);
                }

                // End encode gif file
                gifEncoder.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();

            // Prepare data to preview
            String mime = MediaType.MIME_IMAGE_GIF;
            String url = exportGiftPath;

            // Preview intent
            Intent previewIntent = new Intent(mContext, ShareStickersPreviewActivity.class);
            previewIntent.putExtra(EXTRA_MIME_TYPE, mime);
            previewIntent.putExtra(EXTRA_URL, url);

            startActivity(previewIntent);
        }
    }

}
