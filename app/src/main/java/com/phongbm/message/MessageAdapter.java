package com.phongbm.message;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.phongbm.ahihi.R;
import com.phongbm.common.GlobalApplication;
import com.phongbm.common.OnLoadedInformation;
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
    private OnLoadedInformation onLoadedInformation;

    public void setOnLoadedInformation(OnLoadedInformation onLoadedInformation) {
        this.onLoadedInformation = onLoadedInformation;
    }

    public MessageAdapter(Context context, final String inComingMessageId) {
        layoutInflater = LayoutInflater.from(context);
        this.messageItems = new ArrayList<>();
        globalApplication = (GlobalApplication) context.getApplicationContext();
        outGoingMessageAvatar = globalApplication.getAvatar();

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(inComingMessageId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                final boolean isOnline = parseUser.getBoolean("isOnline");
                ParseFile parseFile = (ParseFile) parseUser.get("avatar");
                if (parseFile != null) {
                    parseFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            inComingMessageAvatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            onLoadedInformation.onLoaded(true, isOnline);
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
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
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
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
                viewHolder.imgAvatar.setImageBitmap(inComingMessageAvatar);
                viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#eeeeee"));
                break;
        }
        if (position == 0 && messageItems.get(0).getMode() == TYPE_EMOTICON) {
            viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#00000000"));
        }
        if (position > 0) {
            if ((type == TYPE_OUTGOING && typePre == TYPE_OUTGOING)
                    || (type == TYPE_INCOMING && typePre == TYPE_INCOMING)) {
                viewHolder.imgAvatar.setImageResource(R.drawable.ic_transparent);
                viewHolder.imgTriangel.setBackgroundColor(Color.parseColor("#00000000"));
            }
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
            case TYPE_TEXT:
            case TYPE_EMOTICON:
                viewHolder.txtMessage.setText(messageItems.get(position).getContent());
                viewHolder.txtMessage.setOnClickListener(null);
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.layoutPicture.setVisibility(View.GONE);
                break;
            case TYPE_FILE:
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.layoutPicture.setVisibility(View.GONE);
                viewHolder.txtMessage.setOnClickListener(this);

                String content = messageItems.get(position).getContent().toString();
                String objectId = content.substring(0, content.lastIndexOf("/"));
                String fileName = content.substring(content.lastIndexOf("/") + 1) + "\t\t\t\t.";

                SpannableStringBuilder spannableStringBuilder =
                        new SpannableStringBuilder(Html.fromHtml(
                                "<u><font color='#f44336'><b>" + fileName + "</b></font></u>"));
                Drawable download = ContextCompat.getDrawable(parent.getContext(),
                        R.drawable.ic_message_download);
                download.setBounds(0, 0, download.getIntrinsicWidth() / 2, download.getIntrinsicHeight() / 2);
                ImageSpan imageSpan = new ImageSpan(download, ImageSpan.ALIGN_BASELINE);
                spannableStringBuilder.setSpan(imageSpan, spannableStringBuilder.length() - 1,
                        spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                viewHolder.txtMessage.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);

                viewHolder.txtMessage.setTag(objectId);
                break;
            case TYPE_PICTURE:
                viewHolder.txtMessage.setVisibility(View.GONE);
                viewHolder.layoutPicture.setVisibility(View.VISIBLE);
                viewHolder.imgPicture.setOnClickListener(this);

                if (messageItems.get(position).getPicture() != null) {
                    Drawable drawable = viewHolder.imgPicture.getDrawable();
                    if (drawable != null && drawable instanceof AsyncDrawable) {
                        PictureAsyncTask pictureAsyncTask = ((AsyncDrawable) drawable).getLoadImageMessage();
                        if (pictureAsyncTask != null) {
                            pictureAsyncTask.cancel(true);
                        }
                    }
                    viewHolder.imgPicture.setImageBitmap(messageItems.get(position).getPicture());
                } else {
                    this.loadPicture(position, messageItems.get(position).getContent().toString(),
                            viewHolder.imgPicture);
                }

                viewHolder.imgPicture.setTag(messageItems.get(position).getContent());
                Log.i(TAG, "setTag...");
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

        viewHolder.txtDate.setText(messageItems.get(position).getDate());
        if (position == messageItems.size() - 1) {
            viewHolder.txtDate.setVisibility(View.VISIBLE);
        } else {
            if (getItemViewType(position) == TYPE_OUTGOING
                    && getItemViewType(position + 1) == TYPE_INCOMING
                    || getItemViewType(position) == TYPE_INCOMING
                    && getItemViewType(position + 1) == TYPE_OUTGOING) {
                viewHolder.txtDate.setVisibility(View.VISIBLE);
            } else {
                viewHolder.txtDate.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    @Override
    public void onClick(final View view) {
        int type = -1;
        if (view instanceof TextView) {
            type = TYPE_FILE;
        } else {
            if (view instanceof ImageView) {
                type = TYPE_PICTURE;
            }
        }
        switch (type) {
            case TYPE_FILE:
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
                                        String fileName = parseFile.getName().substring(
                                                parseFile.getName().lastIndexOf("-") + 1);
                                        final String path = Environment.getExternalStorageDirectory().getPath() +
                                                "/" + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
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
                break;
            case TYPE_PICTURE:
                if (view == null) {
                    Log.i(TAG, "View NULL");
                    return;
                }
                SpannableString spannableString = (SpannableString) view.getTag();
                if (spannableString == null) {
                    Log.i(TAG, "NULL SpannableString");
                    return;
                }
                String url = ((SpannableString) view.getTag()).toString();
                Log.i(TAG, url);
                if (url != null) {
                    PictureActivity.launch((MessageActivity) view.getContext(),
                            view.findViewById(R.id.imgPicture), url);
                } else {
                    Log.i(TAG, "NULL");
                }
                break;
        }
    }

    private class ViewHolder {
        View space;
        TriangleShapeView imgTriangel;
        CircleImageView imgAvatar;
        TextView txtMessage, txtDate;
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
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.image_placeholder),
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