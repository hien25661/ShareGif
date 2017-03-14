package com.gnt.sharestickers_android;

import android.content.Context;
import android.graphics.Bitmap;
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
    private ArrayList<Bitmap> mBitmaps;
    private Context mContext;
    private LayoutInflater mInflater;

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
        ImageView itemImage;
        if (convertView != null) {
            itemView = convertView;
        } else {
            itemView = mInflater.inflate(R.layout.gridview_item, null);
        }

        itemImage = (ImageView) itemView.findViewById(R.id.item_image);
        itemImage.setImageBitmap(mBitmaps.get(position));

        return itemView;
    }

    public void addImageItem(Bitmap bitmap) {
        mBitmaps.add(bitmap);
        notifyDataSetChanged();
    }

    public void removeImageItem(int pos) {
        mBitmaps.remove(pos);
        notifyDataSetChanged();
    }

    public ArrayList<Bitmap> getBitmaps() {
        return mBitmaps;
    }
}
