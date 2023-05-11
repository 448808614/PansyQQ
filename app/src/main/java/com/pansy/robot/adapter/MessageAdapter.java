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
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.activity.ChatActivity;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.fragments.FragmentMessage;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.protocol.UnPackDatagram;
import com.pansy.robot.struct.QQGroup;
import com.pansy.robot.struct.QQMessage;
import com.pansy.robot.struct.UnReadMessage;
import com.pansy.robot.view.CircleImageView;
import com.pansy.robot.view.UnReadNumTip;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessageAdapter extends BaseAdapter {
    private Context mContext;
    private List<QQMessage> mList;
    private ExecutorService threadPool;
    //一级缓存
    private LruCache<String,Bitmap> mHeadCache;
    private LruCache<String,String> mNickCache;
    public MessageAdapter(Context context,List<QQMessage> list){
        mContext=context;
        mList=list;
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
        mNickCache=new LruCache<String,String>(cacheSize){
            @Override
            protected int sizeOf(String key, String value) {
                return value.getBytes().length;
            }
        };
    }
    public void addToFirst(QQMessage qm){
        if(mList.size()>=256){
            //消息列表超过256条则清空之前的消息
            mList.clear();
        }
        for(int i=0;i<mList.size();i++){
            if(mList.get(i).type==0 && qm.type==0){
                if(mList.get(i).gn==qm.gn){
                    mList.remove(i);
                }
            }else if(mList.get(i).type==1 && qm.type==1){
                if(mList.get(i).QQ==qm.QQ){
                    mList.remove(i);
                }
            }
        }
        mList.add(0,qm);

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        //System.out.println("getView:"+position);
        ViewHolder holder;
        if(convertView==null){
            convertView=LayoutInflater.from(mContext).inflate(R.layout.message_list,parent,false);
            holder=new ViewHolder();
            holder.img_message_head=convertView.findViewById(R.id.img_message_head);
            holder.txt_message_nick=convertView.findViewById(R.id.txt_message_nick);
            holder.txt_message_brief=convertView.findViewById(R.id.txt_message_brief);
            holder.txt_message_time=convertView.findViewById(R.id.txt_message_time);
            holder.urnt=convertView.findViewById(R.id.urnt);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        String head_url="";//头像
        String nick_url="";//昵称
        String card_url="";//群成员名片
        final QQMessage qm=mList.get(position);
        if(qm.type==0){
            head_url="http://p.qlogo.cn/gh/"+mList.get(position).gn+"/"+qm.gn+"/140";
            card_url="http://qinfo.clt.qq.com/cgi-bin/mem_card/get_group_mem_card?gc=" + qm.gn + "&bkn=" + APP.getGtk() + "&u=" +qm.QQ;
            holder.txt_message_nick.setText(getGroupName(UnPackDatagram.getGroups(),qm.gn));
            holder.img_message_head.setTag(head_url);
            holder.txt_message_brief.setTag(card_url);
            //如果缓存中存在就到缓存中获取
            Bitmap bitmap=mHeadCache.get(head_url);
            if(bitmap!=null){
                holder.img_message_head.setImageBitmap(bitmap);
            }else{
                threadPool.execute(new SetHeadThread(holder,qm));
            }
            String card=mNickCache.get(card_url);
            if(card!=null){
                holder.txt_message_brief.setText(card+":"+qm.msg);
            }else{
                threadPool.execute(new SetNickThread(holder,qm));
            }
            //holder.txt_message_brief.setText(mList.get(position).QQ+":"+mList.get(position).brief);
        }else if(qm.type==1){
            head_url="http://q4.qlogo.cn/g?b=qq&nk="+qm.QQ+"&s=100";
            nick_url="http://r.qzone.qq.com/fcg-bin/cgi_get_score.fcg?mask=7&uins="+qm.QQ;
            holder.img_message_head.setTag(head_url);
            holder.txt_message_nick.setTag(nick_url);
            //如果缓存中存在就到缓存中获取
            Bitmap bitmap=mHeadCache.get(head_url);
            if(bitmap!=null){
                holder.img_message_head.setImageBitmap(bitmap);
            }else{
                threadPool.execute(new SetHeadThread(holder,qm));
            }
            String nick=mNickCache.get(nick_url);
            if(nick!=null){
                holder.txt_message_nick.setText(nick);
            }else{
                threadPool.execute(new SetNickThread(holder,qm));
            }
            holder.txt_message_brief.setText(qm.msg);
        }
        holder.txt_message_time.setText(qm.time);
        if(qm.type==0) {
            UnReadMessage urm=APP.getUrm(qm.type, qm.gn);
            if(urm!=null) holder.urnt.setUnRead(urm.getNum());
        } else if(qm.type==1){
            UnReadMessage urm=APP.getUrm(qm.type, qm.QQ);
            if(urm!=null) holder.urnt.setUnRead(urm.getNum());
        }

        //点击跳转到聊天界面
        convertView.setOnClickListener((v)-> {
                final Intent intent=new Intent(mContext,ChatActivity.class);
                intent.putExtra("msg_type",qm.type);
                if(qm.type==0){
                    intent.putExtra("gn",qm.gn);
                    intent.putExtra("QQ",qm.QQ);
                    intent.putExtra("name",getGroupName(UnPackDatagram.getGroups(),qm.gn));
                    intent.putExtra("msg",qm.msg);
                    mContext.startActivity(intent);
                    try{
                        APP.getUrm(qm.type,qm.gn).setNum(0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if(qm.type==1){
                    intent.putExtra("gn",0);
                    intent.putExtra("QQ",qm.QQ);
                    threadPool.execute(()-> {
                        String name=QQAPI.getNick(qm.QQ);
                        intent.putExtra("name",name);
                        intent.putExtra("msg",qm.msg);
                        mContext.startActivity(intent);
                        try{
                            APP.getUrm(qm.type,qm.QQ).setNum(0);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }
        });
        convertView.setOnLongClickListener((v)-> {
            ImitateIosDialog dialog=new ImitateIosDialog(mContext);
            dialog.hideTitle();
            dialog.hideContent();
            dialog.hideConfirm();
            dialog.hideCancel();
            dialog.hideV1();
            dialog.addItem(mContext,new String[]{"删除会话","清空会话"},(p)->{
                if(p==0){
                    QQMessage qm_=mList.get(position);
                    if(qm_.type==0)
                        APP.removeUrm(qm_.type,qm_.gn);
                    else if(qm_.type==1)
                        APP.removeUrm(qm_.type,qm_.QQ);
                    mList.remove(position);
                    notifyDataSetChanged();
                    if(mList.size()==0)
                        FragmentMessage.img_no_message.setVisibility(View.VISIBLE);
                }else if(p==1){
                    APP.clearUrmList();
                    mList.clear();
                    notifyDataSetChanged();
                    FragmentMessage.img_no_message.setVisibility(View.VISIBLE);
                }
                return null;
            });
            dialog.show();
            return true;
        });
        return convertView;
    }

    class SetHeadThread implements Runnable{
        private ViewHolder holder;
        private QQMessage qm;
        public SetHeadThread(ViewHolder holder,QQMessage qm){
            this.holder=holder;
            this.qm=qm;
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
                    if(holder.img_message_head.getTag().equals(url))
                        holder.img_message_head.setImageBitmap(bitmap);
                }
            }

            String url="";
            Bitmap bitmap=null;
            if(qm.type==0){
                url="http://p.qlogo.cn/gh/"+qm.gn+"/"+qm.gn+"/140";
                bitmap=QQAPI.getGroupHead(qm.gn);
            }else if(qm.type==1){
                url="http://q4.qlogo.cn/g?b=qq&nk="+qm.QQ+"&s=100";
                bitmap=QQAPI.getHead(qm.QQ);
            }
            if(bitmap!=null)
                new Handler(Looper.getMainLooper()).post(new R1(url,bitmap));

        }
    }
    class SetNickThread implements Runnable{
        private ViewHolder holder;
        private QQMessage qm;
        public SetNickThread(ViewHolder holder,QQMessage qm){
            this.holder=holder;
            this.qm=qm;
        }
        @Override
        public void run() {
            //if(position==mList.size()-1)
                //System.out.println("SetNickThread:"+position);
            //System.out.println("SetNickThread:"+position+",QQ:"+mList.get(position).QQ);
            String url="";
            String nick="";
            class R1 implements Runnable{
                private String url;
                private String nick;
                public R1(String url,String nick){
                    this.url=url;
                    this.nick=nick;
                }
                @Override
                public void run() {
                    if(mNickCache.get(url)==null){
                        if(nick!=null)
                            mNickCache.put(url,nick);
                    }
                    if(qm.type==0){
                        if (holder.txt_message_brief.getTag().equals(url)){
                            holder.txt_message_brief.setText(nick+":"+qm.msg);
                        }
                    }else if(qm.type==1){
                        if(holder.txt_message_nick.getTag().equals(url)) {
                            holder.txt_message_nick.setText(nick);
                        }
                    }
                }
            }
            if(qm.type==0){
                url="http://qinfo.clt.qq.com/cgi-bin/mem_card/get_group_mem_card?gc=" + qm.gn + "&bkn=" + APP.getGtk() + "&u=" +qm.QQ;
                nick=QQAPI.getGroupCard(qm.gn,qm.QQ);
                new Handler(Looper.getMainLooper()).post(new R1(url,nick));
            }else if(qm.type==1){
                url="http://r.qzone.qq.com/fcg-bin/cgi_get_score.fcg?mask=7&uins="+qm.QQ;
                nick=QQAPI.getNick(qm.QQ);
                new Handler(Looper.getMainLooper()).post(new R1(url,nick));
            }

        }
    }
    class ViewHolder{
        CircleImageView img_message_head;
        TextView txt_message_nick;
        TextView txt_message_brief;
        TextView txt_message_time;
        UnReadNumTip urnt;
    }
    //根据群号找群名
    private String getGroupName(List<QQGroup> groups,long gn){
        if(groups==null) return "";
        String name="";
        for(int i=0;i<groups.size();i++){
            if(groups.get(i).gn==gn){
                name=groups.get(i).name;
                break;
            }
        }
        return name;
    }

}
