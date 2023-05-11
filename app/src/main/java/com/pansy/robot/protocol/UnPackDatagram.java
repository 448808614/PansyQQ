package com.pansy.robot.protocol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.pansy.robot.APP;
import com.pansy.robot.activity.LoginActivity;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.R;
import com.pansy.robot.crypter.ECC;
import com.pansy.robot.crypter.JNI_ECDH;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.crypter.Tea;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.fragments.FragmentLog;
import com.pansy.robot.service.AlarmHeartbeatService;
import com.pansy.robot.service.HeartbeatService;
import com.pansy.robot.service.SendService;
import com.pansy.robot.struct.Private_0x30;
import com.pansy.robot.struct.QQGroup;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.PhoneUtil;
import com.pansy.robot.utils.QRCodeUtil;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.utils.ByteUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * 消息解包类
 */
public class UnPackDatagram {
    private static byte[] loginTime;
    private static byte[] loginIP;
    private static byte[] _0825_token;
    private static byte[] tgtgt_key;
    private static boolean isVerify=false;
    private static byte[] _00BA_token;
    private static byte[] _00DE_0x29_tlv;
    private static byte[] _0828_decipher_key;
    private static byte[] verify_code;
    private static int verify_seq=0;
    private static byte[] verify_token;
    private static Dialog verify_dialog;
    private static Dialog scanQR_dialog;
    private static ImageView img_verify;
    private static EditText edt_verify;
    private static byte[] _00DE_key;
    private static byte[] _00DE_token2;
    private static volatile String _0388_img_ukey="";
    private static volatile boolean isRece_0388_img=false;
    private static volatile String _0388_voice_ukey="";
    private static volatile boolean isRece_0388_voice=false;
    private static volatile String _0352_ukey="";
    private static volatile byte[] _0352_key;
    private static volatile boolean isRece_0352=false;
    private static List<QQGroup> mGroups;
    private static ExecutorService threadPool= Executors.newSingleThreadExecutor();
    private static byte[] _0x38_token;
    private static byte[] _0x88_token;
    private static byte[] _0828_cipher_key;
    private static volatile boolean isRece_0825=false;
    private static volatile boolean isRece_0836=false;
    private static volatile boolean isRece_0828=false;
    private static volatile boolean isRece_0058=false;
    private static volatile boolean isRece_0818=false;
    private static volatile boolean isRece_00BA=false;
    private static volatile boolean isRece_00EC=false;
    public static byte[] _0819_encipher_key;
    public static byte[] _0819_tlv_0303;
    public static byte[] _0819_tlv_0006;//0819中返回
    private static Bitmap bitmap_qrcode;
    private static volatile boolean isScanQRSucceed=false;
    private static volatile boolean isScarnQRDialogClose=false;

    public static boolean isIs_0836_63() {
        return is_0836_63;
    }

    public static void setIs_0836_63(boolean is_0836_63) {
        UnPackDatagram.is_0836_63 = is_0836_63;
    }

    private static boolean is_0836_63=false;
    public static boolean isIsScanQRSucceed() {
        return isScanQRSucceed;
    }

    public static void setIsScanQRSucceed(boolean isScanQRSucceed) {
        UnPackDatagram.isScanQRSucceed = isScanQRSucceed;
    }
    public static void setIsScanQRDialogClose(boolean isScanQRDialogClose) {
        UnPackDatagram.isScarnQRDialogClose = isScanQRDialogClose;
    }
    public static boolean getIsScanQRDialogClose() {
        return UnPackDatagram.isScarnQRDialogClose;
    }

    public static boolean getIsRece_0058(){
        return isRece_0058;
    }
    public static void setIsRece_0058(boolean isRece_0058){
        UnPackDatagram.isRece_0058=isRece_0058;
    }
    public static void unPacket(byte[] data){
        byte[] flag=ByteUtil.subBytes(data,3,2);
        System.out.println("收到"+Converter.byteArray2HexStr(flag)+"<<"+data.length+"字节数据");
        if(Arrays.equals(flag,new byte[]{8,37})){
            isRece_0825=true;
            unPack_0825(data);
        }else if(Arrays.equals(flag,new byte[]{8,54})){
            isRece_0836=true;
            unPack_0836(data);
        }else if(Arrays.equals(flag,new byte[]{8,40})){
            isRece_0828=true;
            unPack_0828(data);
        }else if(Arrays.equals(flag,new byte[]{0,-20})){
            isRece_00EC=true;
            unPack_00EC();
        }else if(Arrays.equals(flag,new byte[]{0,29})){
            unPack_001D(data);
        } else if(Arrays.equals(flag,new byte[]{0,23}) || Arrays.equals(flag,new byte[]{0,-50})){
            unPack_msg(data);
        }else if(Arrays.equals(flag,new byte[]{0,-70})){
            unPack_00BA(data);
        }else if(Arrays.equals(flag,new byte[]{0,-34})){
            unPack_00DE(data);
        }else if(Arrays.equals(flag,new byte[]{3,-120})){
            unPack_0388(data);
        }else if(Arrays.equals(flag,new byte[]{3,82})){
            unPack_0352(data);
        }else if(Arrays.equals(flag,new byte[]{8,24})){
            unPack_0818(data);
        } else if(Arrays.equals(flag,new byte[]{8,25})){
            unPack_0819(data);
        } else if(Arrays.equals(flag,new byte[]{0,88})){
            isRece_0058=true;
            if(FragmentLog.print_heartbeat)
                QQAPI.log_("心跳","心跳成功",0);
            APP.setNoReceiveHeartbeat(0);
            SendService.heartbeat_count++;
            SendService.notifyNotification();
        }else if(Arrays.equals(flag,new byte[]{3,89})){
            unpack_0359(data);
        }else if(Arrays.equals(flag,new byte[]{0,2})){
            unPack_0002(data);
        }else if(Arrays.equals(flag,new byte[]{0,-82})){
            unPack_00AE(data);
        }else if(Arrays.equals(flag,new byte[]{0,-51})){
            if(data.length==31) {
                if(FragmentLog.print_success)
                    QQAPI.log_("发送消息成功", "好友消息已发送√", 2);
            }else
                QQAPI.log_("发送消息异常","发送好友消息貌似被屏蔽",1);
        }else if(Arrays.equals(flag,new byte[]{0,-30})){
            if(data.length==31) {
                if(FragmentLog.print_success)
                    QQAPI.log_("发送消息成功", "私聊消息已发送√", 2);
            }else
                QQAPI.log_("发送消息异常","发送私聊消息貌似被屏蔽",1);
        }
    }

