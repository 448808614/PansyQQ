package com.pansy.robot.crypter;

public class JNI_AES {
    static {
        System.loadLibrary("Openssl");
    }
    public native byte[] encryptRedPack(byte[] data,byte[] param,int data_length);
    public native byte[] decryptRedPack(byte[] data,byte[] param,int data_length);
}
