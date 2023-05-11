package com.pansy.robot.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.pansy.robot.APP;
import com.pansy.robot.crypter.Tea;

public class SPHelper {
    public static String[] read_QQ(){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("QQ",Context.MODE_PRIVATE);
        String[] arr=new String[2];
        arr[0]=sp.getString("QQ","");
        arr[1]=sp.getString("pwd","");
        try{
            if(arr[1].equals("")==false){
                arr[1]=ByteUtil.splitHex(arr[1]);
                arr[1]=new String(new Tea().decrypt(Converter.hexStr2ByteArray(arr[1]),new byte[]{-22,17,-100,92,39,108,-83,-30,24,-5,115,76,73,57,-125,50}));
            }
            return arr;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static void write_QQ(String account,String pwd){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("QQ",Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putString("QQ",account);
        if(pwd.equals("")==false){
            pwd=Converter.byteArray2HexStr(new Tea().encrypt(pwd.getBytes(),new byte[]{-22,17,-100,92,39,108,-83,-30,24,-5,115,76,73,57,-125,50})).replace(" ","");
            edt.putString("pwd",pwd);
        }
        edt.commit();
    }
    public static long readLong(String fileName,String key,long def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getLong(key,def);
    }
    public static void writeLong(String fileName,String key,long value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putLong(key,value);
        edt.commit();
    }

    public static long readLong(String key,long def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getLong(key,def);
    }
    public static void writeLong(String key,long value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putLong(key,value);
        edt.commit();
    }

    public static int readInt(String fileName,String key,int def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getInt(key,def);
    }
    public static void writeInt(String fileName,String key,int value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putInt(key,value);
        edt.commit();
    }

    public static int readInt(String key,int def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getInt(key,def);
    }
    public static void writeInt(String key,int value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putInt(key,value);
        edt.commit();
    }

    public static String readString(String fileName,String key,String def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getString(key,def);
    }
    public static void writeString(String fileName,String key,String value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putString(key,value);
        edt.commit();
    }

    public static String readString(String key,String def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getString(key,def);
    }
    public static void writeString(String key,String value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putString(key,value);
        edt.commit();
    }

    public static boolean readBool(String fileName,String key,boolean def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getBoolean(key,def);
    }
    public static void writeBool(String fileName,String key,boolean value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putBoolean(key,value);
        edt.commit();
    }

    public static boolean readBool(String key,boolean def){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getBoolean(key,def);
    }
    public static void writeBool(String key,boolean value){
        SharedPreferences sp=APP.getAppContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt=sp.edit();
        edt.putBoolean(key,value);
        edt.commit();
    }

}
