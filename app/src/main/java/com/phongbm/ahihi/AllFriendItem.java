package com.phongbm.ahihi;

import android.graphics.Bitmap;

public class AllFriendItem extends FriendItem implements Comparable {

    public AllFriendItem(String id, Bitmap avatar, String phoneNumber, String fullName) {
        super(id, avatar, phoneNumber, fullName);
    }

    @Override
    public int compareTo(Object another) {
        return super.getFullName().toLowerCase()
                .compareTo(((AllFriendItem) another).getFullName().toLowerCase());
    }

}