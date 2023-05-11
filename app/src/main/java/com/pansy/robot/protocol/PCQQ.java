package com.pansy.robot.protocol;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.pansy.robot.APP;
import com.pansy.robot.activity.LoginActivity;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.R;
import com.pansy.robot.crypter.ECC;
import com.pansy.robot.crypter.JNI_ECDH;
import com.pansy.robot.crypter.JNI_RSA;
import com.pansy.robot.crypter.JNI_Security;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.crypter.Tea;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.fragments.FragmentMessage;
import com.pansy.robot.service.ReceiveService;
import com.pansy.robot.service.SendService;
import com.pansy.robot.struct.MsgPack;
import com.pansy.robot.struct.QQMessage;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.FileUtil;
import com.pansy.robot.utils.Gzip;
import com.pansy.robot.utils.HttpRequest;
import com.pansy.robot.utils.PhoneUtil;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.SoundUtil;
import com.pansy.robot.utils.ZLib;
import com.pansy.robot.view.LoadingView;
import com.yy.runscore.Util.ToastUtil;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static android.content.Context.CLIPBOARD_SERVICE;


/**
 * 封装了QQ协议的功能
 */
public class PCQQ {
    private static Context mContext;
    private static Intent mIntent=new Intent(APP.getAppContext(),SendService.class);
    public static LoadingView loadingView;
    public static AlertDialog loadingDialog;
    private static Thread timeoutThread;
    public static String select_tencent_server="sz.tencent.com";
    /**
     * 初始化socket线程
     */
    private static class InitUdpThread extends Thread{
        @Override
        public void run() {
            String ip=LoginActivity.getSpi_server_list().getSelectedItem().toString();
            select_tencent_server=ip;
            UDP udp=new UDP(ip,8000,4000);
            APP.setUdp(udp);
        }
    }
    /**
     * 在service向服务器发送数据
     */
    public static void send(byte[] buf){
        mIntent.putExtra("buf",buf);
        if(Build.VERSION.SDK_INT>=26)
            APP.getAppContext().startForegroundService(mIntent);
        else
            APP.getAppContext().startService(mIntent);
    }

    /**
     * 登录
     * @param account
     * @param pwd
     * @return
     */
    public static void login(Context context,long account,String pwd,int login_way) {
        initLogin(context,account, pwd, login_way);
        //loadMainTabActivity(account,pwd);
    }

