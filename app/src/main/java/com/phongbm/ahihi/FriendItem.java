package com.phongbm.ahihi;

import android.graphics.Bitmap;

public class FriendItem {
    private String id, fullName, phoneNumber;
    private Bitmap avatar;

    public FriendItem() {
    }

    public FriendItem(String id, Bitmap avatar, String phoneNumber, String fullName) {
        this.id = id;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}