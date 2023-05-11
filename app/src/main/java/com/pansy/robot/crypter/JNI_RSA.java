package com.pansy.robot.crypter;

public class JNI_RSA {
    static {
        System.loadLibrary("Openssl");
    }
    public static native String encrypt(String data);
    public static native String decrypt(String data);
}
