package com.example.myapplication;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DetectionInfo {
    private float boxMaxWidth;
    private float boxMaxHeight;
    private Integer boxNumber;

    public DetectionInfo(float boxMaxWidth, float boxMaxHeight, Integer boxNumber) {
        this.boxMaxWidth = boxMaxWidth;
        this.boxMaxHeight = boxMaxHeight;
        this.boxNumber = boxNumber;
    }

    public float getBoxMaxWidth() {
        return boxMaxWidth;
    }

    public float getBoxMaxHeight() {
        return boxMaxHeight;
    }

    public Integer getBoxNumber() {
        return boxNumber;
    }

    public void setBoxMaxWidth(float boxMaxWidth) {
        this.boxMaxWidth = boxMaxWidth;
    }

    public void setBoxMaxHeight(float boxMaxHeight) {
        this.boxMaxHeight = boxMaxHeight;
    }

    public void setBoxNumber(Integer boxNumber) {
        this.boxNumber = boxNumber;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            Log.d("TAG", "toByteArray: " + boxMaxWidth);
            Log.d("TAG", "toByteArray: " + boxMaxHeight);
            Log.d("TAG", "toByteArray: " + boxNumber);
            dataOutputStream.writeFloat(boxMaxWidth);
            dataOutputStream.writeFloat(boxMaxHeight);
            dataOutputStream.writeInt(boxNumber);
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();

    }

}
