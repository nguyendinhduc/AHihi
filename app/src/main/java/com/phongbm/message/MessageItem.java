package com.phongbm.message;

import android.graphics.Bitmap;
import android.text.SpannableString;

public class MessageItem {
    private int type;
    private SpannableString content;
    private int mode;
    private Bitmap picture;

    public MessageItem(int type, SpannableString content, int mode) {
        this.type = type;
        this.content = content;
        this.mode = mode;
    }

    public MessageItem(int type, Bitmap picture, int mode) {
        this.type = type;
        this.picture = picture;
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public SpannableString getContent() {
        return content;
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

}