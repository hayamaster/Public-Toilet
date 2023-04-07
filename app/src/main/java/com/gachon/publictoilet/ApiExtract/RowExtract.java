package com.gachon.publictoilet.ApiExtract;

import android.util.Log;

import com.gachon.publictoilet.PublicToilet;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RowExtract {
    @SerializedName("row")
    @Expose
    private ArrayList<PublicToilet> row;
    public ArrayList<PublicToilet> getRow() {
        Log.i("zona", "si");
        return row;
    }
}

