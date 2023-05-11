package com.pansy.robot.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.zip.CRC32;

public class ByteUtil {
    /**
     * 生成随机byte数组
     * @return
     */
    public static byte[] getRanByteArray(int n){
        Random random=new Random();
        byte[] bytes=new byte[n];
        //StringBuilder sb=new StringBuilder();
        for(int i=0;i<n;i++) {
            int r = random.nextInt(256);
           bytes[i]=(byte)r;
        }
        return bytes;
    }
    /**
     * 截取byte数组
     */
    public static byte[] subBytes(byte[] bytes,int start,int length){
        if(start+length>bytes.length) length=bytes.length-start;
        byte[] buf=new byte[length];
        System.arraycopy(bytes,start,buf,0,length);
        return buf;
    }

    public static byte[] subBytes(byte[] bytes,int length){
        if(length>bytes.length) length=bytes.length;
        byte[] buf=new byte[length];
        System.arraycopy(bytes,0,buf,0,length);
        return buf;
    }

    /**
     * 截取int数组
     */
    public static int[] subInts(int[] ints,int start,int length){
        int[] buf=new int[length];
        System.arraycopy(ints,start,buf,0,length);
        return buf;
    }
    /*public static String subString(String data,int start,int length){
        return data.substring(start*3,(start+length)*3-1);
    }*/

    public static byte[] getCrc32(byte[] bytes){
        CRC32 crc32=new CRC32();
        crc32.update(bytes);
        long l=crc32.getValue();
        byte[] ret=Converter.hexStr2ByteArray(Converter.long2Hex(l));
        return ret;
    }
    /**
     * 反取crc32
     * @param bytes
     * @return
     */
    public static byte[] getRevCrc32(byte[] bytes){
        CRC32 crc32=new CRC32();
        byte[] ret=new byte[4];
        crc32.update(bytes);
        long l=crc32.getValue();
        byte[] buf=Converter.hexStr2ByteArray(Converter.long2Hex(l));
        for(int i=0;i<buf.length;i++){
            ret[i]=buf[buf.length-i-1];
        }
        return ret;
    }
    //skey转换为gtk
    public static String getGtk(String skey){
        int hash=5381;
        for(int i=0;i<skey.length();i++){
            hash+=(hash << 5)+(int)skey.charAt(i);
        }
        return Integer.toString(hash & 0x7FFFFFFF);
    }
    //取消转义符
    public static String cancelESC(String str){
        return str.replace("&nbsp;"," ").replace("&#39;","'").replace("&amp;","&")
                .replace("[em]","").replace("[/em]","").replace("&lt;","")
                .replace("&gt;","").replace("&quot;","\"");
    }
    /**
     * 获取图片类型
     */
    public static int getImgType(byte[] bin){
        if(Arrays.equals(subBytes(bin,2),new byte[]{-1,-40}) && Arrays.equals(subBytes(bin,bin.length-2,2),new byte[]{-1,-39})){
            return 1;//jpg
        }else if(Arrays.equals(subBytes(bin,8),new byte[]{-119,80,78,71,13,10,26,10}) || Arrays.equals(subBytes(bin,4),new byte[]{82,73,70,70})){
            return 2;//png
        }else if(Arrays.equals(subBytes(bin,6),new byte[]{71,73,70,56,57,97}) || Arrays.equals(subBytes(bin,6),new byte[]{71,73,70,56,55,97})){
            return 3;//gif
        }else{
            return 1;
        }
    }

    /**
     * 查找指定byte数组，返回索引
     * @return
     */
    public static int byteIndexOf(byte[] srcBytes,byte[] searchBytes){
        for (int i = 0; i < srcBytes.length - searchBytes.length+1; i++)
        {
            if (srcBytes[i] == searchBytes[0])
            {
                if (searchBytes.length == 1) { return i; }
                boolean flag = true;
                for (int j = 1; j < searchBytes.length; j++)
                {
                    if (srcBytes[i + j] != searchBytes[j])
                    {
                        flag = false;
                        break;
                    }
                }
                if (flag) { return i; }
            }
        }
        return -1;
    }
    public static byte[] inputStream2ByteArray(InputStream inputStream){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int rc = 0;
            while ((rc = inputStream.read(buf, 0, 1024)) > 0) {
                outputStream.write(buf, 0, rc);
            }
            byte[] bin = outputStream.toByteArray();
            return bin;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static Drawable byteArray2Drawable(byte bytes[]){
        Bitmap bitmap=BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        BitmapDrawable drawable=new BitmapDrawable(bitmap);
        return drawable;
    }
    public static String getNow(){
        Calendar calendar=Calendar.getInstance();
        int y=calendar.get(Calendar.YEAR);
        int m=calendar.get(Calendar.MONTH)+1;
        int d=calendar.get(Calendar.DATE);
        int h=calendar.get(Calendar.HOUR_OF_DAY);
        int mi=calendar.get(Calendar.MINUTE);
        int s=calendar.get(Calendar.SECOND);
        StringBuilder builder=new StringBuilder();
        builder.append(y);
        builder.append(m<10?"0"+m:m);
        builder.append(d<10?"0"+d:d);
        builder.append(h<10?"0"+h:h);
        builder.append(mi<10?"0"+mi:mi);
        builder.append(s<10?"0"+s:s);
        builder.append("0000");
        return builder.toString();
    }
    public static String getTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        String time=sdf.format(Long.valueOf(System.currentTimeMillis()));
        return time;
    }
    public static String getTimeDay(){
        SimpleDateFormat sdf=new SimpleDateFormat("MM-dd HH:mm:ss");
        String time=sdf.format(Long.valueOf(System.currentTimeMillis()));
        return time;
    }
    public static String splitHex(String hex){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<hex.length()/2;i++){
            sb.append(hex.substring(i*2,i*2+2));
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    public static String getRanChar(int count){
        String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<count;i++){
            Random r=new Random();
            sb.append(chars.charAt(r.nextInt(26)));
        }
        return sb.toString();
    }
    public static String getRandomNum(int c){
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<c;i++){
            Random r=new Random();
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }
    public static String getRanMac(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<6;i++){
            int n=new Random().nextInt(255);
            if(n<16)
                sb.append("0");
            sb.append(Integer.toHexString(n));
            if(i<5)
                sb.append("-");
        }
        return sb.toString().toUpperCase();
    }

    public static String byteArray2String(byte[] bytes){
        return new String(bytes);
    }

}
