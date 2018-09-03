package com.softhings.localizer.parsing;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("userID")
    @Expose
    private String userID;
    @SerializedName("UUID")
    @Expose
    private String uUID;
    @SerializedName("scanPeriod")
    @Expose
    private String scanPeriod;
    @SerializedName("interScanPeriod")
    @Expose
    private String interScanPeriod;
    @SerializedName("ray")
    @Expose
    private String ray;
    @SerializedName("homeBeacons")
    @Expose
    private List<HomeBeacons> homeBeacons = null;
    @SerializedName("POIBeacons")
    @Expose
    private List<POIBeacons> pOIBeacons = null;
    @SerializedName("cityZones")
    @Expose
    private List<CityZone> cityZones = null;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUUID() {
        return uUID;
    }

    public void setUUID(String uUID) {
        this.uUID = uUID;
    }

    public String getScanPeriod() {
        return scanPeriod;
    }

    public void setScanPeriod(String scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    public String getInterScanPeriod() {
        return interScanPeriod;
    }

    public void setInterScanPeriod(String interScanPeriod) {
        this.interScanPeriod = interScanPeriod;
    }

    public String getRay() {
        return ray;
    }

    public void setRay(String ray) {
        this.ray = ray;
    }

    public List<HomeBeacons> getHomeBeacons() {
        return homeBeacons;
    }

    public void setHomeBeacons(List<HomeBeacons> homeBeacons) {
        this.homeBeacons = homeBeacons;
    }

    public List<POIBeacons> getPOIBeacons() {
        return pOIBeacons;
    }

    public void setPOIBeacons(List<POIBeacons> pOIBeacons) {
        this.pOIBeacons = pOIBeacons;
    }

    public List<CityZone> getCityZones() {
        return cityZones;
    }

    public void setCityZones(List<CityZone> cityZones) {
        this.cityZones = cityZones;
    }

}