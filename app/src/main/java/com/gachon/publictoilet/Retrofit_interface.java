package com.gachon.publictoilet;

import com.gachon.publictoilet.ApiExtract.GeoInfoExtract;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Retrofit_interface {
    @GET(".")
    Call<GeoInfoExtract> test_api_get();
}