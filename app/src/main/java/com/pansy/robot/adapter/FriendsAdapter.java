package com.pansy.robot.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.pansy.robot.R;
import com.pansy.robot.activity.ChatActivity;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.struct.QQFriend;
import com.pansy.robot.view.CircleImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsAdapter extends BaseExpandableListAdapter {
    private String[] mGroups;//好友分组
    private QQFriend[][] mChilds;
    private Context mContext;
    private ExecutorService threadPool;
    private LruCache<String,Bitmap> mHeadCache;
    //private LruCache<String,String> mSignCache;
    public FriendsAdapter(Context context,String[] groups,QQFriend[][] childs){
        this.mContext=context;
        this.mGroups=groups;
        this.mChilds=childs;
        threadPool= Executors.newCachedThreadPool();
        int maxMemory=(int)Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/16;
        mHeadCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        /*mSignCache=new LruCache<String,String>(cacheSize){
            @Override
            protected int sizeOf(String key, String value) {
                return value.getBytes().length;
            }
        };*/
    }
    @Override
    public int getGroupCount() {
        return mGroups.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(mChilds[groupPosition]!=null)
            return mChilds[groupPosition].length;
        else
            return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChilds[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(mContext).inflate(R.layout.tab_friends_group,parent,false);
            holder=new GroupHolder();
            holder.txt_friends_group=convertView.findViewById(R.id.txt_friends_group);
            convertView.setTag(holder);
        }else{
            holder=(GroupHolder)convertView.getTag();
        }
        holder.txt_friends_group.setText(mGroups[groupPosition]);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder holder;
        if(convertView==null){
            convertView=LayoutInflater.from(mContext).inflate(R.layout.tab_friends_child,parent,false);
            holder=new ChildHolder();
            holder.img_friends_head=convertView.findViewById(R.id.img_friends_head);
            holder.txt_friends_nick=convertView.findViewById(R.id.txt_friends_nick);
            //holder.txt_friends_sign=convertView.findViewById(R.id.txt_friends_sign);
            convertView.setTag(holder);
        }else{
            holder=(ChildHolder) convertView.getTag();
        }
        String head_url="http://q4.qlogo.cn/g?b=qq&nk="+mChilds[groupPosition][childPosition].QQ+"&s=100";
        holder.img_friends_head.setTag(head_url);
        Bitmap bitmap=mHeadCache.get(head_url);
        if(bitmap!=null){
            holder.img_friends_head.setImageBitmap(bitmap);
        }else{
            threadPool.execute(new SetHeadThread(holder,mChilds[groupPosition][childPosition]));
        }
        /*String sign_url="sign:"+mChilds[groupPosition][childPosition].QQ;
        holder.txt_friends_sign.setTag(sign_url);
        String sign=mSignCache.get(sign_url);
        if(sign!=null){
            holder.txt_friends_sign.setText(sign);
        }else{
            threadPool.execute(new SetSignThread(holder,mChilds[groupPosition][childPosition]));
        }*/
        holder.txt_friends_nick.setText(mChilds[groupPosition][childPosition].nick);
        //holder.txt_friends_sign.setText(mChilds[groupPosition][childPosition].sign);
        convertView.setOnClickListener((v)-> {
                final Intent intent=new Intent(mContext,ChatActivity.class);
                intent.putExtra("msg_type",1);
                intent.putExtra("gn",0);
                intent.putExtra("QQ",mChilds[groupPosition][childPosition].QQ);
                threadPool.execute(()-> {
                        String name=QQAPI.getNick(mChilds[groupPosition][childPosition].QQ);
                        intent.putExtra("name",name);
                        mContext.startActivity(intent);
                });
        });
        return convertView;
    }
    class SetHeadThread implements Runnable{
        ChildHolder holder;
        QQFriend qf;
        public SetHeadThread(ChildHolder holder,QQFriend qf){
            this.holder=holder;
            this.qf=qf;
        }
        @Override
        public void run() {
            final String url="http://q4.qlogo.cn/g?b=qq&nk="+qf.QQ+"&s=100";
            final Bitmap bitmap=QQAPI.getHead(qf.QQ);
            if(bitmap!=null){
                new Handler(Looper.getMainLooper()).post(()->{
                    if(mHeadCache.get(url)==null)
                        mHeadCache.put(url,bitmap);
                    if(holder.img_friends_head.getTag().equals(url))
                        holder.img_friends_head.setImageBitmap(bitmap);
                });
            }
        }
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    class GroupHolder{
        TextView txt_friends_group;
    }
    class ChildHolder{
        CircleImageView img_friends_head;
        TextView txt_friends_nick;
        //TextView txt_friends_sign;
    }
}
