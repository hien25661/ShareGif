package com.gnt.sharestickers_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by tuan.tn on 3/14/2017.
 */

public class GridImageAdapter extends BaseAdapter {
    private final int BITMAP_MAX_HEIGHT = 500;
    private final int BITMAP_MAX_WIDTH = 500;

    private ArrayList<Bitmap> mBitmaps;
    private Context mContext;
    private LayoutInflater mInflater;

    private int constrainWidth;
    private int constrainHeight;

    public GridImageAdapter(Context context) {
        mContext = context;
        mBitmaps = new ArrayList<>();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mBitmaps.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return mBitmaps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;
        SquareImageView itemImage;
        if (convertView != null) {
            itemView = convertView;
        } else {
            itemView = mInflater.inflate(R.layout.gridview_item, null);
        }

        itemImage = (SquareImageView) itemView.findViewById(R.id.item_image);
        itemImage.setImageBitmap(mBitmaps.get(position));

        return itemView;
    }

    public void addImageItem(Bitmap bitmap) {
        Bitmap resizedBitmap = scaleBitmap(bitmap);
        int width = resizedBitmap.getWidth();
        int height = resizedBitmap.getHeight();

        if (mBitmaps.size() == 0) {
            constrainWidth = width;
            constrainHeight = height;

            mBitmaps.add(resizedBitmap);
            notifyDataSetChanged();
        } else {
            if (width == constrainWidth && height == constrainHeight) {
                mBitmaps.add(resizedBitmap);
                notifyDataSetChanged();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Error")
                        .setMessage("Pictures must be the same size.\nPlease try again")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", null)
                        .show();
            }
        }

    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        int currWidth = bitmap.getWidth();
        int currHeight = bitmap.getHeight();

        if (currWidth > BITMAP_MAX_WIDTH || currHeight > BITMAP_MAX_HEIGHT) {
            int newWidth, newHeight;
            if (currWidth > currHeight) {
                newWidth = BITMAP_MAX_WIDTH;
                newHeight = (newWidth*currHeight) / currWidth;
            } else {
                newHeight = BITMAP_MAX_HEIGHT;
                newWidth = (newHeight*currWidth) / currHeight;
            }

            Matrix matrix = new Matrix();
            float scaleWidth = ((float) newWidth) / currWidth;
            float scaleHeight = ((float) newHeight) / currHeight;
            matrix.postScale(scaleWidth, scaleHeight);

            Bitmap scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, currWidth, currHeight, matrix, false);
            bitmap.recycle();
            return scaleBitmap;
        } else {
            return bitmap;
        }
    }

    public void removeImageItem(int pos) {
        Bitmap removedBitmap = mBitmaps.remove(pos);
        removedBitmap.recycle();
        notifyDataSetChanged();
    }

    public ArrayList<Bitmap> getBitmaps() {
        return mBitmaps;
    }

    public int getConstrainWidth() {
        return constrainWidth;
    }

    public int getConstrainHeight() {
        return constrainHeight;
    }
}
