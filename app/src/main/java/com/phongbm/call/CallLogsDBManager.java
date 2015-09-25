package com.phongbm.call;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.phongbm.common.CommonValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class CallLogsDBManager {
    private static final String TAG = "CallLogsDBManager";

    private static final String DATA_PATH = Environment.getDataDirectory()
            + "/data/" + CommonValue.PACKAGE_NAME_MAIN + "/databases/";
    private static final String DATA_NAME = "CallLogs";

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public CallLogsDBManager(Context context) {
        this.context = context;
        this.copyDatabaseFile();
    }

    private void copyDatabaseFile() {
        new File(DATA_PATH).mkdir();
        File file = new File(DATA_PATH + DATA_NAME);
        if (file.exists()) {
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

    public ArrayList<CallLogItem> getData() {
        this.openDatabase();
        ArrayList<CallLogItem> callLogItems = new ArrayList<>();
        int indexId, indexFullName, indexPhoneNumber, indexDate, indexState;
        String id, fullName, phoneNumber, date, state;
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM CallLogs", null);
        if (cursor == null)
            return null;
        cursor.moveToFirst();
        indexId = cursor.getColumnIndex("id");
        indexFullName = cursor.getColumnIndex("fullName");
        indexPhoneNumber = cursor.getColumnIndex("phoneNumber");
        indexDate = cursor.getColumnIndex("date");
        indexState = cursor.getColumnIndex("state");
        while (!cursor.isAfterLast()) {
            id = cursor.getString(indexId);
            fullName = cursor.getString(indexFullName);
            phoneNumber = cursor.getString(indexPhoneNumber);
            date = cursor.getString(indexDate);
            state = cursor.getString(indexState);
            callLogItems.add(0, new CallLogItem(id, fullName, phoneNumber, date, state));
            cursor.moveToNext();
        }
        cursor.close();
        return callLogItems;
    }

    public void deleteAllData() {
        this.openDatabase();
        sqLiteDatabase.execSQL("DELETE FROM CallLogs");
    }

}