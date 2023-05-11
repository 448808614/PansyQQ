package com.pansy.robot.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.struct.QQMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<QQMessage> mMsgList;
    private LruCache<String,Bitmap> mHeadCache;
    private ExecutorService threadPool;
    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftLayout,rightLayout;
        TextView leftMsg,rightMsg;
        ImageView iv_head_myself,iv_head_other;
        public ViewHolder(View view){
            super(view);
            leftLayout=view.findViewById(R.id.linear_chat_left);
            rightLayout=view.findViewById(R.id.linear_chat_right);
            leftMsg=view.findViewById(R.id.edt_chat_left);
            rightMsg=view.findViewById(R.id.edt_chat_right);
            iv_head_myself=view.findViewById(R.id.iv_head_myself);
            iv_head_other=view.findViewById(R.id.iv_head_other);
        }
    }
    public ChatAdapter(List<QQMessage> msgList){
        mMsgList=msgList;
        threadPool=Executors.newCachedThreadPool();
        //设置缓存大小
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){               //onCreateViewHolder()用于创建ViewHolder实例
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        QQMessage msg=mMsgList.get(position);
        if(msg.QQ==APP.getQQ()){//我发的
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.msg);
            if(MainTabActivity.myHeadBitmap!=null)
                holder.iv_head_myself.setImageBitmap(MainTabActivity.myHeadBitmap);
        }else {//别人发的
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.msg);
            String head_url="http://q4.qlogo.cn/g?b=qq&nk="+msg.QQ+"&s=100";
            holder.iv_head_other.setTag(head_url);
            Bitmap bitmap=mHeadCache.get(head_url);
            if(bitmap!=null){
                holder.iv_head_other.setImageBitmap(bitmap);
            }else{
                threadPool.execute(new SetHeadThread(holder,msg.QQ));
            }
        }
    }
    @Override
    public int getItemCount(){
        int count=mMsgList.size();
        return count;
    }

    class SetHeadThread implements Runnable{
        private ViewHolder holder;
        private long QQ;
        public SetHeadThread(ViewHolder holder,long QQ){
            this.holder=holder;
            this.QQ=QQ;
        }

        @Override
        public void run() {
            class R1 implements Runnable{
                private String url;
                private Bitmap bitmap;
                public R1(String url,Bitmap bitmap){
                    this.url=url;
                    this.bitmap=bitmap;
                }
                @Override
                public void run() {
                    //添加到缓存中
                    if(mHeadCache.get(url)==null){
                        if(bitmap!=null)
                            mHeadCache.put(url,bitmap);
                    }
                    //防止图片错位
                    if(holder.iv_head_other.getTag().equals(url))
                        holder.iv_head_other.setImageBitmap(bitmap);
                }
            }

            String url="http://q4.qlogo.cn/g?b=qq&nk="+QQ+"&s=100";
            Bitmap bitmap=QQAPI.getHead(QQ);

            if(bitmap!=null)
                new Handler(Looper.getMainLooper()).post(new R1(url,bitmap));

        }
    }

}