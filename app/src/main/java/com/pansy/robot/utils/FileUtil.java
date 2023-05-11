package com.pansy.robot.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import com.pansy.robot.APP;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

public class FileUtil {

    public static byte[] readByteArray(String path){
        try{
            FileInputStream inputStream=new FileInputStream(path);
            return ByteUtil.inputStream2ByteArray(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void writeByteArray(String path,byte[] data){
        try{
            ByteArrayInputStream inputStream=new ByteArrayInputStream(data);
            FileOutputStream outputStream=new FileOutputStream(path);
            byte[] buf=new byte[1024];
            int count=0;
            while((count=inputStream.read(buf))!=-1){
                outputStream.write(buf,0,count);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String read(String path){
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line=bufferedReader.readLine();
            StringBuilder sb=new StringBuilder();
            while (line!=null){
                sb.append(line);
                sb.append("\n");
                line = bufferedReader.readLine();
            }
            //删除最后一个换行符
            sb.delete(sb.length()-1,sb.length());
            bufferedReader.close();
            fileReader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void write(String path,String content){
        try{
            File file = new File(path);
            OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(file),"utf-8");
            int i=0;
            while (i<content.length()){
                osw.write(content.charAt(i));
                i++;
            }
            osw.flush();
            osw.close();
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public static void append(String path,String content){
        write(path,read(path)+content);
    }

    public static boolean exists(String path){
        try{
            File file = new File(path);
            return file.exists();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void delete(String path){
        try{
            File file = new File(path);
            file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getPath(Context context, Uri uri)  {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean hasEncrypted(String str){
        if(str.length()<256)
            return false;
        char c=str.charAt(0);
        char c1=str.charAt(str.length()-1);
        try{
            if((c>='0' && c<='9') || (c>='A' && c<='Z'))
                if((c1>='0' && c1<='9') || (c1>='A' && c1<='Z'))
                    return true;
                else
                    return false;
            else
                return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static boolean create(String path){
        try{
            File file=new File(path);
            if(!file.exists()) {
                file.createNewFile();
                return true;
            }else
                return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void showFileChooser(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            ((Activity)context).startActivityForResult(Intent.createChooser(intent, "选择插件"), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 安装apk
     */
    public static void installApk(Context context,String apkName){
        try{
            File file=new File(APP.getPansyQQPath()+apkName);
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.N){
                Uri apkUri=FileProvider.getUriForFile(context,"com.pansy.robot.fileprovider",file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri,"application/vnd.android.package-archive");
            }else{
                intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
            }
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
