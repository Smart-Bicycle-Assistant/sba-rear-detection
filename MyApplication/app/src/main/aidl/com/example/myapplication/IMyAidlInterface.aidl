// IMyAidlInterface.aidl
package com.example.myapplication;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void sendInfo(in Bundle bundle);
}