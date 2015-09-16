package com.phongbm.ahihi;

import android.graphics.Bitmap;

public class ActiveFriendItem extends FriendItem {

    public ActiveFriendItem(String id, Bitmap avatar, String phoneNumber, String fullName) {
        super(id, avatar, phoneNumber, fullName);
    }
    public ActiveFriendItem(String id, String urlAvatar, String phoneNumber, String fullName) {
        super(id, urlAvatar, phoneNumber, fullName);
    }


}