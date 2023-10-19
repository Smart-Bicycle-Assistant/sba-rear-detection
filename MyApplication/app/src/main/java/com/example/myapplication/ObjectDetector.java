package com.example.myapplication;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDetector implements ImageAnalysis.Analyzer {
    private static final int IMG_SIZE_X = 300;
    private static final int IMG_SIZE_Y = 300;
    private static final int MAX_DETECTION_NUM = 10;
    private static final float SCORE_THRESHOLD = 0.5f;
    private static final float NORMALIZE_MEAN = 0f;
    private static final float NORMALIZE_STD = 1f;




        private int imageRotationDegrees = 0;
        private final ImageProcessor tfImageProcessor;
        private final TensorImage tfImageBuffer;
        private float[][][] outputBoundingBoxes = new float[1][MAX_DETECTION_NUM][4];
        private float[][] outputLabels = new float[1][MAX_DETECTION_NUM];
        private float[][] outputScores = new float[1][MAX_DETECTION_NUM];
        private float[] outputDetectionNum = new float[1];
    private final YuvToRgbConverter yuvToRgbConverter;
    private final Interpreter interpreter;
    private final List<String> labels;
    private final Size resultViewSize;
    private final ObjectDetectorCallback listener;

        public ObjectDetector(
                YuvToRgbConverter yuvToRgbConverter,
                Interpreter interpreter,
                List<String> labels,
                Size resultViewSize,
                ObjectDetectorCallback listener
        ) {
            this.yuvToRgbConverter = yuvToRgbConverter;
            this.interpreter = interpreter;
            this.labels = labels;
            this.resultViewSize = resultViewSize;
            this.listener = listener;

            tfImageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(IMG_SIZE_X, IMG_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new Rot90Op(-imageRotationDegrees / 90))
                    .add(new NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD))
                    .build();
            tfImageBuffer = new TensorImage(DataType.UINT8);


// outputMap 생성

        }
        Map<Integer, Object> outputMap = new HashMap<>();
        {
            outputMap.put(0, outputBoundingBoxes);
            outputMap.put(1, outputLabels);
            outputMap.put(2, outputScores);
            outputMap.put(3, outputDetectionNum);
        }

        @SuppressLint("UnsafeExperimentalUsageError")
        @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
        @Override
        public void analyze(ImageProxy imageProxy) {
            if (imageProxy.getImage() == null) return;
            imageRotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            List<DetectionObject> detectedObjectList = detect(imageProxy.getImage());
            listener.invoke(detectedObjectList);
            imageProxy.close();
        }

        private List<DetectionObject> detect(Image targetImage) {
            Bitmap targetBitmap = Bitmap.createBitmap(targetImage.getWidth(), targetImage.getHeight(), Bitmap.Config.ARGB_8888);
            yuvToRgbConverter.yuvToRgb(targetImage, targetBitmap);
            tfImageBuffer.load(targetBitmap);
            TensorImage tensorImage = tfImageProcessor.process(tfImageBuffer);

            interpreter.runForMultipleInputsOutputs(new Object[]{tensorImage.getBuffer()}, outputMap);

            List<DetectionObject> detectedObjectList = new ArrayList<>();

//            Log.d("TAG", "detect: outputScores size" + outputScores[0].length);
//            Log.d("TAG", "detect: outputLabels size" + outputLabels[0].length);
//            Log.d("TAG", "detect: label size" + labels.size());
//            for(int i = 0; i< outputLabels[0].length; i++)
//            {
//                Log.d("TAG", ""+outputLabels[0][i]);
//            }

            for(int i = 0; i < outputScores[0].length; i++) {
//            for (int i = 0; i < outputDetectionNum[0]; i++) {
                float score = outputScores[0][i];
                String label = labels.get((int) outputLabels[0][i]);
                Log.d("TAG", label + " : " + score + " / " + outputBoundingBoxes[0][i][0]);
                Log.d("TAG", "detect: " + resultViewSize.getHeight());
                RectF boundingBox = new RectF(
                        outputBoundingBoxes[0][i][1] * resultViewSize.getWidth(),
                        outputBoundingBoxes[0][i][0] * resultViewSize.getHeight(),
                        outputBoundingBoxes[0][i][3] * resultViewSize.getWidth(),
                        outputBoundingBoxes[0][i][2] * resultViewSize.getHeight()
                );


                if (score >= SCORE_THRESHOLD) {
                    detectedObjectList.add(new DetectionObject(score, label, boundingBox));
                } else {
                    break;
                }
            }

            return detectedObjectList.subList(0, Math.min(detectedObjectList.size(), 4));
        }
    }

