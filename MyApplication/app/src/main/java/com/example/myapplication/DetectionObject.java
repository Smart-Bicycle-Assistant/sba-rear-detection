package com.example.myapplication;

import android.graphics.RectF;

/**
 * 감지 결과를 저장하는 클래스
 */
public class DetectionObject {
    private final float score;
    private final String label;
    private final RectF boundingBox;

    public DetectionObject(float score, String label, RectF boundingBox) {
        this.score = score;
        this.label = label;
        this.boundingBox = boundingBox;
    }

    public float getScore() {
        return score;
    }

    public String getLabel() {
        return label;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }
}