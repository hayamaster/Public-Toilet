package com.gachon.publictoilet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private ArrayList<Call<ApiExtract>> getApiServices;
    private double mlat;
    private double mlon;
    private ArrayList<Marker> marked = new ArrayList<>();
    private Marker clickedMark = new Marker();
    private SearchView searchView;
    private Boolean isAddrError;

    private DatabaseReference mDatabaseRef;
    private EditText mAddress;
    private Button mSearch;


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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String addr){
                if (addr.length() > 2) {

                    Handler mHandler = new Handler();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            requestGeocode(addr);
                        }
                    }).start();

                    // requestGeocode 메소드 호출 후, 시간 지연시켜 isAddrError값 업데이트
                    mHandler.postDelayed(new Runnable() {
                        @Override
                            public void run(){
                                if (isAddrError == null) return;
                                if (isAddrError){
                                    Toast.makeText(getApplicationContext(),  "입력된 주소값이 잘못되었습니다.\n도로명 주소를 입력해주세요.\nEx) 수정구 성남대로 1342", Toast.LENGTH_LONG).show();
                                }else{
                                    setPositionOnMap();
                                    // 클릭 한 곳을 중심으로 카메라 이동
                                    naverMap.setCameraPosition(new CameraPosition(new LatLng(mlat, mlon), 15, 0, 0));
                                    fetchApi(getApiServices, naverMap);
                                }
                            }
                    }, 500);
                } else {
                    Toast.makeText(getApplicationContext(), "3글자 이상 입력해주세요.", Toast.LENGTH_LONG).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

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
        getApiServices = new ArrayList<>(
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

            setPositionOnMap();
            fetchApi(getApiServices, this.naverMap);
        });
    }

    // 새로운 화장실 마크 렌더링
    public void setPositionOnMap(){
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

    // geocoding api
    public void requestGeocode(String address){
        try{
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String query = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(address, "UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "g2o699d48z");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "Vl0LQg8UEmldclaI1PBcssLa70nQlw1vmlPiOpu5");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                }else{
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line = null;
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line + '\n');
                }

                // 에러값 초기화
                isAddrError = false;

                int indexFirst;
                int indexLast;

                indexFirst = stringBuilder.indexOf("\"x\":\"");
                indexLast = stringBuilder.indexOf("\",\"y\":");
                String lon = stringBuilder.substring(indexFirst + 5, indexLast);

                indexFirst = stringBuilder.indexOf("\"y\":\"");
                indexLast = stringBuilder.indexOf("\",\"distance\":");
                String lat = stringBuilder.substring(indexFirst + 5, indexLast);

                mlat = Double.parseDouble(lat);
                mlon = Double.parseDouble(lon);

                bufferedReader.close();
                conn.disconnect();
            }
        } catch(Exception e){
            isAddrError = true;
            e.printStackTrace();
        }
    }


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

