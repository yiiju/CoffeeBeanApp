package com.ncku_tainan.coffeebean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static int select_model;
    private ImageButton basic_model_button;
    private ImageButton aug_model_button;
    private ImageButton t_vgg16_model_button;
    private ImageButton t_vgg16_aug_model_button;
    private ImageButton t_vgg16_fine_aug_model_button;
    private ImageButton aboutme_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm;
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo NetInfo = cm.getActiveNetworkInfo();

        if (NetInfo == null) {
            Toast.makeText(getApplicationContext(), "Offline status", Toast.LENGTH_SHORT).show();
        } else {
            if (NetInfo.isConnected()) {
                Toast.makeText(getApplicationContext(), "Connect to the internet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Offline", Toast.LENGTH_SHORT).show();
            }
        }

        getSupportActionBar().hide(); //隱藏標題
        int ColorValue = Color.parseColor("#462F0E");
        getWindow().setStatusBarColor(ColorValue);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        basic_model_button = findViewById(R.id.basic_model_button);
        aug_model_button = findViewById(R.id.aug_model_button);
        t_vgg16_model_button = findViewById(R.id.t_vgg16_model_button);
        t_vgg16_aug_model_button = findViewById(R.id.t_vgg16_aug_model_button);
        t_vgg16_fine_aug_model_button = findViewById(R.id.t_vgg16_fine_aug_model_button);
        aboutme_button = findViewById(R.id.aboutme_button);

        basic_model_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_model = 1;
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , TensorflowModel.class);
                startActivity(intent);
            }
        });

        aug_model_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_model = 2;
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , TensorflowModel.class);
                startActivity(intent);
            }
        });

        t_vgg16_model_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_model = 3;
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , TensorflowModel.class);
                startActivity(intent);
            }
        });

        t_vgg16_aug_model_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_model = 4;
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , TensorflowModel.class);
                startActivity(intent);
            }
        });

        t_vgg16_fine_aug_model_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_model = 5;
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , TensorflowModel.class);
                startActivity(intent);
            }
        });

        aboutme_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , AboutMe.class);
                startActivity(intent);
            }
        });
    }
}