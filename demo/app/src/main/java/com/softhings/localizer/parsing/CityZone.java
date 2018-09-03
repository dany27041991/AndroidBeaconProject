package com.softhings.localizer.parsing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CityZone {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("center")
    @Expose
    private String center;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

}