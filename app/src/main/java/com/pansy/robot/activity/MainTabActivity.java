package com.pansy.robot.activity;

import com.pansy.robot.APP;
import com.pansy.robot.BuildConfig;
import com.pansy.robot.R;
import com.pansy.robot.crypter.JNI_RSA;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.fragments.FragmentLog;
import com.pansy.robot.protocol.Analy;
import com.pansy.robot.protocol.SendQQMessage;
import com.pansy.robot.receiver.LoadPluginReceiver;
import com.pansy.robot.service.AlarmHeartbeatService;
import com.pansy.robot.service.HeartbeatService;
import com.pansy.robot.service.ReceiveService;
import com.pansy.robot.service.SendService;
import com.pansy.robot.struct.Log;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.pansy.robot.fragments.FragmentContact;
import com.pansy.robot.fragments.FragmentMessage;
import com.pansy.robot.fragments.FragmentPlugin;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.struct.Plugin;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.FileUtil;
import com.pansy.robot.utils.Gzip;
import com.pansy.robot.utils.HttpRequest;
import com.pansy.robot.utils.PhoneUtil;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.utils.SoundUtil;
import com.pansy.robot.view.CircleImageView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.yy.runscore.Util.ToastUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import project.pyp9536.wanxiang.util.ScreenUtil;

