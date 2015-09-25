package com.phongbm.message;

public class MessagesLogItem {
    private String id, fullName, message, date;
    private int isRead;

    public MessagesLogItem(String id, String fullName, String message, String date, int isRead) {
        this.id = id;
        this.fullName = fullName;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
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

    public int isRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof MessagesLogItem)) {
            return false;
        }
        return ((MessagesLogItem) object).id.equals(id);
    }

}