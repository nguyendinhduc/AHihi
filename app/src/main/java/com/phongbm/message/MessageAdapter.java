package com.phongbm.message;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.phongbm.ahihi.R;
import com.phongbm.common.GlobalApplication;
import com.phongbm.libs.SquareImageView;
import com.phongbm.libs.TriangleShapeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends BaseAdapter implements View.OnClickListener {
    public static final String TAG = "MessageAdapter";

    public static final int TYPE_COUNT = 2;
    public static final int TYPE_OUTGOING = 0;
    public static final int TYPE_INCOMING = 1;

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_EMOTICON = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_PICTURE = 3;

    private GlobalApplication globalApplication;
    private ArrayList<MessageItem> messageItems;
    private LayoutInflater layoutInflater;
    private Bitmap outGoingMessageAvatar, inComingMessageAvatar;

    public MessageAdapter(Context context, String inComingMessageId) {
        layoutInflater = LayoutInflater.from(context);
        this.messageItems = new ArrayList<>();
        globalApplication = (GlobalApplication) context.getApplicationContext();
        outGoingMessageAvatar = globalApplication.getAvatar();
    }

    @Override
    public int getCount() {
        return messageItems.size();
    }

    @Override
    public MessageItem getItem(int position) {
        return messageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return messageItems.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        int typePre = (position > 0) ? getItemViewType(position - 1) : 0;
        ViewHolder viewHolder;
        if (convertView == null) {
            switch (type) {
                case TYPE_OUTGOING:
                    convertView = layoutInflater.inflate(R.layout.item_message_outgoing, parent, false);
                    break;
                case TYPE_INCOMING:
                    convertView = layoutInflater.inflate(R.layout.item_message_incoming, parent, false);
                    break;
            }
            viewHolder = new ViewHolder();
            viewHolder.space = convertView.findViewById(R.id.space);
            viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
            viewHolder.imgTriangel = (TriangleShapeView) convertView.findViewById(R.id.imgTriangel);
            viewHolder.txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
            viewHolder.layoutPicture = (LinearLayout) convertView.findViewById(R.id.layoutPicture);
            viewHolder.imgPicture = (SquareImageView) convertView.findViewById(R.id.imgPicture);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (type) {
            case TYPE_OUTGOING:
                viewHolder.imgAvatar.setImageBitmap(outGoingMessageAvatar);
                viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#4caf50"));
                break;
            case TYPE_INCOMING:
                viewHolder.imgAvatar.setImageResource(R.drawable.ic_ava_2);
                viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#eeeeee"));
                break;
        }
        if (position == 0 && messageItems.get(0).getMode() == TYPE_EMOTICON) {
            viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#00000000"));
        }
        if (position > 0 && (type == TYPE_OUTGOING && typePre == TYPE_OUTGOING)
                || (type == TYPE_INCOMING && typePre == TYPE_INCOMING)) {
            viewHolder.imgAvatar.setImageResource(R.drawable.ic_transparent);
            viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#00000000"));
        }
        if ((type == TYPE_OUTGOING && typePre == TYPE_INCOMING)
                || (type == TYPE_INCOMING && typePre == TYPE_OUTGOING)) {
            viewHolder.space.setVisibility(View.VISIBLE);
            if (messageItems.get(position).getMode() == 1) {
                viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#00000000"));
            }
        } else {
            viewHolder.space.setVisibility(View.GONE);
        }

        switch (messageItems.get(position).getMode()) {
            case 0:
            case 1:
                viewHolder.txtMessage.setText(messageItems.get(position).getContent());
                viewHolder.txtMessage.setOnClickListener(null);
                viewHolder.txtMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.layoutPicture.setVisibility(View.GONE);
                break;
            case 2:
                viewHolder.txtMessage.setOnClickListener(this);
                String content = messageItems.get(position).getContent().toString();
                String objectId = content.substring(0, content.lastIndexOf("/"));
                String fileName = content.substring(content.lastIndexOf("/") + 1);
                viewHolder.txtMessage.setText(Html.fromHtml(
                        "<u><font color='#827ca3'>" + fileName + "</font></u>   "));
                viewHolder.txtMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.ic_message_download, 0);
                viewHolder.txtMessage.setTag(objectId);
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.layoutPicture.setVisibility(View.GONE);
                break;
            case 3:
                viewHolder.txtMessage.setVisibility(View.GONE);
                viewHolder.layoutPicture.setVisibility(View.VISIBLE);
//                if (messageItems.get(position).getPicture() != null) {
//                    Drawable drawable = viewHolder.imgPicture.getDrawable();
//                    if (drawable != null && drawable instanceof AsyncDrawable) {
//                        PictureAsyncTask pictureAsyncTask = ((AsyncDrawable) drawable).getLoadImageMessage();
//                        if (pictureAsyncTask != null) pictureAsyncTask.cancel(true);
//                    }
//                    viewHolder.imgPicture.setImageBitmap(messageItems.get(position).getPicture());
//                } else {
                    this.loadPicture(position,
                            messageItems.get(position).getContent() == null ? " " : messageItems.get(position).getContent().toString(),
                            viewHolder.imgPicture);
//                }
                break;
        }

        if (messageItems.get(position).getMode() == 1) {
            viewHolder.txtMessage.setBackgroundColor(Color.parseColor("#00000000"));
        } else {
            switch (type) {
                case TYPE_OUTGOING:
                    viewHolder.txtMessage.setBackgroundResource(R.drawable.bg_message_outgoing);
                    break;
                case TYPE_INCOMING:
                    viewHolder.txtMessage.setBackgroundResource(R.drawable.bg_message_incoming);
                    break;
            }
        }
        return convertView;
    }

    @Override
    public void onClick(final View view) {
        final AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
        alertDialog.setTitle("Confirm");
        alertDialog.setMessage("Download");
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DOWNLOAD",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String objectId = view.getTag().toString();
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
                        query.getInBackground(objectId, new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e != null) {
                                    return;
                                }
                                final ParseFile parseFile = parseObject.getParseFile("file");
                                Log.i(TAG, parseFile.getName());
                                Log.i(TAG, parseFile.getUrl());
                                String fileName = parseFile.getName().substring(
                                        parseFile.getName().lastIndexOf("-") + 1);
                                Log.i(TAG, fileName);
                                final String path = Environment.getExternalStorageDirectory().getPath() +
                                        "/" + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
                                Log.i(TAG, path);
                                parseFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, ParseException e) {
                                        if (e != null) {
                                            Toast.makeText(view.getContext(), "Download fail: "
                                                    + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        File file = new File(path);
                                        Log.i(TAG, "new File");
                                        boolean checkCreateFile = false;
                                        int i = 1;
                                        while (!checkCreateFile) {
                                            if (!file.exists()) try {
                                                Log.i(TAG, "!file.exists()");
                                                file.createNewFile();
                                                checkCreateFile = true;
                                                FileOutputStream outputStream = new FileOutputStream(file);
                                                outputStream.write(bytes);
                                                outputStream.close();
                                                Toast.makeText(view.getContext(), "Download successfully",
                                                        Toast.LENGTH_SHORT).show();
                                            } catch (IOException iOE) {
                                                iOE.printStackTrace();
                                            }
                                            else {
                                                int index = path.lastIndexOf(".");
                                                String newPath = path.replace(path.substring(index,
                                                        index + 1), "(" + i + ").");
                                                file = new File(newPath);
                                                i++;
                                            }
                                        }
                                    }
                                }, new ProgressCallback() {
                                    @Override
                                    public void done(Integer progress) {
                                        Log.i(TAG, "percent: " + progress);
                                        // if (progress > 50) parseFile.cancel();
                                    }
                                });
                            }
                        });
                        alertDialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private class ViewHolder {
        View space;
        TriangleShapeView imgTriangel;
        CircleImageView imgAvatar;
        TextView txtMessage;
        LinearLayout layoutPicture;
        SquareImageView imgPicture;
    }

    public void addMessage(int position, MessageItem messageItem) {
        messageItems.add(position, messageItem);
        this.notifyDataSetChanged();
    }

    private void loadPicture(int position, String url, SquareImageView imgPicture) {
        if (cancelPotentialWork(url, imgPicture)) {
            Context context = imgPicture.getContext();
            PictureAsyncTask pictureAsyncTask = new PictureAsyncTask(position, url, imgPicture);
            AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), messageItems
                    .get(position).getPicture() != null ? messageItems.get(position).getPicture() :
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_picture),
                    pictureAsyncTask);
            imgPicture.setImageDrawable(asyncDrawable);
            if (messageItems.get(position).getPicture() == null) {
                pictureAsyncTask.execute();
            }
        }
    }

    private boolean cancelPotentialWork(String url, SquareImageView imgPicture) {
        final PictureAsyncTask pictureAsyncTask = getLoadImageMessage(imgPicture);
        if (pictureAsyncTask != null) {
            if (!url.equals(pictureAsyncTask.url)) {
                pictureAsyncTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<PictureAsyncTask> weakReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, PictureAsyncTask pictureAsyncTask) {
            super(resources, bitmap);
            weakReference = new WeakReference<>(pictureAsyncTask);
        }

        public PictureAsyncTask getLoadImageMessage() {
            return weakReference.get();
        }
    }

    private PictureAsyncTask getLoadImageMessage(SquareImageView imgPicture) {
        if (imgPicture != null) {
            Drawable drawable = imgPicture.getDrawable();
            if (drawable != null && drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getLoadImageMessage();
            }
        }
        return null;
    }

    private class PictureAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        private int position = -1;
        private String url = null;
        private final WeakReference<SquareImageView> weakReference;

        public PictureAsyncTask(int position, String url, SquareImageView imgPicture) {
            this.position = position;
            this.url = url;
            this.weakReference = new WeakReference<>(imgPicture);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (messageItems.get(position).getPicture() != null)
                return null;
            try {
                URL url = new URL(this.url);
                Bitmap picture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return picture;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap picture) {
            SquareImageView imgPicture = weakReference.get();
            if (picture != null && imgPicture != null) {
                messageItems.get(position).setPicture(picture);
                imgPicture.setImageBitmap(picture);
            }
            super.onPostExecute(picture);
        }
    }

}