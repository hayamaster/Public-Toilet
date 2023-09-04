package com.gachon.publictoilet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gachon.publictoilet.ApiExtract.ApiExtract;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private boolean isFabOpen = false;

//    Call<GeoInfoExtract> call;
    private ArrayList<PublicToilet2> data= new ArrayList<>();
    private LocationManager mLocationManager;
    private ArrayList<Call<ApiExtract>> getApiServices;
    private double mlat;
    private double mlon;
    private ArrayList<Marker> marked = new ArrayList<>();
    private Marker clickedMark = new Marker();
    private SearchView searchView;
    private Boolean isAddrError;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LinearLayout toiletInfoLayout;
    private TextView getBuildingName;
    private TextView getToiletAddr;
    private TextView getFemaleCnt;
    private TextView getMaleCnt;
    private TextView getCommon;
    private Button mapInfoBtn;
    private String destination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 네이버 맵
        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);


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
        getMyPos();
        naverMap.setCameraPosition(new CameraPosition(new LatLng(mlat, mlon), 15, 0, 0));


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

    // 나의 위치 값 업데이트
    public void getMyPos() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null ? mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) : mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                Toast.makeText(this, location.toString(), Toast.LENGTH_LONG).show();
            } else {
                mlat = location.getLatitude();
                mlon = location.getLongitude();
            }
        }
    }

    // 새로운 화장실 마크 렌더링
    public void setPositionOnMap(){
        // 이전에 표시된 마크들을 삭제
        for(Marker mark : marked){
            mark.setMap(null);
            mark.setOnClickListener(null);
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
                        double lat = data.get(i).getLat();
                        double lon = data.get(i).getLon();

                        double dist = (DistanceByDegreeAndroid(lat, lon) / 1000);
                        if (dist <= 2) {
                            Log.i("imsy", String.valueOf(lat));
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(lat, lon));
                            marker.setIcon(OverlayImage.fromResource(R.drawable.toilets));
                            marker.setWidth(120);
                            marker.setHeight(120);
                            marker.setMap(naverMap);

                            // 마커 클릭 이벤트
                            // 수정구 성남대로 1342
                            marker.setOnClickListener(new Overlay.OnClickListener(){
                                @Override
                                public boolean onClick(@NonNull Overlay overlay){
                                    if (overlay instanceof Marker){
                                        getBuildingName = findViewById(R.id.building_name);
                                        getToiletAddr = findViewById(R.id.toilet_addr);
                                        getFemaleCnt = findViewById(R.id.female_counts);
                                        getMaleCnt = findViewById(R.id.male_counts);
                                        getCommon = findViewById(R.id.common);
                                        toiletInfoLayout = findViewById(R.id.toilet_info_layout);
                                        mapInfoBtn = findViewById(R.id.map_info_button);

                                        getToiletAddr.setText("주소: ");
                                        toiletInfoLayout.setVisibility(View.VISIBLE);


                                        db.collection("ADDRESS").whereEqualTo("REFINE_WGS84_LAT", String.valueOf(lat)).whereEqualTo("REFINE_WGS84_LOGT", String.valueOf(lon)).get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document_imsy : task.getResult()) {
                                                                String buildingName = String.valueOf(document_imsy.getData().get("PBCTLT_PLC_NM"));
                                                                String addr = String.valueOf(document_imsy.getData().get("REFINE_LOTNO_ADDR"));
                                                                Map<String, String> fecnt = (Map) document_imsy.getData().get("FEMALE_WTRCLS_CNT");
                                                                Map<String, String> mcnt = (Map) document_imsy.getData().get("MALE_WTRCLS_CNT");
                                                                String common = String.valueOf(document_imsy.getData().get("MALE_FEMALE_CMNUSE_TOILET_YN"));

                                                                int feAvailable = 0;
                                                                int feUsed = 0;
                                                                int feCrash = 0;
                                                                int mAvailable = 0;
                                                                int mUsed = 0;
                                                                int mCrash = 0;
                                                                for(String key : mcnt.keySet()){
                                                                    if (String.valueOf(mcnt.get(key)).equals("0")){
                                                                        mAvailable += 1;
                                                                    }else if (String.valueOf(mcnt.get(key)).equals("1")){
                                                                        mUsed += 1;
                                                                    }else{
                                                                        mCrash += 1;
                                                                    }
                                                                }
                                                                for(String key : fecnt.keySet()){
                                                                    if (String.valueOf(fecnt.get(key)).equals("0")){
                                                                        feAvailable += 1;
                                                                    }else if (String.valueOf(fecnt.get(key)).equals("1")){
                                                                        feUsed += 1;
                                                                    }else{
                                                                        feCrash += 1;
                                                                    }
                                                                }

                                                                destination = toURLEncodeUtf8(buildingName);
                                                                getBuildingName.setText("건물명: " + buildingName);
                                                                getToiletAddr.setText("주소: " + addr);
                                                                getMaleCnt.setText("남자화장실: 이용가능-" + mAvailable + " 이용중-" + mUsed + " 고장-" + mCrash);
                                                                getFemaleCnt.setText("여자화장실: 이용가능-" + feAvailable + " 이용중-" + feUsed + " 고장-" + feCrash);
                                                                getCommon.setText("남여 공용 화장실 여부: " + common);
                                                                break;
                                                            }
                                                        } else {
                                                            Log.d("siv", "Error getting documents: ", task.getException());
                                                            Toast.makeText(getApplicationContext(),"k;;;;", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                        // 길찾기 버튼 클릭 시, 네이버 지도 앱 연동
                                        mapInfoBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                getMyPos();

                                                String url = "nmap://route/walk?slat=" + mlat + "&slng=" + mlon +"&sname=%EB%82%B4+%EC%9C%84%EC%B9%98&dlat=" + lat + "&dlng=" + lon + "&dname=" + destination + "&appname=com.gachon.publictoilet";

                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                intent.addCategory(Intent.CATEGORY_BROWSABLE);

                                                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                                                if (list == null || list.isEmpty()) {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")));
                                                } else {
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            marked.add(marker);
                        }
                    }
                };

                @Override
                public void onFailure(Call<ApiExtract> call, Throwable t) {
                    Log.i("fail", "failed getting data...");
                }
            });
        }
    };

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

    // 주소를 UTF8로 인코딩
    public static String toURLEncodeUtf8(String str){
        if (str == null || str.trim().equals("")) return "";
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch(UnsupportedEncodingException ex){
            return null;
        }
    }
}

