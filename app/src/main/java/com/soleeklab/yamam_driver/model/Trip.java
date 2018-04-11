package com.soleeklab.yamam_driver.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by fath_soleeklab on 3/25/2018.
 */

public class Trip implements Serializable {
    @SerializedName("data")
    private Trip trip;
    @SerializedName("state")
    private Integer state;
    @SerializedName("tripId")
    private String tripId;
    @SerializedName("riderName")
    private String riderName;
    @SerializedName("riderId")
    private String riderId;
    @SerializedName("pick_up")
    private Location pickUp;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public Location getPickUp() {
        return pickUp;
    }

    public void setPickUp(Location pickUp) {
        this.pickUp = pickUp;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public static Trip fromJson(String json){
        return new Gson().fromJson(json,Trip.class);
    }
}
