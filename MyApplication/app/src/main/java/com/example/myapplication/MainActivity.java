package com.example.myapplication;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.myapplication.DetectionObject;



public class MainActivity extends AppCompatActivity {
    private static final String MODEL_FILE_NAME = "ssd_mobilenet_v1.tflite";
    private static final String LABEL_FILE_NAME = "coco_dataset_labels.txt";

    private OverlaySurfaceView overlaySurfaceView;
    private ExecutorService cameraExecutor;

    private Interpreter interpreter;

    private List<String> labels;

    private YuvToRgbConverter yuvToRgbConverter;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    LocalBroadcastManager broadcaster;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        startService(new Intent(this, CommunicationService.class));

        broadcaster = LocalBroadcastManager.getInstance(this);
        yuvToRgbConverter = new YuvToRgbConverter(this);
        interpreter = new Interpreter(loadModel());
        labels = loadLabels();
        Log.d("TAG", "onCreate: " + labels.size());
        SurfaceView resultView = findViewById(R.id.resultView);
        overlaySurfaceView = new OverlaySurfaceView(resultView);



        cameraExecutor = Executors.newSingleThreadExecutor();
        if (allPermissionsGranted()) {
            Log.d("start","startPermissions");
            setupCamera();
        } else {
            Log.d("start","deniedPermissions");
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Log.d("start","onRequestPermissions");
                setupCamera();
            } else {
                Toast.makeText(this, "사용자가 권한을 승인하지 않았습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }




    void setupCamera() {
        PreviewView viewFinder = findViewById(R.id.cameraView);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Log.d("TAG", "setupCamera: " + cameraProvider);
            Preview preview = new Preview.Builder().build();

            preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;


            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setTargetRotation(findViewById(R.id.cameraView).getDisplay().getRotation())
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            Log.d("TAG", "setupCamera: label size : " + labels.size());
//            Log.d("TAG", "setupCamera: ");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            imageAnalysis.setAnalyzer(cameraExecutor, new ObjectDetector(
                    yuvToRgbConverter,
                    interpreter,
                    labels,
//                     new Size(overlaySurfaceView.getMeasuredWidth(), overlaySurfaceView.getMeasuredHeight()),
                    new Size(displayMetrics.widthPixels, displayMetrics.heightPixels),
                    detectedObjectList -> overlaySurfaceView.draw(detectedObjectList)
                    , broadcaster));
            try {
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception exc) {
                Log.e("ERROR: Camera", "Use case binding failed", exc);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private ByteBuffer loadModel() {
        ByteBuffer modelBuffer = null;
        AssetFileDescriptor file = null;
        try {
            file = getAssets().openFd(MODEL_FILE_NAME);
            FileInputStream inputStream = new FileInputStream(file.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, file.getStartOffset(), file.getDeclaredLength());
        } catch (Exception e) {
            Toast.makeText(this, "모델 파일 읽기 오류", Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return modelBuffer;
    }

    private List<String> loadLabels() {
        List<String> labelss = new ArrayList<>();
        InputStream inputStream = null;
        try{

            inputStream = getAssets().open(LABEL_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String buffer = null;
            while((buffer = reader.readLine()) != null) {
                Log.d("TAG", "loadLabels: "+ buffer);
                labelss.add(buffer);
            }

        }catch (Exception e) {
            Log.d("TAG", "loadLabels: exception");
            e.printStackTrace();
        }

//        List<String> labels = null;
//        InputStream inputStream = null;
//        try {
//            inputStream = getAssets().open(LABEL_FILE_NAME);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            labels = Collections.singletonList(reader.readLine());
//        } catch (Exception e) {
//            Toast.makeText(this, "txt 파일 읽기 오류", Toast.LENGTH_SHORT).show();
//            finish();
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return labels;
        Log.d("TAG", "loadLabels: " + labelss.size());
        return labelss;
    }


}
