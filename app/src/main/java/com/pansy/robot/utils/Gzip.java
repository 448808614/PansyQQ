package com.pansy.robot.utils;

import com.pansy.robot.crypter.JNI_Security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {
    //压缩
    public static byte[] gzip(byte[] data) {
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            byte[] ret = bos.toByteArray();
            bos.close();
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    //解压
    public static byte[] ungzip(byte[] data) {
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, num);
            }
            gzip.close();
            bis.close();
            byte[] ret = bos.toByteArray();
            bos.flush();
            bos.close();
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] pluginEncrypt(String path){
        try{
            byte[] b1=FileUtil.readByteArray(path);
            byte[] b2=Gzip.gzip(b1);
            if(b2.length>=1024)
                b2=new JNI_Security().pluginEncrypt(b2,b2.length);
            return b2;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] pluginDecrypt(String path){
        try{
            byte[] b3=FileUtil.readByteArray(path);
            if(b3[0]==31 && b3[1]==-117 && b3.length>=1024){
                b3=new JNI_Security().pluginEncrypt(b3,b3.length);
                byte[] b4=Gzip.ungzip(b3);
                return b4;
            }
            return b3;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
