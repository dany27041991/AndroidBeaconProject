package com.softhings.localizer.parsing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HomeBeacons {

    @SerializedName("room")
    @Expose
    private String room;
    @SerializedName("macAddress")
    @Expose
    private String macAddress;

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

}