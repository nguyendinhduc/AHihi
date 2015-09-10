package com.phongbm.ahihi;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FriendsDBManager {
    private static final String DATA_PATH = Environment.getDataDirectory()
            + "/data/com.phongbm.ahihi/databases/";
    private static final String DATA_NAME = "Friends";

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public FriendsDBManager(Context context) {
        this.context = context;
        copyDatabaseFile();
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
        }
    }

    private void openDatabase() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            sqLiteDatabase = SQLiteDatabase.openDatabase(DATA_PATH + DATA_NAME, null,
                    SQLiteDatabase.OPEN_READWRITE);
        }
    }

    private void closeDatabase() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    private void insertData(ContentValues contentValues) {
        openDatabase();
        sqLiteDatabase.insert(DATA_NAME, null, contentValues);
    }

    private void update(ContentValues contentValues) {
        openDatabase();
        sqLiteDatabase.update(DATA_NAME, contentValues,
                "username=?", new String[]{contentValues.getAsString("username")});
    }

    private void deleteData(String username) {
        openDatabase();
        sqLiteDatabase.delete(DATA_NAME, "name=?", new String[]{username});
    }

}