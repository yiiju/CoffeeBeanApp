package com.ncku_tainan.coffeebean;

import android.graphics.Color;
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
        int ColorValue = Color.parseColor("#462F0E");
        getWindow().setStatusBarColor(ColorValue);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        Description = findViewById(R.id.Description);
        String source = "收集四種不同產地的烘培過咖啡豆，分別是衣索比亞、牙買加、哥倫比亞和尼加拉瓜。" +
                "建立五種不同 Convolutional Neural Networks (CNN) 的模型，" +
                "以分類四種咖啡豆：第一種是基本的 CNN 模型，第二種是透過 Image Augmentation 增加資料量訓練於第一種模型，" +
                "第三種是基於 VGG16 的 CNN 模型，第四種是透過 Image Augmentation 增加資料量訓練於第三種模型，" +
                "第五種是透過 Image Augmentation 增加資料量並 Fine-tuning 訓練於第三種模型。\n";
        Description.setText(source);
    }
}