public class MainTabActivity extends FragmentActivity{
    private FragmentTabHost mTabHost;
    private LayoutInflater layoutInflater;
    private Class fragmentArray[]={FragmentMessage.class,FragmentContact.class,FragmentPlugin.class,FragmentLog.class};
    private int mImageViewArray[]={R.drawable.tab_message_btn,R.drawable.tab_contact_btn,R.drawable.tab_plugin_btn,R.drawable.tab_log_btn};
    private String mTextViewArray[]={"消息","联系人","插件","日志"};
    private LinearLayout linear_main,linear_exit,linear_about,linear_relogin,linear_bubble,linear_whatNew,linear_setting,linear_support;
    private CircleImageView img_head;
    private TextView txt_nick,txt_expire,tv_current_device;
    private Button btn_recharge;
    private static DrawerLayout drawerLayout;
    private static ExecutorService threadPool=Executors.newSingleThreadExecutor();
    private static List<PluginInfo> listInfo;
    private static List<Log> listLog=new ArrayList<>();
    private long mExitTime;
    private static LoadPluginReceiver loadPluginReceiver;
    private static long lastClickTime=0;
    private static long clickCount=0;
    public static Bitmap myHeadBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
        initView();
        APP.setMainTabContext(this);
        linear_main=findViewById(R.id.linear_main);
        linear_exit=findViewById(R.id.linear_exit);
        linear_about=findViewById(R.id.linear_about);
        linear_relogin=findViewById(R.id.linear_relogin);
        linear_bubble=findViewById(R.id.linear_bubble);
        linear_whatNew=findViewById(R.id.linear_whatNew);
        linear_setting=findViewById(R.id.linear_setting);
        linear_support=findViewById(R.id.linear_support);
        img_head=findViewById(R.id.img_drawer_head);
        txt_nick=findViewById(R.id.txt_drawer_nick);
        txt_expire=findViewById(R.id.txt_expire);
        btn_recharge=findViewById(R.id.btn_recharge);
        tv_current_device=findViewById(R.id.tv_current_device);
        tv_current_device.setText("当前设备："+Build.BRAND+" "+Build.MODEL);

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        txt_expire.setText(sdf.format(new Date(APP.getExpire()*1000)));
        txt_expire.setOnClickListener((v)->{
            recharge(v.getContext());
        });
        btn_recharge.setOnClickListener((v)->{
            recharge(v.getContext());
        });
        img_head.setOnClickListener((v)->{
            if(System.currentTimeMillis()-lastClickTime<=2000 && clickCount<5){
                if(clickCount>0 && clickCount<4){
                    if(SPHelper.readBool("developer_mode",false)){
                        clickCount=0;
                        Toast.makeText(MainTabActivity.this,"你已进入开发者模式",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainTabActivity.this,"再点击"+(5-clickCount-1)+"次进入开发者模式",Toast.LENGTH_SHORT).show();
                }
                clickCount++;
                lastClickTime=System.currentTimeMillis();
            }else{
                lastClickTime=System.currentTimeMillis();
                clickCount=1;
            }

            if(clickCount>=5){
                SPHelper.writeBool("developer_mode",true);
                Toast.makeText(MainTabActivity.this,"你已进入开发者模式",Toast.LENGTH_SHORT).show();
            }
        });

        if(APP.getQQ()==0){//如果手动清理了后台
            ToastUtil.INSTANCE.toast(this,"请重新登录");
            startActivity(new Intent(this,LoginActivity.class));
            finish();
            return;
        }
        linear_exit.setOnClickListener((v)-> {
            showExitDialog();
        });
        linear_about.setOnClickListener((v)-> {
            showAboutDialoig();
        });
        linear_setting.setOnClickListener((v)-> {
            startActivity(new Intent(MainTabActivity.this,SettingActivity.class));
        });
        linear_bubble.setOnClickListener((v)-> {
            final ImitateIosDialog dialog=new ImitateIosDialog(v.getContext());
            dialog.setTitle("请输入气泡id");
            dialog.setContent("支持数千个限定和绝版气泡");
            dialog.needInput(false);
            dialog.inputOnlyNumber();
            dialog.setInputString(SPHelper.readInt("bubble",0)+"");
            dialog.setOnConfirmListener(()-> {
                if(!TextUtils.isEmpty(dialog.getMInputString())){
                    SPHelper.writeInt("bubble",Integer.valueOf(dialog.getMInputString()));
                    Toast.makeText(MainTabActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                    SendQQMessage.bubbleId = SPHelper.readInt("bubble",0);
                }
                return null;
            });
            dialog.show();
        });
        linear_relogin.setOnClickListener((v)->PCQQ.relogin());
        linear_whatNew.setOnClickListener((v)-> {
            ImitateIosDialog dialog=new ImitateIosDialog(MainTabActivity.this);
            dialog.setTitle("更新内容");
            dialog.setContent(getUpdateContent());
            dialog.hideCancel();
            dialog.show();
        });

        ignoreBatteryOptimization();

        drawerLayout=findViewById(R.id.drawer_layout);
        if(!BuildConfig.DEBUG)
            preventCrash();
        //加载侧滑栏的个人信息
        if(APP.getSession_key()!=null){
            loadHead();
            loadNick();
        }
        //读取日志设置
        readLogSetting();
        //注册插件信息返回广播
        loadPluginReceiver=new LoadPluginReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.pansyqq.receive.loadplugin");
        intentFilter.addAction("com.pansyqq.receive.loadplugin.disconnect");
        registerReceiver(loadPluginReceiver,intentFilter);
        //获取公告
        getNotice();
        loadBg();
        checkExipire();
        //加载插件
        if (listInfo==null)
            loadPlugins(true);
    }

    private void checkExipire(){
        new Thread(()->{
            try{
                while (true){
                    Thread.sleep(1000*600);
                    if(System.currentTimeMillis()/1000>APP.getExpire()){
                        try{
                            APP.setQQ(0);
                            PCQQ.logout();
                            Thread.sleep(1000);
                            stopService(new Intent(MainTabActivity.this,SendService.class));
                            stopService(new Intent(MainTabActivity.this,ReceiveService.class));
                            if(SPHelper.readBool("prevent_offline",false)){
                                stopService(new Intent(MainTabActivity.this,AlarmHeartbeatService.class));
                                AlarmHeartbeatService.cancelAlarmManager();
                            }
                            stopService(new Intent(MainTabActivity.this,HeartbeatService.class));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        runOnUiThread(()->{
                            ImitateIosDialog dialog=new ImitateIosDialog(MainTabActivity.this);
                            dialog.setContent("你的QQ已到期，请进行续费");
                            dialog.setContentColor(Color.RED);
                            dialog.hideCancel();
                            dialog.setConfirmText("退出");
                            dialog.setOnConfirmListener(()->{
                                System.exit(-1);
                                return null;
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        });
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void recharge(Context context){
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
                    json2.put("QQ",APP.getQQ());
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
                        APP.setExpire(expire2);
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        txt_expire.setText(sdf.format(new Date(APP.getExpire()*1000)));
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

    private void loadBg(){
        try{
            FileInputStream inputStream=new FileInputStream(APP.getPansyQQPath()+"bg_main.jpg");
            byte bytes[]=ByteUtil.inputStream2ByteArray(inputStream);
            Drawable drawable=ByteUtil.byteArray2Drawable(bytes);
            linear_main.setBackground(drawable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getNotice(){
        if(System.currentTimeMillis()-SPHelper.readLong("showNoticeTime",0)>24*3600*1000){
            new Thread(()->{
                String str=HttpRequest.get("http://"+APP.getMyService()+"/Pansy/notice.txt");
                if(str.length()>2){
                    SPHelper.writeLong("showNoticeTime",System.currentTimeMillis());
                    String finalStr = str;
                    new Handler(getMainLooper()).post(()->{
                        ImitateIosDialog dialog=new ImitateIosDialog(MainTabActivity.this);
                        dialog.setCancelable(false);
                        if(finalStr.startsWith("\uFEFF[")){
                            int index=finalStr.indexOf("]");
                            String title=finalStr.substring(2,index);
                            String content=finalStr.substring(index+1);
                            dialog.setTitle(title);
                            dialog.setContent(content);
                        }else{
                            dialog.setTitle("公告");
                            dialog.setContent(finalStr);
                        }
                        dialog.setCountdown(3);
                        dialog.hideCancel();
                        dialog.show();
                    });
                }
            }).start();
        }
    }

    private void ignoreBatteryOptimization() {
        if(Build.VERSION.SDK_INT>=23) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(this.getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        }
    }
    private String getUpdateContent(){
        Future<String> future=threadPool.submit(()-> {
            String str=HttpRequest.get("http://"+APP.getMyService() +"/Pansy/update.txt");
            return str;
        });
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    private void readLogSetting(){
        FragmentLog.print_plugin=SPHelper.readBool("log","插件",false);
        FragmentLog.print_heartbeat=SPHelper.readBool("log","心跳",false);
        FragmentLog.print_success=SPHelper.readBool("log","发送成功",false);
    }
    /**
     * 防止应用崩溃
     */
    private void preventCrash(){
        new Handler(Looper.getMainLooper()).post(()->{
            //主线程异常拦截
            while (true) {
                try {
                    Looper.loop();//主线程的异常会从这里抛出
                } catch (Throwable e) {
                    String str=getStackTrace(e);
                    if(SPHelper.readBool("export_log",false))
                        FileUtil.append(APP.getPansyQQPath()+"log.txt","\n"+ByteUtil.getTimeDay()+str);
                    Toast.makeText(MainTabActivity.this,"主线程捕获到了一个异常，详细请查看日志",Toast.LENGTH_LONG).show();
                    QQAPI.log_("主线程异常",str,1);
                }
            }
        });

        //所有线程异常拦截，由于主线程的异常都被我们catch住了，所以下面的代码拦截到的都是子线程的异常
        Thread.setDefaultUncaughtExceptionHandler((t,e)-> {
            new Handler(Looper.getMainLooper()).post(()->{
                String str=getStackTrace(e);
                if(SPHelper.readBool("export_log",false))
                    FileUtil.append(APP.getPansyQQPath()+"log.txt","\n"+ByteUtil.getTimeDay()+str);
                Toast.makeText(MainTabActivity.this,"子线程捕获到了一个异常，详细请查看日志",Toast.LENGTH_LONG).show();
                QQAPI.log_("子线程异常",str,1);
            });
        });
    }
    private static String getStackTrace(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            t.printStackTrace(pw);
            return sw.toString();
        }
        finally {
            pw.close();
        }
    }

    private void loadHead(){
        new Thread(()->{
             myHeadBitmap=QQAPI.getHead(APP.getQQ());
            if(myHeadBitmap!=null){
                new Handler(Looper.getMainLooper()).post(()-> {
                    img_head.setImageBitmap(myHeadBitmap);
                });
            }
            String url="http://"+APP.getMyService()+"/Pansy/redpack.txt";
            String str=HttpRequest.get(url);
            if(str.equals("1"))
                APP.setCanRobEnvelope(true);
            else
                APP.setCanRobEnvelope(false);
        }).start();

    }
    private void loadNick(){
        new Thread(()-> {
            final String nick=QQAPI.getNick(APP.getQQ());
            new Handler(Looper.getMainLooper()).post(()->{
                txt_nick.setText(nick);
            });
        }).start();
    }
    public static DrawerLayout getDrawerLayout(){return drawerLayout;}
    public static void loadPlugins(boolean is_wait){
        threadPool.execute(()-> {
            try{
                if(is_wait) Thread.currentThread().sleep(1000);
                File file = new File(APP.getPansyQQPath());
                if (!file.exists())
                    file.mkdir();
                //插件数据的存放目录
                File file_data = new File(APP.getPansyQQPath()+"data/");
                if (file_data.exists() == false)
                    file_data.mkdir();
                //插件的存放目录
                file = new File(APP.getPansyQQPath()+"plugin/");
                if (file.exists() == false) {
                    file.mkdir();
                }else {
                    File[] files = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        String filePath = files[i].getAbsolutePath();
                        if (filePath.endsWith(".apk")){
                            RePlugin.install(filePath);
                        }else if(filePath.endsWith(".cpk")){
                            byte[] b=Gzip.pluginDecrypt(filePath);
                            String temp=filePath.replace(".cpk",".apk");
                            FileUtil.writeByteArray(temp,b);
                            RePlugin.install(temp);
                            FileUtil.delete(temp);
                            FileUtil.delete(filePath);
                        }
                    }
                }
                listInfo=new ArrayList<>();
                //保留aidl版插件
                if(loadPluginReceiver.listPlugin==null)
                    loadPluginReceiver.listPlugin=new ArrayList<>();
                else{
                    List<Integer> dels=new ArrayList<>();
                    for (int i=0;i<loadPluginReceiver.listPlugin.size();i++){
                        Plugin plugin=loadPluginReceiver.listPlugin.get(i);
                        if(!plugin.is_aidl)
                            dels.add(i);
                    }
                    for (int i=0;i<dels.size();i++)
                        loadPluginReceiver.listPlugin.remove(dels.get(i).intValue()-i);
                }
                loadPluginReceiver.listPlugin_replugin=new ArrayList<>();
                listInfo=RePlugin.getPluginInfoList();
                if(listInfo.size()>0)
                    new Handler(Looper.getMainLooper()).post(()->{
                        ToastUtil.INSTANCE.toast(APP.getMainTabContext(),"正在加载插件...");
                    });
                //360的坑，上一次未卸载成功，所以插件列表中还存在，但无法加载
                //第一次加载
                for(int i=0;i<listInfo.size();i++){
                    //初始化插件比较耗时
                    try{
                        PluginInfo plugin=listInfo.get(i);
                        Intent intent=RePlugin.createIntent(plugin.getPackageName(),plugin.getPackageName()+".event.PluginInitActivity");
                        RePlugin.startActivity(APP.getMainTabContext(),intent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                try{
                    Thread.currentThread().sleep(500);
                }catch (Exception e){
                    e.printStackTrace();
                }
                //部分插件没加载出来
                List<Plugin> listPluginSuccess=loadPluginReceiver.listPlugin_replugin;
                int c=0;
                int d=0;
                while(listPluginSuccess.size()<listInfo.size()){
                    //找出加载失败的插件
                    if(c>=24) break;
                    List<PluginInfo> listPluginFailed=new ArrayList<>();
                    for(int i=0;i<listInfo.size();i++){
                        int j=0;
                        for(;j<listPluginSuccess.size();j++){
                            if(listInfo.get(i).getPackageName().equals(listPluginSuccess.get(j).packageName)){
                                j=0;
                                break;
                            }
                        }
                        //没找到则为加载失败的插件
                        if(j==listPluginSuccess.size())
                            listPluginFailed.add(listInfo.get(i));
                    }
                    System.out.println("共有"+listPluginFailed.size()+"个插件加载失败，开始重新加载");
                    if(listPluginFailed.size()==1)
                        d++;
                    if(d==3)//3次都只有一个插件加载失败，则放弃加载这个插件
                        break;
                    //重新加载
                    for(int i=0;i<listPluginFailed.size();i++){
                        try{
                            PluginInfo plugin=listPluginFailed.get(i);
                            Intent intent=RePlugin.createIntent(plugin.getPackageName(),plugin.getPackageName()+".event.PluginInitActivity");
                            RePlugin.startActivity(APP.getMainTabContext(),intent);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    listPluginSuccess=loadPluginReceiver.listPlugin_replugin;
                    c++;
                }
                if(loadPluginReceiver.listPlugin!=null && loadPluginReceiver.listPlugin.size()>0){
                    new Handler(Looper.getMainLooper()).post(()->{
                        try {
                            ToastUtil.INSTANCE.toast(APP.getMainTabContext(),"共加载"+ loadPluginReceiver.listPlugin.size()+"个插件");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    public static List<PluginInfo> getListInfo(){return listInfo;}
    public static List<Log> getListLog(){return listLog;}

    private String getNick(String str,String QQ){
        try{
            str=str.substring(17,str.length()-2);
            JsonParser parser=new JsonParser();
            JsonObject object=(JsonObject) parser.parse(str);
            return object.get(QQ).getAsJsonArray().get(6).getAsString();
        }catch (Exception e){
            return "";
        }

    }
    private void showAboutDialoig(){
        ImitateIosDialog dialog=new ImitateIosDialog(MainTabActivity.this);
        dialog.setTitle("关于");
        String content="PansyQQ是一款基于PC协议的手机QQ机器人\n当前版本：V"+APP.getVersion()+"\n交流群：875867582"+"\n作者QQ：2013146005";
        dialog.setContent(content);
        dialog.hideCancel();
        dialog.show();
    }
    private void showExitDialog(){
        ImitateIosDialog dialog=new ImitateIosDialog(MainTabActivity.this);
        dialog.setContent("你确定要退出吗？");
        dialog.setOnConfirmListener(()-> {
            try{
                APP.setQQ(0);
                PCQQ.logout();
                Thread.sleep(1000);
                stopService(new Intent(MainTabActivity.this,SendService.class));
                stopService(new Intent(MainTabActivity.this,ReceiveService.class));
                if(SPHelper.readBool("prevent_offline",false)){
                    stopService(new Intent(MainTabActivity.this,AlarmHeartbeatService.class));
                    AlarmHeartbeatService.cancelAlarmManager();
                }
                stopService(new Intent(MainTabActivity.this,HeartbeatService.class));
            }catch (Exception e){
                e.printStackTrace();
            }
            System.exit(0);
            return null;
        });
        dialog.show();
    }
    private void initView(){
        layoutInflater=LayoutInflater.from(this);
        mTabHost=findViewById(android.R.id.tabhost);
        mTabHost.setup(this,getSupportFragmentManager(),R.id.realtabcontent);
        mTabHost.getTabWidget().setDividerDrawable(null);
        for(int i=0;i<fragmentArray.length;i++){
            TabHost.TabSpec tabSpec=mTabHost.newTabSpec(mTextViewArray[i]).setIndicator(getTabItemView(i));
            mTabHost.addTab(tabSpec,fragmentArray[i],null);
        }
    }
    private View getTabItemView(int index){
        View view=layoutInflater.inflate(R.layout.tab_item_view,null);
        ImageView imageView=view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);
        TextView textView=view.findViewById(R.id.textview);
        textView.setText(mTextViewArray[index]);
        return view;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit(){
        if(System.currentTimeMillis()-mExitTime>2000){
            Toast.makeText(MainTabActivity.this, "再按一次退出PansyQQ", Toast.LENGTH_LONG).show();
            mExitTime=System.currentTimeMillis();
        }else{
            PCQQ.logout();
            onDestroy();
            System.exit(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(APP.getNoReceiveHeartbeat()>=3)
            //PCQQ.relogin();
    }

    @Override
    protected void onDestroy() {
        try{
            APP.setQQ(0);
            unregisterReceiver(loadPluginReceiver);
            stopService(new Intent(MainTabActivity.this,SendService.class));
            stopService(new Intent(MainTabActivity.this,ReceiveService.class));
            if(SPHelper.readBool("prevent_offline",false)){
                stopService(new Intent(MainTabActivity.this,AlarmHeartbeatService.class));
                AlarmHeartbeatService.cancelAlarmManager();
            }
            stopService(new Intent(MainTabActivity.this,HeartbeatService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}



