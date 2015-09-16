package com.phongbm.common;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.parse.Parse;
import com.parse.ParseUser;

import java.util.ArrayList;

public class GlobalApplication extends Application {
    public static int WIDTH_SCREEN, HEIGHT_SCREEN;
    public static float DENSITY_DPI;

    private Bitmap avatar;
    private String fullName, phoneNumber;
    private Bitmap pictureSend;
    public static String linkAvatarReceiver = null;
    public static String linkAvatarSender = null;

    private ArrayList<String> idUsers;
    volatile public static boolean checkLoginThisId = false;
    volatile public static boolean startActivityMessage = false;
    volatile public static boolean startWaittingAHihi = false;
    private SharedpreferenceAccount sharedpreferenceAcount;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, ServerInfo.PARSE_APPLICATION_ID, ServerInfo.PARSE_CLIENT_KEY);
        ParseUser.enableAutomaticUser();
        this.initializeComponent();
        sharedpreferenceAcount = new SharedpreferenceAccount(this);
        idUsers = sharedpreferenceAcount.readListID();
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

    public Bitmap getPictureSend() {
        return pictureSend;
    }

    public void setPictureSend(Bitmap pictureSend) {
        this.pictureSend = pictureSend;
    }

    public void addIdUser(String idUser) {
        idUsers.add(idUser);
        sharedpreferenceAcount.writeUserID(idUser);
    }

    public ArrayList<String> getIdUers() {
        return idUsers;
    }


}