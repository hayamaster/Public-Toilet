package com.gachon.publictoilet.ApiExtract;

import android.util.Log;

import com.gachon.publictoilet.PublicToilet;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GeoInfoExtract {
    @SerializedName("GeoInfoPublicToiletWGS")
    @Expose
    private RowExtract geoInfo;

    public RowExtract getGeoInfo() {
        Log.i("bozs", geoInfo.toString());
        return geoInfo;
    }
}

