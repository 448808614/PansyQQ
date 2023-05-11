package com.pansy.robot.crypter;

import android.content.Context;

public class JNI_Official {
    static {
        System.loadLibrary("Openssl");
    }
    public native byte[] computeOfficial(byte[] offkey,byte[] bufsig,byte[] bufTGTGT,Context context);
}
