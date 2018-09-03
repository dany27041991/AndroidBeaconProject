package com.softhings.localizer.estimote;

public class EstimoteCloudBeaconDetails {

    private String beaconName;

    public EstimoteCloudBeaconDetails(String beaconName) {
        this.beaconName = beaconName;
    }

    public String getBeaconName() {
        return beaconName;
    }


    @Override
    public String toString() {
        return "[beaconName: " + getBeaconName();
    }
}
