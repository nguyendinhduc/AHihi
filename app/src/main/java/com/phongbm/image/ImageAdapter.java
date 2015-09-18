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
import android.support.v4.content.ContextCompat;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private static final String TAG = "ImageAdapter";
    private Context context;
    private ArrayList<String> imageURLs;
    volatile private ArrayList<ImageState> imageStates;
    private LayoutInflater layoutInflater;
    private final int SIZE_IMAGE;

    public ImageAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        initializeListImage();
        for (String i : imageURLs) {
            Log.i(TAG, "uri: " + i);
        }

        SIZE_IMAGE = (GlobalApplication.WIDTH_SCREEN - CommonMethod.getInstance().
                convertSizeIcon(GlobalApplication.DENSITY_DPI, 4) * 4)/ 3;

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
        imageStates = new ArrayList<ImageState>();
        for (int i = 0; i < imageURLs.size(); i++) {
            imageStates.add(new ImageState());
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SquareImageView imgImage;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_image, parent, false);
            imgImage = (SquareImageView) convertView.findViewById(R.id.imgImage);
            convertView.setTag(imgImage);
        } else {
            imgImage = (SquareImageView) convertView.getTag();
        }
        // loadBitmap(position, imageURLs.get(position), imgImage);
        Picasso.with(parent.getContext())
                .load(new File(imageURLs.get(position)))
                .resize(SIZE_IMAGE, SIZE_IMAGE)
                .placeholder(R.drawable.loading_picture)
                .error(R.drawable.ic_launcher_ahihi)
                .centerCrop()
                .into(imgImage);

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

    private class ImageState {
        Bitmap image;
        boolean isLoading = false, isFinish = false;
    }

    private class ImageAsyncTask extends AsyncTask<String, Bitmap, Void> {
        int position = -1;
        ImageView picture = null;

        public ImageAsyncTask(int position, ImageView picture) {
            this.position = position;
            this.picture = picture;
        }

        @Override
        protected Void doInBackground(String... params) {
            Bitmap bitmap = decodeSampledBitmapFromMemory(params[0], 100, 100);
            publishProgress(bitmap);
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            picture.setImageBitmap(values[0]);
            imageStates.get(this.position).image = values[0];
            imageStates.get(this.position).isFinish = true;
        }
    }

    public void loadBitmap(int position, String data, ImageView imageView) {
        if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(position, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(this.context.getResources(), imageStates.get(position).image != null ? imageStates.get(position).image :
                            BitmapFactory.decodeResource(context.getResources(), R.drawable.download), task);
            imageView.setImageDrawable(asyncDrawable);
            if (imageStates.get(position).image == null) {
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
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
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
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.position = posistion;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            if (imageStates.get(this.position).image != null) return null;
            else return CommonMethod.getInstance().decodeSampledBitmapFromResource(data, 300, 300);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    imageStates.get(this.position).image = bitmap;
                    imageStates.get(this.position).isFinish = true;
                }
            }
        }
    }


}