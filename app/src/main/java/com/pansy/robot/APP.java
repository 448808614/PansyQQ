package com.pansy.robot;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.webkit.WebView;
import com.pansy.robot.crypter.ECC;
import com.pansy.robot.protocol.UDP;
import com.pansy.robot.struct.UnReadMessage;
import com.qihoo360.replugin.RePlugin;

import java.util.ArrayList;
import java.util.List;

public class APP extends Application {
    private static UDP udp;
    private static Context loginContext;
    private static Context mainTabContext;
    private static Context appContext;
    private static byte[] tlv_0105;
    private static long QQ;
    private static String pwd;
    private static ECC ecc;
    private static String ip;
    private static String client_key;
    private static byte[] session_key;
    private static String skey;
    private static String p_skey_qun;
    private static String p_skey_tenpay;
    private static byte[] httpconn_key;
    private static byte[] httpconn_token;
    private static String qzone_skey;
    private static String cookies;
    private static String gtk;
    private static final String myService="129.204.222.149";
    private static boolean canRobEnvelope=false;
    private static boolean isRelogin=false;
    private static long expire;
    private static int login_way;//1密码登录，2扫码登录，3重登
    private static String user_agent;
    private static boolean authorization;
    private static int noReceiveHeartbeat=0;
    private static List<UnReadMessage> urmList;
    private static boolean relogin_outside=false;//是否从登录界面重登

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        RePlugin.App.attachBaseContext(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RePlugin.App.onCreate();
        appContext=getApplicationContext();
        user_agent = new WebView(this).getSettings().getUserAgentString();
        if(user_agent==null || user_agent.equals(""))
            user_agent="Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1)";
    }
    public static UDP getUdp(){
        return udp;
    }
    public static void setLoginContext(Context ctx) {
        loginContext = ctx;
    }
    public static Context getLoginContext(){ return loginContext; }
    public static Context getAppContext(){
        return appContext;
    }
    public static void setQQ(long qq){
        QQ=qq;
    }
    public static void setTlv_0105(byte[] tlv_0105){
        APP.tlv_0105=tlv_0105;
    }
    public static void setUdp(UDP udp){ APP.udp=udp; }
    public static byte[] getTlv_0105() { return tlv_0105; }
    public static long getQQ() { return QQ; }
    public static ECC getEcc() { return ecc; }
    public static void setEcc(ECC ecc) { APP.ecc=ecc; }
    public static void setIp(String ip){
        APP.ip=ip;
    }
    public static String getIp(){
        return ip;
    }
    public static void setPwd(String pwd){APP.pwd=pwd;}
    public static String getPwd(){return pwd;}
    public static void setClient_key(String client_key){APP.client_key=client_key;}
    public static String getClient_key(){return client_key;}
    public static void setSession_key(byte[] session_key){APP.session_key=session_key;}
    public static byte[] getSession_key(){return session_key;}
    public static Context getMainTabContext(){return mainTabContext;}
    public static void setMainTabContext(Context context){mainTabContext=context;}
    public static String getSkey() {
        return skey;
    }
    public static void setSkey(String skey) {
        APP.skey = skey;
    }
    public static String getP_skey_tenpay() {
        return p_skey_tenpay;
    }
    public static void setP_skey_tenpay(String p_skey_tenpay) {
        APP.p_skey_tenpay = p_skey_tenpay;
    }
    public static String getP_skey_qun() {
        return p_skey_qun;
    }
    public static void setP_skey_qun(String p_skey_qun) {
        APP.p_skey_qun = p_skey_qun;
    }
    public static String getCookies() {
        return cookies;
    }
    public static void setCookies(String cookies) {
        APP.cookies = cookies;
    }
    public static String getGtk() {
        return gtk;
    }
    public static void setGtk(String gtk) {
        APP.gtk = gtk;
    }
    public static String getMyService(){return myService;}
    public static boolean isCanRobEnvelope() {
        return canRobEnvelope;
    }
    public static void setCanRobEnvelope(boolean canRobEnvelope) {
        APP.canRobEnvelope = canRobEnvelope;
    }
    public static boolean getIsRelogin() {
        return isRelogin;
    }
    public static void setIsRelogin(boolean isRelogin) {
        APP.isRelogin = isRelogin;
    }
    public static String getVersion() {
        PackageManager manager = appContext.getPackageManager();
        String version = null;
        try {
            PackageInfo info = manager.getPackageInfo(appContext.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
    public static long getExpire() {
        return expire;
    }

    public static void setExpire(long expire) {
        APP.expire = expire;
    }
    public static int getLogin_way() {
        return login_way;
    }

    public static void setLogin_way(int login_way) {
        APP.login_way = login_way;
    }

    public static boolean isAuthorization() {
        return authorization;
    }

    public static void setAuthorization(boolean authorization) {
        APP.authorization = authorization;
    }

    public static String getUser_agent() {
        return user_agent;
    }

    public static void setUser_agent(String user_agent) {
        APP.user_agent = user_agent;
    }

    public static void setNoReceiveHeartbeat(int noReceiveHeartbeat) {
        APP.noReceiveHeartbeat = noReceiveHeartbeat;
    }
    public static int getNoReceiveHeartbeat() {
        return noReceiveHeartbeat;
    }

    public static void addUrmList(int type,Long QQ){
        if(urmList==null) urmList=new ArrayList<>();
        UnReadMessage urm=getUrm(type,QQ);
        if(urm!=null){
            urm.setNum(urm.getNum()+1);
        }else{
            urm=new UnReadMessage(type,QQ,1);
            urmList.add(urm);
        }
    }

    public static UnReadMessage getUrm(int type,Long QQ){
        if(urmList!=null){
            for (int i=0;i<APP.urmList.size();i++){
                UnReadMessage urm=APP.urmList.get(i);
                if(type==0 && urm.getType()==0 && QQ==urm.getQQ())
                    return urm;
            }
        }
        return null;
    }

    public static void removeUrm(int type,Long QQ){
        if(urmList!=null){
            for (int i=0;i<APP.urmList.size();i++){
                UnReadMessage urm=APP.urmList.get(i);
                if(type==0 && urm.getType()==0 && QQ==urm.getQQ()){
                    urmList.remove(urm);
                    break;
                }
            }
        }
    }
    public static void clearUrmList(){
        if(urmList!=null)
            urmList.clear();
    }

    public static String getPansyQQPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/PansyQQ/";
    }

    public static boolean isRelogin_outside() {
        return relogin_outside;
    }

    public static void setRelogin_outside(boolean relogin_outside) {
        APP.relogin_outside = relogin_outside;
    }

    public static byte[] getHttpconn_key() {
        return httpconn_key;
    }

    public static void setHttpconn_key(byte[] httpconn_key) {
        APP.httpconn_key = httpconn_key;
    }

    public static byte[] getHttpconn_token() {
        return httpconn_token;
    }

    public static void setHttpconn_token(byte[] httpconn_token) {
        APP.httpconn_token = httpconn_token;
    }

    public static String getQzone_skey() {
        return qzone_skey;
    }

    public static void setQzone_skey(String qzone_skey) {
        APP.qzone_skey = qzone_skey;
    }
}
