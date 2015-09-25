package com.phongbm.common;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.parse.Parse;
import com.phongbm.ahihi.ActiveFriendItem;
import com.phongbm.ahihi.AllFriendItem;

import java.util.ArrayList;

public class GlobalApplication extends Application {
    public static final String TAG = "GlobalApplication";
    public static int WIDTH_SCREEN, HEIGHT_SCREEN;

    private Bitmap avatar;
    private String fullName, phoneNumber, email;
    private Bitmap pictureSend;
    private ArrayList<AllFriendItem> allFriendItems;
    private ArrayList<ActiveFriendItem> activeFriendItems;

    private ArrayList<String> idUsers;
    volatile public static boolean checkLoginThisId = false;
    volatile public static boolean startActivityMessage = false;
    volatile public static boolean startWaitingAHihi = false;
    private SharedPreferencesAHihi sharedPreferencesAHihi;
    public static float DENSITY_DPI;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()...");
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, ServerInfo.PARSE_APPLICATION_ID, ServerInfo.PARSE_CLIENT_KEY);
        this.initializeComponent();
        sharedPreferencesAHihi = new SharedPreferencesAHihi(this);
        idUsers = sharedPreferencesAHihi.readListID();
        allFriendItems = new ArrayList<>();
        activeFriendItems = new ArrayList<>();
    }

    private void initializeComponent() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        WIDTH_SCREEN = metrics.widthPixels;
        HEIGHT_SCREEN = metrics.heightPixels;
        DENSITY_DPI = metrics.densityDpi;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getPictureSend() {
        return pictureSend;
    }

    public void setPictureSend(Bitmap pictureSend) {
        this.pictureSend = pictureSend;
    }

    public ArrayList<AllFriendItem> getAllFriendItems() {
        return allFriendItems;
    }

    public void setAllFriendItems(ArrayList<AllFriendItem> allFriendItems) {
        this.allFriendItems = allFriendItems;
    }

    public ArrayList<ActiveFriendItem> getActiveFriendItems() {
        return activeFriendItems;
    }

    public void setActiveFriendItems(ArrayList<ActiveFriendItem> activeFriendItems) {
        this.activeFriendItems = activeFriendItems;
    }

    public void addIdUser(String idUser) {
        idUsers.add(idUser);
        sharedPreferencesAHihi.writeUserID(idUser);
    }

    public ArrayList<String> getIdUers() {
        return idUsers;
    }

}