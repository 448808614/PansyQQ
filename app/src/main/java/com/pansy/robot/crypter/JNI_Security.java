package com.pansy.robot.crypter;

public class JNI_Security {
    static {
        System.loadLibrary("Openssl");
    }
    //public native void checkEnvironment(String version);
    public native String checkEnvironment();
    public native byte[] pluginEncrypt(byte[] data,int len);

}
