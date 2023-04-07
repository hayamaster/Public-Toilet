package com.gachon.publictoilet.ApiExtract;

import com.gachon.publictoilet.PublicToilet;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GeoInfoExtract {
    @SerializedName("GeoInfoPublicToiletWGS")
    @Expose
    private RowExtract geoInfo;

    public RowExtract getGeoInfo() {
        return geoInfo;
    }
}
