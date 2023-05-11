package com.pansy.robot.protocol;

import android.content.Intent;

import com.pansy.robot.APP;
import com.pansy.robot.activity.ChatActivity;
import com.pansy.robot.crypter.Tea;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.service.SendService;
import com.pansy.robot.struct.MsgPack;
import com.pansy.robot.struct.QQMessage;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.FileUtil;
import com.pansy.robot.utils.Gzip;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.ZLib;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 分析包数据类
 */
public class Analy {
    private static List<byte[]> mListCacheGMsg=new ArrayList<>();
    private static List<byte[]> mListCacheFMsg=new ArrayList<>();
    private static List<byte[]> mListCachePMsg=new ArrayList<>();
    private static List<byte[]> mListCacheEvent=new ArrayList<>();
    private static List<byte[]> mListCacheWithdraw=new ArrayList<>();
    private static HashMap<Long,String> mWithdrawRecord=new HashMap<>();
    private static List<MsgPack> mListMsgPack=new ArrayList<>();

    /**
     * 私聊消息
     * @param data
     * @return
     */
    public static QQMessage analy_008D(byte[] data,byte[] seq){
        try{
            //缓存16条消息，seq重复则忽略
            if(mListCachePMsg.size()>=16){
                mListCachePMsg.clear();
            }
            for(int i=0;i<mListCachePMsg.size();i++){
                if(Arrays.equals(mListCachePMsg.get(i),seq)){
                    return null;
                }
            }
            mListCachePMsg.add(seq);
            long timeStamp=Converter.byteArray2Long(ByteUtil.subBytes(data,127,4));
            long QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,0,4));
            long gn=Converter.byteArray2Long(ByteUtil.subBytes(data,151,4));
            if(timeStamp<Converter.byteArray2Long(UnPackDatagram.getLoginTime()))
                return null;
            //私聊消息以读
            //PCQQ.send(PackDatagram.pack_0319(QQ,timeStamp));
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
            String time=sdf.format(Long.valueOf(timeStamp+"000"));
            int msg_length=data[258];

            String msg=new String(ByteUtil.subBytes(data,259,msg_length));

