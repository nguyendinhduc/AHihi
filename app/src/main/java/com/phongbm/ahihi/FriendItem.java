package com.phongbm.ahihi;

import android.graphics.Bitmap;

public class FriendItem {
    private String id, fullName, phoneNumber;
    private Bitmap avatar;

    public FriendItem(String id, Bitmap avatar, String phoneNumber, String fullName) {
        this.id = id;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

}