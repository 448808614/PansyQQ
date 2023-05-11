package com.pansy.robot.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pansy.robot.R;
import com.pansy.robot.activity.ChatActivity;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.struct.QQGroup;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.view.CircleImageView;
import com.pansy.robot.view.SwitchButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class GroupsAdapter extends BaseAdapter {
    private static List<QQGroup> mGroups;
    private Context mContext;
    private LruCache<String,Bitmap> mHeadCache;
    private ExecutorService threadPool;
    public GroupsAdapter(Context context,List<QQGroup> groups){
        this.mContext=context;
        this.mGroups=groups;
        threadPool= Executors.newCachedThreadPool();
        int maxMemory=(int)Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/16;
        mHeadCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }
    @Override
    public int getCount() {
        return mGroups.size();
    }

    @Override
    public Object getItem(int position) {
        return mGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(mContext).inflate(R.layout.tab_groups_item,parent,false);
            holder=new ViewHolder();
            holder.img_groups_head=convertView.findViewById(R.id.img_groups_head);
            holder.txt_groups_name=convertView.findViewById(R.id.txt_groups_name);
            holder.sw_group_open=convertView.findViewById(R.id.sw_group_open);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        final QQGroup group=mGroups.get(position);
        String url="http://p.qlogo.cn/gh/"+group.gn+"/"+group.gn+"/140";
        holder.img_groups_head.setTag(url);
        Bitmap bitmap=mHeadCache.get(url);
        if(bitmap!=null){
            holder.img_groups_head.setImageBitmap(bitmap);
        }else{
            threadPool.execute(new SetHeadThread(holder,group));
        }
        //System.out.println("gn:"+mGroups.get(position).gn+"open:"+mGroups.get(position).open);
        holder.txt_groups_name.setText(group.name+"("+group.gn+")");
        if(group.open==false){
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.img_groups_head.setColorFilter(filter);
        }else {
            holder.img_groups_head.clearColorFilter();
        }
        convertView.setOnClickListener((v)-> {
                Intent intent=new Intent(mContext,ChatActivity.class);
                intent.putExtra("msg_type",0);
                intent.putExtra("gn",group.gn);
                intent.putExtra("QQ",0);
                intent.putExtra("name",group.name);
                mContext.startActivity(intent);
        });
        holder.sw_group_open.setOnChangedListener((v,checkState)-> {
                group.open=checkState;
                SPHelper.writeBool("group_open",group.gn+"",checkState);
                notifyDataSetChanged();
        });
        holder.sw_group_open.setCheck(group.open);
        convertView.setOnLongClickListener((v)-> {
            ImitateIosDialog dialog=new ImitateIosDialog(mContext);
            dialog.hideTitle();
            dialog.hideContent();
            dialog.hideConfirm();
            dialog.hideCancel();
            dialog.hideV1();
            dialog.addItem(mContext, new String[]{"全部关闭", "全部开启"}, (p)-> {
                if(p==0){
                    for(int i=0;i<mGroups.size();i++){
                        mGroups.get(i).open=false;
                        SPHelper.writeBool("group_open",mGroups.get(i).gn+"",false);
                        notifyDataSetChanged();
                    }
                    Toast.makeText(mContext,"已关闭全部群",Toast.LENGTH_LONG).show();
                }else if(p==1){
                    for(int i=0;i<mGroups.size();i++){
                        mGroups.get(i).open=true;
                        SPHelper.writeBool("group_open",mGroups.get(i).gn+"",true);
                        notifyDataSetChanged();
                    }
                    Toast.makeText(mContext,"已开启全部群",Toast.LENGTH_LONG).show();
                }
                return null;
            });
            dialog.show();
            return true;
        });

        return convertView;
    }
    class SetHeadThread implements Runnable{
        ViewHolder holder;
        QQGroup qg;
        public SetHeadThread(ViewHolder holder,QQGroup qg){
            this.holder=holder;
            this.qg=qg;
        }
        @Override
        public void run() {
            final String url="http://p.qlogo.cn/gh/"+qg.gn+"/"+qg.gn+"/140";
            final Bitmap bitmap=QQAPI.getGroupHead(qg.gn);
            if(bitmap!=null){
                new Handler(Looper.getMainLooper()).post(()-> {
                    if(mHeadCache.get(url)==null)
                        mHeadCache.put(url,bitmap);
                    if(holder.img_groups_head.getTag().equals(url))
                        holder.img_groups_head.setImageBitmap(bitmap);
                });
            }
        }
    }
    class ViewHolder{
        CircleImageView img_groups_head;
        TextView txt_groups_name;
        SwitchButton sw_group_open;
    }
}
