package com.ncku_tainan.coffeebean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final int SHOW_PHOTO_IMAGE_CODE = 29;
    private static final int DIM_IMG_SIZE_X = 128;
    private static final int DIM_IMG_SIZE_Y = 128;

    private ImageButton start_button;
    private ImageButton Aboutme_button;
    private Button show_img;
    private ImageView logo_image;

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
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        start_button = findViewById(R.id.start_button);
        Aboutme_button = findViewById(R.id.Aboutme_button);
        show_img = findViewById(R.id.show_img);
        logo_image = findViewById(R.id.logo_image);


        show_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SHOW_PHOTO_IMAGE_CODE);
            }
        });
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , TensorflowModel.class);
                startActivity(intent);
            }
        });
        Aboutme_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this , AboutMe.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == SHOW_PHOTO_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    Bitmap bitmap = Bitmap.createScaledBitmap(selectedImage, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);
                    logo_image.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
//                Toast.makeText(PostImage.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}