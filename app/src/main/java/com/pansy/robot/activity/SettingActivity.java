package com.pansy.robot.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.service.AlarmHeartbeatService;
import com.pansy.robot.service.HeartbeatService;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.FileUtil;
import com.pansy.robot.utils.Gzip;
import com.pansy.robot.utils.PhoneUtil;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.view.SwitchButton;
import com.yy.runscore.Util.ToastUtil;

public class SettingActivity extends AppCompatActivity {
    private SwitchButton sw_prevent_offline,sw_export_log,sw_https_longMsg;
    private RelativeLayout rl_encrypt_plugin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);
        sw_prevent_offline=findViewById(R.id.sw_prevent_offline);
        sw_export_log=findViewById(R.id.sw_export_log);
        sw_https_longMsg=findViewById(R.id.sw_https_longMsg);
        rl_encrypt_plugin=findViewById(R.id.rl_encrypt_plugin);
        sw_prevent_offline.setCheck(SPHelper.readBool("prevent_offline",false));
        sw_export_log.setCheck(SPHelper.readBool("export_log",false));
        sw_https_longMsg.setCheck(SPHelper.readBool("https_longMsg",false));

        if(SPHelper.readBool("developer_mode",false))
            rl_encrypt_plugin.setVisibility(View.VISIBLE);
        else
            rl_encrypt_plugin.setVisibility(View.GONE);
        rl_encrypt_plugin.setOnClickListener((v)->{
            FileUtil.showFileChooser(this);
        });

        sw_prevent_offline.setOnChangedListener((v,checkState)-> {
            if(checkState){
                ImitateIosDialog dialog2=new ImitateIosDialog(SettingActivity.this);
                dialog2.setContent("开启防掉模式会增加耗电，确认开启吗？");
                dialog2.setOnConfirmListener(()-> {
                    SPHelper.writeBool("prevent_offline",true);
                    try{
                        stopService(new Intent(APP.getAppContext(),HeartbeatService.class));
                        if(Build.VERSION.SDK_INT>=26)
                            startForegroundService(new Intent(APP.getAppContext(),AlarmHeartbeatService.class));
                        else
                            startService(new Intent(APP.getAppContext(),AlarmHeartbeatService.class));
                        Toast.makeText(SettingActivity.this,"已开启防掉模式",Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                });
                dialog2.setOnCancelListener(()-> {
                    sw_prevent_offline.setCheck(false);
                    return null;
                });
                dialog2.show();

                if(Build.MANUFACTURER.toLowerCase().equals("xiaomi") && Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    ImitateIosDialog dialog=new ImitateIosDialog(SettingActivity.this);
                    dialog.setContent("MIUI系统关闭睡眠模式后效果更佳哦");
                    dialog.setConfirmText("现在关闭");
                    dialog.setCancelText("我已关闭");
                    dialog.setOnConfirmListener(()->{
                        try{
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ComponentName n = new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.ScenarioPowerSavingActivity");
                            intent.setComponent(n);
                            startActivity(intent);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return null;
                    });
                    dialog.show();
                }else if(Build.MANUFACTURER.toLowerCase().equals("vivo") && Build.VERSION.SDK_INT>=23){
                    ImitateIosDialog dialog=new ImitateIosDialog(SettingActivity.this);
                    dialog.setContent("Vivo系统开启后台高耗电后效果更佳哦");
                    dialog.setConfirmText("现在开启");
                    dialog.setCancelText("我已开启");
                    dialog.setOnConfirmListener(()->{
                        try{
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ComponentName n = new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity");
                            intent.setComponent(n);
                            startActivity(intent);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return null;
                    });
                    dialog.show();
                }else if((Build.MANUFACTURER.toLowerCase().equals("huawei") || Build.MANUFACTURER.toLowerCase().equals("honor")) && Build.VERSION.SDK_INT>=26){
                    ImitateIosDialog dialog=new ImitateIosDialog(SettingActivity.this);
                    dialog.setContent("EMUI系统开启休眠时保持网络连接后效果更佳哦");
                    dialog.setConfirmText("现在开启");
                    dialog.setCancelText("我已开启");
                    dialog.setOnConfirmListener(()->{
                        try{
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ComponentName n = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.power.ui.PowerSettingActivity");
                            intent.setComponent(n);
                            startActivity(intent);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return null;
                    });
                    dialog.show();
                }

                ImitateIosDialog dialog=new ImitateIosDialog(SettingActivity.this);
                dialog.setContent("开启自启动后效果更佳哦");
                dialog.setConfirmText("现在开启");
                dialog.setCancelText("我已开启");
                dialog.setOnConfirmListener(()->{
                    openAutoStart(SettingActivity.this);
                    return null;
                });
                dialog.show();

            }else{
                SPHelper.writeBool("prevent_offline",false);
                try{
                    stopService(new Intent(APP.getAppContext(),AlarmHeartbeatService.class));
                    startService(new Intent(APP.getAppContext(),HeartbeatService.class));
                }catch (Exception e){
                    e.printStackTrace();
                }
                Toast.makeText(SettingActivity.this,"已关闭防掉模式",Toast.LENGTH_LONG).show();
            }
        });

        sw_export_log.setOnChangedListener((v,checkState)-> {
            if(checkState){
                SPHelper.writeBool("export_log",true);
                if(!FileUtil.exists(APP.getPansyQQPath()+"log.txt"))
                    FileUtil.create(APP.getPansyQQPath()+"log.txt");
                ToastUtil.INSTANCE.toast(this,"已开启异常日志导出，日志保存在PansyQQ/log.txt");
            }else{
                SPHelper.writeBool("export_log",false);
                FileUtil.delete(APP.getPansyQQPath()+"log.txt");
                ToastUtil.INSTANCE.toast(this,"已关闭异常日志导出");
            }
        });

        sw_https_longMsg.setOnChangedListener((v,checkState)-> {
            if(checkState){
                ImitateIosDialog dialog=new ImitateIosDialog(SettingActivity.this);
                dialog.setCancelable(false);
                dialog.setContent("如果你的长消息经常被屏蔽，建议你开启此选项\n注意：暂时只支持纯文本，图片/艾特/face表情会自动忽略，且不能使用气泡");
                dialog.setOnConfirmListener(()->{
                    SPHelper.writeBool("https_longMsg",true);
                    ToastUtil.INSTANCE.toast(this,"已开启https长消息");
                    return null;
                });
                dialog.setOnCancelListener(()->{
                    sw_https_longMsg.setCheck(false);
                    return null;
                });
                dialog.show();
            }else{
                SPHelper.writeBool("https_longMsg",false);
                ToastUtil.INSTANCE.toast(this,"已关闭https长消息");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = FileUtil.getPath(this, uri);
                    if(path!=null && path.endsWith("apk")){
                        byte[] b=Gzip.pluginEncrypt(path);
                        String temp=path.replace(".apk",".cpk");
                        FileUtil.writeByteArray(temp,b);
                        ImitateIosDialog dialog=new ImitateIosDialog(this);
                        dialog.setTitle("加密成功");
                        dialog.setContent("加密插件路径为："+temp+"\n是否删除原插件");
                        dialog.setConfirmText("删除");
                        dialog.setConfirmColor(Color.RED);
                        dialog.setOnConfirmListener(()->{
                            FileUtil.delete(path);
                            ToastUtil.INSTANCE.toast(this,"删除成功");
                            return null;
                        });
                        dialog.show();
                    }else
                        ToastUtil.INSTANCE.toast(this,"请选择.apk的插件文件");
                }
                break;
        }
    }

    private void openAutoStart(Context context){
        try{
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String m=getManufacturer();
            ComponentName n=null;
            int api=Build.VERSION.SDK_INT;
            switch (m.toLowerCase()) {
                case "xiaomi":
                    n = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                    break;
                case "huawei":
                case "honor":
                    if(api>=28)
                        n = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");
                    else if(api>=26 && api<28)
                        n = ComponentName.unflattenFromString("com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity");
                    else if(api>=23 && api<26)
                        n = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");
                    else
                        n = ComponentName.unflattenFromString("com.huawei.systemmanager/.optimize.bootstart.BootStartActivity");
                    break;
                case "vivo":
                    if(api>=23)
                        n = ComponentName.unflattenFromString("com.vivo.permissionmanager/.activity.BgStartUpManagerActivity");
                    else
                        n = ComponentName.unflattenFromString("com.iqoo.secure/.ui.phoneoptimize.SoftwareManagerActivity");
                    break;
                case "oppo":
                    if(api>=23)
                        n = ComponentName.unflattenFromString("com.coloros.safecenter/.startupapp.StartupAppListActivity");
                    else if(api>=21 && api<23)
                        n = ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity");
                    else
                        n = ComponentName.unflattenFromString("com.color.safecenter/.permission.startup.StartupAppListActivity");
                    break;
                case "samsung":
                    n = ComponentName.unflattenFromString("com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity");
                    break;
                case "meizu":
                    n = ComponentName.unflattenFromString("com.meizu.safe/.permission.SmartBGActivity");
                    break;
            }
            intent.setComponent(n);
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getManufacturer(){
        return Build.MANUFACTURER;
    }
}
