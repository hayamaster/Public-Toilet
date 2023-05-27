package com.gachon.publictoilet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PublicToilet {
//    @SerializedName("OBJECTID")
//    @Expose
//    private String id;

    @SerializedName("LAT")
    @Expose
    private double lat;

    @SerializedName("LNG")
    @Expose
    private double lon;

//    public String getID(){
//        return id;
//    }

    public Double getLat(){
        return lat;
    }

    public Double getLon(){
        return lon;
    }
}
