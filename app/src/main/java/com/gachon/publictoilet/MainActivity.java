package com.gachon.publictoilet;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
//<<<<<<< HEAD
//import android.content.pm.PackageManager;
//import android.graphics.PointF;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//=======
//>>>>>>> 80a53f8541b0e7c44c9317b8f7bb6d1cc2f825e2
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.gachon.publictoilet.ApiExtract.ApiExtract;
import com.gachon.publictoilet.ApiExtract.GeoInfoExtract;
import com.google.android.gms.common.api.Api;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Context mContext;
    private FloatingActionButton main_btn, x, ok;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    Call<GeoInfoExtract> call;
//    private ArrayList<PublicToilet> data = new ArrayList<>();
    private ArrayList<PublicToilet2> data= new ArrayList<>();
    private LocationManager mLocationManager;
    private double mlat;
    private double mlon;
    private ArrayList<Marker> marked = new ArrayList<>();
    private Marker clickedMark = new Marker();

    private DatabaseReference mDatabaseRef;
    private EditText mAddress;
    private Button mSearch;
    private SearchView searchView;

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

        searchView = findViewById(R.id.search_view);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener{
//
//        });
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

        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        // 네이버 지도 렌더링 시, 나의 위치 표시하기
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ? mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) : mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                Toast.makeText(this, "위치가 없습니다.", Toast.LENGTH_LONG).show();
            } else {
                mlat = location.getLatitude();
                mlon = location.getLongitude();
                Toast.makeText(this, "위도: " + mlat + "경도: " + mlon, Toast.LENGTH_LONG).show();
                naverMap.setCameraPosition(new CameraPosition(new LatLng(mlat, mlon), 15, 0, 0));
            }
        }

        // 공공화장실 데이터
        // 서울시 화장실 api
//        ArrayList<Call<GeoInfoExtract>> getApiServices = new ArrayList<>(
//                Arrays.asList(
//                        PublicToiletResult.getApiService1().test_api_get()
////                        PublicToiletResult.getApiService2().test_api_get(),
////                        PublicToiletResult.getApiService3().test_api_get(),
////                        PublicToiletResult.getApiService4().test_api_get(),
////                        PublicToiletResult.getApiService5().test_api_get(),
////                        PublicToiletResult.getApiService6().test_api_get()
//                )
//        );
        ArrayList<Call<ApiExtract>> getApiServices = new ArrayList<>(
                Arrays.asList(
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 1, 1000),
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 2, 1000),
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 3, 1000),
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 4, 1000),
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 5, 1000),
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 6, 1000),
                        PublicToiletResult.getApiService().test_api_get("fb6f9bdc95274f0d96598c6568334936", "json", 7, 1000)
                )
        );

        fetchApi(getApiServices, naverMap);

        // 맵을 클릭할 시, 클릭한 곳의 위치를 기반으로 새롭게 화장실 마크를 구성
        naverMap.setOnMapClickListener((point, coord) -> {
            mlat = coord.latitude;
            mlon = coord.longitude;
            // 이전에 표시된 마크들을 삭제
            for(Marker mark : marked){
                mark.setMap(null);
            }
            marked.clear();

            // 클릭한 위치에 빨간 핀 세우기
            clickedMark.setMap(null);
            clickedMark.setPosition(new LatLng(mlat, mlon));
            clickedMark.setIcon(OverlayImage.fromResource(R.drawable.pin));
            clickedMark.setWidth(120);
            clickedMark.setHeight(120);
            clickedMark.setMap(naverMap);

            fetchApi(getApiServices, this.naverMap);
        });
    }

    // 공공화장실 다중 API 호출하기
    // 경기도 공중화장실
    public void fetchApi(ArrayList<Call<ApiExtract>> getApiServices, NaverMap naverMap) {
        for (Call<ApiExtract> call : getApiServices) {
            call.clone().enqueue(new Callback<ApiExtract>() {
                @Override
                public void onResponse(Call<ApiExtract> call, Response<ApiExtract> response) {
                    ApiExtract result = response.body();
                    data = result.getApiInfo().getRow();
                    for (int i = 0; i < data.size(); i++) {
                        double dist = (DistanceByDegreeAndroid(data.get(i).getLat(), data.get(i).getLon()) / 1000);
                        if (dist <= 2) {
                            Log.i("imsy", data.get(i).getLat().toString());
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(data.get(i).getLat(), data.get(i).getLon()));
                            marker.setIcon(OverlayImage.fromResource(R.drawable.toilets));
                            marker.setWidth(120);
                            marker.setHeight(120);
                            marker.setMap(naverMap);
                            marked.add(marker);
                        }
                    }
                };

                @Override
                public void onFailure(Call<ApiExtract> call, Throwable t) {
                    Log.i("fail", "failed getting data...");
                };
            });
        }
    };

    // 서울시 화장실 api
//    public void fetchApi(ArrayList<Call<GeoInfoExtract>> getApiServices, NaverMap naverMap) {
//        for (Call<GeoInfoExtract> call : getApiServices) {
//            call.clone().enqueue(new Callback<GeoInfoExtract>() {
//                @Override
//                public void onResponse(Call<GeoInfoExtract> call, Response<GeoInfoExtract> response) {
//                    GeoInfoExtract result = response.body();
//                    data = result.getGeoInfo().getRow();
//                    for (int i = 0; i < data.size(); i++) {
//                        double dist = (DistanceByDegreeAndroid(data.get(i).getLat(), data.get(i).getLon()) / 1000);
//                        if (dist <= 2) {
//                            Log.i("imsy", data.get(i).getLat().toString());
//                            Marker marker = new Marker();
//                            marker.setPosition(new LatLng(data.get(i).getLat(), data.get(i).getLon()));
//                            marker.setIcon(OverlayImage.fromResource(R.drawable.toilets));
//                            marker.setWidth(120);
//                            marker.setHeight(120);
//                            marker.setMap(naverMap);
//                            marked.add(marker);
//                        }
//                    }
//                };
//
//                @Override
//                public void onFailure(Call<GeoInfoExtract> call, Throwable t) {
//                    Log.i("fail", "failed getting data...");
//                };
//            });
//        }
//    };


    // 내 위치와 화장실 지점(위도, 경도) 사이의 거리
    public double DistanceByDegreeAndroid(double _latitude2, double _longitude2){
        Location startPos = new Location("PointA");
        Location endPos = new Location("PointB");

        startPos.setLatitude(mlat);
        startPos.setLongitude(mlon);
        endPos.setLatitude(_latitude2);
        endPos.setLongitude(_longitude2);

        double distance = startPos.distanceTo(endPos);

        return distance;
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

