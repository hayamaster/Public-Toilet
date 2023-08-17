package com.gachon.publictoilet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.naver.maps.map.CameraPosition;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Context mContext;
    private FloatingActionButton main_btn, x, ok;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    Call<GeoInfoExtract> call;
    private ArrayList<PublicToilet> data;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 네이버 맵
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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

//    private void setMark(Marker marker, double lat, double lng) {
//        //원근감 표시
//        marker.setIconPerspectiveEnabled(true);
//        //아이콘 지정
//        marker.setIcon(OverlayImage.fromResource(R.drawable.toilets));
//        //마커의 투명도
//        marker.setAlpha(0.8f);
//        //마커 위치
//        marker.setPosition(new LatLng(lat, lng));
//        //마커 우선순위
//        marker.setZIndex(0);
//        //마커 표시
//        marker.setMap(naverMap);
//    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        // 네이버 지도에 위치 추적 소스를 설정
        naverMap.setLocationSource(locationSource);

        // 네이버 지도의 UI 설정
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        // 네이버 지도 렌더링 시, 나의 위치 표시하기 (버전 1)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//
//            String locationProvider = LocationManager.NETWORK_PROVIDER;
////            Location location = locationManager.getLastKnownLocation(locationProvider);
//            double cur_lat = location.getLatitude();
//            double cur_lon = location.getLongitude();
//            Toast.makeText(getApplicationContext(), "zzzz "+ cur_lat, Toast.LENGTH_LONG).show();
//            naverMap.setCameraPosition(new CameraPosition(new LatLng(cur_lat, cur_lon), 15, 0, 0));
//        }

        // 네이버 지도 렌더링 시, 나의 위치 표시하기 (버전 2)
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }else {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ? mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) : mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location == null){
                Toast.makeText(this, "위치가 없습니다.", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "위도: " +location.getLatitude() + "경도: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                naverMap.setCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 15, 0, 0));
            }
        }



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

    // 위치 정보 업데이트 코드
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // 위치 리스너는 위치정보를 전달할 때 호출되므로 onLocationChanged()메소드 안에 위지청보를 처리를 작업을 구현 해야합니다.
            double longitude = location.getLongitude(); // 위도
            double latitude = location.getLatitude(); // 경도
            Toast.makeText(getApplicationContext(), "else "+ latitude + longitude, Toast.LENGTH_LONG).show();
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };
}


