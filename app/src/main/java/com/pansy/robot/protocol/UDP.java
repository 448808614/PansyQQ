package com.pansy.robot.protocol;

import com.pansy.robot.APP;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP {
    private InetAddress ip;
    private int PORT;
    private static DatagramSocket datagramSocket;
    private DatagramPacket recePacket;
    public UDP(String str, int server_port, int local_port){
        try {
            PORT=server_port;
            ip=InetAddress.getByName(str);
            String s=ip.toString();
            APP.setIp(s.substring(s.indexOf("/")+1,s.length()));
            if(datagramSocket==null)
                datagramSocket=new DatagramSocket(local_port);
            byte[] receBuf = new byte[1024];
            recePacket = new DatagramPacket(receBuf,receBuf.length);
        } catch (Exception e) {
            System.out.println("端口异常");
            e.printStackTrace();
        }
    }
    public void send(byte[] buf){
        try{
            DatagramPacket datagramPacket=new DatagramPacket(buf,buf.length,ip,PORT);
            datagramSocket.send(datagramPacket);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("发送异常");
        }
    }
    public byte[] receive(){
        try {
            datagramSocket.receive(recePacket);
            byte[] buf=new byte[recePacket.getLength()];
            System.arraycopy(recePacket.getData(),0,buf,0,buf.length);
            return buf;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("接收异常");
            return null;
        }
    }
}
