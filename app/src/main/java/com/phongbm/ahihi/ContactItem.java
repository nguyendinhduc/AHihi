package com.phongbm.ahihi;

import android.net.Uri;

public class ContactItem implements Comparable {
    private String phoneNumber, name;
    private Uri photo;

    public ContactItem(String phoneNumber, String name, Uri photo) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.photo = photo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }

    public Uri getPhoto() {
        return photo;
    }

    @Override
    public int compareTo(Object another) {
        return name.toLowerCase().compareTo(((ContactItem) another).name.toLowerCase());
    }

}