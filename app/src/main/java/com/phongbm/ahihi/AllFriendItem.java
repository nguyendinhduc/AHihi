package com.phongbm.ahihi;

import android.graphics.Bitmap;

public class AllFriendItem extends FriendItem implements Comparable {
    private String urlAvatar;
    // private int type;

    public AllFriendItem(String id, int type) {
        super.setId(id);
        super.setAvatar(null);
        this.urlAvatar = null;
        super.setPhoneNumber(null);
        super.setFullName(null);
        //this.type = type;
    }

    public AllFriendItem(String id, Bitmap avatar, String phoneNumber, String fullName) {
        super(id, avatar, phoneNumber, fullName);
        this.urlAvatar = null;
        // this.type = 1;
    }

    public AllFriendItem(String id, String urlAvatar, String phoneNumber, String fullName) {
        super.setId(id);
        super.setAvatar(null);
        this.urlAvatar = urlAvatar;
        super.setPhoneNumber(phoneNumber);
        super.setFullName(fullName);
        // this.type = 1;
    }

    @Override
    public int compareTo(Object another) {
        return super.getFullName().toLowerCase()
                .compareTo(((AllFriendItem) another).getFullName().toLowerCase());
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof AllFriendItem)) {
            return false;
        }
        return ((AllFriendItem) object).getId().equals(this.getId());
    }

   /* public int getType() {
        return type;
    }*/

}