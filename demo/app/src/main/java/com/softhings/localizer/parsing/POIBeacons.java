package com.softhings.localizer.parsing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class POIBeacons {

    @SerializedName("place")
    @Expose
    private String place;
    @SerializedName("macAddress")
    @Expose
    private String macAddress;

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

}