package com.pansy.robot.utils;

import com.pansy.robot.APP;
import com.pansy.robot.protocol.QQAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class HttpRequest {
    private final static int TIMEOUT=5000;

    private static ExecutorService threadPool=Executors.newSingleThreadExecutor();

    public static String get(String url){
        return get(url,null,null,"utf-8");
    }
    public static String get(String url,String charset){
        return get(url,null,null,charset);
    }

    public static String get(String url,String cookies,String charset){
        return get(url,cookies,null,charset);
    }
    public static String get(String url,String cookies,HashMap<String,String> requestMap,String charset){
        if(url==null || url.equalsIgnoreCase(""))
            return "";
        BufferedReader reader=null;
        try{
            URL url1=new URL(url);
            HttpURLConnection conn=(HttpURLConnection)url1.openConnection();
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Referer",url);
            conn.setRequestProperty("Accept-Language","zh");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection","keep-Alive");
            conn.setRequestProperty("User-Agent",APP.getUser_agent());
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            if(cookies!=null)
                conn.setRequestProperty("Cookie",cookies);
            if(requestMap!=null){
                Iterator iterator=requestMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,String> entry=(Map.Entry<String, String>) iterator.next();
                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }
            conn.connect();
            if(charset==null)
                charset="utf-8";
            reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),charset));
            StringBuffer sb=new StringBuffer();
            String temp;
            while((temp=reader.readLine())!=null){
                sb.append(temp);
                sb.append("\n");
            }
            //删除最后一个换行符
            sb.delete(sb.length()-1,sb.length());
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }finally {
            if(reader!=null){
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //获取返回cookie
    public static String get_c(String url,String cookies){
        if(url==null || url.equalsIgnoreCase(""))
            return "";
        BufferedReader reader=null;
        try{
            URL url1 = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();
            conn.setUseCaches(false);//请求不能使用缓存
            conn.setConnectTimeout(TIMEOUT);//链接超时
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Referer",url);
            conn.setRequestProperty("Accept-Language","zh");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection","keep-Alive");
            conn.setRequestProperty("User-Agent",APP.getUser_agent());
            conn.setRequestMethod("GET");
            if(cookies!=null)
                conn.setRequestProperty("Cookie",cookies);
            int status = conn.getResponseCode();
            if(status==200){
                String cookieVal = conn.getHeaderField("Set-Cookie");
                return cookieVal;
            }else
                return "";
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }finally {
            if(reader!=null){
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static String post(String url){
        return post(url,null,null,null,null);
    }
    public static String post(String url,String param){
        return post(url,param,null,null,null);
    }

    public static String getSync(final String url,String charset){
        class C implements Callable<String> {
            @Override
            public String call() {
                try{
                    return get(url,null,null,charset);
                }catch (Exception e){
                    e.printStackTrace();
                    return "";
                }
            }
        }
        Future<String> future=threadPool.submit(new C());
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String postSync(final String url,final String param){
        class C implements Callable<String> {
                @Override
                public String call() {
                    try{
                        return post(url,param,null,null,null);
                    }catch (Exception e){
                        e.printStackTrace();
                        return "";
                    }
                }
        }
        Future<String> future=threadPool.submit(new C());
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String post(String url,String param,String cookies){
        return post(url,param,cookies,null,null);
    }
    public static String post(String url,String param,String cookies,HashMap<String,String> requestMap){
        return post(url,param,cookies,requestMap,null);
    }
    public static String post(String url,String param,String cookies,HashMap<String,String> requestMap,byte[] bytes){
        if(url==null || url.equals(""))
            return "";
        BufferedReader reader=null;
        try{
            URL url1=new URL(url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Referer",url);
            conn.setRequestProperty("Accept-Language","zh");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection","keep-Alive");
            conn.setRequestProperty("User-Agent",APP.getUser_agent());
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            if(cookies!=null)
                conn.setRequestProperty("Cookie",cookies);
            if(requestMap!=null){
                Iterator iterator=requestMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,String> entry=(Map.Entry<String, String>) iterator.next();
                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.setDoInput(true);
            PrintWriter out=new PrintWriter(conn.getOutputStream());

            if(param!=null){
                out.print(param);
                out.flush();
            }
            if(bytes!=null){
                DataOutputStream dataOut=new DataOutputStream(conn.getOutputStream());
                //每次发送1024字节，防止一次性发送给软件增加负载
                if(bytes.length>=1024){
                    int n=bytes.length/1024;
                    int r=bytes.length%1024;
                    if(r>0) n++;
                    for(int i=0;i<n;i++){
                        if(i==n-1){
                            dataOut.write(bytes,1024*i,r);
                        }else{
                            dataOut.write(bytes,1024*i,1024);
                        }
                    }
                }else
                    dataOut.write(bytes,0,bytes.length);
                dataOut.flush();
            }
            reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb=new StringBuffer();
            String temp;
            while((temp=reader.readLine())!=null){
                sb.append(temp);
                sb.append("\n");
            }
            //删除最后一个换行符
            sb.delete(sb.length()-1,sb.length());
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }finally {
            if(reader!=null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void post_voice(String url,HashMap<String,String> requestMap,byte[] bytes){
        if(url==null || url.equals(""))
            return;
        BufferedReader reader=null;
        try{
            long start=System.currentTimeMillis();
            URL url1=new URL(url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Referer",url);
            conn.setRequestProperty("Accept-Language","zh");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection","keep-Alive");
            conn.setRequestProperty("User-Agent",APP.getUser_agent());
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            if(requestMap!=null){
                Iterator iterator=requestMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,String> entry=(Map.Entry<String, String>) iterator.next();
                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.setDoInput(true);

            if(bytes!=null){
                DataOutputStream dataOut=new DataOutputStream(conn.getOutputStream());
                //每次发送1024字节，防止一次性发送给软件增加负载
                if(bytes.length>=1024){
                    int n=bytes.length/1024;
                    int r=bytes.length%1024;
                    if(r>0) n++;
                    for(int i=0;i<n;i++){
                        if(i==n-1){
                            dataOut.write(bytes,1024*i,r);
                        }else{
                            dataOut.write(bytes,1024*i,1024);
                        }
                    }
                }else
                    dataOut.write(bytes,0,bytes.length);
                dataOut.flush();
                QQAPI.log_("语音上传完成","耗时"+(System.currentTimeMillis()-start)+"ms√",2);
                reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                return;
            }
        }catch(Exception e){
            e.printStackTrace();
            QQAPI.log_("语音上传失败",e.getMessage(),1);
            return;
        }finally {
            if(reader!=null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    //返回byte数组
    public static byte[] post_b(String url,String param,String cookies,HashMap<String,String> requestMap,byte[] bytes){
        if(url==null || url.equals(""))
            return null;
        BufferedReader reader=null;
        try{
            URL url1=new URL(url);
            HttpURLConnection conn=(HttpURLConnection)url1.openConnection();
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Referer",url);
            conn.setRequestProperty("Accept-Language","zh");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection","keep-Alive");
            conn.setRequestProperty("User-Agent",APP.getUser_agent());
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            if(cookies!=null)
                conn.setRequestProperty("Cookie",cookies);
            if(requestMap!=null){
                Iterator iterator=requestMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,String> entry=(Map.Entry<String, String>) iterator.next();
                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.setDoInput(true);
            PrintWriter out=new PrintWriter(conn.getOutputStream());

            if(param!=null){
                out.print(param);
                out.flush();
            }
            if(bytes!=null){
                DataOutputStream dataOut=new DataOutputStream(conn.getOutputStream());
                //每次发送1024字节，防止一次性发送给软件增加负载
                if(bytes.length>=1024){
                    int n=bytes.length/1024;
                    int r=bytes.length%1024;
                    if(r>0) n++;
                    for(int i=0;i<n;i++){
                        if(i==n-1){
                            dataOut.write(bytes,1024*i,r);
                        }else{
                            dataOut.write(bytes,1024*i,1024);
                        }
                    }
                }else{
                    dataOut.write(bytes,0,bytes.length);
                }
                dataOut.flush();
            }
            return ByteUtil.inputStream2ByteArray(conn.getInputStream());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if(reader!=null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
