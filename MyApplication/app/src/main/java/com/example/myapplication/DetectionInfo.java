package com.example.myapplication;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DetectionInfo {
    private float boxMaxWidth;
    private Integer boxMaxType;
    private Integer boxNumber;

    public DetectionInfo(float boxMaxWidth, Integer boxMaxType, Integer boxNumber) {
        this.boxMaxWidth = boxMaxWidth;
        this.boxMaxType = boxMaxType;
        this.boxNumber = boxNumber;
    }

    public float getBoxMaxWidth() {
        return boxMaxWidth;
    }

    public Integer getBoxMaxType() {
        return boxMaxType;
    }

    public void setBoxMaxType(Integer boxMaxType) {
        this.boxMaxType = boxMaxType;
    }

    public Integer getBoxNumber() {
        return boxNumber;
    }

    public void setBoxMaxWidth(float boxMaxWidth) {
        this.boxMaxWidth = boxMaxWidth;
    }


    public void setBoxNumber(Integer boxNumber) {
        this.boxNumber = boxNumber;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
//            Log.d("TAG", "toByteArray: " + boxMaxWidth);
//            Log.d("TAG", "toByteArray: " + boxMaxHeight);
//            Log.d("TAG", "toByteArray: " + boxNumber);
            dataOutputStream.writeFloat(boxMaxWidth);
            dataOutputStream.writeInt(boxMaxType);
            dataOutputStream.writeInt(boxNumber);
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();

    }

}
