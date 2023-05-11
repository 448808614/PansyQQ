package com.pansy.robot.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.pansy.robot.APP;
import com.pansy.robot.fragments.FragmentLog;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.PackDatagram;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.protocol.UnPackDatagram;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeartbeatService extends Service {
    private Thread t;
    private static ExecutorService threadPool=Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        t=new Thread(()-> {
            PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wakelock:heartbeat");
            try {
                while (true){
                    wakeLock.acquire();
                    if(APP.getUdp()!=null){
                        if(FragmentLog.print_heartbeat)
                            QQAPI.log_("心跳","发送心跳",0);
                        UnPackDatagram.setIsRece_0058(false);
                        PCQQ.send(PackDatagram.pack_0058());
                        offlineRelogin();
                    }
                    Thread.currentThread().sleep(60000);
                    wakeLock.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();

        //更新skey
        new Thread(()-> {
            while(true){
                try{
                    Thread.currentThread().sleep(3600*1000*24);
                    PCQQ.send(PackDatagram.pack_001D_qun());
                    PCQQ.send(PackDatagram.pack_001D_httpconn());
                    PCQQ.send(PackDatagram.pack_001D_tenpay());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 掉线重登
     */
    private void offlineRelogin(){
        threadPool.execute(()-> {
            for(int i=0;i<25;i++) {
                try {
                    if (UnPackDatagram.getIsRece_0058())
                        break;
                    Thread.currentThread().sleep(200);
                    if (i == 24 && UnPackDatagram.getIsRece_0058()==false) {
                        APP.setNoReceiveHeartbeat(APP.getNoReceiveHeartbeat()+1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(APP.getNoReceiveHeartbeat()>=3){
                //在后台无法重登
                //PCQQ.relogin();
                SendService.notifyOnline(false);
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(t!=null)
            t.interrupt();
    }
}
