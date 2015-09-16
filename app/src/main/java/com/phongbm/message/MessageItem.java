package com.phongbm.message;

import android.graphics.Bitmap;
import android.text.SpannableString;

public class MessageItem {
    private int type;
    private SpannableString content;
    private int mode;
    private Bitmap picture;
    private String date;

    public MessageItem(int type, SpannableString content, int mode, String date) {
        this.type = type;
        this.content = content;
        this.mode = mode;
        this.date = date;
    }

    public MessageItem(int type, SpannableString content, Bitmap picture, int mode, String date) {
        this.type = type;
        this.content = content;
        this.picture = picture;
        this.mode = mode;
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public SpannableString getContent() {
        return content;
    }

    public void setContent(SpannableString content) {
        this.content = content;
    }

    public int getMode() {
        return mode;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public String getDate() {
        return date;
    }

}