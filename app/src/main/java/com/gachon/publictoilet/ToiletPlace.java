package com.gachon.publictoilet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ToiletPlace {

    public ToiletPlace() {
    }

    public String getIdToken(){return idToken;}

    public void setIdToken(String idToken){ this.idToken = idToken;}

    private String idToken;

    //latitude
    public int getLatitude(){return latitude;}
    public void setLatitude(int latitude){ this.latitude = latitude;}
    private int latitude;

    //longitude
    public int getLongitude(){return longitude;}
    public void setLongitude(int longitude){this.longitude = longitude;}
    private int longitude;

    //name
    public String getToiletName(){return toiletName;}
    public void setToiletName(String toiletName){ this.toiletName = toiletName;}
    private String toiletName;

    //ManNumL(남자대변기)
    public int getManNumL(){return manNumL;}
    public void setManNumL(int manNumL){this.manNumL = manNumL;}
    private int manNumL;

    //ManNumS(남자 소변기)
    public int getManNumS(){return manNumS;}
    public void setManNumS(int manNumS){this.manNumS= manNumS;}
    private int manNumS;

    //WomanNum
    public int getWomanNum(){return womanNum;}
    public void setWomanNum(int womanNum){this.womanNum=womanNum;}
    private int womanNum;

}

