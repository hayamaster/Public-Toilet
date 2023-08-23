package com.gachon.publictoilet.ApiExtract;

import android.util.Log;

import com.gachon.publictoilet.PublicToilet2;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RowExtract2 {
    @SerializedName("row")
    @Expose
    private ArrayList<PublicToilet2> row;
    public ArrayList<PublicToilet2> getRow() {
        return row;
    }
}
