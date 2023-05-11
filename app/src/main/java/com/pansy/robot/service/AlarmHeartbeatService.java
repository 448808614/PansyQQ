package com.pansy.robot.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.fragments.FragmentLog;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.PackDatagram;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.protocol.UnPackDatagram;
import com.pansy.robot.utils.Converter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmHeartbeatService extends Service {
    private static ExecutorService threadPool=Executors.newSingleThreadExecutor();
    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;
    private static Context mContext;
    private static Thread t;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext=this;
        NotificationCompat.Builder builder;
        NotificationManager notificationManager;
        notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel("id2","name2",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            channel.enableLights(false);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
        builder=new NotificationCompat.Builder(APP.getAppContext(),"id2");
        builder.setSmallIcon(R.drawable.logo).setContentTitle("PansyQQ").setContentText("已开启防掉模式");
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        Intent intent=new Intent(APP.getAppContext(), MainTabActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(APP.getAppContext(),2,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(2,builder.build());
        startForeground(2,builder.build());

        //更新skey
        t=new Thread(()-> {
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
        });
        t.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wakelock:alarm_heartbeat");
        wakeLock.acquire();
        if(APP.getUdp()!=null){
            if(FragmentLog.print_heartbeat)
                QQAPI.log_("心跳","发送心跳",0);
            UnPackDatagram.setIsRece_0058(false);
            PCQQ.send(PackDatagram.pack_0058());
            offlineRelogin();
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime()+60000;
        Intent alarmIntent = new Intent(this,AlarmHeartbeatService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            pendingIntent = PendingIntent.getForegroundService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            pendingIntent = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // 6.0
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //  4.4
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis, pendingIntent);

        wakeLock.release();
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
        //开启自启动后不会调用该方法
        try{
            alarmManager.cancel(pendingIntent);
            if(t!=null)
                t.interrupt();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                stopForeground(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void cancelAlarmManager(){
        if(alarmManager!=null && mContext!=null){
            try{
                alarmManager.cancel(pendingIntent);
                if(t!=null)
                    t.interrupt();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ((AlarmHeartbeatService)mContext).stopForeground(true);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
