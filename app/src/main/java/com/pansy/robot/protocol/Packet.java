package com.pansy.robot.protocol;


import com.pansy.robot.APP;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.ByteUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 包体构造类
 */
public class Packet {
    private List<Byte> list;
    public final static String QQ_VERSION="9.0.9.24445";
    public final static byte[] PB_VERSION={-21,43};
    private final byte[] PACKET_HEAD={2};
    public final static byte[] PACKET_VER={56,11};
    public final static int PACKET_VER_INT=5611;
    private final byte[] PACKET_TAIL={3};
    public final static byte[] KEY_0825_DATA1={0,24,0,22,0,1};
    public final static byte[] PUB_NO={0,0,4,86,0,0,0,1,0,0,21,-21};//SSO版本+主程序版本
    public final static byte[] KEY_QQDATA_KEY={119,69,55,94,51,105,109,103,35,105,41,37,104,49,50,93};
    public final static byte[] KEY_FIX_VER_02={2,0,0,0,1,1,1,0,0,105,6};
    public final static byte[] KEY_FIX_VER_03={3,0,0,0,1,1,1,0,0,105,6,0,0,0,0};
    public final static byte[] KEY_FIX_VER_04={4,0,0,0,1,1,1,0,0,105,6,0,0,0,0,0,0,0,0};
    public Packet(){
        list=new ArrayList<>();
    }
    public Packet putHead(){
        add(PACKET_HEAD);
        return this;
    }
    public Packet putVer(){
        add(PACKET_VER);
        return this;
    }
    public Packet putTail(){
        add(PACKET_TAIL);
        return this;
    }
    public Packet putQQ(){
        add(Converter.long2ByteArray(APP.getQQ()));
        return this;
    }
    public Packet put(byte[] buf){
        add(buf);
        return this;
    }
    public Packet put(byte b){
        list.add(b);
        return this;
    }
    public Packet put(int b){
        list.add((byte)b);
        return this;
    }
    public Packet putStr(String str){
        add(str.getBytes());
        return this;
    }
    public Packet putEnter(){
        add(new byte[]{13,10});
        return this;
    }
    public Packet putZero(int n){
        byte[] bytes=new byte[n];
        for(int i=0;i<bytes.length;i++){
            bytes[i]=0;
        }
        add(bytes);
        return this;
    }
    public Packet putRan(int n){
        add(ByteUtil.getRanByteArray(n));
        return this;
    }
    public Packet putTime(){
        add(Converter.long2ByteArray( System.currentTimeMillis()/1000));
        return this;
    }
    public Packet putFont(){
        add(new byte[]{0,12,-27,-66,-82,-24,-67,-81,-23,-101,-123,-23,-69,-111,0,0});
        return this;
    }
    public byte[] get(){
        byte[] buf=new byte[list.size()];
        for(int i=0;i<list.size();i++){
            buf[i]=list.get(i);
        }
        return buf;
    }
    private void add(byte[] buf){
        for(int i=0;i<buf.length;i++){
            list.add(buf[i]);
        }
    }

}
