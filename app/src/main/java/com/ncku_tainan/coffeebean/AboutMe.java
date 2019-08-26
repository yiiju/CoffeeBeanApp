package com.ncku_tainan.coffeebean;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class AboutMe extends AppCompatActivity {

    private TextView Description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutme);
        getSupportActionBar().hide(); //隱藏標題
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        Description = findViewById(R.id.Description);
        String source = "About Me";
        Description.setText(source);
    }
}
