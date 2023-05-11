package com.pansy.robot.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.pansy.robot.R;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.struct.Log;
import java.util.List;


import static android.content.Context.CLIPBOARD_SERVICE;

public class LogAdapter extends BaseAdapter {
    private Context mContext;
    private List<Log> mList;
    public LogAdapter(Context context,List<Log> list){
        mContext=context;
        mList=list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            convertView=LayoutInflater.from(mContext).inflate(R.layout.log_item,parent,false);
            holder=new ViewHolder();
            holder.txt_log_time=convertView.findViewById(R.id.txt_log_time);
            holder.txt_log_from=convertView.findViewById(R.id.txt_log_from);
            holder.txt_log_info=convertView.findViewById(R.id.txt_log_info);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        Log log=mList.get(position);
        holder.txt_log_time.setText(log.time);
        holder.txt_log_from.setText(log.from);
        holder.txt_log_info.setText(log.info);
        if(log.type==1) {
            holder.txt_log_from.setTextColor(Color.parseColor("#FF0000"));
            holder.txt_log_info.setTextColor(Color.parseColor("#FF0000"));
        }else if(log.type==2) {
            holder.txt_log_from.setTextColor(Color.parseColor("#00FA9A"));
            holder.txt_log_info.setTextColor(Color.parseColor("#00FA9A"));
        }else{
            holder.txt_log_from.setTextColor(Color.parseColor("#000000"));
            holder.txt_log_info.setTextColor(Color.parseColor("#000000"));
        }
        convertView.setOnLongClickListener((v)->{
            ImitateIosDialog dialog=new ImitateIosDialog(mContext);
            dialog.hideTitle();
            dialog.hideContent();
            dialog.hideConfirm();
            dialog.hideCancel();
            dialog.hideV1();
            dialog.addItem(mContext,new String[]{"清空日志"},(p)->{
                mList.clear();
                notifyDataSetChanged();
                return null;
            });
            dialog.show();
            return true;
        });
        convertView.setOnClickListener(new Click(holder.txt_log_info.getText().toString()));
        return convertView;
    }

    class Click implements View.OnClickListener{
        private String info;
        public Click (String info){this.info=info;}
        @Override
        public void onClick(View v) {
            ImitateIosDialog dialog=new ImitateIosDialog(v.getContext());
            if(info.startsWith("群消息发送>>") || info.startsWith("群xml发送>>") || info.startsWith("群json发送>>")
                    || info.startsWith("好友消息发送>>") || info.startsWith("好友xml发送>>") || info.startsWith("好友json发送>>")
                    || info.startsWith("私聊消息发送>>")){
                String type=info.substring(0,info.indexOf(">>"));
                info=info.substring(info.indexOf(">>")+2);
                dialog.setTitle(type);
                dialog.setContent(info);
            }else{
                dialog.setTitle("日志");
                dialog.setContent(info);
            }
            dialog.setConfirmText("复制");
            dialog.setOnConfirmListener(()-> {
                ClipboardManager myClipboard = (ClipboardManager)mContext.getSystemService(CLIPBOARD_SERVICE);
                ClipData myClip=ClipData.newPlainText("log",info);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(mContext,"已复制到剪贴板文本",Toast.LENGTH_LONG).show();
                return null;
            });
            dialog.show();
        }
    }
    class ViewHolder{
        TextView txt_log_time;
        TextView txt_log_from;
        TextView txt_log_info;
    }
}
