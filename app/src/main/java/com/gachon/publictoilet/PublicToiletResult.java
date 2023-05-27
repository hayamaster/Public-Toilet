package com.gachon.publictoilet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PublicToiletResult {
    private static final String URL = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/1/1000/";

    // this is 전국 화장실 API
    private static final String URL2 = "http://api.data.go.kr/openapi/tn_pubr_public_toilet_api?serviceKey=3gp%2BgNqc%2FG9eq3wANZeiJv9VTT41ZR1vVqltt%2BXxdeY%2FJRXjLkALy77q34%2BsUhbmwpnwz8hhd8VGHsUpKjXgnA%3D%3D&pageNo=0&numOfRows=100&type=json";

    public static Retrofit_interface getApiService(){return getInstance().create(Retrofit_interface.class);}

    private static Retrofit getInstance() {

        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}