package com.gachon.publictoilet;

import com.gachon.publictoilet.ApiExtract.ApiExtract;
import com.gachon.publictoilet.ApiExtract.GeoInfoExtract;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Retrofit_interface {
    @GET(".")
    Call<ApiExtract> test_api_get(
            @Query("KEY") String KEY,
            @Query("Type") String Type,
            @Query("pIndex") Number pIndex,
            @Query("pSize") Number pSize
    );
//    Call<GeoInfoExtract> test_api_get();
}