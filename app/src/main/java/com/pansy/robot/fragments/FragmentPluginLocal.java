package com.pansy.robot.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.R;
import com.pansy.robot.adapter.PluginLocalAdapter;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.receiver.LoadPluginReceiver;
import com.pansy.robot.struct.Plugin;
import com.pansy.robot.utils.SPHelper;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.yy.runscore.Util.ToastUtil;

import java.io.File;
import java.util.List;

public class FragmentPluginLocal extends Fragment {
    public static SwipeMenuListView listView;
    private static ImageButton btn_refresh;
    private static Context mContext;
    private static PluginLocalAdapter adapter;
    private View rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView==null){
            View view=inflater.inflate(R.layout.tab_plugin_local,null);
            listView=view.findViewById(R.id.lv_plugin_local);
            btn_refresh=view.findViewById(R.id.btn_plugin_local_refresh);
            mContext=this.getContext();
            btn_refresh.setOnClickListener((v)-> {
                adapter=null;
                MainTabActivity.loadPlugins(false);
            });
            rootView=view;
            return view;
        }else
            return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(adapter!=null)
            listView.setAdapter(adapter);
        else{
            if(LoadPluginReceiver.listPlugin!=null) {
                adapter = new PluginLocalAdapter(this.getContext(), LoadPluginReceiver.listPlugin);
                listView.setAdapter(adapter);
            }
         }
         createSwipMenu();
    }
    public static void notifyDataSetChanged(){
        if(adapter==null){
            if(listView!=null){
                adapter=new PluginLocalAdapter(FragmentPluginLocal.mContext,LoadPluginReceiver.listPlugin);
                listView.setAdapter(adapter);
            }
        }else
            adapter.notifyDataSetChanged();
    }
    private static void createSwipMenu(){
        final SwipeMenuCreator creator=(menu)-> {
                SwipeMenuItem item_enable = new SwipeMenuItem(
                        mContext);
                item_enable.setBackground(new ColorDrawable(Color.rgb(0x37, 0xC3,
                        0xA4)));
                item_enable.setWidth(dp2px(72));
                item_enable.setIcon(R.drawable.enable);
                menu.addMenuItem(item_enable);

                SwipeMenuItem item_diable=new SwipeMenuItem(mContext);

                item_diable.setBackground(new ColorDrawable(Color.rgb(0xC7, 0xC7,
                        0xCD)));
                // set item width
                item_diable.setWidth(dp2px(72));
                item_diable.setIcon(R.drawable.disable);
                menu.addMenuItem(item_diable);

                SwipeMenuItem item_set=new SwipeMenuItem(mContext);
                item_set.setBackground(new ColorDrawable(Color.rgb(0xFF, 0x9D,
                        0x00)));
                // set item width
                item_set.setWidth(dp2px(72));
                item_set.setIcon(R.drawable.set_small);
                menu.addMenuItem(item_set);

                SwipeMenuItem item_delete=new SwipeMenuItem(mContext);
                item_delete.setBackground(new ColorDrawable(Color.rgb(0xFF, 0x3A,
                        0x30)));
                item_delete.setWidth(dp2px(72));
                item_delete.setIcon(R.drawable.delete);
                menu.addMenuItem(item_delete);

        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener((position,menu,index)-> {
                try{
                    Plugin plugin;
                    Intent intent;
                    switch(index){
                        case 0:
                            plugin=LoadPluginReceiver.listPlugin.get(position);
                            if(plugin.is_aidl){
                                ToastUtil.INSTANCE.toast(mContext,"aidl插件安装即可用，无需启用");
                                break;
                            }
                            if(plugin.forceUpdate)
                                Toast.makeText(mContext,"插件作者开启了强制更新，请先更新插件。记得删除旧版插件后再更新哦",Toast.LENGTH_LONG).show();//。更新方法：卸载旧版插件后再下载新版插件
                            else{
                                adapter.setPluginEnable(position,true);
                                adapter.notifyDataSetInvalidated();
                                SPHelper.writeBool("plugin",plugin.packageName,true);
                                intent=new Intent();
                                intent.setComponent(new ComponentName(plugin.packageName,plugin.packageName+".event.PluginEnableActivity"));
                                RePlugin.startActivity(mContext,intent);
                                Toast.makeText(mContext,"插件已启用",Toast.LENGTH_LONG).show();
                            }
                            break;
                        case 1:
                            plugin=LoadPluginReceiver.listPlugin.get(position);
                            if(plugin.is_aidl){
                                ToastUtil.INSTANCE.toast(mContext,"aidl插件退出即无效，无需禁用");
                                break;
                            }
                            Toast.makeText(mContext,"插件已禁用",Toast.LENGTH_LONG).show();
                            adapter.setPluginEnable(position,false);
                            adapter.notifyDataSetInvalidated();
                            SPHelper.writeBool("plugin",LoadPluginReceiver.listPlugin.get(position).packageName,false);
                            intent=new Intent();
                            intent.setComponent(new ComponentName(plugin.packageName,plugin.packageName+".event.PluginDisableActivity"));
                            RePlugin.startActivity(mContext,intent);
                            break;
                        case 2:
                            plugin=LoadPluginReceiver.listPlugin.get(position);
                            if(plugin.is_aidl){
                                ToastUtil.INSTANCE.toast(mContext,"aidl插件请直接打开安装的插件进行设置");
                                break;
                            }
                            intent=new Intent();
                            intent.setComponent(new ComponentName(plugin.packageName,plugin.packageName+".event.PluginSetActivity"));
                            RePlugin.startActivity(mContext,intent);
                            break;
                        case 3:
                            plugin=LoadPluginReceiver.listPlugin.get(position);
                            if(plugin.is_aidl){
                                ToastUtil.INSTANCE.toast(mContext,"aidl插件请直接从手机中卸载");
                                break;
                            }
                            ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                            dialog.setContent("你确定要删除该插件吗？");
                            dialog.setConfirmText("删除");
                            dialog.setConfirmColor(Color.RED);
                            dialog.setOnConfirmListener(()->{
                                    List<Plugin> listPlugin=LoadPluginReceiver.listPlugin;
                                    List<PluginInfo> listInfo=MainTabActivity.getListInfo();
                                    try{
                                        //获取listInfo的index
                                        int index2=position;
                                        for(int i=0;i<listPlugin.size();i++){
                                            if(listPlugin.get(position).packageName.equals(listInfo.get(i).getPackageName())){
                                                index2=i;
                                                break;
                                            }
                                        }
                                        PluginInfo plugin2=listInfo.get(index2);
                                        Intent intent2=new Intent();
                                        intent2.setComponent(new ComponentName(plugin2.getPackageName(),plugin2.getPackageName()+".event.PluginUninstallActivity"));
                                        RePlugin.startActivity(mContext,intent2);
                                        int finalIndex = index2;
                                        new Thread(()->{
                                            //卸载插件，延时2秒卸载，否则可能无法调用插件卸载事件
                                            try {
                                                Thread.sleep(2000);
                                                RePlugin.uninstall(plugin2.getPackageName());
                                                File file=new File(plugin2.getApkFile().getAbsolutePath());
                                                if(file.exists()) file.delete();
                                                file=new File(plugin2.getDexFile().getAbsolutePath());
                                                if(file.exists()) file.delete();
                                                file=new File(plugin2.getApkFile().getAbsolutePath().replace(".jar",".cpk"));
                                                if(file.exists()) file.delete();

                                                new Handler(Looper.getMainLooper()).post(()->{
                                                    listPlugin.remove(position);
                                                    listInfo.remove(finalIndex);
                                                    adapter.notifyDataSetChanged();
                                                    Toast.makeText(mContext, "插件已删除", Toast.LENGTH_LONG).show();
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return null;
                            });
                            dialog.show();
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
        });
    }
    private static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }
}
