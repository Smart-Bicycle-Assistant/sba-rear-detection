package com.example.myapplication;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DetectionInfo {
    private Double boxMaxWidth;
    private Double boxMaxHeight;
    private Integer boxNumber;

    public DetectionInfo(Double boxMaxWidth, Double boxMaxHeight, Integer boxNumber) {
        this.boxMaxWidth = boxMaxWidth;
        this.boxMaxHeight = boxMaxHeight;
        this.boxNumber = boxNumber;
    }

    public Double getBoxMaxWidth() {
        return boxMaxWidth;
    }

    public Double getBoxMaxHeight() {
        return boxMaxHeight;
    }

    public Integer getBoxNumber() {
        return boxNumber;
    }

    public void setBoxMaxWidth(Double boxMaxWidth) {
        this.boxMaxWidth = boxMaxWidth;
    }

    public void setBoxMaxHeight(Double boxMaxHeight) {
        this.boxMaxHeight = boxMaxHeight;
    }

    public void setBoxNumber(Integer boxNumber) {
        this.boxNumber = boxNumber;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeInt(boxNumber);
            dataOutputStream.writeDouble(boxMaxWidth);
            dataOutputStream.writeDouble(boxMaxHeight);
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();

    }

}
