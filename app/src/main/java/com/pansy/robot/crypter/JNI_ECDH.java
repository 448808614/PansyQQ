package com.pansy.robot.crypter;

public class JNI_ECDH {
    static{
        System.loadLibrary("Openssl");
    }
    public native ECC ecdh(Object context);
    public native byte[] ecdh2(ECC ecc,byte[] tk);
}
