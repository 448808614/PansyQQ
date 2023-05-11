package com.pansy.robot.protocol;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.pansy.robot.APP;
import com.pansy.robot.crypter.AES;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.fragments.FragmentLog;
import com.pansy.robot.receiver.LoadPluginReceiver;
import com.pansy.robot.service.SendService;
import com.pansy.robot.struct.Plugin;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.HttpRequest;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.utils.ByteUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//QQ的接口
public class QQAPI {
    /**
     * 获取群列表
     */
    public static String getGroupList(){
        String url="https://qun.qq.com/cgi-bin/qun_mgr/get_group_list";
        try {
            String str = HttpRequest.post(url, "bkn=" + APP.getGtk(), APP.getCookies());
            return str;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取好友列表
     */
    public static String getFriendList(){
        String url = "https://qun.qq.com/cgi-bin/qun_mgr/get_friend_list";
        try {
            String str = HttpRequest.post(url, "bkn=" + APP.getGtk(), APP.getCookies(), null, null);
            return str;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取QQ昵称
     * @return
     */
    public static String getNick(long QQ){
        String url="https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins="+QQ;
        try{
            String str=HttpRequest.get(url,"gb2312");
            str=str.substring(17,str.length()-1);
            JsonParser parser=new JsonParser();
            JsonObject object=(JsonObject) parser.parse(str);
            str=object.get(Long.toString(QQ)).getAsJsonArray().get(6).getAsString();
            str=ByteUtil.cancelESC(str);
            return str;
        }catch (Exception e){
            return "";
        }
    }
    /**
     * 获取群信息
     */
    public static String getGroupInfo(long gn){
        String url="https://qinfo.clt.qq.com/cgi-bin/qun_info/get_group_info_all?gc="+gn+"&bkn="+APP.getGtk()+"&src=qinfo_v3&_ti="+System.currentTimeMillis();
        try{
            return HttpRequest.get(url,APP.getCookies(),null);
        }catch (Exception e){
            return "";
        }
    }
    /**
     * 获取群成员
     */
    public static String getGroupMembers(long gn){
        String url="https://qinfo.clt.qq.com/cgi-bin/qun_info/get_members_info_v1?friends=1&gc="+gn+"&bkn="+APP.getGtk()+"&src=qinfo_v3&_ti="+System.currentTimeMillis();
        try{
            return HttpRequest.get(url,APP.getCookies(),null);
        }catch (Exception e){
            return "";
        }
    }
    /**
     * 获取群名
     */
    public static String getGroupName(long gn){
        try{
            String str=getGroupInfo(gn);
            JsonParser parser=new JsonParser();
            String groupName=((JsonObject)parser.parse(str)).get("gName").getAsString();
            return ByteUtil.cancelESC(groupName);
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 获取群名片
     * @return
     */
    public static String getGroupCard(long gn,long QQ){
        String url="https://qinfo.clt.qq.com/cgi-bin/mem_card/get_group_mem_card?gc=" + gn + "&bkn=" + APP.getGtk() + "&u=" +QQ;
        String card = "";
        try {
            String str = HttpRequest.get(url, APP.getCookies(),null);
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(str);
            if (object.has("name")) {
                card = object.get("name").getAsString();
                card = ByteUtil.cancelESC(card);
            } else {
                card = getNick(QQ);
            }
            return card;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置群名片
     * @param gn
     * @param QQ
     * @param card
     * @return
     */
    public static String setGroupCard(long gn,long QQ,String card){
        String url="https://qun.qq.com/cgi-bin/qun_mgr/set_group_card";
        try{
            String param="gc=" +gn +"&bkn="+APP.getGtk() + "&name=" +URLEncoder.encode(card,"utf-8")+"&u=" + QQ;
            String str=HttpRequest.post(url,param,APP.getCookies());
            return str;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取QQ头像
     * @param QQ
     * @return
     */
    public static Bitmap getHead(long QQ){
        String url="http://q4.qlogo.cn/g?b=qq&nk="+QQ+"&s=100";
        try {
            Bitmap bitmap= BitmapFactory.decodeStream(new URL(url).openStream());
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取群头像
     */
    public static Bitmap getGroupHead(long gn){
        String url="http://p.qlogo.cn/gh/"+gn+"/"+gn+"/140";
        try {
            Bitmap bitmap= BitmapFactory.decodeStream(new URL(url).openStream());
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取好友个性签名
     */
    public static String getSign(long QQ){
        String str=getQQInfo(QQ);
        JsonParser parser=new JsonParser();
        try {
            JsonObject object = (JsonObject) parser.parse(str);
            if(object.get("result").getAsJsonObject().has("buddy")){
                JsonArray info_list=object.get("result").getAsJsonObject().get("buddy").getAsJsonObject().get("info_list").getAsJsonArray();
                if(info_list.size()>0)
                    return info_list.get(0).getAsJsonObject().get("lnick").getAsString();
                else return "";
            }else
                return "";
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取QQ详细信息
     * @param QQ
     * @return
     */
    public static String getQQInfo(long QQ){
        return null;
    }


    /**
     * 禁言
     */
    public static String shutup(long gn,long QQ,long seconds){
      String url="https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_shutup";
      String param="gc="+ gn +"&shutup_list="+ URLEncoder.encode("[{\"uin\":"+QQ+",\"t\":"+seconds+"}]") +"&bkn="+ APP.getGtk() +"&src=qinfo_v2";
      HashMap<String,String> map=new HashMap<>();
      map.put("Referer","http://qinfo.clt.qq.com/qinfo_v2/index.html");
      try {
          String str = HttpRequest.post(url, param, APP.getCookies(), map);
          return str;
      }catch (Exception e){
          return null;
      }
    }
    /**
     * 全体禁言
     */
    public static String shutupAll(long gn,boolean isShutup){
        String url="https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_shutup";
        String param="";
        if(isShutup)
            param="src=qinfo_v3&gc="+gn+"&bkn=" + APP.getGtk()+ "&all_shutup=4294967295";
        else
            param="src=qinfo_v3&gc="+gn+"&bkn=" + APP.getGtk()+ "&all_shutup=0";
        try {
            String str = HttpRequest.post(url, param, APP.getCookies());
            return str;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 踢人
     */
    public static String kick(long gn,long QQ) {
        PCQQ.kick(gn,QQ);
        return "success";
    }

    /**
     * 进群处理
     * @param gn
     * @param QQ
     * @param is_agree true同意，false拒绝
     * @param refuse_reason 拒绝理由
     * @return
     */
    public static String joinGroupDispose(long gn,long QQ,boolean is_agree,String refuse_reason){
        String url="https://async.qun.qq.com/cgi-bin/sys_msg/getmsg?ver="+Packet.PACKET_VER_INT+"&filter=2";
        try{
            String str=HttpRequest.get(url,APP.getCookies(),null);
            String regex="<dd seq=\"(\\d+)\" type=\"\\d+\" uin=\"\\d+\".*?qid=\"(\\d+)\"[\\s\\S]*? uin=\"(\\d+)\"";
            Pattern p=Pattern.compile(regex);
            Matcher m=p.matcher(str);
            String seq="";
            String param="";
            while(m.find()){
                if(Long.valueOf(m.group(2))==gn && Long.valueOf(m.group(3))==QQ){
                    seq=m.group(1);
                    break;
                }
            }
            if(seq.equals("")==false){
                if(is_agree)
                    param="seq="+seq+ "&t=1&gc=" +gn+"&cmd=1&uin="+ APP.getQQ() + "&ver="+Packet.PACKET_VER_INT+"&from=2&bkn=" +APP.getGtk();
                else{
                    param="seq="+seq+ "&t=1&gc=" +gn+"&cmd=2&uin="+ APP.getQQ() + "&msg=&flag=0&ver="+Packet.PACKET_VER_INT+"&from=2&bkn=" +APP.getGtk();
                    if(!TextUtils.isEmpty(refuse_reason))
                        param=param+"&msg="+refuse_reason;
                }
                url="https://async.qun.qq.com/cgi-bin/sys_msg/set_msgstate";
                str=HttpRequest.post(url,param,APP.getCookies());
                return str;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 上传群图片
     */
    public static void uploadGroupImg(long gn,String ukey,byte[] bin){
        String url="http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0071&ver="+Packet.PACKET_VER_INT+"&term=pc&ukey="+ukey+"&filesize=" +bin.length+"&range=0&uin=" +APP.getQQ() +"&groupcode="+gn;
        HashMap<String,String> map=new HashMap<>();
        map.put("User-Agent","QQClient");
        map.put("Content-Length",bin.length+"");
        HttpRequest.post(url,null,null,map,bin);
    }
    /**
     * 上传好友图片
     */
    public static void uploadFriendImg(String ukey,byte[] bin){
        String url="http://htdata2.qq.com/cgi-bin/httpconn?htcmd=0x6ff0070&ver="+Packet.PACKET_VER_INT+"&ukey=" +ukey+"&filesize=" +bin.length+"&range=0&uin="+APP.getQQ();
        HashMap<String,String> map=new HashMap<>();
        map.put("User-Agent","QQClient");
        map.put("Content-Length",bin.length+"");
        HttpRequest.post(url,null,null,map,bin);
    }
    public static void uploadGroupVoice(String ukey,byte[] bin){
        String md5=Converter.byteArray2HexStr(new Md5().b(bin)).replace(" ","");
        String url = "http://grouptalk.c2c.qq.com/?ver="+Packet.PACKET_VER_INT+"&ukey="+ ukey + "&filesize=" +bin.length + "&filekey=&bmd5="+ md5 +"&voice_codec=1";
        HashMap<String,String> map=new HashMap<>();
        map.put("User-Agent","QQClient");
        map.put("Content-Length",bin.length+"");
        HttpRequest.post_voice(url,map,bin);
    }
    public static void uploadFriendVoice(String ukey,String filekey,byte[] bin){
        String md5=Converter.byteArray2HexStr(new Md5().b(bin)).replace(" ","");
        String url="http://grouptalk.c2c.qq.com/?ver="+Packet.PACKET_VER_INT+"&ukey="+ukey+"&filekey="+filekey+"&filesize="+bin.length+"&bmd5="+md5+"&range=1000&voice_codec=1";
        HashMap<String,String> map=new HashMap<>();
        map.put("User-Agent","QQClient");
        map.put("Content-Length",bin.length+"");
        HttpRequest.post(url,null,null,map,bin);
    }
    public static double robEnvelope(long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        if(APP.isCanRobEnvelope()){
            byte[] data=PackDatagram.pack_envelope(gn,envelope_p1,envelope_p2,envelope_p3);
            byte[] param=ByteUtil.getRanByteArray(16);
            byte[] key=new Md5().b(new Packet().put(new byte[]{ 99, 48, 50, 101, 98, 100, 52, 49, 48, 102, 56, 51, 48, 97, 52, 97, 98, 48, 98, 56, 52, 57, 102, 48, 53, 55, 53, 49, 53, 57, 97, 48 }).put(param).get());
            byte[] iv=new Md5().b(new Packet().put(new byte[]{ 54, 98, 51, 50, 102, 48, 56, 55, 102, 56, 101, 98, 49, 99, 53, 98, 99, 101, 49, 53, 55, 48, 102, 99, 50, 98, 52, 97, 50, 55, 57, 55 }).put(param).get());
            data= AES.encrypt(data,key,iv);
            Packet pack=new Packet();
            pack.put(new byte[]{10,16}).put(param);
            pack.put(18).put(Converter.long2ByteArrayNoFill(Protobuf.serialize(data.length))).put(data);

            data=pack.get();
            String url="https://mqq.tenpay.com/cgi-bin/pc-hb/qpay_hb_pc_grab.cgi?msgno="+APP.getQQ()+ByteUtil.getNow();
            HashMap<String,String> map=new HashMap<>();
            //map.put("Host","mqq.tenpay.com");
            map.put("Content-Length",data.length+"");
            byte[] ret=HttpRequest.post_b(url,null,null,map,data);
            param=ByteUtil.subBytes(ret,2,16);
            ret=Protobuf.analy(ret,new int[]{18});
            key=new Md5().b(new Packet().put(new byte[]{ 99, 48, 50, 101, 98, 100, 52, 49, 48, 102, 56, 51, 48, 97, 52, 97, 98, 48, 98, 56, 52, 57, 102, 48, 53, 55, 53, 49, 53, 57, 97, 48 }).put(param).get());
            iv=new Md5().b(new Packet().put(new byte[]{ 54, 98, 51, 50, 102, 48, 56, 55, 102, 56, 101, 98, 49, 99, 53, 98, 99, 101, 49, 53, 55, 48, 102, 99, 50, 98, 52, 97, 50, 55, 57, 55 }).put(param).get());
            ret=AES.decrypt(ret,key,iv);
            long count=Protobuf.get(ret,new int[]{26,26,32});
            return count*0.01;
        }else
            return -1;
    }

    private static byte[] getEnvelopeDetail_probuf(long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        if(APP.isCanRobEnvelope()){
            byte[] data=PackDatagram.pack_envelope_detail(gn,envelope_p1,envelope_p2,envelope_p3);
            byte[] param=ByteUtil.getRanByteArray(16);
            byte[] key=new Md5().b(new Packet().put(new byte[]{ 99, 48, 50, 101, 98, 100, 52, 49, 48, 102, 56, 51, 48, 97, 52, 97, 98, 48, 98, 56, 52, 57, 102, 48, 53, 55, 53, 49, 53, 57, 97, 48 }).put(param).get());
            byte[] iv=new Md5().b(new Packet().put(new byte[]{ 54, 98, 51, 50, 102, 48, 56, 55, 102, 56, 101, 98, 49, 99, 53, 98, 99, 101, 49, 53, 55, 48, 102, 99, 50, 98, 52, 97, 50, 55, 57, 55 }).put(param).get());
            data= AES.encrypt(data,key,iv);
            Packet pack=new Packet();
            pack.put(new byte[]{10,16}).put(param);
            pack.put(18).put(Converter.long2ByteArrayNoFill(Protobuf.serialize(data.length))).put(data);

            data=pack.get();
            String url="https://mqq.tenpay.com/cgi-bin/pc-hb/qpay_hb_pc_detail.cgi?msgno="+APP.getQQ()+ByteUtil.getNow();
            HashMap<String,String> map=new HashMap<>();
            //map.put("Host","mqq.tenpay.com");
            map.put("Content-Length",data.length+"");
            byte[] ret=HttpRequest.post_b(url,null,null,map,data);
            param=ByteUtil.subBytes(ret,2,16);
            ret=Protobuf.analy(ret,new int[]{18});
            key=new Md5().b(new Packet().put(new byte[]{ 99, 48, 50, 101, 98, 100, 52, 49, 48, 102, 56, 51, 48, 97, 52, 97, 98, 48, 98, 56, 52, 57, 102, 48, 53, 55, 53, 49, 53, 57, 97, 48 }).put(param).get());
            iv=new Md5().b(new Packet().put(new byte[]{ 54, 98, 51, 50, 102, 48, 56, 55, 102, 56, 101, 98, 49, 99, 53, 98, 99, 101, 49, 53, 55, 48, 102, 99, 50, 98, 52, 97, 50, 55, 57, 55 }).put(param).get());
            ret=AES.decrypt(ret,key,iv);
            return ret;
        }else
            return null;
    }

    public static void log_(String name,String msg,int type){
        FragmentLog.notifyDataSetChanged(name,msg,type);
    }

    /***********************暴露给插件的方法***********************/
    /**
     * 判断插件是否启用
     * @return
     */
    private static boolean isEnable(String packageName){
        List<Plugin> list=LoadPluginReceiver.listPlugin;
        for(int i=0;i<list.size();i++){
            if(list.get(i).packageName.equals(packageName)) {
                return list.get(i).enable;
            }
        }
        return false;
    }

    /**
     * 根据包名获取插件名
     */
    private static String getPluginNameByPackName(String packName){
        List<Plugin> list=LoadPluginReceiver.listPlugin;
        for(int i=0;i<list.size();i++){
            if(list.get(i).packageName.equals(packName)) {
                return list.get(i).name;
            }
        }
        return packName;
    }
    private static boolean isGroupOpen(long gn){
        return SPHelper.readBool("group_open",gn+"",true);
    }
    public static void sendGroupMessage(String packName,long gn,String msg){
        if(isEnable(packName) && isGroupOpen(gn)) {
            SendQQMessage.sendGroupMessage(gn, msg);
            log(getPluginNameByPackName(packName),"群消息发送>>"+msg);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }
    public static void sendGroupXml(String packName,long gn,String xml){
        if(isEnable(packName) && isGroupOpen(gn)){
            SendQQMessage.sendGroupXml(gn,xml);
            log(getPluginNameByPackName(packName),"群xml发送>>"+xml);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }
    public static void sendGroupJson(String packName,long gn,String json){
        if(isEnable(packName) && isGroupOpen(gn)){
            SendQQMessage.sendGroupJson(gn,json);
            log(getPluginNameByPackName(packName),"群json发送>>"+json);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }

    public static void sendGroupVoice(String packName,long gn,byte[] voice,int seconds){
        if(isEnable(packName) && isGroupOpen(gn)){
            SendQQMessage.sendGroupVoice(gn,voice,seconds);
            String md5=Converter.byteArray2HexStr(new Md5().b(voice)).replace(" ","");
            log(getPluginNameByPackName(packName),"群语音发送>>"+md5+".amr");
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }

    public static void sendGroupVoice(String packName,long gn,String url,int seconds){
        if(isEnable(packName) && isGroupOpen(gn)){
            SendQQMessage.sendGroupVoice(gn,url,seconds);
            log(getPluginNameByPackName(packName),"群语音发送>>"+url);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }

    public static void sendPrivateMessage(String packName,long gn,long QQ,String msg){
        if(isEnable(packName)) {
            SendQQMessage.sendPrivateMessage_(gn,QQ, msg);
            log(getPluginNameByPackName(packName),"私聊消息发送>>"+msg);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }

    public static void sendFriendMessage(String packName,long QQ,String msg){
        if(isEnable(packName)) {
            SendQQMessage.sendFriendMessage(QQ, msg);
            log(getPluginNameByPackName(packName),"好友消息发送>>"+msg);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }
    public static void sendFriendXml(String packName,long QQ,String xml){
        if(isEnable(packName)) {
            SendQQMessage.sendFriendXml(QQ, xml);
            log(getPluginNameByPackName(packName),"好友xml发送>>"+xml);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }
    public static void sendFriendJson(String packName,long QQ,String json){
        if(isEnable(packName)) {
            SendQQMessage.sendFriendJson(QQ, json);
            log(getPluginNameByPackName(packName),"好友json发送>>"+json);
            SendService.send_count++;
            SendService.notifyNotification();
        }
    }
    public static String shutup(String packName,long gn,long QQ,long seconds){
        if(isEnable(packName))
            return shutup(gn, QQ, seconds);
        else
            return null;
    }
    public static String shutupAll(String packName,long gn,boolean isShutup){
        if(isEnable(packName))
            return shutupAll(gn,isShutup);
        else
            return null;
    }
    public static String kick(String packName,long gn,long QQ){
        if(isEnable(packName))
            return kick(gn,QQ);
        else
            return null;
    }
    public static String getGroupInfo(String packName,long gn){
        if(isEnable(packName))
            return getGroupInfo(gn);
        else
            return null;
    }
    public static String getGroupMembers(String packName,long gn){
        if(isEnable(packName))
            return getGroupMembers(gn);
        else
            return null;
    }
    public static String getGroupName(String packName,long gn){
        if(isEnable(packName))
            return getGroupName(gn);
        else
            return null;
    }
    public static String joinGroupDispose(String packName,long gn,long QQ,boolean state,String refuse_reason){
        if(isEnable(packName))
            return joinGroupDispose(gn,QQ,state,refuse_reason);
        else
            return null;
    }
    public static String getNick(String packName,long QQ){
        if(isEnable(packName))
           return getNick(QQ);
        else
            return null;
    }
    public static String getGroupCard(String packName,long gn,long QQ){
        if(isEnable(packName))
            return getGroupCard(gn,QQ);
        else
            return null;
    }
    public static String setGroupCard(String packName,long gn,long QQ,String card){
        if(isEnable(packName))
            return setGroupCard(gn,QQ,card);
        else
            return null;
    }
    public static String getGroupList(String packName){
        if(isEnable(packName))
            return getGroupList();
        else
            return null;
    }
    public static String getFriendList(String packName){
        if(isEnable(packName))
            return getFriendList();
        else
            return null;
    }
    public static long getQQ(String packName){
        if(isEnable(packName))
            return APP.getQQ();
        else
            return -1;
    }
    public static String getCookies(String packName){
        if(isEnable(packName))
            return APP.getCookies();
        else
            return null;
    }
    public static String getGtk(String packName){
        if(isEnable(packName))
            return APP.getGtk();
        else
            return null;
    }
    public static double robEnvelope(String packName,long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        if(isEnable(packName))
            return robEnvelope(gn,envelope_p1,envelope_p2,envelope_p3);
        else
            return -1;
    }

    public static String getEnvelopeDetail(long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        byte data[]=getEnvelopeDetail_probuf(gn,envelope_p1,envelope_p2,envelope_p3);
        JSONArray jsonArr=new JSONArray();
        data=Protobuf.analy(data,new int[]{26});
        for(int i=0;i<100;i++){
            byte b[]=Protobuf.analyBySeq(data,i);
            if(Arrays.equals(b,new byte[]{0}))
                break;
            else{
                try{
                    long QQ=Protobuf.get(b,new int[]{16});
                    double money=Protobuf.get(b,new int[]{32})*0.01;
                    String mark="";
                    String nickName="";
                    byte[] b1=Protobuf.analy(b,new int[]{34});
                    if(!Arrays.equals(b1,new byte[]{0}))
                        mark=new String(b1);
                    b1=Protobuf.analy(b,new int[]{26});
                    if(!Arrays.equals(b1,new byte[]{0}))
                        nickName=new String(b1);
                    long timestamp=Protobuf.get(b,new int[]{40});
                    JSONObject jsonObj=new JSONObject();
                    jsonObj.put("QQ",QQ);
                    if(money==0)
                        jsonObj.put("money","0");
                    else
                        jsonObj.put("money",money+"");
                    if(!mark.equals(""))
                        jsonObj.put("mark",mark);
                    if(!nickName.equals(""))
                        jsonObj.put("nickName",nickName);
                    jsonObj.put("timestamp",timestamp);
                    jsonArr.put(jsonObj);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(jsonArr.length()==0) return "";
        else return jsonArr.toString();
    }

    public static String getEnvelopeDetail(String packName,long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        if(isEnable(packName))
            return getEnvelopeDetail(gn,envelope_p1,envelope_p2,envelope_p3);
        else
            return "";
    }

    public static void praise(String packName,long QQ){
        if(isEnable(packName)){
            PCQQ.praise(QQ);
        }
    }


    /*private static String getQzoneGtk(String skey){
        int hash=5381;
        for(int i=0;i<skey.length();i++){
            hash += (hash <<5 & 0x7FFFFFFF)+(int)skey.charAt(i) & 0x7FFFFFFF;
            hash &= 0x7FFFFFFF;
        }
        return Integer.toString(hash & 0x7FFFFFFF);
    }*/

    public static void withdraw(String packName,long gn,long withdraw_seq,long withdraw_id){
        if(isEnable(packName)){
            PCQQ.withdraw(gn,withdraw_seq,withdraw_id);
        }
    }
    public static void agreeFriend(String packName,long QQ,boolean isAgree){
        if(isEnable(packName)){
            PCQQ.agreeFriend(QQ,isAgree);
        }
    }
    public static void setBubble(String packName,int bubbleId){
        if(isEnable(packName))
            SendQQMessage.bubbleId=bubbleId;
    }
    public static int getBubble(String packName){
        if(isEnable(packName))
            return SendQQMessage.bubbleId;
        else
            return 0;
    }
    public static String getQzonePskey(String packName){
        if(isEnable(packName))
            return APP.getQzone_skey();
        else
            return "";
    }
    public static void agreeInviteMe(String packName,long gn){
        if(isEnable(packName))
            PCQQ.send(PackDatagram.pack_0359_agreeInviteMe(gn));
    }
    public static void log(String packName,String name,String msg){
        if(isEnable(packName)){
            if(FragmentLog.print_plugin)
                log_(name,msg,0);
        }
    }
    private static void log(String name,String msg){
        log_(name,msg,0);
    }
    public static byte[] getSessionKey(String packName){
        if(isEnable(packName))
            return APP.getSession_key();
        else
            return null;
    }
    public static void sendUdp(String packName,byte[] data){
        if(isEnable(packName))
            PCQQ.send(data);
    }

    public static String groupImgGuid2Url(String guid){
        guid=guid.replace("{","").replace("}","").replace("-","").replace(".gif","").replace(".jpg","").replace(".png","").replace(".jepg","").toUpperCase();
        return "http://gchat.qpic.cn/gchatpic_new/0/0-0-"+guid+"/0";
    }

    public static String friendImgGuid2Url(String guid){
        guid=guid.replace("{","").replace("}","").replace("-","").replace(".gif","").replace(".jpg","").replace(".png","").replace(".jepg","").toUpperCase();
        return "http://gchat.qpic.cn/gchatpic_new/0/0-"+APP.getQQ()+"-"+guid+"/0";
    }
}
