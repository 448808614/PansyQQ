package com.pansy.robot.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.Replaceable;

import com.pansy.robot.APP;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.crypter.JNI_RSA;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.fragments.FragmentPluginLocal;
import com.pansy.robot.struct.Plugin;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.HttpRequest;
import com.pansy.robot.utils.SPHelper;
import com.qihoo360.replugin.RePlugin;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class LoadPluginReceiver extends BroadcastReceiver {
    public static volatile List<Plugin> listPlugin;//所有插件
    public static volatile List<Plugin> listPlugin_replugin;//replugin版插件
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction()=="com.pansyqq.receive.loadplugin"){
            if(listPlugin==null)
                listPlugin=new ArrayList<>();
            if(listPlugin_replugin==null)
                listPlugin_replugin=new ArrayList<>();
            String plugin_packageName=intent.getStringExtra("plugin_packageName");
            System.out.println("插件返回:"+plugin_packageName);
            //判断是否为重复插件
            for(int i=0;i<listPlugin.size();i++)
                if(listPlugin.get(i).packageName.equalsIgnoreCase(plugin_packageName))
                    return;
            try{
                String plugin_name=intent.getStringExtra("plugin_name");
                String plugin_brief=intent.getStringExtra("plugin_brief");
                String plugin_author=intent.getStringExtra("plugin_author");
                String plugin_version=intent.getStringExtra("plugin_version");
                boolean is_aidl=intent.getBooleanExtra("is_aidl",false);
                byte[] bytes=intent.getByteArrayExtra("plugin_icon");
                String server_info=HttpRequest.postSync("http://"+APP.getMyService()+"/Pansy/develope/get_plugin_info.php","packageName="+plugin_packageName);
                String server_version="",server_forceUpdate="";
                try{
                    JSONObject json=new JSONObject(server_info);
                    server_version=json.getString("version");
                    server_forceUpdate=json.getString("forceUpdate");
                }catch (Exception e){
                    e.printStackTrace();
                }
                boolean hasNewVersion=false;
                boolean forceUpdate=false;
                //验证插件是否有新版本
                if(!server_version.equals("") && !server_version.equals(plugin_version)){
                    hasNewVersion=true;
                    if(server_forceUpdate.equals("1"))
                        forceUpdate=true;
                }
                Bitmap plugin_icon=null;
                if(bytes!=null)
                    plugin_icon= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                boolean enable=SPHelper.readBool("plugin",plugin_packageName,false);
                if(forceUpdate){
                    enable=false;
                    SPHelper.writeBool("plugin",plugin_packageName,false);
                }
                if(!plugin_packageName.equals("") && enable){
                    Intent intent2=new Intent();
                    intent2.setComponent(new ComponentName(plugin_packageName,plugin_packageName+".event.PluginEnableActivity"));
                    RePlugin.startActivity(APP.getMainTabContext(),intent2);
                }
                if(is_aidl) enable=true;
                Plugin plugin=new Plugin(null,plugin_packageName,plugin_name,plugin_brief,plugin_author,plugin_version,plugin_icon,enable);
                plugin.hasNewVersion=hasNewVersion;
                plugin.forceUpdate=forceUpdate;
                plugin.is_aidl=is_aidl;
                //只有非aidl插件才添加，防止加载卡死
                if(!plugin_packageName.equals(""))
                    listPlugin.add(plugin);
                if(!plugin.is_aidl)
                    listPlugin_replugin.add(plugin);
                FragmentPluginLocal.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(intent.getAction()=="com.pansyqq.receive.loadplugin.disconnect"){
            //aidl插件断开连接，把它从插件列表中删除
            String plugin_packageName=intent.getStringExtra("plugin_packageName");
            if(listPlugin==null)
                listPlugin=new ArrayList<>();
            if(listPlugin_replugin==null)
                listPlugin_replugin=new ArrayList<>();
            for(int i=0;i<listPlugin.size();i++){
                if(listPlugin.get(i).packageName.equals(plugin_packageName)){
                    listPlugin.remove(i);
                    FragmentPluginLocal.notifyDataSetChanged();
                    break;
                }
            }
        }

    }





}
