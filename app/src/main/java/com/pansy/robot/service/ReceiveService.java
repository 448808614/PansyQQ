package com.pansy.robot.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.pansy.robot.APP;
import com.pansy.robot.protocol.UDP;
import com.pansy.robot.protocol.UnPackDatagram;

public class ReceiveService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(()-> {
            while(true){
                UDP udp=APP.getUdp();
                if(udp!=null){
                    System.out.println("开始接收");
                    byte[] buf=udp.receive();
                    System.out.println("接收完成");
                    if(buf!=null){
                        try{
                            UnPackDatagram.unPacket(buf);
                        }catch (final Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
