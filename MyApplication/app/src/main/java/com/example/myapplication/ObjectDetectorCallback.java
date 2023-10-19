package com.example.myapplication;

import java.util.List;

public interface ObjectDetectorCallback {

    void invoke(List<DetectionObject> detectedObjectList);
}
