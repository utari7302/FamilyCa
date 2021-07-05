package com.usama.familyca.Model;

public class ModelUser {
    private String e_name, e_cnic, e_email, e_completeAddress,
            e_country, e_state, e_city, e_spinner, e_password, uid, accountType, token;
    private double longitude, latitude;
    private boolean silent;

    public ModelUser() {
    }

    public ModelUser(String e_name, String e_cnic, String e_email, String e_completeAddress, String e_country, String e_state, String e_city, String e_spinner, String e_password, String uid, String accountType, double longitude, double latitude, boolean silent, String token) {
        this.e_name = e_name;
        this.e_cnic = e_cnic;
        this.e_email = e_email;
        this.e_completeAddress = e_completeAddress;
        this.e_country = e_country;
        this.e_state = e_state;
        this.e_city = e_city;
        this.e_spinner = e_spinner;
        this.e_password = e_password;
        this.uid = uid;
        this.accountType = accountType;
        this.longitude = longitude;
        this.latitude = latitude;
        this.silent = silent;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getE_name() {
        return e_name;
    }

    public void setE_name(String e_name) {
        this.e_name = e_name;
    }

    public String getE_cnic() {
        return e_cnic;
    }

    public void setE_cnic(String e_cnic) {
        this.e_cnic = e_cnic;
    }

    public String getE_email() {
        return e_email;
    }

    public void setE_email(String e_email) {
        this.e_email = e_email;
    }

    public String getE_completeAddress() {
        return e_completeAddress;
    }

    public void setE_completeAddress(String e_completeAddress) {
        this.e_completeAddress = e_completeAddress;
    }

    public String getE_country() {
        return e_country;
    }

    public void setE_country(String e_country) {
        this.e_country = e_country;
    }

    public String getE_state() {
        return e_state;
    }

    public void setE_state(String e_state) {
        this.e_state = e_state;
    }

    public String getE_city() {
        return e_city;
    }

    public void setE_city(String e_city) {
        this.e_city = e_city;
    }

    public String getE_spinner() {
        return e_spinner;
    }

    public void setE_spinner(String e_spinner) {
        this.e_spinner = e_spinner;
    }

    public String getE_password() {
        return e_password;
    }

    public void setE_password(String e_password) {
        this.e_password = e_password;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