            if(msg.equals("")==false){
                System.out.println("私聊消息("+gn+")："+msg);
                APP.addUrmList(1,QQ);
                QQMessage qm=new QQMessage(gn,QQ,msg,time,7);
                if(ChatActivity.msg_type==1 && ChatActivity.QQ==QQ)
                    ChatActivity.addMsg(qm);
                SendService.rece_count++;
                SendService.notifyNotification();
                Intent intent=new Intent();
                intent.setAction("com.pansyqq.receive.qqmessage");
                intent.putExtra("msg_type",7);
                intent.putExtra("gn",gn);
                intent.putExtra("QQ",QQ);
                intent.putExtra("msg",msg);

                APP.getAppContext().sendBroadcast(intent);
                return qm;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 好友消息
     * @param data
     * @return
     */
    public static QQMessage analy_00A6(byte[] data,byte[] seq){
        try{
            //缓存16条消息，seq重复则忽略
            if(mListCacheFMsg.size()>=16){
                mListCacheFMsg.clear();
            }
            for(int i=0;i<mListCacheFMsg.size();i++){
                if(Arrays.equals(mListCacheFMsg.get(i),seq)){
                    return null;
                }
            }
            mListCacheFMsg.add(seq);
            int msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,22,2));
            long timeStamp=Converter.byteArray2Long(ByteUtil.subBytes(data,24+msg_length+30,4));
            long QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,0,4));
            if(timeStamp<Converter.byteArray2Long(UnPackDatagram.getLoginTime()))
                return null;
            //好友消息以读
            PCQQ.send(PackDatagram.pack_0319(QQ,timeStamp));
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
            String time=sdf.format(Long.valueOf(timeStamp+"000"));

            //窗口抖动
            if(Arrays.equals(ByteUtil.subBytes(data,24+msg_length+26,2),new byte[]{0,-81})){
                Intent intent=new Intent();
                intent.setAction("com.pansyqq.receive.qqmessage");
                intent.putExtra("msg_type",6);
                intent.putExtra("gn",-1);
                intent.putExtra("QQ",QQ);
                intent.putExtra("msg","【窗口抖动】");
                QQMessage qm=new QQMessage(0,QQ,"【窗口抖动】",time,1);
                APP.getAppContext().sendBroadcast(intent);
                if(ChatActivity.msg_type==1 && ChatActivity.QQ==QQ)
                    ChatActivity.addMsg(qm);
                return qm;
            }
            int font_length=Converter.byteArray2Int(ByteUtil.subBytes(data,24+msg_length+69,2));
            String font=new String(ByteUtil.subBytes(data,24+msg_length+71,font_length));
            int previous_length=24+msg_length+71+font_length+2;
            //气泡包体
            //if(Arrays.equals(ByteUtil.subBytes(data,previous_length,3),new byte[]{14,0,21})){
                //previous_length+=76;
            //}
            byte msg_flag=data[previous_length];
            StringBuilder sb=new StringBuilder();
            if(msg_flag==25){
                //部分表情
                if(Arrays.equals(ByteUtil.subBytes(data,previous_length+14,2),new byte[]{120,-100})==false){
                    int replay_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                    previous_length+=3+replay_length;
                    msg_flag=data[previous_length];
                }
            }if(msg_flag==20){
                //xml
                msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                String xml=new String(ZLib.decompress(ByteUtil.subBytes(data,previous_length+7,msg_length-4)));
                if(xml.contains("action=\"viewMultiMsg\"")){
                    int start=xml.indexOf("m_resid=\"")+9;
                    int end=xml.indexOf("\"",start+1);
                    String m_resid=xml.substring(start,end);
                    if(!m_resid.equals("")){
                        if(APP.getSkey()!=null){
                            if(APP.getHttpconn_key()!=null){
                                String longMsg=getLongMsg(m_resid);
                                if(!longMsg.equals(""))
                                    sb.append(longMsg);
                            }else
                                QQAPI.log_("接收长消息异常","httpconnkey为空",1);

                        }else
                            QQAPI.log_("接收长消息异常","skey为空",1);
                    }
                }else
                    sb.append(xml);
            }
            while(msg_flag==1 || msg_flag==2 || msg_flag==6 || msg_flag==12 || msg_flag==102){
                //1文字(emoji是文字)，6图片，2表情,12大表情
                String temp="";
                if(msg_flag==1){
                    msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                    int msg_length2=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+4,2));
                    temp=new String(ByteUtil.subBytes(data,previous_length+6,msg_length2));
                    previous_length=previous_length+3+msg_length;
                }else if(msg_flag==2){
                    msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                    int id=ByteUtil.subBytes(data,previous_length+6,1)[0] & 0xff;
                    temp="[face:"+id+"]";
                    previous_length=previous_length+3+msg_length;
                }else if(msg_flag==6){
                    previous_length=ByteUtil.byteIndexOf(data,new byte[]{-1,0,117})+3;
                    temp="[img:guid=\""+new String(ByteUtil.subBytes(data,previous_length+24,36))+"\"]";
                    previous_length+=117;
                }else if(msg_flag==12){
                    msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+4,2));
                    temp=new String(ByteUtil.subBytes(data,previous_length+6,msg_length));
                    previous_length=previous_length+5+msg_length;
                }
                //else if(msg_flag==102){//图片多余的内容
                    //previous_length+=39;
                //}
                sb.append(temp);
                if(previous_length<data.length){//防止解完消息后取msg_flag出现OutArrayBounds异常
                    msg_flag=ByteUtil.subBytes(data,previous_length,1)[0];
                }else{
                    msg_flag=-1;
                }
            }
            String msg=sb.toString();
            if(msg.equals("")==false){
                System.out.println("好友消息："+msg);
                APP.addUrmList(1,QQ);
                QQMessage qm=new QQMessage(0,QQ,msg,time,1);
                if(ChatActivity.msg_type==1 && ChatActivity.QQ==QQ)
                    ChatActivity.addMsg(qm);
                SendService.rece_count++;
                SendService.notifyNotification();
                Intent intent=new Intent();
                intent.setAction("com.pansyqq.receive.qqmessage");
                intent.putExtra("msg_type",1);
                intent.putExtra("gn",-1);
                intent.putExtra("QQ",QQ);
                intent.putExtra("msg",msg);

                if(msg.contains("[转账]") && font.equals("Times New Roman")){
                    //好友转账
                    if(ByteUtil.byteIndexOf(data,"待好友收款".getBytes())>-1 || ByteUtil.byteIndexOf(data,"已转入好友的余额".getBytes())>-1){
                        double money=-1;
                        String remark="";

                        int transfer_length=ByteUtil.byteIndexOf(data,new byte[]{2,16,1,26})+4;
                        int unknow_length=(int)data[transfer_length];
                        String unknow=new String(ByteUtil.subBytes(data,transfer_length+1,unknow_length));

                        if(unknow.contains("元")){
                            money=Double.parseDouble(unknow.substring(0,unknow.length()-1));
                            int brief_length=(int)data[transfer_length+1+unknow_length+1];
                            int remark_length=(int)data[transfer_length+1+unknow_length+2+brief_length+1];
                            remark=new String(ByteUtil.subBytes(data,transfer_length+1+unknow_length+2+brief_length+2,remark_length));
                        }else{
                            int money_length=(int)data[transfer_length+1+unknow_length+1];
                            String str_money=new String(ByteUtil.subBytes(data,transfer_length+1+unknow_length+2,money_length));
                            money=Double.parseDouble(str_money.substring(0,str_money.length()-1));
                            int remark_length=(int)data[transfer_length+1+unknow_length+2+money_length+1];
                            remark=new String(ByteUtil.subBytes(data,transfer_length+1+unknow_length+2+money_length+2,remark_length));
                        }
                        //remark=remark.substring(3,remark.length());
                        intent.putExtra("msg_type",4);
                        intent.putExtra("money",money);
                        intent.putExtra("remark",remark);
                    }
                }
                APP.getAppContext().sendBroadcast(intent);
                return qm;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 群消息
     * @param data
     */
    public static QQMessage analy_0052(byte[] data ,byte[] seq){
        try{
            //缓存16条消息，seq重复则忽略

            if(mListCacheGMsg.size()>=16){
                mListCacheGMsg.clear();
            }
            for(int i=0;i<mListCacheGMsg.size();i++){
                if(Arrays.equals(mListCacheGMsg.get(i),seq)){
                    return null;
                }
            }
            mListCacheGMsg.add(seq);
            long timeStamp=Converter.byteArray2Long(ByteUtil.subBytes(data,64,4));
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
            String time=sdf.format(Long.valueOf(timeStamp+"000"));
            int msg_length=0;
            int previous_length=23;
            int pp_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length,1));
            long withdraw_seq=Converter.byteArray2Long(ByteUtil.subBytes(data,24+pp_length+9,4));
            long withdraw_id=Converter.byteArray2Long(ByteUtil.subBytes(data,24+pp_length+45,4));
            long gn=Converter.byteArray2Long(ByteUtil.subBytes(data,24+pp_length,4));
            final long QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,24+pp_length+5,4));
            boolean groupOpen=SPHelper.readBool("group_open",gn+"",true);
            if(!groupOpen) return null;
            //群消息已读
            PCQQ.send(PackDatagram.pack_0391(gn));
            //判断是否分包
            int packNum=data[previous_length+pp_length+26];
            int packSeq=0;
            int packId=0;
            if(packNum>1){
                //分包序号，从0开始
                packSeq=data[previous_length+pp_length+27];
                //包id
                packId=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+pp_length+28,2));
            }

            previous_length+=1+pp_length+57;
            int font_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length,2));
            String font=new String(ByteUtil.subBytes(data,previous_length+2,font_length));
            previous_length+=2+font_length+2;
            //气泡包体
            if(Arrays.equals(ByteUtil.subBytes(data,previous_length,3),new byte[]{14,0,21})){
                int bubble_length=data[previous_length+26];
                previous_length+=37+bubble_length;
            }
            byte msg_flag=data[previous_length];
            StringBuilder sb=new StringBuilder();
            if(msg_flag==25){
                //回复消息或部分表情
                if(Arrays.equals(ByteUtil.subBytes(data,previous_length+14,2),new byte[]{120,-100})==false){
                    int replay_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                    previous_length+=3+replay_length;
                    msg_flag=data[previous_length];
                }
            }
            if(msg_flag==20){
                //xml
                msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                String xml=new String(ZLib.decompress(ByteUtil.subBytes(data,previous_length+7,msg_length-4)));
                if(xml.contains("action=\"viewMultiMsg\"")){
                    int start=xml.indexOf("m_resid=\"")+9;
                    int end=xml.indexOf("\"",start+1);
                    String m_resid=xml.substring(start,end);
                    if(!m_resid.equals("") && APP.getSkey()!=null && APP.getHttpconn_key()!=null){
                        String longMsg=getLongMsg(m_resid);
                        if(!longMsg.equals(""))
                            sb.append(longMsg);
                    }
                }else
                    sb.append(xml);
            }else if(msg_flag==18){
                //sb.append("[QQ红包]请使用新版手机QQ查收红包。");//大于3块的红包
                pp_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                previous_length+=3+pp_length;
                msg_flag=data[previous_length];
            }
            //int offset=previous_length+4;//解析红包的offset
            while(msg_flag==1 || msg_flag==2 || msg_flag==3 || msg_flag==6 || msg_flag==12 || msg_flag==25){
                //1,6文字(emoji是文字)，2表情,3图片，12大表情,25json
                String temp="";
                if(msg_flag==1 || msg_flag==3 || msg_flag==6){
                    msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                    int msg_length2=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+4,2));
                    if(ByteUtil.subBytes(data,previous_length+6+msg_length2,1)[0]==6){//艾特，回复也是艾特
                        String targetQQ=Long.toString(Converter.byteArray2Long(ByteUtil.subBytes(data,previous_length+6+msg_length2+10,4)));
                        sb.append("[At:"+targetQQ+"]");
                    }else{
                        if(msg_flag==3){
                            int img_length=data[previous_length+5];
                            String guid=new String(ByteUtil.subBytes(data,previous_length+6,img_length));
                            if(!(guid.endsWith("jpg") || guid.endsWith("png") || guid.endsWith("gif")))
                                guid=guid+"jpg";
                            temp="[img:guid=\""+guid+"\"]";
                        }else{
                            //byte[] b=Util.subBytes(data,previous_length+6,msg_length2);
                            temp=new String(ByteUtil.subBytes(data,previous_length+6,msg_length2));
                        }
                    }
                    previous_length=previous_length+3+msg_length;
                }else if(msg_flag==2){
                    msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2));
                    int id=ByteUtil.subBytes(data,previous_length+6,1)[0] & 0xff;
                    temp="[face:"+id+"]";
                    previous_length=previous_length+3+msg_length;
                }else if(msg_flag==12){
                    msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+4,2));
                    temp=new String(ByteUtil.subBytes(data,previous_length+6,msg_length));
                    previous_length=previous_length+5+msg_length;
                }else if(msg_flag==25){//xml,json
                    if(Arrays.equals(ByteUtil.subBytes(data,previous_length+14,2),new byte[]{120,-100})){
                        msg_length=Converter.byteArray2Int(ByteUtil.subBytes(data,previous_length+1,2))-11;
                        temp=new String(ZLib.decompress(ByteUtil.subBytes(data,previous_length+14,msg_length)));
                        previous_length+=14+msg_length;
                    }else if(ByteUtil.byteIndexOf(data,new byte[]{91,-25,-66,-92,-25,-83,-66,-27,-120,-80,93,-24,-81,-73,-28,-67,-65,-25,-108,-88,-26,-106,-80,-25,-119,-120,81,81,-24,-65,-101,-24,-95,-116,-26,-97,-91,-25,-100,-117,-29,-128,-126})>-1){
                        temp="[群签到]请使用新版QQ进行查看。";
                        previous_length=data.length;
                    }
                    else{
                        break;
                    }
                }
                sb.append(temp);
                if(previous_length<data.length){
                    msg_flag=ByteUtil.subBytes(data,previous_length,1)[0];
                }else{
                    msg_flag=-1;
                }

            }
            String msg=sb.toString();
            if(msg.equals("")==false){
                if(packNum>1){
                    mListMsgPack.add(new MsgPack(packNum,packId,packSeq,msg));
                    //判断分包是否接收完成
                    int c=0;
                    List<MsgPack> list_temp=new ArrayList<>();
                    List<Integer> list_index=new ArrayList<>();
                    for (int i=0;i<mListMsgPack.size();i++){
                        MsgPack msgPack=mListMsgPack.get(i);
                        if(msgPack.id==packId){
                            list_temp.add(msgPack);
                            list_index.add(i);
                            c++;
                        }
                    }
                    //接收完成
                    if(c==packNum){
                        Collections.sort(list_temp);
                        StringBuilder sb2=new StringBuilder();
                        for (int i=0;i<list_temp.size();i++){
                            MsgPack msgPack=list_temp.get(i);
                            sb2.append(msgPack.msg);
                        }
                        msg=sb2.toString();
                        //删除该id的列表
                        for (int i=0;i<list_index.size();i++)
                            mListMsgPack.remove(list_index.get(i).intValue()-i);
                    }else
                        return null;
                }

                QQMessage qm= new QQMessage(gn,QQ,msg,time,0);
                if(ChatActivity.msg_type==0 && ChatActivity.gn==gn)
                    ChatActivity.addMsg(qm);
                System.out.println("群消息(来自:"+gn+"):"+msg);

                if(QQ!=APP.getQQ()){
                    APP.addUrmList(0,gn);
                    SendService.rece_count++;
                    SendService.notifyNotification();
                    Intent intent=new Intent();
                    intent.setAction("com.pansyqq.receive.qqmessage");
                    intent.putExtra("msg_type",0);
                    intent.putExtra("gn",gn);
                    intent.putExtra("QQ",QQ);
                    intent.putExtra("msg",msg);
                    intent.putExtra("withdraw_seq",withdraw_seq);
                    intent.putExtra("withdraw_id",withdraw_id);
                    //红包
                    if(font.equals("Times New Roman") && msg.startsWith("[QQ红包]")){
                        try{
                            //offset=getOffset(data,offset);
                            String remark="";
                            try{
                                int remark_length=(int)data[previous_length+26];
                                remark=new String(ByteUtil.subBytes(data,previous_length+27,remark_length));
                                data=ByteUtil.subBytes(data,previous_length+27+remark_length,data.length-previous_length-27-remark_length);
                                byte[] p1=Protobuf.analy(data,new int[]{35329,18});
                                byte[] p2=Protobuf.analy(data,new int[]{35329,26});
                                byte[] p3=Protobuf.analy(data,new int[]{37377});

                                if(!Arrays.equals(p1,new byte[]{0}) && !Arrays.equals(p1,new byte[]{0}) && !Arrays.equals(p1,new byte[]{0})){
                                    intent.putExtra("msg_type",2);
                                    intent.putExtra("envelope_p1",p1);
                                    intent.putExtra("envelope_p2",p2);
                                    intent.putExtra("envelope_p3",p3);
                                    intent.putExtra("envelope_remark",remark);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    //大于1024条清空
                    if(mWithdrawRecord.size()>1024)
                        mWithdrawRecord.clear();
                    mWithdrawRecord.put(withdraw_id,msg);//记录消息，防撤回
                    APP.getAppContext().sendBroadcast(intent);
                }
                return qm;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static String getLongMsg(String m_resid){
        try{
            byte[] b1=PackDatagram.pack_httpsGetLongMsg(m_resid);
            byte[] b2=TCP.send("htdata3.qq.com",443,b1);
            if(b2==null || b2.length<19)
                return "";
            int index=ByteUtil.byteIndexOf(b2,new byte[]{1,18,10})+13;
            byte[] b3=ByteUtil.subBytes(b2,index,b2.length-index-1);
            byte[] b4=new Tea().decrypt(b3,APP.getHttpconn_key());
            b4=Protobuf.analy(b4,new int[]{10,26});
            b4=Gzip.ungzip(b4);
            b4=Protobuf.analy(b4,new int[]{10,26,10});

            StringBuilder sb=new StringBuilder();
            for (int i=0;i<128;i++){
                byte[] b5=Protobuf.analyBySeq(b4,i);
                if(!Arrays.equals(b5,new byte[]{0})){
                    //文字，如果是艾特，QQ在文字的后面
                    byte[] b6=Protobuf.analy(b5,new int[]{10,10});
                    if(!Arrays.equals(b6,new byte[]{0})) {
                        byte[] b7=Protobuf.analy(b5,new int[]{10,26});
                        if(!Arrays.equals(b7,new byte[]{0}) && b7.length==13){
                            sb.append("[At:"+Converter.byteArray2Long(ByteUtil.subBytes(b7,7,4))+"]");
                        }else
                            sb.append(new String(b6));
                    }else{
                        //face表情
                        b6=Protobuf.analy(b5,new int[]{18,8});
                        if(!Arrays.equals(b6,new byte[]{0}))
                            sb.append("[face:"+Protobuf.unSerialize(Converter.byteArray2Long(b6))+"]");
                        else{
                            //图片
                            b6=Protobuf.analy(b5,new int[]{66,106});
                            if(!Arrays.equals(b6,new byte[]{0}) && b6.length==16)
                                sb.append("[img:guid=\""+Converter.byteArray2HexStr(b6).replace(" ","")+".jpg\"]");
                            else{
                                b6=Protobuf.analy(b5,new int[]{34,10});
                                if(!Arrays.equals(b6,new byte[]{0})){
                                    if(b6.length==32)
                                        sb.append("[img:guid=\""+new String(b6).replace(" ","")+".jpg\"]");
                                    else if(b6.length==36)
                                        sb.append("[img:guid=\""+new String(b6).replace(" ","")+"\"]");
                                }

                            }
                        }
                    }
                }else
                    break;
            }
            return sb.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static void analy_event(byte[] data,byte[] seq,byte[] flag){
        long gn=-1;
        try {
            //缓存16条消息，seq重复则忽略
            if(mListCacheEvent.size()>=16){
                mListCacheEvent.clear();
            }
            for(int i=0;i<mListCacheEvent.size();i++){
                if(Arrays.equals(mListCacheEvent.get(i),seq)){
                    return;
                }
            }
            mListCacheEvent.add(seq);
            gn = Converter.gid2Gn(Converter.byteArray2Long(ByteUtil.subBytes(data, 0, 4)));
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean groupOpen=SPHelper.readBool("group_open",gn+"",true);
        if(!groupOpen) return;

        long QQ=-1;
        long operator=-1;
        int event_type=-1;
        String mark=null;
        if(Arrays.equals(flag,new byte[]{0,84})){
            //申请进群
            int length=data[23];
            event_type=0;
            QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,29+length,4));
            //operator=Converter.byteArray2Long(ByteUtil.subBytes(data,34+length,4));
            int mark_len=data[33+8];
            mark=new String(ByteUtil.subBytes(data,34+8,mark_len));
        }else if(Arrays.equals(flag,new byte[]{0,33})){
            //有人进群
            event_type=1;
            int length=(int)data[23];
            QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,29+length,4));
            operator=Converter.byteArray2Long(ByteUtil.subBytes(data,34+length,4));
        }else if(Arrays.equals(flag,new byte[]{0,34})){
            int length=(int)data[23];
            QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,29+length,4));
            if(data[41]==2){
                //退群
                event_type=2;
            }else if(data[41]==3){
                //被踢
                event_type=3;
                operator=Converter.byteArray2Long(ByteUtil.subBytes(data,34+length,4));
            }
        }else if(Arrays.equals(flag,new byte[]{0,44})){
            QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,38,4));
            if(data[37]==1){
                //升管
                event_type=4;
            }else if(data[37]==0){
                //降管
                event_type=5;
            }
        }else if(Arrays.equals(flag,new byte[]{2,-33})){//被添加好友
            QQ=Converter.byteArray2Long(ByteUtil.subBytes(data,0,4));
            if(QQ!=APP.getQQ()){
                event_type=6;
            }else
                QQ=-1;
        }else if(Arrays.equals(flag,new byte[]{0,87})){//被拉进群
            event_type=7;
            int length=(int)data[23];
            operator=Converter.byteArray2Long(ByteUtil.subBytes(data,34+length,4));
        }

        if(event_type>-1){
            Intent intent=new Intent();
            intent.setAction("com.pansyqq.receive.groupevent");
            intent.putExtra("event_type",event_type);
            intent.putExtra("gn",gn);
            intent.putExtra("QQ",QQ);
            intent.putExtra("operator",operator);
            intent.putExtra("mark",mark);
            APP.getAppContext().sendBroadcast(intent);
        }
    }

    public static void prevent_withdraw(byte[] data,byte[] seq){
        if(mListCacheWithdraw.size()>=16){
            mListCacheWithdraw.clear();
        }
        for(int i=0;i<mListCacheWithdraw.size();i++){
            if(Arrays.equals(mListCacheWithdraw.get(i),seq)){
                return;
            }
        }
        mListCacheWithdraw.add(seq);
        int length=(int)data[23];
        data=ByteUtil.subBytes(data,24+length+5,data.length-24-length-5);
        long timestamp=Protobuf.get(data,new int[]{90,26,16});
        if(timestamp<Converter.byteArray2Long(UnPackDatagram.getLoginTime()))
            return;
        long gn=Protobuf.get(data,new int[]{32});
        long QQ=Protobuf.get(data,new int[]{90,8});
        long id=Protobuf.get(data,new int[]{90,26,24});

        boolean groupOpen=SPHelper.readBool("group_open",gn+"",true);
        if(!groupOpen) return;

        Iterator iterator=mWithdrawRecord.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Long,String> entry=(Map.Entry<Long, String>) iterator.next();
            if(entry.getKey()==id){
                Intent intent=new Intent();
                intent.setAction("com.pansyqq.receive.qqmessage");
                intent.putExtra("msg_type",5);
                intent.putExtra("gn",gn);
                intent.putExtra("QQ",QQ);
                intent.putExtra("msg",entry.getValue());
                intent.putExtra("withdraw_seq",-1);
                intent.putExtra("withdraw_id",id);
                APP.getAppContext().sendBroadcast(intent);
                break;
            }
        }
    }
}
