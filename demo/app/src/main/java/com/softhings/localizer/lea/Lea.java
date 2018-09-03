package com.softhings.localizer.lea;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by danilogiovannico on 23/06/17.
 */

public class Lea {

    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("pilot")
    @Expose
    private String pilot;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("position")
    @Expose
    private String position;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("payload")
    @Expose
    private Payload payload;
    @SerializedName("data_source_type")
    @Expose
    private List<String> dataSourceType = null;
    @SerializedName("extra")
    @Expose
    private Extra extra;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPilot() {
        return pilot;
    }

    public void setPilot(String pilot) {
        this.pilot = pilot;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public List<String> getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(List<String> dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }
}
