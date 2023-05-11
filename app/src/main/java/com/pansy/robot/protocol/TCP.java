package com.pansy.robot.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP {
    public static byte[] send(String ip,int port,byte[] buf){
        try{
            InetAddress ia=InetAddress.getByName(ip);
            Socket socket = new Socket(ia, port);
            socket.setSoTimeout(5000);
            OutputStream os = socket.getOutputStream();
            os.write(buf);
            InputStream is=socket.getInputStream();
            byte[] buf2=new byte[2048];//tcp可以接收比较长的数据
            int len=is.read(buf2);
            socket.close();
            if(len>-1){
                byte[] ret=new byte[len];
                System.arraycopy(buf2,0,ret,0,ret.length);
                return ret;
            }else
                return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /*public static byte[] receive(){
        try{
            ServerSocket ss = new ServerSocket(10086);
            ss.setSoTimeout(5000);
            Socket s = ss.accept();
            InputStream is = s.getInputStream();
            byte[] rec = new byte[2048];
            int len=is.read(rec);
            byte[] buf=new byte[len];
            System.arraycopy(rec,0,buf,0,buf.length);
            s.close();
            return buf;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }*/
}
