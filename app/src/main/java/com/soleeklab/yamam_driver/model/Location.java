package com.soleeklab.yamam_driver.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by fath_soleeklab on 3/14/2018.
 */

public class Location implements Serializable {

    @SerializedName("lat")
    private Double lat;
    @SerializedName("lon")
    private Double lon;

    public Location(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
