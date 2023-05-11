package com.pansy.robot.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.pansy.robot.APP;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.R;
import com.pansy.robot.adapter.MessageAdapter;
import com.pansy.robot.struct.QQMessage;

import java.util.ArrayList;
import java.util.List;

public class FragmentMessage extends Fragment {
    private static ListView lv_message;
    private static MessageAdapter adapter;
    private static List<QQMessage> mList=new ArrayList<>();
    private static ImageButton btn_message_openDrawer;
    public static ImageView img_no_message;
    private View rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rootView==null){
            View view=inflater.inflate(R.layout.fragment_message,null);
            lv_message=view.findViewById(R.id.lv_message);
            btn_message_openDrawer=view.findViewById(R.id.btn_message_openDrawer);
            img_no_message=view.findViewById(R.id.img_no_message);
            btn_message_openDrawer.setOnClickListener((v)-> {
                MainTabActivity.getDrawerLayout().openDrawer(Gravity.LEFT);
            });
            rootView=view;
            return view;
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(adapter!=null){
            try {
                lv_message.setAdapter(adapter);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void setMessageAdatper(final QQMessage qm){
        if(adapter==null){
            mList.add(qm);
            adapter=new MessageAdapter(APP.getMainTabContext(),mList);
            new Handler(Looper.getMainLooper()).post(()-> {
                try{
                    if(mList!=null && mList.size()>0)
                        img_no_message.setVisibility(View.GONE);
                    else
                        img_no_message.setVisibility(View.VISIBLE);
                    lv_message.setAdapter(adapter);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }else{
            new Handler(Looper.getMainLooper()).post(()-> {
                try{
                    adapter.addToFirst(qm);
                    adapter.notifyDataSetChanged();
                    if(mList!=null && mList.size()>0)
                        img_no_message.setVisibility(View.GONE);
                    else
                        img_no_message.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }
}
