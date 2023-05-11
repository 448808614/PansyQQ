package com.pansy.robot.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.pansy.robot.R;

public class FragmentPlugin extends Fragment {
    private static TabHost mTabHost;
    private static TabManager mTabManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin, null);
        //Resources res = getResources();
        mTabHost = view.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.getTabWidget().setDividerDrawable(null);
        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent_plugin);
        mTabManager.addTab(mTabHost.newTabSpec("local").setIndicator("本地"),
                FragmentPluginLocal.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("shop").setIndicator("商城"),
                FragmentPluginShop.class, null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}