package com.pansy.robot.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenuLayout;
import com.pansy.robot.R;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.fragments.FragmentPluginLocal;
import com.pansy.robot.struct.Plugin;
import com.pansy.robot.view.MarqueeTextView;
import com.pansy.robot.view.RoundImageView;

import java.util.List;

public class PluginLocalAdapter extends BaseAdapter {
    private static List<Plugin> mPluginList;
    private Context mContext;

    public PluginLocalAdapter(Context context, List<Plugin> list) {
        this.mContext=context;
        this.mPluginList=list;
    }
    public void add(Plugin plugin){
        mPluginList.add(plugin);
    }
    public void remove(int i){
        mPluginList.remove(i);
    }
    public static List<Plugin> getmList(){return mPluginList;}
    public void setPluginEnable(int position,boolean enable){
        mPluginList.get(position).enable=enable;
    }
    @Override
    public int getCount() {
        return mPluginList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPluginList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            holder=new ViewHolder();
            convertView=LayoutInflater.from(mContext).inflate(R.layout.plugin_local_item,parent,false);
            holder.img_plugin_local_icon=convertView.findViewById(R.id.img_plugin_local_icon);
            holder.txt_plugin_local_name=convertView.findViewById(R.id.txt_plugin_local_name);
            holder.txt_plugin_local_brief=convertView.findViewById(R.id.txt_plugin_loacl_brief);
            holder.txt_plugin_local_author=convertView.findViewById(R.id.txt_plugin_loacl_author);
            holder.txt_plugin_local_version=convertView.findViewById(R.id.txt_plugin_loacl_version);
            holder.img_plugin_local_new=convertView.findViewById(R.id.img_plugin_local_new);
            convertView.setTag(holder);
        }else
            holder=(ViewHolder)convertView.getTag();
        Plugin plugin=mPluginList.get(position);
        if(mPluginList.get(position).icon!=null){
            holder.img_plugin_local_icon.setImageBitmap(plugin.icon);
        }else{
            Bitmap bitmap= BitmapFactory.decodeResource(mContext.getResources(),R.drawable.plugin_default);
            holder.img_plugin_local_icon.setImageBitmap(bitmap);
        }
        if(plugin.enable==false){
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);//饱和度 0灰色 100过度彩色,50正常
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.img_plugin_local_icon.setColorFilter(filter);
        }else
            holder.img_plugin_local_icon.clearColorFilter();
        holder.txt_plugin_local_name.setText(plugin.name);
        holder.txt_plugin_local_brief.setText(plugin.brief);
        holder.txt_plugin_local_author.setText(plugin.author);
        holder.txt_plugin_local_version.setText(plugin.version);
        if(plugin.hasNewVersion)
            holder.img_plugin_local_new.setVisibility(View.VISIBLE);
        else
            holder.img_plugin_local_new.setVisibility(View.GONE);
        /*holder.img_plugin_local_new.setOnClickListener((v)->{
            if(!plugin.is_aidl){
                ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                dialog.setContent("replugin插件更新请卸载旧版插件后，重启主程序，再下载新版插件");
                dialog.hideCancel();
                dialog.show();
            }
        });*/
        return convertView;
    }
    class ViewHolder{
        RoundImageView img_plugin_local_icon;
        TextView txt_plugin_local_name;
        MarqueeTextView txt_plugin_local_brief;
        TextView txt_plugin_local_author;
        TextView txt_plugin_local_version;
        ImageView img_plugin_local_new;
    }
}
