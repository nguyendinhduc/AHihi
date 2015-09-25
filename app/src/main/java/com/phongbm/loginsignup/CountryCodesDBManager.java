package com.phongbm.loginsignup;

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

public class CountryCodesDBManager {
    private static final String DATA_PATH = Environment.getDataDirectory()
            + "/data/" + CommonValue.PACKAGE_NAME_MAIN + "/databases/";
    private static final String DATA_NAME = "CountryCodes";

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public CountryCodesDBManager(Context context) {
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

    public ArrayList<CountryCodeItem> getData() {
        this.openDatabase();
        ArrayList<CountryCodeItem> countryCodeItems = new ArrayList<>();
        int indexName, indexPhoneCode;
        String name, phoneCode;
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM CountryCodes", null);
        if (cursor == null)
            return null;
        cursor.moveToFirst();
        indexName = cursor.getColumnIndex("name");
        indexPhoneCode = cursor.getColumnIndex("phoneCode");
        while (!cursor.isAfterLast()) {
            name = cursor.getString(indexName);
            phoneCode = cursor.getString(indexPhoneCode);
            countryCodeItems.add(new CountryCodeItem(name + " (+" + phoneCode + ")", 0));
            cursor.moveToNext();
        }
        cursor.close();
        return countryCodeItems;
    }

}