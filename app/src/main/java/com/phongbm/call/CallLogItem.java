package com.phongbm.call;

public class CallLogItem {
    private String id, fullName, phoneNumber, date, state;

    public CallLogItem(String id, String fullName, String phoneNumber, String date, String state) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.state = state;
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

    public String getDate() {
        return date;
    }

    public String getState() {
        return state;
    }

}