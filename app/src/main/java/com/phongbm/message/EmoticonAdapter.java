package com.phongbm.message;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.libs.SquareImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class EmoticonAdapter extends BaseAdapter {
    private static final String TAG = "EmoticonAdapter";
    private ArrayList<EmoticonItem> emoticonItems;
    private LayoutInflater layoutInflater;
    private String inComingMessageId;
    private final int MAX_SiZE_EMOTION;
    private Context context;

    public EmoticonAdapter(Context context, ArrayList<EmoticonItem> emoticonItems,
                           String inComingMessageId) {
        MAX_SiZE_EMOTION = (int) (((float) GlobalApplication.WIDTH_SCREEN - 9 * 4 * (GlobalApplication.DENSITY / 160) / 4));
        this.emoticonItems = emoticonItems;
        this.inComingMessageId = inComingMessageId;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return emoticonItems.size();
    }

    @Override
    public EmoticonItem getItem(int position) {
        return emoticonItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return emoticonItems.get(position).getEmotionId();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_emoticon, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgEmoticon = (SquareImageView) convertView.findViewById(R.id.imgEmoticon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        loadBitmap(emoticonItems.get(position).getEmotionId(), viewHolder.imgEmoticon);

        viewHolder.imgEmoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int emoticonId = (int) EmoticonAdapter.this.getItemId(position);
                Intent intentEmoticon = new Intent();
                intentEmoticon.setAction(CommonValue.ACTION_SEND_MESSAGE);
                intentEmoticon.putExtra(CommonValue.INCOMING_MESSAGE_ID, inComingMessageId);
                intentEmoticon.putExtra(CommonValue.MESSAGE_CONTENT, "" + emoticonId);
                intentEmoticon.putExtra(CommonValue.AHIHI_KEY, CommonValue.AHIHI_KEY_EMOTICON);
                parent.getContext().sendBroadcast(intentEmoticon);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        SquareImageView imgEmoticon;
    }

    public void loadBitmap(int resId, SquareImageView imgEmoticon) {
        if (cancelPotentialWork(resId, imgEmoticon)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imgEmoticon);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), null, task);
            imgEmoticon.setImageDrawable(asyncDrawable);
            task.execute(resId);
        }
    }

    public static boolean cancelPotentialWork(int data, SquareImageView imgEmoticon) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imgEmoticon);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
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

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

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

    private static BitmapWorkerTask getBitmapWorkerTask(SquareImageView imgEmoticon) {
        if (imgEmoticon != null) {
            final Drawable drawable = imgEmoticon.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<SquareImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(SquareImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<SquareImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            Bitmap mBimtap = null;
            try {
                mBimtap = BitmapFactory.decodeResource(EmoticonAdapter.this.context.getResources(), data);
            } catch (OutOfMemoryError outOfMemoryError) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), data, options);
                int width = options.outWidth;
                int height = options.outHeight;
                Pair<Integer, Integer> pair = CommonMethod.getInstance().
                        getStandSizeBitmap(width, height, MAX_SiZE_EMOTION, MAX_SiZE_EMOTION);
                mBimtap = CommonMethod.decodeSampledBitmapFromResource(context.getResources(),
                        data, pair.first, pair.second);

            }
            return mBimtap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final SquareImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}