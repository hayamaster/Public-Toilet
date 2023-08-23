package com.gachon.publictoilet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PublicToilet2 {
    @SerializedName("REFINE_WGS84_LAT")
    @Expose
    private double lat;

    @SerializedName("REFINE_WGS84_LOGT")
    @Expose
    private double lon;


    public Double getLat(){
        return lat;
    }

    public Double getLon(){
        return lon;
    }
}