    public static boolean verify_expire(long QQ,String pwd){
        try{
            SPHelper.write_QQ(Long.toString(QQ),pwd);
            APP.setQQ(QQ);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }
    private static void initLogin(Context context,long account,String pwd,int login_way){
        if (account < 9999) return;
        mContext=context;
        new JNI_Security().checkEnvironment();
        JSONObject json=new JSONObject();
        try {
            json.put("QQ",account);
            json.put("device_id",PhoneUtil.INSTANCE.getDeviceId(context));
            json.put("timestamp",System.currentTimeMillis()/1000);
            json.put("brand",Build.BRAND);
            json.put("model",Build.MODEL);
        }catch (Exception e){
            e.printStackTrace();
        }
        String data=JNI_RSA.encrypt(json.toString());
        try {
            data=URLEncoder.encode(data,"utf-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        String str=HttpRequest.postSync("http://"+APP.getMyService()+"/Pansy/develope/verify_expire.php","data="+data);
        if(str.equals("")){
            ImitateIosDialog dialog=new ImitateIosDialog(context);
            dialog.setCancelable(false);
            dialog.setContent("网络连接失败");
            dialog.setContentColor(Color.RED);
            dialog.hideCancel();
            dialog.show();
            return;
        }
        str=JNI_RSA.decrypt(str);
        try{
            json=new JSONObject(str);
            long timestamp=json.getLong("timestamp");
            long expire=json.getLong("expire");
            String msg=json.getString("msg");

            if(Math.abs(System.currentTimeMillis()/1000-timestamp)>300){
                ImitateIosDialog dialog=new ImitateIosDialog(context);
                dialog.setCancelable(false);
                dialog.setContent("系统时间错误");
                dialog.setContentColor(Color.RED);
                dialog.hideCancel();
                dialog.show();
                return;
            }
            if(expire==-1){
                ImitateIosDialog dialog=new ImitateIosDialog(context);
                dialog.setCancelable(false);
                dialog.setContent(msg);
                dialog.setContentColor(Color.RED);
                dialog.hideCancel();
                dialog.show();
                return;
            }
            if(expire<=System.currentTimeMillis()/1000){
                ImitateIosDialog dialog=new ImitateIosDialog(context);
                dialog.setCancelable(false);
                dialog.setContent(msg);
                dialog.setContentColor(Color.RED);
                dialog.setConfirmText("续费");
                dialog.setCancelText("退出");
                dialog.setOnConfirmListener(()->{
                    recharge(account,context);
                    return null;
                });
                dialog.setOnCancelListener(()->{
                    System.exit(-1);
                    return null;
                });
                dialog.show();
                return;
            }else{
                APP.setExpire(expire);
                initLogin2(context, account, pwd, login_way);
            }
        }catch (Exception e){
            e.printStackTrace();
            ImitateIosDialog dialog=new ImitateIosDialog(context);
            dialog.setCancelable(false);
            dialog.setContent("服务器验证失败");
            dialog.setContentColor(Color.RED);
            dialog.hideCancel();
            dialog.show();
            return;
        }
    }

    private static void recharge(long account,Context context){
        String content=HttpRequest.getSync("http://"+APP.getMyService()+"/Pansy/develope/buy_card.php",null);
        ImitateIosDialog dialog2=new ImitateIosDialog(context);
        dialog2.setCancelable(false);
        dialog2.setTitle("卡密续费");
        if(!content.equals(""))
            dialog2.setContent(content);
        else
            dialog2.hideContent();
        dialog2.needInput(false);
        dialog2.setOnConfirmListener(()->{
            String card=dialog2.getMInputString();
            if(!TextUtils.isEmpty(card)){
                try{
                    JSONObject json2=new JSONObject();
                    json2.put("QQ",account);
                    json2.put("card",card);
                    json2.put("device_id",PhoneUtil.INSTANCE.getDeviceId(context));
                    json2.put("timestamp",System.currentTimeMillis()/1000);
                    json2.put("brand",Build.BRAND);
                    json2.put("model",Build.MODEL);
                    String data2=JNI_RSA.encrypt(json2.toString());
                    data2=URLEncoder.encode(data2,"utf-8");
                    String str2=HttpRequest.postSync("http://"+APP.getMyService()+"/Pansy/develope/card_recharge.php","data="+data2);
                    if(str2.equals("")){
                        ImitateIosDialog dialog3=new ImitateIosDialog(context);
                        dialog3.setCancelable(false);
                        dialog3.setContent("网络连接失败");
                        dialog3.setContentColor(Color.RED);
                        dialog3.hideCancel();
                        dialog3.show();
                        return null;
                    }
                    str2=JNI_RSA.decrypt(str2);
                    json2=new JSONObject(str2);
                    long timestamp2=json2.getLong("timestamp");
                    long expire2=json2.getLong("expire");
                    String msg2=json2.getString("msg");

                    if(Math.abs(System.currentTimeMillis()/1000-timestamp2)>300){
                        ImitateIosDialog dialog3=new ImitateIosDialog(context);
                        dialog3.setCancelable(false);
                        dialog3.setContent("系统时间错误");
                        dialog3.setContentColor(Color.RED);
                        dialog3.hideCancel();
                        dialog3.show();
                        return null;
                    }
                    if(expire2==-1){
                        ImitateIosDialog dialog3=new ImitateIosDialog(context);
                        dialog3.setCancelable(false);
                        dialog3.setContent(msg2);
                        dialog3.setContentColor(Color.RED);
                        dialog3.hideCancel();
                        dialog3.show();
                        return null;
                    }
                    if(expire2<=System.currentTimeMillis()/1000){
                        ImitateIosDialog dialog3=new ImitateIosDialog(context);
                        dialog3.setContent(msg2);
                        dialog3.setContentColor(Color.RED);
                        dialog3.hideCancel();
                        dialog3.show();
                    }else{
                        SoundUtil.playSound(context,R.raw.success);
                        ImitateIosDialog dialog3=new ImitateIosDialog(context);
                        dialog3.setContent(msg2);
                        dialog3.setContentColor(Color.GREEN);
                        dialog3.hideCancel();
                        dialog3.show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ImitateIosDialog dialog3=new ImitateIosDialog(context);
                    dialog3.setCancelable(false);
                    dialog3.setContent("卡密验证失败");
                    dialog3.setContentColor(Color.RED);
                    dialog3.hideCancel();
                    dialog3.show();
                }
            }else
                ToastUtil.INSTANCE.toast(context,"请输入卡密");

            return null;
        });
        dialog2.show();
    }

    private static void initLogin2(Context context,long account,String pwd,int login_way){
        APP.setAuthorization(verify_expire(account, pwd));
        //登录加载
        if(login_way==1)
            LoginActivity.getBtn_login().setEnabled(false);
        else if(login_way==2)
            LoginActivity.getBtn_scan().setEnabled(false);
        else if(login_way==3){
            if(SPHelper.readLong("login_info","QQ",0)==0){
                ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                dialog.setContent(ByteUtil.byteArray2String(new byte[]{-26,-78,-95,-26,-100,-119,-26,-119,-66,-27,-120,-80,-28,-67,-96,-25,-102,-124,-25,-103,-69,-27,-67,-107,-24,-82,-80,-27,-67,-107,-17,-68,-116,-24,-81,-73,-27,-123,-120,-28,-67,-65,-25,-108,-88,-27,-81,-122,-25,-96,-127,-26,-120,-106,-26,-119,-85,-25,-96,-127,-26,-106,-71,-27,-68,-113,-24,-65,-101,-24,-95,-116,-25,-103,-69,-27,-67,-107,-27,-109,-90}));
                dialog.hideCancel();
                dialog.show();
                return;
            }
            if(!SPHelper.readString("login_info","device_characteristic","").equals(new Md5().a(PhoneUtil.INSTANCE.getManufacturer()+PhoneUtil.INSTANCE.getId(APP.getAppContext())+PhoneUtil.INSTANCE.getIMEI(APP.getAppContext())))){
                ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                dialog.setContent(ByteUtil.byteArray2String(new byte[]{-25,-103,-69,-27,-67,-107,-28,-65,-95,-26,-127,-81,-27,-68,-126,-27,-72,-72,-17,-68,-116,-24,-81,-73,-28,-67,-65,-25,-108,-88,-27,-81,-122,-25,-96,-127,-26,-120,-106,-26,-119,-85,-25,-96,-127,-26,-106,-71,-27,-68,-113,-24,-65,-101,-24,-95,-116,-25,-103,-69,-27,-67,-107}));
                dialog.setContentColor(Color.RED);
                dialog.hideCancel();
                dialog.show();
                return;
            }
            if(System.currentTimeMillis()/1000-SPHelper.readLong("login_info","timestamp",0)>3*3600*24){
                ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                dialog.setContent(ByteUtil.byteArray2String(new byte[]{-27,-121,-70,-28,-70,-114,-27,-82,-119,-27,-123,-88,-24,-128,-125,-24,-103,-111,-17,-68,-116,-25,-103,-69,-27,-67,-107,-28,-65,-95,-26,-127,-81,-26,-100,-119,-26,-107,-120,-26,-100,-97,-26,-100,-128,-23,-107,-65,-28,-72,-70,-28,-72,-119,-27,-92,-87,-17,-68,-116,-24,-81,-73,-23,-121,-115,-26,-106,-80,-28,-67,-65,-25,-108,-88,-27,-81,-122,-25,-96,-127,-26,-120,-106,-26,-119,-85,-25,-96,-127,-26,-106,-71,-27,-68,-113,-24,-65,-101,-24,-95,-116,-25,-103,-69,-27,-67,-107}));
                dialog.setContentColor(Color.RED);
                dialog.hideCancel();
                dialog.show();
                return;
            }
            login_way=1;//实际还是用第一种方式登录
            LoginActivity.getBtn_relogin().setEnabled(false);
            APP.setIsRelogin(true);
            APP.setRelogin_outside(true);
            APP.setQQ(SPHelper.readLong("login_info","QQ",0));
            APP.setClient_key(SPHelper.readString("login_info","client_key",""));
            UnPackDatagram.set_0x38_token(Converter.hexStr2ByteArray(SPHelper.readString("login_info","_0x38_token","")));
            UnPackDatagram.set_0x88_token(Converter.hexStr2ByteArray(SPHelper.readString("login_info","_0x88_token","")));
            UnPackDatagram.set_0828_cipher_key(Converter.hexStr2ByteArray(SPHelper.readString("login_info","_0828_cipher_key","")));
            UnPackDatagram.set_0828_decipher_key(Converter.hexStr2ByteArray(SPHelper.readString("login_info","_0828_decipher_key","")));
        }
        if(timeoutThread!=null) timeoutThread.interrupt();
        loginTimeout();
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        View view=View.inflate(context,R.layout.loading_layout,null);
        loadingView=view.findViewById(R.id.loadingView);
        loadingDialog=builder.create();
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        loadingDialog.getWindow().setContentView(view);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        APP.setPwd(pwd);
        APP.setLogin_way(login_way);
        UnPackDatagram.setIs_0836_63(false);
        //读取气泡id
        SendQQMessage.bubbleId=SPHelper.readInt("bubble",0);
        SendService.rece_count=0;
        SendService.send_count=0;
        SendService.heartbeat_count=0;
        SendService.relogin_count=0;
        //初始化tlv_0105
        byte[] tlv_0105;
        tlv_0105=Converter.hexStr2ByteArray(SPHelper.readString("tlv_0105",Long.toString(APP.getQQ()),""));
        if(tlv_0105==null){
            tlv_0105=new Packet().put(new byte[]{1,5,0,48}).put(new byte[]{0,1,1,2,0,20,1,1,0,16}).putRan(16)
                    .put(new byte[]{0,20,1,2,0,16}).putRan(16).get();
        }
        APP.setTlv_0105(tlv_0105);
        //初始化包体变量
        PackDatagram.KEY_0825_KEY=ByteUtil.getRanByteArray(16);
        PackDatagram.KEY_0825_REDIRECTION_KEY=ByteUtil.getRanByteArray(16);
        PackDatagram.KEY_00BA_KEY=ByteUtil.getRanByteArray(16);
        PackDatagram.KEY_00BA_FIX_KEY=ByteUtil.getRanByteArray(16);
        String imei=PhoneUtil.INSTANCE.getIMEI(context);
        PackDatagram.COMPUTERID_EX=new Md5().b(imei.getBytes());
        PackDatagram.COMPUTERID_EX_MD5=new Md5().b(PackDatagram.COMPUTERID_EX);
        //设备名
        PackDatagram.pcName="PC-"+imei;
        //设备id
        byte[] device_id;
        device_id=Converter.hexStr2ByteArray(SPHelper.readString("device_id",""));
        if(device_id==null){
            device_id=ByteUtil.getRanByteArray(32);
            SPHelper.writeString("device_id",Converter.byteArray2HexStr(device_id));
        }
        PackDatagram.DEVICE_ID=device_id;

        UnPackDatagram.setIsScanQRSucceed(false);
        UnPackDatagram.setIsScanQRDialogClose(false);
        UnPackDatagram.setVerify_seq(0);
        UnPackDatagram.setVerify_code(null);
        UnPackDatagram.setIsVerify(false);

        //生成ecdh密钥
        ECC ecc=new JNI_ECDH().ecdh(APP.getAppContext());
        APP.setEcc(ecc);
        //if(APP.getSocket()==null){
        //初始化socket
        Thread t1=new InitUdpThread();
        //Thread t2=new ReceiveThread(t1);
        t1.start();
        //t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //}
        //清空异常日志
        if(FileUtil.exists(APP.getPansyQQPath()+"log.txt"))
            FileUtil.write(APP.getPansyQQPath()+"log.txt","");
        if(APP.getIp()!=null){
            //启动接收service
            //Intent intent=new Intent(APP.getAppContext(),ReceiveService.class);
            //APP.getAppContext().startService(intent);
            //启动发送service
            UnPackDatagram.preventLost(PackDatagram.pack_0825(false,login_way),1);
        }else{
            Toast.makeText(mContext,"无网络",Toast.LENGTH_LONG).show();
            LoginActivity.getBtn_login().setEnabled(true);
        }
    }
    private static void loginTimeout(){
        timeoutThread=new Thread(()-> {
            try{
                Thread.currentThread().sleep(20000);
                new Handler(Looper.getMainLooper()).post(()-> {
                    LoginActivity.getBtn_login().setEnabled(true);
                    LoginActivity.getBtn_scan().setEnabled(true);
                    LoginActivity.getBtn_relogin().setEnabled(true);
                    if(PCQQ.loadingDialog!=null && PCQQ.loadingDialog.isShowing() && UnPackDatagram.getScanQR_dialog()==null){
                        PCQQ.loadingDialog.dismiss();
                        Toast.makeText(mContext,"登录超时",Toast.LENGTH_LONG).show();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        timeoutThread.start();
    }

    private static void loadMainTabActivity(long account,String pwd){
        APP.setAuthorization(verify_expire(account, pwd));
        loadMainTabActivity();
    }
    public static void loadMainTabActivity(){
        Intent intent=new Intent(APP.getLoginContext(),MainTabActivity.class);
        APP.getLoginContext().startActivity(intent);
        ((LoginActivity)APP.getLoginContext()).finish();
    }
    /**
     * 掉线重登
     */
    public static void relogin(){
        APP.setRelogin_outside(false);//从里面重登
        QQAPI.log_("重登","掉线重登",0);
        APP.setIsRelogin(true);
        UnPackDatagram.setTgtgt_key(null);
        send(PackDatagram.pack_0825(false,1));
    }
    /**
     * 撤回消息
     */
    public static void withdraw(long gn,long withdraw_seq,long withdraw_id){
        send(PackDatagram.pack_03F7(gn,withdraw_seq,withdraw_id));
    }
    /**
     * 点赞
     */
    public static void praise(long QQ){
        send(PackDatagram.pack_03E3(QQ));
    }
    /**
     * 下线
     */
    public static void logout(){
        send(PackDatagram.pack_0062());
    }

    /**
     * 踢人
     */
    public static void kick(long gn,long QQ){
        Packet pack = new Packet().put((byte)2).put(Converter.long2ByteArray(Converter.gn2Gid(gn))).put((byte)5).put(Converter.long2ByteArray(QQ)).putZero(7);
        send(new Packet().putHead().putVer().put(new byte[]{0, 2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack.get(), APP.getSession_key())).putTail().get());
    }
    /**
     * 同意/拒绝好友
     */
    public static void agreeFriend(long QQ,boolean isAgree){
        send(PackDatagram.pack_00A8(QQ,isAgree));
    }

    /**
     * 退群
     */
    public static void exitGroup(long gn){
        send(new Packet().putHead().putVer().put(new byte[]{0, 2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new Packet().put((byte)9).put(Converter.long2ByteArray(Converter.gn2Gid(gn))).get(), APP.getSession_key())).putTail().get());
    }

    /**
     * 收到新信息后更新消息列表
     */
    public static void updateMessageList(byte[] data,byte[] seq,int type){
        if(APP.getMainTabContext()==null) return;
        if(type==0){
            QQMessage qm=Analy.analy_0052(data,seq);
            if(qm!=null)
                FragmentMessage.setMessageAdatper(qm);
        }else if(type==1){
            QQMessage qm=Analy.analy_00A6(data,seq);
            if(qm!=null)
                FragmentMessage.setMessageAdatper(qm);
        }else if(type==7){
            Analy.analy_008D(data,seq);
            //if(qm!=null)
                //FragmentMessage.setMessageAdatper(qm);
        }
    }

}