    private static void unpack_0359(byte[] data){
        //同意被拉进群，传递时间戳
        data=decryptFirst(data,APP.getSession_key());
        byte[] timestamp=ByteUtil.subBytes(data,data.length-23,8);
        PCQQ.send(PackDatagram.pack_03E3_agreeInviteMe(timestamp));
    }

    private static void unPack_0825(byte[] data){
        if(data.length==39){
            preventLost(PackDatagram.pack_0825(true,APP.getLogin_way()),1);
        }
        byte[] temp=data;
        data=decryptFirst(data,PackDatagram.KEY_0825_KEY);
        if(data==null)
            data=decryptFirst(temp,PackDatagram.KEY_0825_REDIRECTION_KEY);
        if(data[0]==-2){
            //重定向服务器
            byte[] ip_bytes=ByteUtil.subBytes(data,95,4);
            String ip=Converter.byteArray2IP(ip_bytes);
            System.out.println("需要重定向到："+ip);
            //更新socket
            APP.setUdp(new UDP(ip,8000,4000));
            preventLost(PackDatagram.pack_0825(true,APP.getLogin_way()),1);
        }else if(data[0]==0){
            //Toast.makeText(APP.getLoginContext(), "服务器接触成功", Toast.LENGTH_SHORT).show();
            //String s=Converter.byteArray2HexStr(data);
            loginTime=ByteUtil.subBytes(data,67,4);
            loginIP=ByteUtil.subBytes(data,71,4);
            _0825_token=ByteUtil.subBytes(data,5,56);
            if(tgtgt_key==null)
                tgtgt_key=ByteUtil.getRanByteArray(16);
            if(APP.getIsRelogin())
                //return;
                preventLost(PackDatagram.pack_0828(_0x38_token,_0x88_token,_0828_cipher_key),4);
            else{
                if(APP.getLogin_way()==1)
                    preventLost(PackDatagram.pack_0836(false,false,null,null,null,null,null,1,null),2);
                else if(APP.getLogin_way()==2)
                    if(_0819_tlv_0303==null)
                        preventLost(PackDatagram.pack_0818(),3);
                    else
                        preventLost(PackDatagram.pack_0836(false,false,null,null,_0819_tlv_0006,null,null,2,_0819_tlv_0303),2);
            }
        }
    }

    public static int getVerify_seq() {
        return verify_seq;
    }

    public static void setVerify_seq(int verify_seq) {
        UnPackDatagram.verify_seq = verify_seq;
    }

    public static byte[] getVerify_code() {
        return verify_code;
    }

    public static void setVerify_code(byte[] verify_code) {
        UnPackDatagram.verify_code = verify_code;
    }

