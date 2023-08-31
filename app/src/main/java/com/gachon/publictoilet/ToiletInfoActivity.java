package com.gachon.publictoilet;

import static java.lang.Math.round;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowMetrics;



public class ToiletInfoActivity extends AppCompatActivity {

    private double width;
    private double height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toilet_info);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initLayout();
    }

    private void initLayout() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API Level 30 버전
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            width = windowMetrics.getBounds().width();
            height = windowMetrics.getBounds().height();
        } else { // API Level 30 이전 버전
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getRealMetrics(displayMetrics);
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;
        }
        getWindow().setLayout((int) round(width * 0.9), (int)round(height * 0.22));
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}