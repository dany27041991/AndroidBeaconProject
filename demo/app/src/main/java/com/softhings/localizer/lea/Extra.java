package com.softhings.localizer.lea;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Extra {

    @SerializedName("dataSourceObtrusive")
    @Expose
    private Boolean dataSourceObtrusive;

    public Boolean getDataSourceObtrusive() {
        return dataSourceObtrusive;
    }

    public void setDataSourceObtrusive(Boolean dataSourceObtrusive) {
        this.dataSourceObtrusive = dataSourceObtrusive;
    }

}