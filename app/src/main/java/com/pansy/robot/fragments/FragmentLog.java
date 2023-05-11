package com.pansy.robot.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.R;
import com.pansy.robot.adapter.LogAdapter;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.SPHelper;

public class FragmentLog extends Fragment {
    private static LogAdapter adapter;
    private ListView lv_log;
    private CheckBox cb_log_plugin,cb_log_heartbeat,cb_log_success;
    public static boolean print_heartbeat,print_plugin,print_success;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.log_layout,null);
        lv_log=v.findViewById(R.id.lv_log);
        if(MainTabActivity.getListLog()!=null) {
            adapter = new LogAdapter(getContext(), MainTabActivity.getListLog());
            lv_log.setAdapter(adapter);
        }
        cb_log_plugin=v.findViewById(R.id.cb_log_plugin);
        cb_log_heartbeat=v.findViewById(R.id.cb_log_heartbeat);
        cb_log_success=v.findViewById(R.id.cb_log_success);
        cb_log_heartbeat.setChecked(print_heartbeat);
        cb_log_plugin.setChecked(print_plugin);
        cb_log_success.setChecked(print_success);
        cb_log_heartbeat.setOnCheckedChangeListener(new CheckBoxCheck());
        cb_log_plugin.setOnCheckedChangeListener(new CheckBoxCheck());
        cb_log_success.setOnCheckedChangeListener(new CheckBoxCheck());
        return v;
    }

    class CheckBoxCheck implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           if(buttonView==cb_log_heartbeat){
                SPHelper.writeBool("log","心跳",isChecked);
                print_heartbeat=isChecked;
            }else if(buttonView==cb_log_plugin){
                SPHelper.writeBool("log","插件",isChecked);
                print_plugin=isChecked;
            }else if(buttonView==cb_log_success){
               SPHelper.writeBool("log","发送成功",isChecked);
               print_success=isChecked;
           }
        }
    }
    public static LogAdapter getAdapter(){return adapter;}
    public static void notifyDataSetChanged(String name,String msg,int type){
        new Handler(Looper.getMainLooper()).post(()-> {
            try {
                if(MainTabActivity.getListLog().size()>256)
                    MainTabActivity.getListLog().clear();
                MainTabActivity.getListLog().add(0,new com.pansy.robot.struct.Log(ByteUtil.getTime(),name,msg,type));
                adapter.notifyDataSetChanged();
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}
