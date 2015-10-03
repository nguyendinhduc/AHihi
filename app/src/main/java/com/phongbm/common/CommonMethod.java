package com.phongbm.common;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Pair;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.ahihi.ActiveFriendItem;
import com.phongbm.ahihi.AllFriendItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommonMethod {
    private static CommonMethod commonMethod;

    public static CommonMethod getInstance() {
        if (commonMethod == null) {
            commonMethod = new CommonMethod();
        }
        return commonMethod;
    }

    public CommonMethod() {
    }

    public void pushNotification(Activity srcActivity, Class destActivity, String content,
                                 int notificationId, int icon, boolean noClear) {
        if (noClear) {
            // outGoingCall, inComingCall
            Builder builder = new Builder(srcActivity).setSmallIcon(icon).setContentTitle("AHihi")
                    .setContentText(content).setAutoCancel(false);
            Intent intent = new Intent(srcActivity, destActivity);
            PendingIntent pendingIntent = PendingIntent.getService(srcActivity, notificationId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager)
                    srcActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notificationManager.notify(notificationId, notification);
        } else {
            // missedCall
            Builder builder = new Builder(srcActivity).setSmallIcon(icon).setContentTitle("AHihi")
                    .setContentText(content).setAutoCancel(true);
            Intent intent = new Intent(srcActivity, destActivity);
            PendingIntent pendingIntent = PendingIntent.getActivity(srcActivity, notificationId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager)
                    srcActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, builder.build());
        }
    }

    public String convertTimeToString(int timeCall) {
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(timeCall);
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(timeCall) - minutes * 60;
        String time = (minutes < 10 ? "0" + minutes : "" + minutes)
                + ":" + (seconds < 10 ? "0" + seconds : "" + seconds);
        return time;
    }

    public String getCurrentDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public String getMessageDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static void uploadAvatar(ParseUser parseUser, Bitmap avatar) {
        if (parseUser == null) return;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        avatar.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        if (bytes != null) {
            ParseFile parseFile = new ParseFile(bytes);
            parseUser.put("avatar", parseFile);
            parseUser.saveInBackground();
        }
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SpannableString toSpannableString(Context context, int emoticonId) {
        SpannableString spannableString = new SpannableString(String.valueOf(emoticonId));
        Drawable drawable = ContextCompat.getDrawable(context, emoticonId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth() / 2, drawable.getIntrinsicHeight() / 2);
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(imageSpan, 0, spannableString.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public Bitmap decodeSampledBitmapFromResource(String uri,
                                                  int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
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
        return inSampleSize * 2;
    }

    public Pair<Integer, Integer> getStandSizeBitmap(int width, int height,
                                                     final int WIDTH_IMAGE_MAX,
                                                     final int HEIGHT_IMAGE_MAX) {
        if (width < WIDTH_IMAGE_MAX && height < HEIGHT_IMAGE_MAX) {
            return null;
        }
        if (width > WIDTH_IMAGE_MAX) {
            height = (int) ((float) (WIDTH_IMAGE_MAX) / width * height);
            width = WIDTH_IMAGE_MAX;
        }
        if (height > HEIGHT_IMAGE_MAX) {
            width = (int) ((float) (HEIGHT_IMAGE_MAX) / height * width);
            height = HEIGHT_IMAGE_MAX;
        }
        return new Pair<>(width, height);
    }

    public int getOrientation(String path) {
        int rotate = 0;
        try {
            File file = new File(path);
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public Bitmap getBitmap(int orientation, Bitmap bitmap) {
        if (orientation == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    public void loadListFriend(ParseUser currentUser, Activity activity) {
        final ArrayList<String> listFriendId = (ArrayList<String>) currentUser.get("listFriend");
        if (listFriendId == null || listFriendId.size() == 0) {
            return;
        }
        final ArrayList<AllFriendItem> allFriendItems = ((GlobalApplication)
                activity.getApplication()).getAllFriendItems();
        if (allFriendItems != null) {
            allFriendItems.clear();
        }
        final ArrayList<ActiveFriendItem> activeFriendItems = ((GlobalApplication)
                activity.getApplication()).getActiveFriendItems();
        if (activeFriendItems != null) {
            activeFriendItems.clear();
        }
        for (int i = 0; i < listFriendId.size(); i++) {
            ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
            parseQuery.getInBackground(listFriendId.get(i), new GetCallback<ParseUser>() {
                @Override
                public void done(final ParseUser parseUser, ParseException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    ParseFile parseFile = (ParseFile) parseUser.get("avatar");
                    if (parseFile == null) {
                        return;
                    }
                    parseFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e != null) {
                                return;
                            }
                            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            String id = parseUser.getObjectId();
                            String phoneNumber = parseUser.getUsername();
                            String fullName = parseUser.getString("fullName");

                            //null
                            allFriendItems.add(new AllFriendItem(id, avatar, phoneNumber, fullName));
                            Collections.sort(allFriendItems);
                            if (parseUser.getBoolean("isOnline")) {
                                activeFriendItems.add(new ActiveFriendItem(id, avatar, phoneNumber, fullName));
                            }
                        }
                    });
                }
            });
        }
    }

    public int convertSizeIcon(float density, int sizeDp) {
        return (int) (sizeDp * (density / 160));
    }

    public String getPathFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }

}