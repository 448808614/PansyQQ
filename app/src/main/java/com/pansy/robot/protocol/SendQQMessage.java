package com.pansy.robot.protocol;

import android.os.Environment;
import android.text.TextUtils;

import com.pansy.robot.APP;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.crypter.Tea;
import com.pansy.robot.struct.GroupCard;
import com.pansy.robot.struct.Private_0x30;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.utils.ZLib;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SendQQMessage {
    public static int bubbleId=0;
    private static ExecutorService threadPool=Executors.newCachedThreadPool();
    private static List<GroupCard> mListCacheGroupCard=new ArrayList<>();
    public static List<Private_0x30> mListCachePrivate_0X30 =new ArrayList<>();
    public static long mPrivate_gn;
    public static long mPrivate_QQ;
    public static String mPrivate_msg;
    /**
     * 分割总包取出将要发送的包
     * @param total_pack
     * @return
     */
    private static List<byte[]> splitSendPack(byte[] total_pack,boolean isFriend){
        int flag=isFriend?6:3;
        int img_length=isFriend?245:205;
        byte[] total_pack_trans=total_pack;//复制一份副本，留用
        final int PACK_LENGTH=700;//单包最长字节
        //记录非文字消息开始索引
        List<Integer> list_no_text=new ArrayList<>();
        //记录文字消息开始索引
        List<Integer> list_text=new ArrayList<>();
        for(int i=0;i<total_pack.length;i++){
            if(i==total_pack.length-1) break;
            if(total_pack[i]==1){
                int text_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(total_pack,i+4,2));
                int text_length2=(int)Converter.byteArray2Long(ByteUtil.subBytes(total_pack,i+1,2));
                if(text_length2==text_length+3){
                    //文字
                    list_text.add(i);
                    i+=5+text_length;
                    continue;
                }else if(text_length2==text_length+19 && isFriend==false){
                    //艾特
                    list_no_text.add(i);
                    i+=5+text_length+23;
                    continue;
                }
            }else if(total_pack[i]==2){
                //表情
                list_no_text.add(i);
                i+=22;
                continue;
            }else if(total_pack[i]==flag){
                //图片
                list_no_text.add(i);
                i+=img_length;
                continue;
            }
        }
        List<byte[]> list_send=new ArrayList<>();//以分割，将要发送的包
        int c=0;
        int last_index=0;//上一次分割的索引
        int total_index=0;//到上一次为止总分割索引
        int sub=0;//每个包与PACK_LENGTH相差数量的总数
        while(true){
            c++;
            //byte[] pack_single;//单个包要发送的字节
            int index=PACK_LENGTH*c;//分割点,下一个包的开始索引
            if(total_pack.length<PACK_LENGTH){
                setTextLength(total_pack,isFriend);
                if(ByteUtil.subBytes(total_pack,0,1)[0]==1 && ByteUtil.subBytes(total_pack,3,1)[0]==1){
                    int text_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(total_pack,0+4,2));
                    int text_length2=(int)Converter.byteArray2Long(ByteUtil.subBytes(total_pack,0+1,2));
                    if(text_length2==text_length+3){
                        byte[] right=ByteUtil.subBytes(total_pack,6,total_pack.length-6);
                        byte[] b=putTextTL(right,isFriend);
                        //setTextLength(b);
                        list_send.add(b);
                    }else{//艾特
                        if(isFriend==false)
                            list_send.add(total_pack);
                    }
                }else{
                    //只有是文字时才添加
                    if(total_pack[0]==2 || total_pack[0]==flag){
                        //setTextLength(total_pack);
                        list_send.add(total_pack);
                    }else{
                        byte[] b=putTextTL(total_pack,isFriend);
                        //setTextLength(b);
                        list_send.add(b);
                    }
                }
                //list_send.add(total_pack);
                break;
            }
            //寻找前面最近的非文字开始索引
            List<Integer> list_no_text_front=new ArrayList<>();
            for(int i=0;i<list_no_text.size();i++){
                //前面有且长度超过分割点，但是有可能第一个包远小于PACK_LENGTH，第二个包就大于PACK_LENGTH了，
                //所以要减去每个包与PACK_LENGTH相差数量的总数
                if(list_no_text.get(i)<index-sub){
                    int length=0;
                    if(total_pack_trans[list_no_text.get(i)]==1 && isFriend==false){
                        //艾特
                        int text_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(total_pack_trans,total_pack_trans[list_no_text.get(i)]+4,2));
                        length=5+text_length+23;
                    }else if(total_pack_trans[list_no_text.get(i)]==2){
                        //表情
                        length=22;
                    }else if(total_pack_trans[list_no_text.get(i)]==3){
                        //图片
                        length=img_length;
                    }
                    if(list_no_text.get(i)+length>=index-sub || (i<list_no_text.size()-1 && list_no_text.get(i+1)>=index-sub))//防止获取不到
                        list_no_text_front.add(list_no_text.get(i));
                }

            }
            if(list_no_text_front.size()>0) {
                index = list_no_text_front.get(list_no_text_front.size() - 1)-total_index;
            }else {
                //寻找前面最近的文字开始索引
                List<Integer> list_text_front = new ArrayList<>();
                for (int i = 0; i < list_text.size(); i++) {
                    if (list_text.get(i) < index)
                        list_text_front.add(list_text.get(i));

                }
                int start = 0;
                if (list_text_front.size() > 0) {
                    start = list_text_front.get(list_text_front.size() - 1);
                }
                start=start-last_index;
                //确保index不在文字tl中间
                if(index-start<=5){
                    index=start;
                }

                //确保文字完整性
                int lenth=index-(PACK_LENGTH*(c-1));
                byte[] b=ByteUtil.subBytes(total_pack,start,lenth-start);
                //不能把tl传进去，因为l会阻碍判断
                if(b[0]==1){
                    b=ByteUtil.subBytes(b,6,b.length-6);
                    index=getTxtSplitIndex(b)+start+6;
                }else
                    index=getTxtSplitIndex(b)+start;
            }
            //把前面最近的文字开始索引设置为当前索引
            list_text.add(index);
            last_index=index;
            total_index+=index;
            sub+=PACK_LENGTH-index;
            byte[] pack=ByteUtil.subBytes(total_pack,0,index);
            setTextLength(pack,isFriend);
            if(pack[0]==1 && pack[3]==1){
                int text_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(pack,0+4,2));
                int text_length2=(int)Converter.byteArray2Long(ByteUtil.subBytes(pack,0+1,2));
                if(text_length2==text_length+3){
                    byte[] right=ByteUtil.subBytes(pack,6,pack.length-6);
                    byte[] b=putTextTL(right,isFriend);
                    //setTextLength(b);
                    list_send.add(b);
                }else{//艾特
                    if(isFriend==false)
                        list_send.add(pack);
                }
            }else{
                if(pack[0]==2 || pack[0]==flag){
                    list_send.add(pack);
                }else{
                    //只有是文字时才添加
                    byte[] b=putTextTL(pack,isFriend);
                    list_send.add(b);
                }
            }
            byte[] temp=ByteUtil.subBytes(total_pack,index,total_pack.length-index);
            total_pack=new byte[temp.length];
            total_pack=temp;
        }
        return list_send;
    }

    /**
     * 设置文字的length，只需设置最后一个
     * @return
     */
    private static byte[] setTextLength(byte[] bytes,boolean isFriend){
        int flag;
        byte[] flag_b;
        if(isFriend){
            flag=6;
            flag_b=new byte[]{0,-13};
        }else{
            flag=3;
            flag_b=new byte[]{0,-53};
        }
        //倒找
        for(int i=bytes.length-1;i>=0;i--){
            //后面都是文字时才设置
            if(bytes[i]==1 && bytes.length>i+6){
                int text_length=(int)Converter.byteArray2Long(ByteUtil.subBytes(bytes,i+4,2));
                int text_length2=(int)Converter.byteArray2Long(ByteUtil.subBytes(bytes,i+1,2));
                if(text_length2==text_length+3){
                    text_length=bytes.length-i-6;
                    text_length2=bytes.length-i-3;
                    byte[] b1=Converter.int2ByteArray(text_length);
                    byte[] b2=Converter.int2ByteArray(text_length2);
                    bytes[i+1]=b2[0];
                    bytes[i+2]=b2[1];
                    bytes[i+4]=b1[0];
                    bytes[i+5]=b1[1];
                    break;
                }else if(text_length2==text_length+19 && bytes.length>i+6 && isFriend==false){
                    break;
                }

            }else if(bytes[i]==2 && bytes.length>i+6 && Arrays.equals(ByteUtil.subBytes(bytes,i,6),new byte[]{2,0,20,1,0,1})){
                break;
            }else if(bytes[i]==flag && bytes.length>i+3 && Arrays.equals(ByteUtil.subBytes(bytes,i+1,2),flag_b)){
                break;
            }

        }
        return bytes;
    }

    /**
     * 给文字添加tag和length
     * @return
     */
    private static byte[] putTextTL(byte[] bytes,boolean isFriend){
        int flag=isFriend?6:3;
        Packet pack=new Packet();
        int length=bytes.length;
        for(int i=0;i<bytes.length;i++){
            if(bytes[i]==1 || bytes[i]==2 || bytes[i]==flag){
                length=i;
                break;
            }
        }
        /*if(length>包长度)
            length=包长度;*/
        byte[] ret=pack.put(1).put(Converter.int2ByteArray(length+3))
                .put(1).put(Converter.int2ByteArray(length))
                .put(bytes).get();
        return ret;
    }
    /**
     * 确保文字完整性，即文字的完整字节不被分割，返回应该分割的索引
     */
    public static int getTxtSplitIndex(byte[] bytes){
        for(int i=0;i<bytes.length;){
            int n=bytes[i] & 0xff;
            if(n<128){
                //英文或字符，1个字节
                i++;
                if(i>bytes.length){
                    return i;
                }else
                    continue;
            }else{
                if(n==194){
                    //点号，两个字节
                    i+=2;
                    if(i>bytes.length){
                        return i;
                    }else
                        continue;
                }else if(n==240){
                    //emoji,4个字节
                    i+=4;
                    if(i>bytes.length){
                        return i;
                    }else
                        continue;
                }else if(n==226){
                    //emoji,3个字节
                    i+=3;
                    if(i>bytes.length){
                        return i;
                    }else
                        continue;
                }else{
                    //文字或全角字符，3个字节
                    //System.out.println(i+","+Integer.toHexString(n).toUpperCase());
                    i+=3;
                    if(i>bytes.length){
                        return i;
                    }else
                        continue;
                }
            }
        }
        return bytes.length;
    }
    /**
     * 群消息组包
     * @param
     * @return
     */
    public static List<Byte> getGroupMsgPack(List<String> list,long gn) {
        List<Byte> listB=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            String msg=list.get(i);
            if(msg.indexOf("[face:")>-1 && msg.indexOf("]")>-1){
                int id=1;
                try{
                    id=Integer.parseInt(msg.substring(6,msg.indexOf("]")));
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(id>255) id=255;
                Packet pack=new Packet();
                pack.put(new byte[]{2,0,20,1,0,1}).put(Converter.int2ByteArrayNoFill(id));
                pack.put(new byte[]{-1,0,2,20,21});
                pack.put(new byte[]{11,0,8,0,1,0,4,82,-52,-11,-48});
                byte[] b=pack.get();
                for(int j=0;j<b.length;j++){
                    listB.add(b[j]);
                }
            }else if(msg.indexOf("[At:")>-1 && msg.indexOf("]")>-1){
                long targetQQ=1;
                try{
                    targetQQ=Long.parseLong(msg.substring(4,msg.indexOf("]")));
                }catch (Exception e){
                    e.printStackTrace();
                }
                class GetGroupCardThread implements Callable<String> {
                    private long gn;
                    private long QQ;
                    public GetGroupCardThread(long gn,long QQ){
                        this.gn=gn;
                        this.QQ=QQ;
                    }
                    @Override
                    public String call() {
                        String ret= QQAPI.getGroupCard(gn,QQ);
                        return ret;
                    }
                }

                if(targetQQ>0){
                    try {
                        String str=null;
                        //从缓存中获取群名片
                        for (int j=0;j<mListCacheGroupCard.size();j++){
                            GroupCard card=mListCacheGroupCard.get(j);
                            if(card.gn==gn && card.QQ==targetQQ){
                                if(card.card!=null)
                                    str=card.card;
                                break;
                            }
                        }
                        if(str==null){
                            Future<String> future=threadPool.submit(new GetGroupCardThread(gn,targetQQ));
                            str=future.get();
                            mListCacheGroupCard.add(new GroupCard(gn,targetQQ,str));
                        }
                        if(str==null) str="";
                        byte[] card=("@"+str).getBytes();
                        Packet pack=new Packet();
                        pack.put(new byte[]{1}).put(Converter.int2ByteArray(card.length+19));
                        pack.put(new byte[]{1}).put(Converter.int2ByteArray(card.length)).put(card);
                        pack.put(new byte[]{6,0,13,0,1,0,0,0,6,0}).put(Converter.long2ByteArray(targetQQ));
                        pack.put(new byte[]{0,0,1,0,4,1,0,1,32});
                        byte[] b=pack.get();
                        for(int j=0;j<b.length;j++){
                            listB.add(b[j]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(targetQQ==0){//艾特全体
                    Packet pack=new Packet();
                    pack.put(new byte[]{1,0,32,1,0,13});
                    pack.put(new byte[]{64,-27,-123,-88,-28,-67,-109,-26,-120,-112,-27,-111,-104});
                    pack.put(new byte[]{6,0,13,0,1,0,0,0,5,1,0,0,0,0,0,0});
                    byte[] b=pack.get();
                    for(int j=0;j<b.length;j++){
                        listB.add(b[j]);
                    }
                }

            }else if(msg.indexOf("[img:")>-1 && msg.indexOf("]")>-1){
                msg=msg.substring(5,msg.indexOf("]"));
                byte[] bin=null;
                if(msg.startsWith("url=")){
                    String url=msg.substring(5,msg.length()-1);
                    bin=getURLImgByteArray(url);
                }else if(msg.startsWith("path=")){
                    String path=msg.substring(6,msg.length()-1);
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    //得到绝对路径
                    if(!path.startsWith(root)){
                        if(path.startsWith("/"))
                            path=root+path;
                        else
                            path=root+"/"+path;
                    }
                    bin=getDiskImgByteArray(path);
                }else if(msg.startsWith("guid=")){
                    String guid=msg.substring(6,msg.length()-1);
                    String url=QQAPI.groupImgGuid2Url(guid);
                    bin=getURLImgByteArray(url);
                }

                if(bin!=null){
                    UnPackDatagram.setIsRece_0388_img(false);
                    UnPackDatagram.set_0388_img_ukey("");
                    PCQQ.send(PackDatagram.pack_0388_img(gn,bin));
                    //多线程上传图片用户体验更好，但可能会造成线程不安全？
                    threadPool.execute(new Wait0388ImgThread(gn,bin));
                    //try {
                        //future.get();getImgType
                        String guid=Converter.md52Guid(new Md5().b(bin));
                        int type=ByteUtil.getImgType(bin);
                        if(type==1){
                            guid=guid+".jpg";
                        }else if(type==2){
                            guid=guid+".png";
                        }else if(type==3){
                            guid=guid+".gif";
                        }
                        Packet pack=new Packet();
                        pack.put(new byte[]{3,0,-53,2,0,42}).put(guid.getBytes());
                        pack.put(new byte[]{4,0,4,-101,83,-80,8,5,0,4,-39,-118,90,112,6,0,4,0,0,0,80,7,0,1,67,8,0,0,9,0,1,1,11,0,0,20,0,4,17,0,0,0,21,0,4,0,0,2,-68,22,0,4,0,0,2,-68,24,0,4,0,0,125,94,-1,0,92,21,54,32,57,50,107,65,49,67,57,98,53,51,98,48,48,56,100,57,56,97,53,97,55,48});
                        pack.put(new byte[]{32,32,32,32,32,32,53,48,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32});
                        pack.put(guid.getBytes()).put(65);
                        byte[] b=pack.get();
                        for(int j=0;j<b.length;j++){
                            listB.add(b[j]);
                        }
                   // } catch (Exception e) {
                        //e.printStackTrace();
                    //}

                }
            } else{//普通文字
                byte[] bytes=msg.getBytes();
                Packet pack=new Packet();
                pack.put(new byte[]{1}).put(Converter.int2ByteArray(bytes.length+3));
                pack.put(new byte[]{1}).put(Converter.int2ByteArray(bytes.length));
                pack.put(bytes);
                byte[] b=pack.get();
                for(int j=0;j<b.length;j++){
                    listB.add(b[j]);
                }
            }
        }
        return listB;

    }
    static class Wait0388ImgThread implements Runnable{
        private long gn;
        private byte[] bin;
        public Wait0388ImgThread(long gn,byte[] bin){
            this.gn=gn;
            this.bin=bin;
        }
        @Override
        public void run() {
            int i = 0;
            for (; i < 50; i++) {
                try {
                    if (UnPackDatagram.getIsRece_0388_img())
                        break;
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (UnPackDatagram.getIsRece_0388_img()) {
                String ukey = UnPackDatagram.get_0388_img_ukey();
                if (ukey.equals("") == false)
                    QQAPI.uploadGroupImg(gn, ukey, bin);
            }
        }
    }

    static class Wait0352Thread implements Callable<String>{
        private byte[] bin;
        public Wait0352Thread(byte[] bin){
            this.bin=bin;
        }
        @Override
        public String call() {
            System.out.println("0352线程启动");
            int i=0;
            for (; i < 50; i++) {
                try {
                    System.out.println("isRece_0352"+UnPackDatagram.getIsRece_0352());
                    if (UnPackDatagram.getIsRece_0352())
                        break;
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (UnPackDatagram.getIsRece_0352()) {
                String ukey = UnPackDatagram.get_0352_ukey();
                if (ukey.equals("") == false)
                    QQAPI.uploadFriendImg(ukey,bin);
            }
            return "";
        }
    }
    private static byte[] getURLImgByteArray(String url){
        Future<byte[]> future=threadPool.submit(new GetURLByte(url));
        try {
            byte[] bin=future.get();
            return bin;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static byte[] getDiskImgByteArray(String path){
        try{
            FileInputStream inputStream=new FileInputStream(path);
            return ByteUtil.inputStream2ByteArray(inputStream);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    static class GetURLByte implements Callable<byte[]> {
        private String url;
        public GetURLByte(String url){this.url=url;}
        @Override
        public byte[] call(){
            try {
                HttpURLConnection conn=(HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                int responseCode=conn.getResponseCode();
                if(responseCode==302){
                    conn=(HttpURLConnection) conn.getURL().openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                }
                InputStream inputStream=conn.getInputStream();
                return ByteUtil.inputStream2ByteArray(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    /**
     * 消息变量解析
     * @param msg
     * @return
     */
    private static List<String> splitMsg(String msg,boolean isFriend){
        List<String> list=new ArrayList<>();
        for(int i=0;i<msg.length();){
            char ch=msg.charAt(i);
            if(ch=='['){
                if(msg.length()>i+7 && msg.substring(i,i+6).equals("[face:") && msg.indexOf("]",i+1)>-1){
                    int end=msg.indexOf("]",i+1)+1;
                    list.add(msg.substring(i,end));
                    i=end;
                    continue;
                }else if(msg.length()>i+6 && msg.substring(i,i+5).equals("[img:") && msg.indexOf("]",i+1)>-1){
                    int end=msg.indexOf("]",i+1)+1;
                    list.add(msg.substring(i,end));
                    i=end;
                    continue;
                }else if(msg.length()>i+5 && msg.substring(i,i+4).equals("[At:") && msg.indexOf("]",i+1)>-1 && isFriend==false){
                    int end=msg.indexOf("]",i+1)+1;
                    list.add(msg.substring(i,end));
                    i=end;
                    continue;
                }else{
                    //找出最近的下一个变量索引
                    int a=-1,b=-1,c=-1,end=-1;
                    a=msg.indexOf("[face:",i+1);
                    b=msg.indexOf("[img:",i+1);
                    if(isFriend==false) {
                        c = msg.indexOf("[At:", i + 1);
                        end=getMin(new int[]{a,b,c});
                    }
                    else{
                        end=getMin(new int[]{a,b});
                    }
                    if(end>-1){
                        list.add(msg.substring(i, end));
                        i=end;
                        continue;
                    }else{
                        list.add(msg.substring(i,msg.length()));
                        break;
                    }

                }
            }else{
                //找出最近的下一个变量索引
                int a=-1,b=-1,c=-1,end=-1;
                a=msg.indexOf("[face:",i+1);
                b=msg.indexOf("[img:",i+1);
                if(isFriend==false) {
                    c = msg.indexOf("[At:", i + 1);
                    end=getMin(new int[]{a,b,c});
                }
                else{
                    end=getMin(new int[]{a,b});
                }
                if(end>-1){
                    list.add(msg.substring(i, end));
                    i=end;
                    continue;
                }else{
                    list.add(msg.substring(i,msg.length()));
                    break;
                }

            }
        }
        return list;
    }

    public static int getMin(int[] array){
        int min=-1;
        for(int i=0;i<array.length;i++){
            if(array[i]>-1){
                min=array[i];
                break;
            }
        }
        for(int i=0;i<array.length;i++){
            if(array[i]<min && array[i]>-1)
                min=array[i];
        }
        return min;
    }
    /**
     * 发送群消息
     */
    public static void sendGroupMessage(long gn,String msg){
        if(gn<9999 || TextUtils.isEmpty(msg)) return;
        List<String> list=splitMsg(msg,false);
        //开启了https长消息并且消息字节长度超过600
        if(SPHelper.readBool("https_longMsg",false) && msg.getBytes().length>600){
            //过滤其他变量
            StringBuilder sb=new StringBuilder();
            for (int i=0;i<list.size();i++){
                String temp=list.get(i);
                if(!temp.startsWith("[face:") && !temp.startsWith("[At:") && !temp.startsWith("[img:"))
                    sb.append(temp);
            }
            if(APP.getSkey()!=null){
                if(APP.getHttpconn_key()!=null){
                    sendGroupLongMessage_https(gn,sb.toString());
                }else
                    QQAPI.log_("发送长消息异常","httpconnkey为空",1);
            }else
                QQAPI.log_("发送长消息异常","skey为空",1);
            return;
        }

        List<Byte> list_pack=getGroupMsgPack(list,gn);
        if (list_pack==null) return;
        //list转为byte[]
        byte[] total_pack=new byte[list_pack.size()];
        for(int i=0;i<list_pack.size();i++){
            total_pack[i]=list_pack.get(i);
        }
        if(total_pack.length==0)
            return;
        List<byte[]> list_send=splitSendPack(total_pack,false);
        byte[] msg_id=ByteUtil.getRanByteArray(2);
        for(int i=0;i<list_send.size();i++){
            byte[] send=list_send.get(i);
            Packet pack=new Packet();
            pack.put(new byte[]{42}).put(Converter.long2ByteArray(Converter.gn2Gid(gn)));
            if(bubbleId>0)
                pack.put(Converter.int2ByteArray(50+list_send.get(i).length+64));
            else
                pack.put(Converter.int2ByteArray(50+list_send.get(i).length));
            if(ByteUtil.byteIndexOf(send,new byte[]{3,0,-53,2,0,42})>-1)
                pack.put(new byte[]{0,2});//有图片时为{0,2}
            else
                pack.put(new byte[]{0,1});//无图片时为{0,1}
            pack.put(list_send.size());//分片数
            pack.put(i);//分片序号
            if(list_send.size()==1)
                pack.putZero(2);
            else
                pack.put(msg_id);//片id，要相同
            pack.putZero(4).put(new byte[]{77,83,71}).putZero(5).putTime().putRan(4).put(new byte[]{0,0,0,0,9,0,-122,0})
                    .putFont();
            if(bubbleId>0)
                pack.put(new byte[]{14,0,21,1,0,4,0,0,0,0,7,0,4,0,0,0,1,10,0,4,0,0,0,0,25,0,27,1,0,24,-86,2,21,8,1,80,0,96,0,104,0,-120,1,0,-102,1,7,8,6,32,-53,80,120,0,14,0,7,1,0,4,0,0}).put(Converter.int2ByteArray(bubbleId));
            pack.put(send);
            byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                    .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
            PCQQ.send(ret);
        }
    }

    private static void sendGroupLongMessage_https(long gn,String msg){
        try{
            new Thread(()->{
                byte[] b1=PackDatagram.pack_httpsPostLongMsg(gn,msg);
                byte[] b2=TCP.send("htdata3.qq.com",443,b1);
                if(b2==null || b2.length<19){
                    QQAPI.log_("长消息发送异常","服务器返回空",1);
                    return ;
                }
                int index=ByteUtil.byteIndexOf(b2,new byte[]{1,18,10})+13;
                byte[] b3=ByteUtil.subBytes(b2,index,b2.length-index-1);
                byte[] b4=new Tea().decrypt(b3,APP.getHttpconn_key());
                String m_resid=new String(ByteUtil.subBytes(b4,10,64));
                String guid=Converter.md52Guid(new Md5().b(msg.getBytes())).replace("{","").replace("}","");
                sendGroupLongMsgXml(gn,msg.substring(0,100),m_resid,guid);
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendGroupLongMsgXml(long gn,String brief,String m_resid,String guid){
        String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "    <msg brief=\""+brief+"\" m_fileName=\""+guid+"\" m_fileSize=\"0\" action=\"viewMultiMsg\" flag=\"3\" serviceID=\"35\" m_resid=\""+m_resid+"\" > <item layout=\"1\"> <title> "+brief +
                " </title>  <hr></hr> <summary> 查看1条转发消息  </summary> </item><source name=\"聊天记录\"></source> </msg>";
        byte[] msg = ZLib.compress(xml.getBytes());
        int length = msg.length + 11;
        int length2 = msg.length + 1;
        Packet pack = new Packet();
        pack.putTime().putRan(4).put(new byte[]{0, 0, 0, 0, 9, 0, -122, 0}).putFont();
        pack.put(new byte[]{25,0,74,1,0,71,-86,2,68,48,1,58,64});
        pack.put(m_resid.getBytes());
        pack.put(20);
        pack.put(Converter.int2ByteArrayNoFill(length)).put(1).put(Converter.int2ByteArrayNoFill(length2));
        pack.put(1).put(msg).put(new byte[]{2, 0, 4, 0, 0, 0, 35});//77

        Packet pack2 = new Packet();
        pack2.put(42).put(Converter.long2ByteArray(Converter.gn2Gid(gn)));
        pack2.put(Converter.int2ByteArrayNoFill(msg.length + 64+77));
        pack2.put(new byte[]{0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 77, 83, 71, 0, 0, 0, 0, 0});
        pack2.put(pack.get());

        byte[] ret = new Packet().putHead().putVer().put(new byte[]{0, 2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack2.get(), APP.getSession_key())).putTail().get();
        PCQQ.send(ret);

    }

    /**
     * 发送群消息，xml
     */
    public static void sendGroupXml(long gn,String xml){
        byte[] msg = ZLib.compress(xml.getBytes());
        int length = msg.length + 11;
        int length2 = msg.length + 1;
        Packet pack = new Packet();
        pack.putTime().putRan(4).put(new byte[]{0, 0, 0, 0, 9, 0, -122, 0}).putFont();
        pack.put(20);
        pack.put(Converter.int2ByteArrayNoFill(length)).put(1).put(Converter.int2ByteArrayNoFill(length2));
        pack.put(1).put(msg).put(new byte[]{2, 0, 4, 0, 0, 0, 35});//77

        Packet pack2 = new Packet();
        pack2.put(42).put(Converter.long2ByteArray(Converter.gn2Gid(gn)));
        pack2.put(Converter.int2ByteArrayNoFill(msg.length + 64));
        pack2.put(new byte[]{0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 77, 83, 71, 0, 0, 0, 0, 0});
        pack2.put(pack.get());

        byte[] ret = new Packet().putHead().putVer().put(new byte[]{0, 2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack2.get(), APP.getSession_key())).putTail().get();
        PCQQ.send(ret);
    }
    /**
     * 发送群消息，json
     */
    public static void sendGroupJson(long gn,String json){
        try{
            byte[] msg = ZLib.compress(json.getBytes());
            int length = msg.length + 11;
            int length2 = msg.length + 8;
            Packet pack = new Packet();
            pack.putTime().putRan(4).put(new byte[]{0, 0, 0, 0, 10, 0, -122, 0}).putFont();
            pack.put(25);
            pack.put(Converter.int2ByteArray(length)).put(1).put(Converter.int2ByteArray(length2));
            pack.put(new byte[]{-102, 3});
            byte[] pb1 = Converter.long2ByteArrayNoFill(Protobuf.serialize(msg.length + 1));
            byte[] pb2 = Converter.long2ByteArrayNoFill(Protobuf.serialize(msg.length + 2 + pb1.length));
            pack.put(pb2).put(10).put(pb1).put(1);
            pack.put(msg);

            byte[] gid = Converter.long2ByteArray(Converter.gn2Gid(gn));
            Packet pack2 = new Packet();
            pack2.put(42).put(gid).put(Converter.int2ByteArray(msg.length + 64));
            pack2.put(new byte[]{0, 1, 1, 0, 99, 59, 0, 0, 0, 0, 77, 83, 71, 0, 0, 0, 0, 0});
            pack2.put(pack.get());

            byte[] ret = new Packet().putHead().putVer().put(new byte[]{0, 2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02).put(new Tea().encrypt(pack2.get(), APP.getSession_key())).putTail().get();
            PCQQ.send(ret);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendPrivateMessage_(long gn,long QQ,final String msg){
        if(gn<9999 || TextUtils.isEmpty(msg)) return;
        mPrivate_gn=gn;
        mPrivate_QQ=QQ;
        mPrivate_msg=msg;
        byte[] _0x30=null;
        for (int i = 0; i< mListCachePrivate_0X30.size(); i++){
            Private_0x30 p= mListCachePrivate_0X30.get(i);
            if(p.gn==gn && p.QQ==QQ && p._0x30!=null){
                _0x30=p._0x30;
                break;
            }
        }
        if(_0x30==null){
            //获取_0x30
            PCQQ.send(PackDatagram.pack_get_private_0x30(gn,QQ));
        }else{
            //获取_0x20
            PCQQ.send(PackDatagram.pack_get_private_0x20(QQ));
        }
    }
    /**
     * 发送私聊消息
     */
    public static void sendPrivateMessage(byte[] _0x20value,byte[] _0x30value,final long QQ,final String msg){
        if(QQ<9999 || msg.equals("")) return;
        threadPool.execute(()->{
            List<String> list=splitMsg(msg,true);
            List<Byte> list_pack=getFriendMsgPack(list,QQ);
            if(list_pack==null) return;
            //list转为byte[]
            byte[] total_pack=new byte[list_pack.size()];
            for(int i=0;i<list_pack.size();i++){
                total_pack[i]=list_pack.get(i);
            }
            if(total_pack.length==0)
                return;
            List<byte[]> list_send=splitSendPack(total_pack,true);
            byte[] msg_id=ByteUtil.getRanByteArray(2);
            byte[] friend_key=new Md5().b(new Packet().put(Converter.long2ByteArray(QQ)).put(APP.getSession_key()).get());
            for(int i=0;i<list_send.size();i++){
                Packet pack=new Packet();
                pack.putQQ().put(Converter.long2ByteArray(QQ));
                pack.put(new byte[]{0,95,1,2,0,0,0,0,0,0,0,0});
                pack.put(0);
                pack.put(new byte[]{0,32}).put(_0x20value).put(new byte[]{0,48}).put(_0x30value);
                pack.put(Packet.PACKET_VER);
                pack.putQQ().put(Converter.long2ByteArray(QQ));
                pack.put(friend_key);
                pack.put(new byte[]{0,11}).putRan(2).putTime().put(new byte[]{2,64}).putZero(4);
                pack.put(list_send.size());//分片数
                pack.put(i);//分片序号
                if(list_send.size()==1)
                    pack.putZero(2);
                else
                    pack.put(msg_id);
                pack.put(new byte[]{1,77,83,71}).putZero(5).putTime().putRan(4).put(new byte[]{0,0,0,0,10,0,-122,0}).putFont();
                if(bubbleId>0)
                    pack.put(new byte[]{14,0,21,1,0,4,0,0,0,0,7,0,4,0,0,0,1,10,0,4,0,0,0,0,25,0,27,1,0,24,-86,2,21,8,1,80,0,96,0,104,0,-120,1,0,-102,1,7,8,6,32,-53,80,120,0,14,0,7,1,0,4,0,0}).put(Converter.int2ByteArray(bubbleId));
                pack.put(list_send.get(i));
                byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,-30}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                        .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
                PCQQ.send(ret);
            }
        });
    }

    /**
     * 发送好友消息
     */
    public static void sendFriendMessage(final long QQ,final String msg){
        if(QQ<9999 || TextUtils.isEmpty(msg)) return;
        threadPool.execute(()->{
            List<String> list=splitMsg(msg,true);
            List<Byte> list_pack=getFriendMsgPack(list,QQ);
            if(list_pack==null) return;
            //list转为byte[]
            byte[] total_pack=new byte[list_pack.size()];
            for(int i=0;i<list_pack.size();i++){
                total_pack[i]=list_pack.get(i);
            }
            if(total_pack.length==0)
                return;
            List<byte[]> list_send=splitSendPack(total_pack,true);
            byte[] msg_id=ByteUtil.getRanByteArray(2);
            byte[] friend_key=new Md5().b(new Packet().put(Converter.long2ByteArray(QQ)).put(APP.getSession_key()).get());
            for(int i=0;i<list_send.size();i++){
                Packet pack=new Packet();
                pack.putQQ().put(Converter.long2ByteArray(QQ));
                pack.put(new byte[]{0,0,0,8,0,1,0,4,0,0,0,0});
                pack.put(Packet.PACKET_VER);
                pack.putQQ().put(Converter.long2ByteArray(QQ));
                pack.put(friend_key);
                pack.put(new byte[]{0,11}).putRan(2).putTime().put(new byte[]{2,64}).putZero(4);
                pack.put(list_send.size());//分片数
                pack.put(i);//分片序号
                if(list_send.size()==1)
                    pack.putZero(2);
                else
                    pack.put(msg_id);
                pack.put(new byte[]{1,77,83,71}).putZero(5).putTime().putRan(4).put(new byte[]{0,0,0,0,10,0,-122,0}).putFont();
                if(bubbleId>0)
                    pack.put(new byte[]{14,0,21,1,0,4,0,0,0,0,7,0,4,0,0,0,1,10,0,4,0,0,0,0,25,0,27,1,0,24,-86,2,21,8,1,80,0,96,0,104,0,-120,1,0,-102,1,7,8,6,32,-53,80,120,0,14,0,7,1,0,4,0,0}).put(Converter.int2ByteArray(bubbleId));
                pack.put(list_send.get(i));
                byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,-51}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                        .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
                PCQQ.send(ret);
            }
        });
    }

    /**
     *发送好友消息，xml
     */
    public static void sendFriendXml(long QQ,String xml){
        byte[] msg = ZLib.compress(xml.getBytes());
        int length = msg.length + 11;
        int length2 = msg.length + 1;
        Packet pack = new Packet();
        pack.putTime().putRan(4).put(new byte[]{0, 0, 0, 0, 9, 0, -122, 0}).putFont();
        pack.put(20);
        pack.put(Converter.int2ByteArrayNoFill(length)).put(1).put(Converter.int2ByteArrayNoFill(length2));
        pack.put(1).put(msg).put(new byte[]{2, 0, 4, 0, 0, 0, 77});

        byte[] friend_key = new Md5().b(new Packet().put(Converter.long2ByteArray(QQ)).put(APP.getSession_key()).get());
        Packet pack2 = new Packet();
        pack2.putQQ().put(Converter.long2ByteArray(QQ));
        pack2.put(new byte[]{0, 0, 0, 8, 0, 1, 0, 4, 0, 0, 0, 0});
        pack2.put(Packet.PACKET_VER);
        pack2.putQQ().put(Converter.long2ByteArray(QQ));
        pack2.put(friend_key);
        pack2.put(new byte[]{0, 11}).putRan(2).putTime();
        pack2.put(new byte[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 77, 83, 71, 0, 0, 0, 0, 0});
        pack2.put(pack.get());

        byte[] ret = new Packet().putHead().putVer().put(new byte[]{0, -51}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack2.get(), APP.getSession_key())).putTail().get();
        PCQQ.send(ret);
    }
    /**
     * 发送好友消息，json
     */
    public static void sendFriendJson(long QQ,String json){
        try{
            byte[] msg=ZLib.compress(json.getBytes());
            int length=msg.length+11;
            int length2=msg.length+8;
            Packet pack=new Packet();
            pack.putTime().putRan(4).put(new byte[]{0,0,0,0,9,0,-122,0}).putFont();
            pack.put(25);
            pack.put(Converter.int2ByteArray(length)).put(1).put(Converter.int2ByteArray(length2));
            pack.put(new byte[]{-102,3});
            byte[] pb1 = Converter.long2ByteArrayNoFill(Protobuf.serialize(msg.length + 1));
            byte[] pb2 = Converter.long2ByteArrayNoFill(Protobuf.serialize(msg.length + 2 + pb1.length));
            pack.put(pb2).put(10).put(pb1).put(1);
            pack.put(msg);

            byte[] friend_key=new Md5().b(new Packet().put(Converter.long2ByteArray(QQ)).put(APP.getSession_key()).get());
            Packet pack2=new Packet();
            pack2.putQQ().put(Converter.long2ByteArray(QQ));
            pack2.put(new byte[]{0,0,0,8,0,1,0,4,0,0,0,0});
            pack2.put(Packet.PACKET_VER);
            pack2.putQQ().put(Converter.long2ByteArray(QQ));
            pack2.put(friend_key);
            pack2.put(new byte[]{0,11}).putRan(2).putTime();
            pack2.put(new byte[]{0,0,0,0,0,0,1,0,0,0,1,77,83,71,0,0,0,0,0});
            pack2.put(pack.get());

            byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,-51}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                    .put(new Tea().encrypt(pack2.get(),APP.getSession_key())).putTail().get();
            PCQQ.send(ret);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static List<Byte> getFriendMsgPack(List<String> list,long QQ)  {
        final List<Byte> listB=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            String msg=list.get(i);
            if(msg.indexOf("[face:")>-1 && msg.indexOf("]")>-1){
                int id=1;
                try{
                    id=Integer.parseInt(msg.substring(6,msg.indexOf("]")));
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(id>255) id=255;
                Packet pack=new Packet();
                pack.put(new byte[]{2,0,20,1,0,1}).put(Converter.int2ByteArrayNoFill(id));
                pack.put(new byte[]{-1,0,2,20,21});
                pack.put(new byte[]{11,0,8,0,1,0,4,82,-52,-11,-48});
                byte[] b=pack.get();
                for(int j=0;j<b.length;j++){
                    listB.add(b[j]);
                }
            }else if(msg.indexOf("[img:")>-1 && msg.indexOf("]")>-1){
                msg=msg.substring(5,msg.indexOf("]"));
                byte[] bin=null;
                if(msg.startsWith("url=")){
                    String url=msg.substring(5,msg.length()-1);
                    bin=getURLImgByteArray(url);
                }else if(msg.startsWith("path=")){
                    String path=msg.substring(6,msg.length()-1);
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    //得到绝对路径
                    if(!path.startsWith(root)){
                        if(path.startsWith("/"))
                            path=root+path;
                        else
                            path=root+"/"+path;
                    }
                    bin=getDiskImgByteArray(path);
                }else if(msg.startsWith("guid=")){
                    String guid=msg.substring(6,msg.length()-1);
                    String url=QQAPI.friendImgGuid2Url(guid);
                    bin=getURLImgByteArray(url);
                }
                if(bin!=null){
                    UnPackDatagram.set_0352_ukey("");
                    UnPackDatagram.set_0352_key(null);
                    UnPackDatagram.setIsRece_0352(false);
                    PCQQ.send(PackDatagram.pack_0352(QQ,bin));
                    Future<String> future=threadPool.submit(new Wait0352Thread(bin));
                    try{
                        future.get();
                        byte[] _0352_key=UnPackDatagram.get_0352_key();
                        if(_0352_key!=null){
                            String guid=Converter.md52Guid(new Md5().b(bin));
                            guid=guid.replace("{","").replace("}","").replace("-","");
                            int type=ByteUtil.getImgType(bin);
                            byte[] type_b=null;
                            if(type==1){
                                type_b=".jpg".getBytes();
                                guid=guid+".jpg";
                            }else if(type==2){
                                type_b=".png".getBytes();
                                guid=guid+".png";
                            }else if(type==3){
                                type_b=".gif".getBytes();
                                guid=guid+".gif";
                            }
                            Packet pack=new Packet();
                            pack.put(TLV.put_tlv(2,new Packet().putRan(23).put(type_b).get()));
                            pack.put(TLV.put_tlv(3,new Packet().putZero(2).putRan(2).get()));
                            pack.put(TLV.put_tlv(4,_0352_key));
                            pack.put(TLV.put_tlv(20,new byte[]{17,0,0,0}));
                            pack.put(new byte[]{11,0,0});
                            pack.put(TLV.put_tlv(24,_0352_key));
                            pack.put(TLV.put_tlv(25,new byte[]{0,0,2,41}));
                            pack.put(TLV.put_tlv(26,new byte[]{0,0,1,84}));
                            pack.put(TLV.put_tlv(31,new byte[]{0,0,3,-24}));
                            pack.put(TLV.put_tlv(27,ByteUtil.getRanByteArray(16)));
                            pack.put(TLV.put_tlv(255,new Packet().put(new byte[]{22,32,49,49,55,49,48,49,48,54,49,67,66,32,32,32,32,32,51,57,48,55,56,101}).put(guid.getBytes()).put(65+_0352_key.length).put(_0352_key).put(65).get()));
                            Packet pack2=new Packet();
                            pack2.put(TLV.put_tlv(6,pack.get()));
                            byte[] b=pack2.get();
                            for(int j=0;j<b.length;j++){
                                listB.add(b[j]);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            } else{//普通文字
                byte[] bytes=msg.getBytes();
                Packet pack=new Packet();
                pack.put(new byte[]{1}).put(Converter.int2ByteArray(bytes.length+3));
                pack.put(new byte[]{1}).put(Converter.int2ByteArray(bytes.length));
                pack.put(bytes);
                byte[] b=pack.get();
                for(int j=0;j<b.length;j++){
                    listB.add(b[j]);
                }
            }
        }
        return listB;
    }

    /**
     * 发送群语音
     * @param gn
     * @param voice
     */
    public static void sendGroupVoice(long gn,byte[] voice,int seconds){
        if(voice==null)
            return;
        if(seconds<=0)
            seconds=2;
        UnPackDatagram.setIsRece_0388_voice(false);
        UnPackDatagram.set_0388_voice_ukey("");
        PCQQ.send(PackDatagram.pack_0388_voice(gn,voice));
        threadPool.execute(new Wait0388VoiceThread(voice));
        PCQQ.send(PackDatagram.pack_0002_voice(gn,voice,seconds));
    }

    public static void sendGroupVoice(final long gn, final String url,final int seconds){
        new Thread(()->{
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                int responseCode = conn.getResponseCode();
                if (responseCode == 302) {
                    conn = (HttpURLConnection) conn.getURL().openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                }
                InputStream inputStream = conn.getInputStream();
                byte[] voice=ByteUtil.inputStream2ByteArray(inputStream);
                UnPackDatagram.setIsRece_0388_voice(false);
                UnPackDatagram.set_0388_voice_ukey("");
                PCQQ.send(PackDatagram.pack_0388_voice(gn,voice));
                threadPool.execute(new Wait0388VoiceThread(voice));
                PCQQ.send(PackDatagram.pack_0002_voice(gn,voice, seconds));
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    static class Wait0388VoiceThread implements Runnable{
        private byte[] bin;
        public Wait0388VoiceThread(byte[] bin) {
            this.bin=bin;
        }
        @Override
        public void run() {
            int i=0;
            for(;i<50;i++){
                try {
                    if(UnPackDatagram.getIsRece_0388_voice())
                        break;
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(UnPackDatagram.getIsRece_0388_voice()){
                String ukey=UnPackDatagram.get_0388_voice_ukey();
                System.out.println("语音ukey:"+ukey);
                if(ukey.equals("")==false)
                    QQAPI.uploadGroupVoice(ukey,bin);
            }else{
                QQAPI.log_("语音发送失败","获取语音ukey超时",1);
                System.out.println("语音ukey为空");
            }
        }
    }


    /**
     * 发送好友语音
     * @param QQ
     * @param voice
     */
    public static void sendFriendVoice(long QQ,byte[] voice,int seconds){
    }
}
