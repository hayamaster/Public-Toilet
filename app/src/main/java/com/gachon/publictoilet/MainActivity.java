package com.gachon.publictoilet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.gachon.publictoilet.ApiExtract.GeoInfoExtract;
import com.gachon.publictoilet.ApiExtract.RowExtract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.nio.file.attribute.GroupPrincipal;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Context mContext;
    private FloatingActionButton main_btn, x, ok;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    Call<GeoInfoExtract> call;
    private ArrayList<PublicToilet> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 네이버 맵
        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);

        // 버튼
        mContext = getApplicationContext();
        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);
        main_btn = (FloatingActionButton) findViewById(R.id.main_btn);
        x = (FloatingActionButton) findViewById(R.id.x);
        ok = (FloatingActionButton) findViewById(R.id.ok);
    }

    private void setMark(Marker marker,  double lat, double lng)
    {
        //원근감 표시
        marker.setIconPerspectiveEnabled(true);
        //아이콘 지정
        marker.setIcon(OverlayImage.fromResource(R.drawable.toilets));
        //마커의 투명도
        marker.setAlpha(0.8f);
        //마커 위치
        marker.setPosition(new LatLng(lat, lng));
        //마커 우선순위
        marker.setZIndex(0);
        //마커 표시
        marker.setMap(naverMap);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        LatLng mapCenter = naverMap.getCameraPosition().target;


        // 공공화장실
        call = PublicToiletResult.getApiService().test_api_get();
        call.enqueue(new Callback<GeoInfoExtract>(){
            @Override
            public void onResponse(Call<GeoInfoExtract> call, Response<GeoInfoExtract> response) {
                GeoInfoExtract result = response.body();
                data = result.getGeoInfo().getRow();
                Log.i("HA", result.toString());
                if(data.get(1) != null){
                    for(int i = 0; i < 1000; i++){
                        Log.i("imsy", data.get(i).getLon().toString());
                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(data.get(i).getLat(), data.get(i).getLon()));
                        marker.setIcon(OverlayImage.fromResource(R.drawable.toilets));
                        marker.setWidth(120);
                        marker.setHeight(120);
                        marker.setMap(naverMap);
                    }
                }
            }
            @Override
            public void onFailure(Call<GeoInfoExtract> call, Throwable t) {
                Log.i("fail", "failed getting data...");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void ClickBtn(View v){
//        switch (v.getId()) {
//            case R.id.main_btn:
//                toggleFab();
//                break;
//            case R.id.x:
//                toggleFab();
//                Toast.makeText(this, "Camera Open-!", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.ok:
//                toggleFab();
//                Toast.makeText(this, "Map Open-!", Toast.LENGTH_SHORT).show();
//                break;
//        }
        toggleFab();
    }

    private void toggleFab() {
        if (isFabOpen) {
            main_btn.setImageResource(R.drawable.plus);
            x.startAnimation(fab_close);
            ok.startAnimation(fab_close);
            x.setClickable(false);
            ok.setClickable(false);
            isFabOpen = false;
        } else {
            main_btn.setImageResource(R.drawable.plus);
            x.startAnimation(fab_open);
            ok.startAnimation(fab_open);
            x.setClickable(true);
            ok.setClickable(true);
            isFabOpen = true;
        }
    }
}