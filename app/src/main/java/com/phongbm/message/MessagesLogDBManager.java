package com.phongbm.message;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.phongbm.common.CommonValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MessagesLogDBManager {
    private static final String TAG = "MessagesDBManager";

    private static final String DATA_PATH = Environment.getDataDirectory()
            + "/data/" + CommonValue.PACKAGE_NAME_MAIN + "/databases/";
    private static final String DATA_NAME = "Messages";

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public MessagesLogDBManager(Context context) {
        this.context = context;
        this.copyDatabaseFile();
    }

    private void copyDatabaseFile() {
        new File(DATA_PATH).mkdir();
        File file = new File(DATA_PATH + DATA_NAME);
        if (file.exists()) {
            Log.i(TAG, "Exists");
            return;
        }
        try {
            file.createNewFile();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(DATA_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int length = -1;
            byte[] data = new byte[1024];
            while ((length = inputStream.read(data)) > 0) {
                fileOutputStream.write(data, 0, length);
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDatabase() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            sqLiteDatabase = SQLiteDatabase.openDatabase(DATA_PATH + DATA_NAME, null,
                    SQLiteDatabase.OPEN_READWRITE);
        }
    }

    public void closeDatabase() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    public void insertData(ContentValues contentValues) {
        this.openDatabase();
        sqLiteDatabase.insert(DATA_NAME, null, contentValues);
    }

    public void update(ContentValues contentValues) {
        this.openDatabase();
        sqLiteDatabase.update(DATA_NAME, contentValues,
                "id=?", new String[]{contentValues.getAsString("id")});
    }

    public void deleteData(String id) {
        this.openDatabase();
        sqLiteDatabase.delete(DATA_NAME, "id=?", new String[]{id});
    }

    public boolean conversationExist(String id) {
        this.openDatabase();
        Cursor cursor = sqLiteDatabase.query(DATA_NAME, null, "id=?", new String[]{id}, null, null, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public ArrayList<MessagesLogItem> getData() {
        this.openDatabase();
        ArrayList<MessagesLogItem> messagesLogItems = new ArrayList<>();
        int indexId, indexFullName, indexMessage, indexDate, indexIsRead;
        String id, fullName, message, date;
        int isRead;
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM Messages ORDER BY date DESC", null);
        if (cursor == null)
            return null;
        cursor.moveToFirst();
        indexId = cursor.getColumnIndex("id");
        indexFullName = cursor.getColumnIndex("fullName");
        indexMessage = cursor.getColumnIndex("message");
        indexDate = cursor.getColumnIndex("date");
        indexIsRead = cursor.getColumnIndex("isRead");
        while (!cursor.isAfterLast()) {
            id = cursor.getString(indexId);
            fullName = cursor.getString(indexFullName);
            message = cursor.getString(indexMessage);
            date = cursor.getString(indexDate);
            isRead = cursor.getInt(indexIsRead);
            messagesLogItems.add(new MessagesLogItem(id, fullName, message, date, isRead));
            cursor.moveToNext();
        }
        cursor.close();
        return messagesLogItems;
    }

    public void deleteAllData() {
        this.openDatabase();
        sqLiteDatabase.execSQL("DELETE FROM Messages");
    }

}