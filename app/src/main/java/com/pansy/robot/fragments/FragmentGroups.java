package com.pansy.robot.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.adapter.GroupsAdapter;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.protocol.UnPackDatagram;
import com.pansy.robot.struct.QQGroup;

import java.util.List;

public  class FragmentGroups extends Fragment {
    private static GroupsAdapter adapter;
    private ListView lv_tab_groups;
    private SwipeRefreshLayout srl;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_groups, null);
        lv_tab_groups=v.findViewById(R.id.lv_tab_groups);
        srl=v.findViewById(R.id.srl);
        srl.setOnRefreshListener(()->{
            new Thread(()->{
                String str=QQAPI.getGroupList();
                if(!str.equals(""))
                    UnPackDatagram.setGroups(str);
                new Handler(Looper.getMainLooper()).post(()->{
                    loadGroups();
                    srl.setRefreshing(false);
                });
            }).start();

        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(adapter==null){
            loadGroups();
        }else{
            lv_tab_groups.setAdapter(adapter);
        }
    }

    /**
     * 加载群列表
     */
    private void loadGroups(){
        List<QQGroup> groups=UnPackDatagram.getGroups();
        if(groups!=null) {
            adapter = new GroupsAdapter(APP.getMainTabContext(), groups);
            new Handler(Looper.getMainLooper()).post(()->
                    lv_tab_groups.setAdapter(adapter)
            );
        }
    }

}