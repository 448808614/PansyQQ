package com.pansy.robot.utils;


import android.content.Context;
import java.lang.reflect.Method;

public class CheckSimulatorUtil {

    public static boolean inSimulator(Context context){
        try {
            String cpuinfo=FileUtil.read("proc/cpuinfo");
            if(cpuinfo.equals("")) return true;
            String modelname="";
            String arr[]=cpuinfo.split("\n");
            for (int i=0;i<arr.length;i++){
                if(arr[i].startsWith("model name\t: ")){
                    modelname=arr[i].replace("model name\t: ","");
                    break;
                }
            }
            Method method = Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class);
            Object baseband=method.invoke(null,"gsm.version.baseband");
            return baseband.toString().equals("") && (modelname.equals("") || modelname.startsWith("Intel(R)"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
