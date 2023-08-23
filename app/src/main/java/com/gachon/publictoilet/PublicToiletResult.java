package com.gachon.publictoilet;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PublicToiletResult {
    private static final String URL1 = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/1/1000/";
    private static final String URL2 = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/1001/2000/";
    private static final String URL3 = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/2001/3000/";
    private static final String URL4 = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/3001/4000/";
    private static final String URL5 = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/4001/5000/";
    private static final String URL6 = "http://openapi.seoul.go.kr:8088/667151666f6b6a683931576a75656a/json/GeoInfoPublicToiletWGS/5001/6000/";

    // this is 전국 화장실 API
    private static final String URL_other = "http://api.data.go.kr/openapi/tn_pubr_public_toilet_api?serviceKey=3gp%2BgNqc%2FG9eq3wANZeiJv9VTT41ZR1vVqltt%2BXxdeY%2FJRXjLkALy77q34%2BsUhbmwpnwz8hhd8VGHsUpKjXgnA%3D%3D&pageNo=0&numOfRows=100&type=json/";

    // new try
    private static final String URL_another = "https://openapi.gg.go.kr/Publtolt/";
    public static Retrofit_interface getApiService(){return getInstance().create(Retrofit_interface.class);}
    private static Retrofit getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL_another)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    //

    public static Retrofit_interface getApiService1(){return getInstance1().create(Retrofit_interface.class);}
    public static Retrofit_interface getApiService2(){return getInstance2().create(Retrofit_interface.class);}
    public static Retrofit_interface getApiService3(){return getInstance3().create(Retrofit_interface.class);}
    public static Retrofit_interface getApiService4(){return getInstance4().create(Retrofit_interface.class);}
    public static Retrofit_interface getApiService5(){return getInstance5().create(Retrofit_interface.class);}
    public static Retrofit_interface getApiService6(){return getInstance6().create(Retrofit_interface.class);}

    private static Retrofit getInstance1() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL1)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    private static Retrofit getInstance2() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL2)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    private static Retrofit getInstance3() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL3)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    private static Retrofit getInstance4() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL4)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    private static Retrofit getInstance5() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL5)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    private static Retrofit getInstance6() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(URL6)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}