    private static void unPack_0836(byte[] data){
        if(data.length==271 || data.length==207 || data.length==615 || data.length==559 || data.length==623){//二次ecdh
            data=decryptFirst(data,APP.getEcc().sha_key);
            if(Arrays.equals(ByteUtil.subBytes(data,0,4),new byte[]{1,3,0,49}))
                data=updataShaKey(data);
            if(ByteUtil.byteIndexOf(data,new byte[]{-24,-81,-91,-27,-113,-73,-25,-96,-127,-23,-107,-65,-26,-100,-97,-26,-100,-86,-25,-103,-69,-27,-67,-107})>-1) {
                showLoginToast("该号码长期未登录，已被系统设置成保护状态");
                enableLoginButton();
                return;
            }else
                data=new Tea().decrypt(data,tgtgt_key);
            if(data!=null)
                if(data[0]==1 || data[0]==63)
                    send_0836_790(data);

        } else if(data.length==871 || data.length==879){//验证码
            int start=78;
            if(data.length==879) start=86;
            byte[] temp_data;
            if(is_0836_63)
                temp_data=decryptFirst(data,PackDatagram.get_0836_send_encrypt_key());
            else
                temp_data=decryptFirst(data,APP.getEcc().sha_key);
            if(temp_data!=null && temp_data[0]==-5){
                int verify_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(temp_data,start,2));
                if(verify_code==null){
                    //第一次接收
                    verify_code=ByteUtil.subBytes(temp_data,start+2,verify_length);
                }
                _00BA_token=ByteUtil.subBytes(temp_data,temp_data.length-60,40);
                verify_seq=1;
                preventLost(PackDatagram.pack_00BA(_00BA_token,verify_seq),5);
            }else{
                unpack_0836_success(data);
            }
        }else if(data.length==303 || data.length==335 || data.length==367 || data.length==431){
            //00de更新字段或二次ecdh
            int length=data.length;
            data=decryptFirst(data,APP.getEcc().sha_key);
            if(Arrays.equals(ByteUtil.subBytes(data,0,4),new byte[]{1,3,0,49})){
                data=updataShaKey(data);
                data=new Tea().decrypt(data,tgtgt_key);
                if(data[0]==1){
                    send_0836_790(data);
                }else if(data[0]==63){
                    _00DE_key=ByteUtil.subBytes(data,7,16);
                    byte[] token=ByteUtil.subBytes(data,25,56);
                    _00DE_token2=new Packet().put(new byte[]{2,0,32}).put(ByteUtil.subBytes(data,83,32)).put(new byte[]{2,0,32}).put(ByteUtil.subBytes(data,120,32)).get();
                    if(length==303 || length==367){
                        _00DE_0x29_tlv=ByteUtil.subBytes(data,152,52);
                    }else if(length==335){
                        _00DE_0x29_tlv=ByteUtil.subBytes(data,180,52);
                    }else if(length==431){
                        _00BA_token=ByteUtil.subBytes(data,160,56);
                        _00DE_0x29_tlv=ByteUtil.subBytes(data,216,52);
                    }
                    PCQQ.send(PackDatagram.pack_00DE(_00DE_key,token,_00DE_token2));
                }
            }
        }else if(data.length==63){
            is_0836_63=true;
            data=decryptFirst(data,PackDatagram.get_0836_send_encrypt_key());
            byte[] _0063=ByteUtil.subBytes(data,5,32);
            preventLost(PackDatagram.pack_0836(false,false,_0063,null,null,null,null,1,null),2);
        } else if(data.length==255){//更新tgtgtkey
            data=decryptFirst(data,tgtgt_key);
            send_0836_790(data);
        } else if(data.length==191) {//token不知道
            byte temp[] = data;
            data = decryptFirst(data, APP.getEcc().sha_key);
            if (data == null) data = decryptFirst(temp, tgtgt_key);//shakey解不开就用tgtgtkey解
            if (ByteUtil.byteIndexOf(data, new byte[]{-27, -67, -109, -27, -119, -115, -28, -72, -118, -25, -67, -111, -25, -114, -81, -27, -94, -125, -27, -68, -126, -27, -72, -72}) > -1){
                showLoginToast("登录环境异常，建议更换登录服务器或用扫码登录");
                enableLoginButton();
            }else
                send_0836_790(data);
        } else if(data.length>700) {//成功
            unpack_0836_success(data);
        }else if(data.length==319 || data.length==351 || data.length==175){
            showLoginToast("密码错误或登录环境异常，建议更换登录服务器或用扫码登录");
            enableLoginButton();
        }else if(data.length==135){
            showLoginToast("抱歉，请重新输入密码");
            enableLoginButton();
        }else if(data.length==279){
            showLoginToast("你的帐号存在被盗风险，已进入保护模式");
            enableLoginButton();
        }else if(data.length==263){
            showLoginToast("你输入的帐号不存在");
            enableLoginButton();
        }else if(data.length==551 || data.length==487){
            showLoginToast("请关闭设备锁后再进行操作");
            enableLoginButton();
        }else if(data.length==359 || data.length==343){
            showLoginToast("你的帐号长期未登录已被回收");
            enableLoginButton();
        } else {
            final int length=data.length;
            final byte[] finalData=data;

            new Handler(Looper.getMainLooper()).post(()-> {
                ImitateIosDialog dialog=new ImitateIosDialog(APP.getLoginContext());
                dialog.setTitle("未知返回("+ length+")");
                dialog.setContent("是否复制返回数据?");
                dialog.setConfirmText("复制");
                dialog.setOnConfirmListener(()-> {
                    String str="sha_key:"+Converter.byteArray2HexStr(APP.getEcc().sha_key) +"\n"
                            +"tgtgt_key:" +Converter.byteArray2HexStr(tgtgt_key) +"\n"
                            +"send_encrypt_key:" +Converter.byteArray2HexStr(PackDatagram.get_0836_send_encrypt_key()) +"\n"
                            +Converter.byteArray2HexStr(finalData);
                    ClipboardManager myClipboard = (ClipboardManager)APP.getLoginContext().getSystemService(CLIPBOARD_SERVICE);
                    ClipData myClip=ClipData.newPlainText("unknow data",str);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(APP.getLoginContext(),"已复制到剪贴板文本",Toast.LENGTH_LONG).show();
                    return null;
                });
                dialog.show();
                if(verify_dialog!=null && verify_dialog.isShowing())
                    verify_dialog.dismiss();
            });
            enableLoginButton();
        }
    }
    private static void unpack_0836_success(byte[] data){
        if(is_0836_63){
            data=decryptFirst(data,tgtgt_key);
        } else
            data=decryptFirst(data,APP.getEcc().sha_key);
        if(Arrays.equals(ByteUtil.subBytes(data,0,4),new byte[]{1,3,0,49})){
            data=updataShaKey(data);
        }
        if(!is_0836_63)
            data=new Tea().decrypt(data,tgtgt_key);
        if(data==null){
            showLoginToast("获取用户资料失败");
            enableLoginButton();
            return;
        }
        int offset=0;
        byte[] b=ByteUtil.subBytes(data,141,2);
        if(Arrays.equals(b,new byte[]{1,7})){
            offset=0;
        }else if(Arrays.equals(b,new byte[]{0,51})){
            offset=28;
        }else if(Arrays.equals(b,new byte[]{1,16})){
            offset=64;
        }else{
            offset=35;
        }
        _0828_decipher_key=ByteUtil.subBytes(data,171+offset,16);
        String client_key=Converter.byteArray2HexStr(ByteUtil.subBytes(data,484+offset,112)).replace(" ","");
        APP.setClient_key(client_key);
        _0x38_token=ByteUtil.subBytes(data,25,56);
        _0x88_token=ByteUtil.subBytes(data,189+offset,136);
        _0828_cipher_key=ByteUtil.subBytes(data,7,16);
        SPHelper.writeString("login_info","device_characteristic",new Md5().a(PhoneUtil.INSTANCE.getManufacturer()+PhoneUtil.INSTANCE.getId(APP.getAppContext())+PhoneUtil.INSTANCE.getIMEI(APP.getAppContext())));
        SPHelper.writeLong("login_info","QQ",APP.getQQ());
        SPHelper.writeString("login_info","client_key",client_key);
        SPHelper.writeString("login_info","_0x38_token",Converter.byteArray2HexStr(_0x38_token));
        SPHelper.writeString("login_info","_0x88_token",Converter.byteArray2HexStr(_0x88_token));
        SPHelper.writeString("login_info","_0828_cipher_key",Converter.byteArray2HexStr(_0828_cipher_key));
        SPHelper.writeString("login_info","_0828_decipher_key",Converter.byteArray2HexStr(_0828_decipher_key));
        SPHelper.writeLong("login_info","timestamp",System.currentTimeMillis()/1000);
        preventLost(PackDatagram.pack_0828(_0x38_token,_0x88_token,_0828_cipher_key),4);
    }

    private static void send_0836_790(byte[] data){
        //发送包加密依然用一次ecdh的share_key,接收包解密用二次ecdh的share_key
        byte[] tlv_0006=ByteUtil.subBytes(data,25,120);
        byte[] token;
        if(isVerify){
            token=_00BA_token;
            isVerify=false;
        }else{
            if(data.length<209)//0836返回193
                token=ByteUtil.getRanByteArray(56);
            else
                token=ByteUtil.subBytes(data,153,56);
        }
        tgtgt_key=ByteUtil.subBytes(data,5,16);
        preventLost(PackDatagram.pack_0836(true,false,null,token,tlv_0006,null,null,1,null),2);
    }

    private static void unPack_0828(byte[] data){
        int length=data.length;
        data=decryptFirst(data,_0828_decipher_key);
        byte[] session_key;
        if(length==407){
            session_key=ByteUtil.subBytes(data,25,16);
        }else if(length==439 ){
            session_key=ByteUtil.subBytes(data,63,16);
        }else if(length==527){
            session_key=ByteUtil.subBytes(data,63,16);
            byte[] tlv_0105=new Packet().put(new byte[]{1,5,0,-120,0,1,1,2}).put(new byte[]{0,64,2,1,3,60,1,3,0,0})
                    .put(ByteUtil.subBytes(data,data.length-123,56)).put(new byte[]{0,64,2,2,3,60,1,3,0,0})
                    .put(ByteUtil.subBytes(data,data.length-56,56)).get();
            SPHelper.writeString("tlv_0105",Long.toString(APP.getQQ()),Converter.byteArray2HexStr(tlv_0105));
        } else if(length==495){
            session_key=ByteUtil.subBytes(data,25,16);
            byte[] tlv_0105=new Packet().put(new byte[]{1,5,0,-120,0,1,1,2}).put(new byte[]{0,64,2,1,3,60,1,3,0,0})
                    .put(ByteUtil.subBytes(data,341,56)).put(new byte[]{0,64,2,2,3,60,1,3,0,0})
                    .put(ByteUtil.subBytes(data,408,56)).get();
            SPHelper.writeString("tlv_0105",Long.toString(APP.getQQ()),Converter.byteArray2HexStr(tlv_0105));
        }else{
            showLoginToast("登录失败");
            enableLoginButton();
            return;
        }
        APP.setSession_key(session_key);
        preventLost(PackDatagram.pack_00EC(),6);
        //PCQQ.send(PackDatagram.pack_00EC());
    }
    private static byte[] updataShaKey(byte[] data){
        byte[] tk=ByteUtil.subBytes(data,4,49);
        ECC ecc=APP.getEcc();
        ecc.sha_key_new=new JNI_ECDH().ecdh2(ecc,tk);//更新sha_key_new
        APP.setEcc(ecc);
        data=new Tea().decrypt(ByteUtil.subBytes(data,53,data.length-53),ecc.sha_key_new);
        return data;
    }
    private static void unPack_00EC(){
        if(APP.getIsRelogin()==false || APP.isRelogin_outside()){
            if(verify_dialog!=null && verify_dialog.isShowing())
                verify_dialog.dismiss();
            if(scanQR_dialog!=null && scanQR_dialog.isShowing())
                scanQR_dialog.dismiss();
            //HttpRequest.post("http://"+APP.getMyService()+"/Pansy/develope/verify_expire.php","QQ="+ APP.getQQ());
            new Handler(Looper.getMainLooper()).post(()->{
                if(PCQQ.loadingDialog!=null && PCQQ.loadingDialog.isShowing()){
                    PCQQ.loadingView.showSuccess();
                    PCQQ.loadingView.setText("登录成功");
                }

                threadPool.execute(()-> {
                    try {
                        if(APP.getLogin_way()==2)
                            Thread.currentThread().sleep(2000);
                        else
                            Thread.currentThread().sleep(1000);
                        if(PCQQ.loadingDialog!=null && PCQQ.loadingDialog.isShowing())
                            PCQQ.loadingDialog.dismiss();
                        Intent intent2=new Intent(APP.getLoginContext(),MainTabActivity.class);
                        APP.getLoginContext().startActivity(intent2);
                        ((LoginActivity)APP.getLoginContext()).finish();
                        //启动心跳
                        if(SPHelper.readBool("prevent_offline",false)){
                            Intent intent=new Intent(APP.getAppContext(),AlarmHeartbeatService.class);
                            if(Build.VERSION.SDK_INT>=26)
                                APP.getAppContext().startForegroundService(intent);
                            else
                                APP.getAppContext().startService(intent);
                        }else{
                            Intent intent=new Intent(APP.getAppContext(),HeartbeatService.class);
                            APP.getAppContext().startService(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }else{
            showLoginToast("重登成功");
            APP.setNoReceiveHeartbeat(0);
            QQAPI.log_("重登","重登成功",0);
            SendService.relogin_count++;
            SendService.notifyNotification();
            SendService.notifyOnline(true);
        }
        PCQQ.send(PackDatagram.pack_001D_qun());
        PCQQ.send(PackDatagram.pack_001D_httpconn());
        PCQQ.send(PackDatagram.pack_001D_tenpay());
    }
    private static void unPack_001D(byte[] data){
        data=decryptFirst(data,APP.getSession_key());
        if(data[0]==51){
            if(Arrays.equals(ByteUtil.subBytes(data,120,10),new byte[]{113,117,110,46,113,113,46,99,111,109})){
                String skey=new String(ByteUtil.subBytes(data,4,10));
                String p_skey=new String(ByteUtil.subBytes(data,132,44));
                String gtk=ByteUtil.getGtk(skey);
                String cookies="uin=o"+APP.getQQ()+"; p_uin=o"+APP.getQQ()+"; skey="+skey+"; p_skey="+p_skey;
                APP.setSkey(skey);
                APP.setP_skey_qun(p_skey);
                APP.setGtk(gtk);
                APP.setCookies(cookies);
                int index=ByteUtil.byteIndexOf(data,new byte[]{113,122,111,110,101,46,113,113,46,99,111,109});
                if(index>-1){
                    index+=13;
                    int length=data[index];
                    String qzone_skey=new String(ByteUtil.subBytes(data,index+1,length));
                    APP.setQzone_skey(qzone_skey);
                }

                if(APP.getIsRelogin()==false || APP.isRelogin_outside())
                    setGroups(QQAPI.getGroupList());
            }else{
                String p_skey_tenpay=new String(ByteUtil.subBytes(data,197,44));
                APP.setP_skey_tenpay(p_skey_tenpay);
            }
        }else if(data[0]==38){//httpconn_key
            byte[] httpconn_key=ByteUtil.subBytes(data,2,16);
            APP.setHttpconn_key(httpconn_key);
            byte[] httpconn_token=ByteUtil.subBytes(data,31,104);
            APP.setHttpconn_token(httpconn_token);
        }
    }

    public static byte[] getVerify_token() {
        return verify_token;
    }

    public static void setVerify_token(byte[] verify_token) {
        UnPackDatagram.verify_token = verify_token;
    }

    private static void unPack_00BA(byte[] data){
        isRece_00BA=true;
        int length=data.length;
        data=decryptFirst(data,PackDatagram.KEY_00BA_KEY);
        //String s=Converter.byteArray2HexStr(data);
        if(length==95){
            //验证码正确
            _00BA_token=ByteUtil.subBytes(data,10,56);
            tgtgt_key=ByteUtil.getRanByteArray(16);
            isVerify=true;
            PCQQ.send(PackDatagram.pack_0836(true,true,null,_00BA_token,null,null,null,1,null ));
            return;
        }
        int verify_token_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(data,8,2));
        verify_token=ByteUtil.subBytes(data,10,verify_token_length);//确认验证码时使用
        int verify_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(data,10+verify_token_length,2));
        byte[] code=ByteUtil.subBytes(data,10+verify_token_length+2,verify_length);
        //if(verify_completed==false){
            byte[] temp;
            if(verify_code!=null){
                temp=new byte[verify_code.length+code.length];
                System.arraycopy(verify_code,0,temp,0,verify_code.length);
                System.arraycopy(code,0,temp,verify_code.length,code.length);
            }else{
                temp=new byte[code.length];
                System.arraycopy(code,0,temp,0,code.length);
            }
            verify_code=temp;

        int flag=(int)Converter.byteArray2Long(ByteUtil.subBytes(data,10+verify_token_length+2+verify_length+1,1));
        System.out.println("flag:"+flag);
        if(flag==0){
            //verify_completed=true;
            System.out.println("验证码接收完成");
            //showVefiryDialog();
            showVerifyDialog();

        }else{
            verify_seq++;
            byte[] token=ByteUtil.subBytes(data,data.length-58,40);
            preventLost(PackDatagram.pack_00BA(token,verify_seq),5);
        }
    }
    private static void unPack_00DE(byte[] data){
        data=decryptFirst(data,_00DE_key);
        byte[] token=ByteUtil.subBytes(data,6,58);
        boolean bool=_00BA_token!=null;
        preventLost(PackDatagram.pack_0836(true,bool,null,_00BA_token,null,token,_00DE_0x29_tlv,1,null),2);
    }
    private static void unPack_msg(byte[] data){
        byte[] flag=ByteUtil.subBytes(data,3,2);
        byte[] sequence=ByteUtil.subBytes(data,5,2);
        data=decryptFirst(data,APP.getSession_key());
        if(data==null) return;
        Packet pack=new Packet();
        pack.putHead().putVer().put(flag).put(sequence)
                .putQQ().put(Packet.KEY_FIX_VER_02).put(new Tea().encrypt(ByteUtil.subBytes(data,0,16),APP.getSession_key())).putTail();
        PCQQ.send(pack.get());
        byte[] event=ByteUtil.subBytes(data,18,2);

        if(data!=null){
            //发送原始消息广播
            Intent intent=new Intent();
            intent.setAction("com.pansyqq.receive.rawmessage");
            intent.putExtra("data",data);
            intent.putExtra("flag",flag);
            intent.putExtra("event",event);
            APP.getAppContext().sendBroadcast(intent);
        }

        if(Arrays.equals(flag,new byte[]{0,23})){//系统消息，0017
            if(Arrays.equals(event,new byte[]{0,82})){//群消息，0052
                PCQQ.updateMessageList(data,sequence,0);
            }else if(Arrays.equals(event,new byte[]{2,-36})){//群消息撤回
                Analy.prevent_withdraw(data,sequence);
            }
            else{//其他消息
                Analy.analy_event(data,sequence,event);
            }
        }else if(Arrays.equals(flag,new byte[]{0,-50})){//个人消息,00CE
            if(Arrays.equals(event,new byte[]{0,-90}))//好友消息，00A6
                PCQQ.updateMessageList(data,sequence,1);
            else if(Arrays.equals(event,new byte[]{0,-115}))//私聊消息，008D
                PCQQ.updateMessageList(data,sequence,7);
        }

    }
    private static void unPack_0388(byte[] data){
        int length=data.length;
        data=decryptFirst(data,APP.getSession_key());
        //图片ukey:1A 42
        //语音ukey:2A 3A
        if(data[12]==1){//图片
            isRece_0388_img=true;
            if(length==239)
                _0388_img_ukey=Converter.byteArray2HexStr(Protobuf.analy(ByteUtil.subBytes(data,8,data.length-8),new int[]{26,66})).replace(" ","");
            else
                _0388_img_ukey="";
        }else if(data[12]==3){//语音
            if(ByteUtil.byteIndexOf(data,new byte[]{111,118,101,114,32,102,105,108,101,32,115,105,122,101,32,109,97,120})>-1){//语音大小超过限制
                isRece_0388_voice=false;
                _0388_voice_ukey="";
                QQAPI.log_("语音发送失败","文件大小超过限制，请上传1m以内的语音",1);
            }else{
                isRece_0388_voice=true;
                _0388_voice_ukey=Converter.byteArray2HexStr(Protobuf.analy(ByteUtil.subBytes(data,8,data.length-8),new int[]{42,58})).replace(" ","");
            }
        }
    }
    private static void unPack_0352(byte[] data){
        int length=data.length;
        data=decryptFirst(data,APP.getSession_key());
        //图片ukey:12 4A,key:12 52
        if(data[12]==1){//图片
            data=Protobuf.analyBySeq(ByteUtil.subBytes(data,8,data.length-8),2);
            isRece_0352=true;
            if(length>300)
                _0352_ukey=Converter.byteArray2HexStr(Protobuf.analy(data,new int[]{74})).replace(" ","");
            else
                _0352_ukey="";
            _0352_key=Protobuf.analy(data,new int[]{82});
        }else{
            isRece_0352=false;
            _0352_ukey="";
            _0352_key=null;
        }
    }
    //type:1 0825,2 0836，3 0818，4 0828，5 00ba, 6 00ec
    public static void preventLost(final byte[] data,int type){
        threadPool.execute(()-> {
            if(type==1){
                isRece_0825=false;
            }else if(type==2){
                isRece_0836=false;
            }else if(type==3){
                isRece_0818=false;
            }else if(type==4){
                isRece_0828=false;
            }else if(type==5){
                isRece_00BA=false;
            }else if(type==6){
                isRece_00EC=false;
            }
            PCQQ.send(data);
            //0836丢包重发
            int i=0;
            for(;i<5;i++){
                //重发5次都没收到就放弃
                if(type==1){
                    if(isRece_0825) break;
                }else if(type==2){
                    if(isRece_0836) break;
                }else if(type==3){
                    if(isRece_0818) break;
                }else if(type==4){
                    if(isRece_0828) break;
                }else if(type==5){
                    if(isRece_00BA) break;
                }else if(type==6){
                    if(isRece_00EC) break;
                }
                for(int j=0;j<18;j++){
                    try {
                        Thread.currentThread().sleep(200);
                        if(type==1){
                            if(isRece_0825) break;
                        }else if(type==2){
                            if(isRece_0836) break;
                        }else if(type==3){
                            if(isRece_0818) break;
                        }else if(type==4){
                            if(isRece_0828) break;
                        }else if(type==5){
                            if(isRece_00BA) break;
                        }else if(type==6){
                            if(isRece_00EC) break;
                        }
                        //if(isRece_0836)
                            //break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //3秒内没收到就重发
                    if(j==17){
                        boolean b=false;
                        if(type==1){
                            if(!isRece_0825) b=true;
                        }else if(type==2){
                            if(!isRece_0836) b=true;
                        }else if(type==3){
                            if(!isRece_0818) b=true;
                        }else if(type==4){
                            if(!isRece_0828) b=true;
                        }else if(type==5){
                            if(!isRece_00BA) b=true;
                        }else if(type==6){
                            if(!isRece_00EC) b=true;
                        }
                        if(b){
                            System.out.println("丢包重发->>");
                            PCQQ.send(data);
                        }
                    }
                }
            }
            if(i==5){
                showLoginToast("登录超时，请检查网络设置或更换登录服务器");
                enableLoginButton();
            }
        });
    }

    private static void showVerifyDialog(){
        new Handler(Looper.getMainLooper()).post(()-> {
            try{
                if(verify_dialog==null){
                    AlertDialog.Builder builder=new AlertDialog.Builder(APP.getLoginContext());
                    View view=View.inflate(APP.getLoginContext(), R.layout.verify_layout,null);
                    verify_dialog=builder.create();
                    verify_dialog.show();
                    verify_dialog.getWindow().setContentView(view);
                    //解决AppcompatActiviy的两侧白边的问题
                    verify_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    //解决editext不弹出输入法的问题
                    verify_dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    verify_dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    verify_dialog.setOnCancelListener((dialog)-> LoginActivity.getBtn_login().setEnabled(true));
                    //点击图片更换验证码
                    img_verify=view.findViewById(R.id.img_verify);
                    Button btn=view.findViewById(R.id.btn_verify);
                    edt_verify=view.findViewById(R.id.edt_verify);
                    img_verify.setOnClickListener((v)-> {
                        System.out.println("刷新验证码");
                        verify_seq=0;
                        verify_code=null;
                        preventLost(PackDatagram.pack_00BA_ref(),5);
                    });
                    //发送验证码
                    btn.setOnClickListener((v)->{
                        String code;
                        if((code=edt_verify.getText().toString()).length()==4) {
                            verify_seq=0;
                            verify_code=null;
                            preventLost(PackDatagram.pack_00BA_in(code.getBytes()),5);
                        }
                    });
                }
                //设置验证码
                if(verify_dialog.isShowing()==false){
                    verify_dialog.show();
                }
                edt_verify.setText("");
                Bitmap bitmap= BitmapFactory.decodeByteArray(verify_code,0,verify_code.length);
                BitmapDrawable drawable=new BitmapDrawable(bitmap);
                img_verify.setBackgroundDrawable(drawable);
            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    private static void showScanQRDialog(final String qrcode_url){
        new Handler(Looper.getMainLooper()).post(()->{
            try{
                if(scanQR_dialog==null){
                    AlertDialog.Builder builder=new AlertDialog.Builder(APP.getLoginContext());
                    View view=View.inflate(APP.getLoginContext(), R.layout.scanqr_layout,null);
                    scanQR_dialog=builder.create();
                    scanQR_dialog.show();
                    scanQR_dialog.getWindow().setContentView(view);
                    scanQR_dialog.setCanceledOnTouchOutside(false);
                    //解决AppcompatActiviy的两侧白边的问题
                    scanQR_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    final ImageView img_qrcode=view.findViewById(R.id.img_qrcode);
                    Button btn_saveQR=view.findViewById(R.id.btn_saveQR);
                    Button btn_closeQR=view.findViewById(R.id.btn_closeQR);

                    btn_saveQR.setOnClickListener((v)-> {
                        if(bitmap_qrcode!=null){
                            File file=new File(APP.getPansyQQPath()+"qrcode.jpg");
                            try {
                                FileOutputStream fos=new FileOutputStream(file);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap_qrcode.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                                byte[] buf=new byte[1024];
                                int count=0;
                                while((count=is.read(buf))!=-1){
                                    fos.write(buf,0,count);
                                }
                                //通知相册更新
                                MediaStore.Images.Media.insertImage(APP.getAppContext().getContentResolver(), BitmapFactory.decodeFile(file.getAbsolutePath()), file.getName(), null);
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri uri = Uri.fromFile(file);
                                intent.setData(uri);
                                APP.getAppContext().sendBroadcast(intent);
                                Toast.makeText(APP.getLoginContext(),"二维码已保存到"+APP.getPansyQQPath()+"qrcode.jpg",Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    btn_closeQR.setOnClickListener((v)-> {
                        scanQR_dialog.dismiss();
                        isScarnQRDialogClose=true;
                        scanQR_dialog=null;
                        _0819_tlv_0303=null;
                        if(PCQQ.loadingDialog!=null && PCQQ.loadingDialog.isShowing())
                            PCQQ.loadingDialog.dismiss();
                        LoginActivity.getBtn_scan().setEnabled(true);
                    });
                    new Thread(()-> {
                        try {
                            //bitmap_qrcode = BitmapFactory.decodeStream(new URL(qrcode_url).openStream());
                            bitmap_qrcode=QRCodeUtil.INSTANCE.createQRCodeBitmap(qrcode_url,800,800);
                            new Handler(Looper.getMainLooper()).post(()-> img_qrcode.setImageBitmap(bitmap_qrcode));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }).start();

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }
    public static byte[] getLoginTime() {
        return loginTime;
    }

    public static byte[] getLoginIP() {
        return loginIP;
    }

    public static byte[] get_0825_token() {
        return _0825_token;
    }

    public static byte[] getTgtgt_key() {
        return tgtgt_key;
    }
    public static void setTgtgt_key(byte[] tgtgt_key) {
        UnPackDatagram.tgtgt_key=tgtgt_key;
    }
    public static boolean getIsVerify() {
        return isVerify;
    }

    public static void setIsVerify(boolean isVerify) {
        UnPackDatagram.isVerify = isVerify;
    }

    public static byte[] get_00BA_token() {
        return _00BA_token;
    }

    public static void set_00BA_token(byte[] _00BA_token) {
        UnPackDatagram._00BA_token = _00BA_token;
    }
    private static void showLoginToast(final String str){
        new Handler(Looper.getMainLooper()).post(()-> {
            Toast.makeText(APP.getAppContext(),str,Toast.LENGTH_LONG).show();
            if(verify_dialog!=null && verify_dialog.isShowing())
                verify_dialog.dismiss();
            if(scanQR_dialog!=null && scanQR_dialog.isShowing())
                scanQR_dialog.dismiss();
        });
    }
    private static void enableLoginButton(){
        new Handler(Looper.getMainLooper()).post(()->{
            LoginActivity.getBtn_login().setEnabled(true);
            LoginActivity.getBtn_scan().setEnabled(true);
            LoginActivity.getBtn_relogin().setEnabled(true);
            if(PCQQ.loadingView!=null && PCQQ.loadingDialog.isShowing()){
                PCQQ.loadingView.showFailed();
                PCQQ.loadingView.setText("登录失败");
                threadPool.execute(()->{
                    try {
                        Thread.currentThread().sleep(1000);
                        PCQQ.loadingDialog.dismiss();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        if(verify_dialog!=null && verify_dialog.isShowing())
            verify_dialog.dismiss();
    }
    public static void setGroups(String str){
        mGroups=new ArrayList<>();
        JsonParser parser=new JsonParser();
        try {
            JsonObject object=(JsonObject) parser.parse(str);
            if(object.has("create")){
                JsonArray array=object.get("create").getAsJsonArray();
                for(int i=0;i<array.size();i++){
                    long gc=array.get(i).getAsJsonObject().get("gc").getAsLong();
                    String gn=array.get(i).getAsJsonObject().get("gn").getAsString();
                    gn=ByteUtil.cancelESC(gn);
                    mGroups.add(new QQGroup(gc,gn,SPHelper.readBool("group_open",gc+"",true)));
                }
            }
            if(object.has("manage")){
                JsonArray array=object.get("manage").getAsJsonArray();
                for(int i=0;i<array.size();i++){
                    long gc=array.get(i).getAsJsonObject().get("gc").getAsLong();
                    String gn=array.get(i).getAsJsonObject().get("gn").getAsString();
                    gn=ByteUtil.cancelESC(gn);
                    mGroups.add(new QQGroup(gc,gn,SPHelper.readBool("group_open",gc+"",true)));
                    //groupMap.put(gc,gn);
                }
            }
            if(object.has("join")){
                JsonArray array2=object.get("join").getAsJsonArray();
                for(int i=0;i<array2.size();i++){
                    long gc=array2.get(i).getAsJsonObject().get("gc").getAsLong();
                    String gn=array2.get(i).getAsJsonObject().get("gn").getAsString();
                    gn=ByteUtil.cancelESC(gn);
                    mGroups.add(new QQGroup(gc,gn,SPHelper.readBool("group_open",gc+"",true)));
                    //groupMap.put(gc,gn);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void unPack_0818(byte[] data){
        isRece_0818=true;
        data=decryptFirst(data,APP.getEcc().sha_key);
        _0819_encipher_key=ByteUtil.subBytes(data,7,16);
        final byte[] qrcode_k=ByteUtil.subBytes(data,91,32);
        final byte[] token= ByteUtil.subBytes(data,29,56);
        showScanQRDialog("http://txz.qq.com/p?k="+new String(qrcode_k)+"&f=1");
        new Thread(()-> {
            while(isScanQRSucceed==false && isScarnQRDialogClose==false){
                try{
                    PCQQ.send(PackDatagram.pack_0819(qrcode_k,token));
                    Thread.currentThread().sleep(2000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static void unPack_0819(byte[] data){
        if(data.length==303) {
            isScanQRSucceed = true;
            data=decryptFirst(data,_0819_encipher_key);
            _0819_tlv_0303=ByteUtil.subBytes(data,1,102);
            tgtgt_key=ByteUtil.subBytes(data,107,16);
            int length=(int)data[138];
            _0819_tlv_0006=ByteUtil.subBytes(data,141+length,120);
            preventLost(PackDatagram.pack_0825(false,1),1);
        }else if(data.length==327)
            showLoginToast("请关闭设备锁");
    }

    public static void unPack_0002(byte[] data){
        if(data.length==119){
            data=decryptFirst(data,APP.getSession_key());
            if(data[0]==115){
                byte[] _0x30=ByteUtil.subBytes(data,4,48);
                SendQQMessage.mListCachePrivate_0X30.add(new Private_0x30(SendQQMessage.mPrivate_gn,SendQQMessage.mPrivate_QQ,_0x30));
                //获取_0x20
                PCQQ.send(PackDatagram.pack_get_private_0x20(SendQQMessage.mPrivate_QQ));
            }
        }else if(data.length==31)
            QQAPI.log_("发送消息异常","发送群消息貌似被屏蔽",1);
        else if(data.length==47 && FragmentLog.print_success)
            QQAPI.log_("发送消息成功","群消息已发送√",2);
    }

    public static void unPack_00AE(byte[] data){
        data=decryptFirst(data,APP.getSession_key());
        if(Arrays.equals(ByteUtil.subBytes(data,0,3),new byte[]{1,66,0})){
            byte[] _0x20=ByteUtil.subBytes(data,6,data.length);
            byte[] _0x30=null;
            for (int i = 0; i< SendQQMessage.mListCachePrivate_0X30.size(); i++){
                Private_0x30 p= SendQQMessage.mListCachePrivate_0X30.get(i);
                if(p.gn==SendQQMessage.mPrivate_gn && p.QQ==SendQQMessage.mPrivate_QQ && p._0x30!=null){
                    _0x30=p._0x30;
                    break;
                }
            }
            if(_0x20!=null && _0x30!=null && SendQQMessage.mPrivate_msg!=null)
                SendQQMessage.sendPrivateMessage(_0x20,_0x30,SendQQMessage.mPrivate_QQ,SendQQMessage.mPrivate_msg);
        }
    }

    public static List<QQGroup> getGroups(){return mGroups;}
    public static String get_0388_img_ukey() {
        return _0388_img_ukey;
    }

    public static void set_0388_img_ukey(String ukey) {
        UnPackDatagram._0388_img_ukey = ukey;
    }
    public static boolean getIsRece_0388_img() {
        return isRece_0388_img;
    }

    public static void setIsRece_0388_img(boolean isRece_0388_img) {
        UnPackDatagram.isRece_0388_img = isRece_0388_img;
    }

    public static String get_0388_voice_ukey() {
        return _0388_voice_ukey;
    }

    public static void set_0388_voice_ukey(String ukey) {
        UnPackDatagram._0388_voice_ukey = ukey;
    }
    public static boolean getIsRece_0388_voice() {
        return isRece_0388_voice;
    }

    public static void setIsRece_0388_voice(boolean isRece_0388_voice) {
        UnPackDatagram.isRece_0388_voice = isRece_0388_voice;
    }

    public static String get_0352_ukey() {
        return _0352_ukey;
    }

    public static void set_0352_ukey(String ukey) {
        UnPackDatagram._0352_ukey = ukey;
    }
    public static boolean getIsRece_0352() {
        return isRece_0352;
    }

    public static void setIsRece_0352(boolean isRece_0352) {
        UnPackDatagram.isRece_0352 = isRece_0352;
    }
    public static byte[] get_0352_key(){return _0352_key;}
    public static void set_0352_key(byte[] key){_0352_key=key;}
    private static byte[] decryptFirst(byte[] data,byte[] key){
        return new Tea().decrypt(ByteUtil.subBytes(data,14,data.length-15),key);
    }

    public static Dialog getScanQR_dialog() {
        return scanQR_dialog;
    }

    public static byte[] get_0x38_token() {
        return _0x38_token;
    }

    public static void set_0x38_token(byte[] _0x38_token) {
        UnPackDatagram._0x38_token = _0x38_token;
    }

    public static byte[] get_0x88_token() {
        return _0x88_token;
    }

    public static void set_0x88_token(byte[] _0x88_token) {
        UnPackDatagram._0x88_token = _0x88_token;
    }

    public static byte[] get_0828_cipher_key() {
        return _0828_cipher_key;
    }

    public static void set_0828_cipher_key(byte[] _0828_cipher_key) {
        UnPackDatagram._0828_cipher_key = _0828_cipher_key;
    }

    public static byte[] get_0828_decipher_key() {
        return _0828_decipher_key;
    }

    public static void set_0828_decipher_key(byte[] _0828_decipher_key) {
        UnPackDatagram._0828_decipher_key = _0828_decipher_key;
    }
}
