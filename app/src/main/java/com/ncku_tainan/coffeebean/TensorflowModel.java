package com.ncku_tainan.coffeebean;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
;import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.File;

import static com.ncku_tainan.coffeebean.MainActivity.select_model;


public class TensorflowModel  extends AppCompatActivity {
    private ImageButton takePictureButton;
    private ImageButton showPictureButton;
    private ImageView imageView;
    private TextView resultText;

    public static final int TAKE_PHOTO_IMAGE_CODE = 29;
    public static final int SHOW_PHOTO_IMAGE_CODE = 30;
    public static final String FileName = "CoffeeBeanApp";
    public static Uri file;

    private static final String TAG = "TensorflowModel";
    private static String HOSTED_MODEL_NAME = "basic_cnn";
    private static String LOCAL_MODEL_NAME = "local_basic_cnn";
    private static String LOCAL_MODEL_ASSET = "basic_cnn.tflite";
    private static final String LABEL_PATH = "retrained_labels.txt";
    private static final int RESULTS_TO_SHOW = 3;
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private static final int DIM_IMG_SIZE_X = 128;
    private static final int DIM_IMG_SIZE_Y = 128;
    /* An instance of the driver class to run model inference with Firebase */
    private FirebaseModelInterpreter mInterpreter;
    /* Data configuration of input & output data of model */
    private FirebaseModelInputOutputOptions mDataOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tensorflowmodel);
        getSupportActionBar().hide(); //隱藏標題
        int ColorValue = Color.parseColor("#462F0E");
        getWindow().setStatusBarColor(ColorValue);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //隱藏狀態

        takePictureButton = findViewById(R.id.take_image);
        showPictureButton = findViewById(R.id.show_image);
        imageView = findViewById(R.id.imageview);
        resultText = findViewById(R.id.result_text);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        showPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SHOW_PHOTO_IMAGE_CODE);
            }
        });

        selectModel();
        initCustomModel();
    }

    private void initCustomModel() {
        try {
            mDataOptions = createInputOutputOptions();
            configureHostedModelSource();
            configureLocalModelSource();
            mInterpreter = createInterpreter();
        } catch (FirebaseMLException e) {
            showToast("Error while setting up the model");
            e.printStackTrace();
        }
    }

    private void selectModel() {
        switch(select_model) {
            case 1:
                HOSTED_MODEL_NAME = "basic_cnn";
                LOCAL_MODEL_NAME = "local_basic_cnn";
                LOCAL_MODEL_ASSET = "basic_cnn.tflite";
                break;
            case 2:
                HOSTED_MODEL_NAME = "aug_cnn";
                LOCAL_MODEL_NAME = "local_aug_cnn";
                LOCAL_MODEL_ASSET = "aug_cnn.tflite";
                break;
            case 3:
                HOSTED_MODEL_NAME = "t_vgg16_cnn";
                LOCAL_MODEL_NAME = "local_t_vgg16_cnn";
                LOCAL_MODEL_ASSET = "t_vgg16_cnn.tflite";
                break;
            case 4:
                HOSTED_MODEL_NAME = "t_vgg16_aug_cnn";
                LOCAL_MODEL_NAME = "local_t_vgg16_aug_cnn";
                LOCAL_MODEL_ASSET = "t_vgg16_aug_cnn.tflite";
                break;
            case 5:
                HOSTED_MODEL_NAME = "t_vgg16_finetuning_aug_cnn_5x";
                LOCAL_MODEL_NAME = "local_t_vgg16_finetuning_aug_cnn_5x";
                LOCAL_MODEL_ASSET = "t_vgg16_finetuning_aug_cnn_5x.tflite";
                break;
        }
    }

    private void configureHostedModelSource() {
        // [START mlkit_cloud_model_source]
        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        // Build a remote model source object by specifying the name you assigned the model
        // when you uploaded it in the Firebase console.
        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder(HOSTED_MODEL_NAME)
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);
        // [END mlkit_cloud_model_source]
    }

    private void configureLocalModelSource() {
        // [START mlkit_local_model_source]
        FirebaseLocalModel localSource =
                new FirebaseLocalModel.Builder(LOCAL_MODEL_NAME)  // Assign a name to this model
                        .setAssetFilePath(LOCAL_MODEL_ASSET)
                        .build();
        FirebaseModelManager.getInstance().registerLocalModel(localSource);
        // [END mlkit_local_model_source]
    }

    private FirebaseModelInputOutputOptions createInputOutputOptions() throws FirebaseMLException {

        int[] inputDims = {DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
        int[] outputDims = {DIM_BATCH_SIZE, 4};

        // [START mlkit_create_io_options]
        FirebaseModelInputOutputOptions inputOutputOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, inputDims)
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, outputDims)
                        .build();
        // [END mlkit_create_io_options]

        return inputOutputOptions;
    }

    private FirebaseModelInterpreter createInterpreter() throws FirebaseMLException {
        // [START mlkit_create_interpreter]
        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setRemoteModelName(HOSTED_MODEL_NAME)
                .setLocalModelName(LOCAL_MODEL_NAME)
                .build();
        FirebaseModelInterpreter firebaseInterpreter =
                FirebaseModelInterpreter.getInstance(options);
        // [END mlkit_create_interpreter]

        return firebaseInterpreter;
    }

    private float[][][][] bitmapToInputArray(Context context) {
        // [START mlkit_bitmap_input]
//        AssetManager assetManager = context.getAssets();
//        InputStream is;
//        Bitmap bitmap = null;
//        try {
//            is = assetManager.open("1.jpg");
//            bitmap = BitmapFactory.decodeStream(is);
//        } catch (IOException e) {
//            Log.d(TAG, "bitmapToInputArray() returned: " + "did not get img");
//            e.printStackTrace();
//        }

        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);

        int batchNum = 0;
        float[][][][] input = new float[DIM_BATCH_SIZE][DIM_IMG_SIZE_X][DIM_IMG_SIZE_Y][DIM_PIXEL_SIZE];
        for (int x = 0; x < DIM_IMG_SIZE_X; x++) {
            for (int y = 0; y < DIM_IMG_SIZE_Y; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [0.0, 1.0].
                input[batchNum][x][y][0] = Color.red(pixel) / 255.0f;
                input[batchNum][x][y][1] = Color.green(pixel) / 255.0f;
                input[batchNum][x][y][2] = Color.blue(pixel) / 255.0f;
            }
        }
        // [END mlkit_bitmap_input]

        return input;
    }

    public void runInference() throws FirebaseMLException {
        // Create input data.
        float[][][][] input = bitmapToInputArray(this);

        // [START mlkit_run_inference]
        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(input)  // add() as many input arrays as your model requires
                .build();
        mInterpreter.run(inputs, mDataOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                // [START_EXCLUDE]
                                // [START mlkit_read_result]
                                float[][] output = result.getOutput(0);
                                float[] probabilities = output[0];
                                try {
                                    useInferenceResult(probabilities);
                                } catch(IOException ex) {
                                    //Do something with the exception
                                    Log.d("IOException", "onSuccess() returned: " + String.format("%1.4f", output[0]));
                                }
                                // [END mlkit_read_result]
                                // [END_EXCLUDE]
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
        // [END mlkit_run_inference]
    }

    private void useInferenceResult(float[] probabilities) throws IOException {
        // [START mlkit_use_inference_result]
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("retrained_labels.txt")));
        String show_text = "預測結果: \n";
        for (int i = 0; i < probabilities.length; i++) {
            String label = reader.readLine();
            Log.i("MLKit", String.format("%s: %1.4f", label, probabilities[i]));
            show_text = show_text + String.format("%s: %1.4f", label, probabilities[i]) + '\n';
        }
        resultText.setText(show_text);
        // [END mlkit_use_inference_result]
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, TAKE_PHOTO_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(file);
                try {
                    runInference();
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                    showToast("Error running model inference");
                }
            }
        }
        if (requestCode == SHOW_PHOTO_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    Bitmap bitmap = Bitmap.createScaledBitmap(selectedImage, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);
                    imageView.setImageBitmap(bitmap);
                    try {
                        runInference();
                    } catch (FirebaseMLException e) {
                        e.printStackTrace();
                        showToast("Error running model inference");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()+File.separator + FileName);

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }
}
