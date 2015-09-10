package com.phongbm.loginsignup;

public class CountryCodeItem {
    private String countryCode;
    private int type;

    public CountryCodeItem(String countryCode, int type) {
        this.countryCode = countryCode;
        this.type = type;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getType() {
        return type;
    }

}