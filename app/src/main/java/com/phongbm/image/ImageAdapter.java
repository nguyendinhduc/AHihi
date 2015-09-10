package com.phongbm.image;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.GlobalApplication;
import com.phongbm.libs.SquareImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private static final String TAG = "ImageAdapter";
    private Context context;
    private ArrayList<String> imageURLs;
    volatile private ArrayList<Bitmap> bitmaps;
    private LayoutInflater layoutInflater;

    public ImageAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        initializeListImage();
        initializeListImageState();
    }

    private void initializeListImage() {
        imageURLs = new ArrayList<String>();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DATA}, null, null,
                MediaStore.Images.Media.DATE_ADDED + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            imageURLs.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            cursor.moveToNext();
        }
        return;
    }

    private void initializeListImageState() {
        bitmaps = new ArrayList<Bitmap>();
        for ( int i = 0; i < imageURLs.size(); i++ ) {
            bitmaps.add(null);
        }
    }

    @Override
    public int getCount() {
        return imageURLs.size();
    }

    @Override
    public String getItem(int position) {
        return imageURLs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquareImageView imgImage;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_image, parent, false);
            imgImage = (SquareImageView) convertView.findViewById(R.id.imgImage);
            convertView.setTag(imgImage);
        } else {
            imgImage = (SquareImageView) convertView.getTag();
        }

        loadBitmap(position, imageURLs.get(position), imgImage);


        return convertView;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapFromMemory(String data, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(data, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(data, options);
    }

    public void loadBitmap(int position, String data, ImageView imageView) {
        if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(position, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(this.context.getResources(), bitmaps.get(position) != null ? bitmaps.get(position) :
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.download), task);
            imageView.setImageDrawable(asyncDrawable);
            if (bitmaps.get(position) == null) {
                task.execute(data);
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            if (!bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = "";
        private int position = -1;

        public BitmapWorkerTask(int posistion, ImageView imageView) {
            this.position = posistion;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            if (bitmaps.get(this.position) != null) return null;
            else {
                CommonMethod commonMethod = CommonMethod.getInstance();
                float sizeImage = (GlobalApplication.WIDTH_SCREEN - 16 * (GlobalApplication.DENSITY / 160)) / 3;
                int orientation = commonMethod.getOrientation(data);
                return commonMethod.getBitmap(orientation,
                        commonMethod.decodeSampledBitmapFromResource(data, (int) sizeImage, (int) sizeImage));
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    bitmaps.remove(this.position);
                    bitmaps.add(this.position, bitmap);
                }
            }
        }
    }
}