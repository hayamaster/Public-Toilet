package com.gachon.publictoilet.ApiExtract;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ApiExtract{
    @SerializedName("Publtolt")
    @Expose
    private ArrayList<RowExtract2> apiInfo;
    public RowExtract2 getApiInfo() {
        return apiInfo.get(1);
    }
}
