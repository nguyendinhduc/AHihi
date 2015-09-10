package com.phongbm.message;

import android.graphics.Bitmap;
import android.text.SpannableString;

public class MessagesLogItem {
    private String id, fullName, date;
    private SpannableString message;
    private Bitmap avatar = null;

    public MessagesLogItem(String id, String fullName, SpannableString message, String date) {
        this.id = id;
        this.fullName = fullName;
        this.message = message;
        this.date = date;
    }
    public MessagesLogItem(String id, String fullName, SpannableString message, String date, Bitmap avatar) {
        this.id = id;
        this.fullName = fullName;
        this.message = message;
        this.date = date;
        this.avatar = avatar;
    }

    public SpannableString getMessage() {
        return message;
    }

    public void setMessage(SpannableString message) {
        this.message = message;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }
}