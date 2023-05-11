package com.pansy.robot.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.protocol.UDP;
import com.pansy.robot.protocol.UnPackDatagram;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.ByteUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SendService extends Service{
    private static ExecutorService thraedPool;
    private static NotificationCompat.Builder mBuilder;
    private static NotificationManager mNotificationManager;
    public static int rece_count=0;
    public static int send_count=0;
    public static int heartbeat_count=0;
    public static int relogin_count=0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //接收消息
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

        thraedPool=Executors.newSingleThreadExecutor();
        mNotificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel("id","name",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            channel.enableLights(false);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder=new NotificationCompat.Builder(APP.getAppContext(),"id");
        mBuilder.setSmallIcon(R.drawable.logo).setContentTitle("PansyQQ正在后台运行").setContentText("收:0  发:0  心跳:0  重登:0");
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        Intent intent=new Intent(APP.getAppContext(), MainTabActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(APP.getAppContext(),1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(1,mBuilder.build());
        startForeground(1,mBuilder.build());
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(intent!=null){
            final byte[] buf=intent.getByteArrayExtra("buf");
            if(buf!=null){
                thraedPool.execute(()-> {
                    byte[] flag=ByteUtil.subBytes(buf,3,2);
                    System.out.println("发送"+Converter.byteArray2HexStr(flag)+">>"+buf.length+"字节数据");
                    UDP udp=APP.getUdp();
                    if(udp!=null)
                        APP.getUdp().send(buf);
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    public static void notifyNotification(){
        mBuilder.setContentText("收:"+rece_count+"  发:"+send_count+"  心跳:"+heartbeat_count+"  重登:"+relogin_count);
        mNotificationManager.notify(1,mBuilder.build());
    }
    public static void notifyOnline(boolean isOnline){
        if(isOnline){
            mBuilder.setSmallIcon(R.drawable.logo);
            mBuilder.setContentTitle("PansyQQ正在后台运行");
        }else {
            mBuilder.setSmallIcon(R.drawable.logo_gray);
            mBuilder.setContentTitle("PansyQQ已掉线");
        }
        mNotificationManager.notify(1,mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //APP.getWakeLock().release();
        stopForeground(true);
    }
}
