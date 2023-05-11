package com.pansy.robot.protocol;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.pansy.robot.APP;
import com.pansy.robot.crypter.JNI_Official;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.crypter.Tea;
import com.pansy.robot.utils.Converter;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.Gzip;

import java.util.Properties;
import java.util.Random;
import java.net.InetAddress;

/**
 * 消息组包类
 */
public class PackDatagram {
    public static byte[] KEY_0825_KEY;
    public static byte[] KEY_0825_REDIRECTION_KEY;
    public static byte[] KEY_00BA_FIX_KEY;
    public static byte[] KEY_00BA_KEY;
    public static byte[] DEVICE_ID;
    private static byte[] COMPUTERID={101,3,-3,-117,0,0,0,0,0,0,0,0,0,0,0,0};
    public static byte[] COMPUTERID_EX;
    public static byte[] COMPUTERID_EX_MD5;
    private static byte[] _0836_send_encrypt_key;
    private static byte[] _0836_encrypt;
    private static volatile  long agreeInviteMeGn=0;
    private static int longMsgId=1000;
    public static String pcName;

    public static byte[] pack_0825(boolean isRedirection, int login_way){
        if(login_way==2){
            Packet pack=new Packet();
            pack.put(Packet.KEY_0825_DATA1).put(Packet.PUB_NO);
            pack.put(new byte[]{0,0,0,0,0,0,0,0,0,4,0,12,0,0,0,8});
            pack.putZero(8);
            if(isRedirection)
                pack.put(new byte[]{3,9,0,12,0,1});
            else
                pack.put(new byte[]{3,9,0,8,0,1});
            pack.put(Converter.ip2ByteArray(APP.getIp()));
            if(isRedirection)
                pack.put(1).put(getip(PCQQ.select_tencent_server)).put(5).put(new byte[]{1,20,0,29,1,3,0,25});
            else
                pack.put(new byte[]{0,1,1,20,0,29,1,3,0,25});
            pack.put(APP.getEcc().pub_key);
            byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,37}).putRan(2).putZero(4).put(Packet.KEY_FIX_VER_03)
                   .put(KEY_0825_KEY).put(new Tea().encrypt(pack.get(),KEY_0825_KEY)).putTail().get();
            return ret;
        }else{
            Packet pack=new Packet();
            pack.putHead().putVer();
            pack.put(new byte[]{8,37});
            pack.putRan(2);
            pack.putQQ();
            pack.put(Packet.KEY_FIX_VER_03);
            if(isRedirection)
                pack.put(KEY_0825_REDIRECTION_KEY);
            else
                pack.put(KEY_0825_KEY);
            Packet pack2=new Packet();
            pack2.put(Packet.KEY_0825_DATA1).put(Packet.PUB_NO).putQQ();
            if(isRedirection)
                pack2.put(new byte[]{0,1,0,0,3,9,0,12,0,1});
            else
                pack2.put(new byte[]{0,0,0,0,3,9,0,8,0,1});
            pack2.put(Converter.ip2ByteArray(APP.getIp()));
            if(isRedirection)
                pack2.put(1).put(getip(PCQQ.select_tencent_server)).put(1);
            else
                pack2.put(new byte[]{0,4});
            pack2.put(new byte[]{0,54,0,18,0,2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
            pack2.put(new byte[]{1,20,0,29,1,3,0,25});
            pack2.put(APP.getEcc().pub_key);

            if(isRedirection)
                pack.put(new Tea().encrypt(pack2.get(),KEY_0825_REDIRECTION_KEY));
            else
                pack.put(new Tea().encrypt(pack2.get(),KEY_0825_KEY));
            pack.putTail();
            return pack.get();
        }
    }
    private static byte[] getip(String name){
        try{
            InetAddress ip=InetAddress.getByName(name);
            String s=ip.toString();
            s=s.substring(s.indexOf("/")+1,s.length());
            return Converter.ip2ByteArray(s);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new byte[]{111,-95,88,34};
    }

    public static byte[] pack_0836(boolean isSecond,boolean isVerify,byte[] _0063,byte[] token,byte[] tlv_0006,byte[] _00DE_0038_token,byte[] _00DE_0029_tlv,int login_way,byte[] tlv_0303){
        _0836_send_encrypt_key=ByteUtil.getRanByteArray(16);
        byte[] tlv_0015=new Packet().put(new byte[]{0,21,0,48,0,1,1}).put(ByteUtil.getCrc32(COMPUTERID)).put(new byte[]{0,16}).put(COMPUTERID).put(2).put(ByteUtil.getRevCrc32(COMPUTERID_EX)).put(new byte[]{0,16}).put(COMPUTERID_EX).get();
        byte[] official_key=ByteUtil.getRanByteArray(16);
        byte[] official_sig;
        byte[] QQData=new Tea().xencrypt(new Packet().put(new byte[]{0,4,4,4,32,104,0,0}).putQQ().put(Converter.ip2ByteArray(APP.getIp())).get(),Packet.KEY_QQDATA_KEY);
        QQData=new Packet().put(new byte[]{0,50,0,99,62,0,99,2,4,3,6,2,0,4,0,82,-39,0,0,0,0,-87,88,62,109,109,73,-86,-10,-90,-39,51,10,-25,126,54,-124,3,1,0,0,104,32,21,-117,0,0,1,2,0,0,3,0,7,-33,0,10,0,12,0,1,0,4,0,3,0,4,32,92,0}).put(QQData).put(104).get();
        if (login_way==1 && (isSecond==false || (isSecond && isVerify))) {
            byte[] md51=Converter.hexStr2ByteArray(ByteUtil.splitHex(APP.getPwd()));
            byte[] md52=new Md5().b(new Packet().put(md51).putZero(4).putQQ().get());
            tlv_0006 = new Packet().putRan(4).put(new byte[]{0, 2}).putQQ().put(Packet.PUB_NO).put(new byte[]{0, 0, 1})
                    .put(md51).put(UnPackDatagram.getLoginTime()).put(new byte[]{0}).putZero(12).put(UnPackDatagram.getLoginIP()).putZero(8)
                    .put(new byte[]{0, 16}).put(COMPUTERID_EX).put(UnPackDatagram.getTgtgt_key()).get();
            tlv_0006 = new Tea().encrypt(tlv_0006, md52);
        }
        Packet pack=new Packet();
        pack.put(new byte[]{1,18}).put(new byte[]{0,56}).put(UnPackDatagram.get_0825_token());
        pack.put(new byte[]{3,15}).put(Converter.int2ByteArray(pcName.getBytes().length+2)).put(Converter.int2ByteArray(pcName.getBytes().length)).put(pcName.getBytes());
        pack.put(new byte[]{0,5,0,6,0,2}).putQQ();
        if(login_way==1)
            pack.put(new byte[]{0, 6}).put(new byte[]{0, 120}).put(tlv_0006);
        else if(login_way==2)
            pack.put(tlv_0303);
        pack.put(tlv_0015);
        pack.put(new byte[]{0,26}).put(new byte[]{0,64}).put(new Tea().encrypt(tlv_0015,UnPackDatagram.getTgtgt_key()));
        pack.put(Packet.KEY_0825_DATA1).put(Packet.PUB_NO).putQQ().putZero(4);
        pack.put(new byte[]{1,3}).put(new byte[]{0,20}).put(new byte[]{0,1}).put(new byte[]{0,16}).put(new byte[]{114,80,-94,60,-32,64,96,109,101,-83,-59,-63,96,122,6,115});//固定
        //比第一次发包多了个token
        if(isSecond){
            official_sig=token;
            pack.put(QQData);
            pack.put(new byte[]{1,16}).put(new byte[]{0,60}).put(new byte[]{0,1}).put(new byte[]{0,56}).put(token);
            if(_00DE_0038_token!=null && _00DE_0029_tlv!=null)
                pack.put(new byte[]{0,35,0,59,2}).put(_00DE_0038_token).put(_00DE_0029_tlv);
        }else{
            official_sig=ByteUtil.getRanByteArray(56);
            //新增的
            pack.put(new byte[]{5,7,0,17,1,11,-28,-125,-77,115,-57,8,-117,64,-106,-122,109,69,-7,-109,95});
        }
        pack.put(new byte[]{3,18}).put(new byte[]{0,5}).put(new byte[]{1,0,0,0,1});//1
        pack.put(new byte[]{5,8}).put(new byte[]{0,5}).put(new byte[]{1,0,0,0,0});//change
        pack.put(new byte[]{3,19}).put(new byte[]{0,25}).put(1);
        pack.put(new byte[]{1,2}).put(new byte[]{0,16}).put(COMPUTERID_EX_MD5);
        pack.put(new byte[]{0,0,0,16});
        pack.put(new byte[]{1,2}).put(new byte[]{0,98});
        pack.put(new byte[]{0,1}).put(official_key).put(new byte[]{0,56}).put(official_sig);
        byte[] official_data=new JNI_Official().computeOfficial(official_key,official_sig,tlv_0006,APP.getLoginContext());
        byte[] official_crc=ByteUtil.getRevCrc32(official_data);
        pack.put(new byte[]{0,20}).put(official_data).put(official_crc);

        _0836_encrypt=pack.get();
        if(_0063==null){
            if(UnPackDatagram.isIs_0836_63()){
                byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,54}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03).put(new byte[]{0,2,1,3,0,25}).put(APP.getEcc().pub_key).putZero(2).put(new byte[]{0,16}).put(_0836_send_encrypt_key).put(new Tea().encrypt(_0836_encrypt,_0836_send_encrypt_key)).putTail().get();
                return ret;
            }else{
                byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,54}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03).put(new byte[]{0,2,1,3,0,25}).put(APP.getEcc().pub_key).putZero(2).put(new byte[]{0,16}).put(_0836_send_encrypt_key).put(new Tea().encrypt(_0836_encrypt,APP.getEcc().sha_key)).putTail().get();
                return ret;
            }
        }else{
            byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,54}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03).put(new byte[]{0,2,1,3,0,25}).put(APP.getEcc().pub_key).put(new byte[]{0,32}).put(_0063).put(new byte[]{0,16}).put(_0836_send_encrypt_key).put(new Tea().encrypt(_0836_encrypt,_0836_send_encrypt_key)).putTail().get();
            return ret;
        }

    }
    public static byte[] pack_0828(byte[] _0x38_token,byte[] _0x88_token,byte[] cipher_key){
        byte[] QQMD5={-36,-92,-115,-23,-123,3,-106,-113,100,-52,-98,89,-59,62,14,50};
        byte[] QQData=new Tea().xencrypt(new Packet().put(new byte[]{0,4,4,4,32,104,0,0}).putQQ().put(Converter.ip2ByteArray(APP.getIp())).get(),Packet.KEY_QQDATA_KEY);
        QQData=new Packet().put(new byte[]{0,99,62,0,99,2,4,3,6,2,0,4,0,82,-39,0,0,0,0,-87,88,62,109,109,73,-86,-10,-90,-39,51,10,-25,126,54,-124,3,1,0,0,104,32,21,-117,0,0,1,2,0,0,3,0,7,-33,0,10,0,12,0,1,0,4,0,3,0,4,32,92,0}).put(QQData).put(new byte[]{104}).get();
        //byte[] deviceID={-92,70,63,-9,-35,-50,-125,-71,34,24,122,-6,-52,45,49,49,-61,-125,53,59,107,-78,-61,-1,77,98,60,53,18,-103,66,83};
        //byte[] deviceID=ByteUtil.getRanByteArray(32);
        byte[] intralIP=Converter.ip2ByteArray("192.168."+(1+new Random().nextInt(253))+"."+(1+new Random().nextInt(253)));
        Packet pack=new Packet();
        pack.put(new byte[]{0,7}).put(new byte[]{0,-120}).put(_0x88_token);
        pack.put(new byte[]{0,12,0,22,0,2,0,0,0,0,0,0,0,0,0,0}).put(Converter.ip2ByteArray(APP.getIp())).put(new byte[]{31,64,0,0,0,0,0,21,0,48,0,1});
        pack.put(1).put(new byte[]{-97,-13,-17,-6}).put(new byte[]{0,16,58,-120,-45,-72,85,-23,-85,-82,116,-73,18,-126,115,-60,-26,-35});
        //pack.put(0).put(new byte[]{19,-94,88,102}).put(new byte[]{0,16,-16,12,101,-95,-40,39,43,-116,-126,-3,-16,68,-89,99,10,-47});
        pack.put(0).put(ByteUtil.getRevCrc32(COMPUTERID_EX)).put(new byte[]{0,16}).put(COMPUTERID_EX);
        pack.put(new byte[]{0,54,0,18,0,2,0,1,0,0,0,10,0,0,0,0,0,0,0,0,0,0}).put(Packet.KEY_0825_DATA1).put(Packet.PUB_NO);
        pack.putQQ().putZero(4);
        pack.put(new byte[]{0,31}).put(new byte[]{0,34}).put(new byte[]{0,1}).put(DEVICE_ID).put(APP.getTlv_0105());
        pack.put(new byte[]{1,11}).put(new byte[]{0,-123}).put(new byte[]{0,2}).put(QQMD5);
        pack.putRan(1).put(new byte[]{16,0,0,0,0,0,0,0,2}).put(QQData);
        pack.put(new byte[]{0,0,0,0,0,45,0,6,0,1}).put(intralIP);
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,40}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new byte[]{0,48,0,58}).put(new byte[]{0,56}).put(_0x38_token).put(new Tea().encrypt(pack.get(),cipher_key)).putTail().get();
        return ret;
    }

    /**
     * 上线包
     * @return
     */
    public static byte[] pack_00EC(){
        byte[] state={10};//在线状态
        Packet pack=new Packet();
        pack.putHead().putVer().put(new byte[]{0,-20}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new Packet().put(new byte[]{1,0}).put(state).put(new byte[]{0,1,0,1,0,4,0,0,0,0}).get(),APP.getSession_key())).putTail();
        return pack.get();
    }

    /**
     * 获取skey和群的p_skey
     * @return
     */
    public static byte[] pack_001D_qun(){
        Packet pack=new Packet();
        return pack.putHead().putVer().put(new byte[]{0,29}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new byte[]{51,0,5,0,8,116,46,113,113,46,99,111,109,0,10,113,117,110,46,113,113,46,99,111,109,0,12,113,122,111,110,101,46,113,113,46,99,111,109,0,12,106,117,98,97,111,46,113,113,46,99,111,109,0,9,107,101,46,113,113,46,99,111,109},APP.getSession_key()))
                .putTail().get();
    }
    /**
     * 获取tanpay的p_skey
     * @return
     */
    public static byte[] pack_001D_tenpay(){
        Packet pack=new Packet();
        return pack.putHead().putVer().put(new byte[]{0,29}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new byte[]{51,0,5,0,12,113,113,119,101,98,46,113,113,46,99,111,109,0,13,109,111,98,105,108,101,46,113,113,46,99,111,109,0,10,116,101,110,112,97,121,46,99,111,109,0,11,100,111,99,115,46,113,113,46,99,111,109,0,11,100,111,99,120,46,113,113,46,99,111,109},APP.getSession_key()))
                .putTail().get();
    }
    /**
     * 获取httpconn_key
     * @return
     */
    public static byte[] pack_001D_httpconn(){
        Packet pack=new Packet();
        return pack.putHead().putVer().put(new byte[]{0,29}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new byte[]{38},APP.getSession_key()))
                .putTail().get();
    }

    public static byte[] pack_00BA(byte[] token,int seq){
        Packet pack=new Packet();
        pack.put(new byte[]{0,2,0,0,8,4,1,-32}).put(Packet.PUB_NO).put(0);
        pack.put(new byte[]{0,56}).put(UnPackDatagram.get_0825_token());
        pack.put(new byte[]{1,3}).put(new byte[]{0,25}).put(APP.getEcc().pub_key);
        pack.put(new byte[]{19,0,5,0,0,0}).put(Converter.int2ByteArray(seq));
        pack.put(new byte[]{0,40}).put(token);
        pack.put(new byte[]{0,16}).put(KEY_00BA_FIX_KEY);
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,-70}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03)
                .put(KEY_00BA_KEY).put(new Tea().encrypt(pack.get(),KEY_00BA_KEY)).putTail().get();
        return ret;
    }

    /**
     * 刷新验证码
     */
    public static byte[] pack_00BA_ref(){
        Packet pack=new Packet();
        pack.putHead().putVer().put(new byte[]{0,-70}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03).put(KEY_00BA_KEY)
                .put(new Tea().encrypt(new Packet().put(new byte[]{0,2,0,0,8,4,1,-32}).put(Packet.PUB_NO)
                        .put(new byte[]{0,0,56}).put(UnPackDatagram.get_0825_token()).put(new byte[]{1,3,0,25}).put(APP.getEcc().pub_key)
                        .put(new byte[]{19,0,5,0,0,0,0,0,0,0}).put(new byte[]{0,16}).put(KEY_00BA_FIX_KEY).get(),KEY_00BA_KEY))
                .putTail();
        return pack.get();
    }
    /**
     * 发送验证码
     */
    public static byte[] pack_00BA_in(byte[] code){
        Packet pack=new Packet();
        pack.putHead().putVer().put(new byte[]{0,-70}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03).put(KEY_00BA_KEY)
                .put(new Tea().encrypt(new Packet().put(new byte[]{0,2,0,0,8,4,1,-32}).put(Packet.PUB_NO)
                        .put(new byte[]{1,0,56}).put(UnPackDatagram.get_0825_token()).put(new byte[]{1,3,0,25}).put(APP.getEcc().pub_key)
                        .put(new byte[]{20,0,5,0,0,0,0,0,4}).put(code)
                        .put(0).put(UnPackDatagram.getVerify_token().length).put(UnPackDatagram.getVerify_token())
                        .put(new byte[]{0,16}).put(KEY_00BA_FIX_KEY).get(),KEY_00BA_KEY))
                .putTail();
        return pack.get();
    }
    public static byte[] pack_00DE(byte[] cipher_key,byte[] token1,byte[] token2){
        Packet pack=new Packet();
        pack.put(new byte[]{0,-90,0,1,0,0,8,4,1,-32});
        pack.put(Packet.PUB_NO);
        pack.put(new byte[]{0,56}).put(UnPackDatagram.get_00BA_token());
        pack.put(token2);
        pack.put(new byte[]{4,0,0,0,1,0,0,0,0,1,0,0,0,16,0,0,0,0,0,0,0,0});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,-34}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_03)
                .put(new byte[]{0,56}).put(token1).put(new Tea().encrypt(pack.get(),cipher_key)).putTail().get();
        return ret;
    }
    /**
     * 心跳包
     */
    public static byte[] pack_0058(){
        Packet pack=new Packet();
        pack.putHead().putVer().put(new byte[]{0,88}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new byte[]{0,1,0,1},APP.getSession_key())).putTail();
        return pack.get();
    }

    /**
     * 好友消息已读
     * @return
     */
    public static byte[] pack_0319(long QQ,long timeStamp){
        Packet pack=new Packet();
        byte[] QQ_pb= Converter.long2ByteArray(Protobuf.serialize(QQ));
        byte[] timeStamp_pb = Converter.long2ByteArray(Protobuf.serialize(timeStamp));
        pack.put(new byte[]{0,0,0,7,0,0,0,16,8,1,18,3,-104,1,0,10});
        pack.put(Converter.int2ByteArrayNoFill(QQ_pb.length+timeStamp_pb.length+4));
        pack.put(new byte[]{8}).put(QQ_pb);
        pack.put(new byte[]{16}).put(timeStamp_pb);
        pack.put(new byte[]{32,0});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,25}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }
    /**
     * 群消息已读
     */
    public static byte[] pack_0391(long gn){
        Packet pack=new Packet();
        pack.put(new byte[]{0,0,0,13,0,0,0,20,8,1,18,9,16,-111,7,-120,1,4,-104,1,0,10});
        byte[] gn_pb=Protobuf.serialize_b(gn);
        pack.put(Converter.int2ByteArrayNoFill(gn_pb.length+13)).put(8).put(gn_pb);
        pack.put(new byte[]{18,10,56,0,64,0,74,4,8,0,16,0});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,-111}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }

    /**
     * 群图片
     * @return
     */
    public static byte[] pack_0388_img(long gn,byte[] bin){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bin,0,bin.length);
        if(bitmap!=null){
            long width=bitmap.getWidth();
            long heigth=bitmap.getHeight();
            byte[] md5=new Md5().b(bin);
            Packet pack=new Packet();
            pack.put(8).put(Converter.long2ByteArray(Protobuf.serialize(gn)));
            pack.put(16).put(Converter.long2ByteArray(Protobuf.serialize(APP.getQQ())));
            pack.put(new byte[]{24,0,34,16}).put(md5);
            pack.put(40).put(Protobuf.serialize_b(bin.length));
            pack.put(new byte[]{50,26,55,0,77,0,50,0,37,0,76,0,49,0,86,0,50,0,123,0,57,0,48,0,41,0,82,0});
            pack.put(new byte[]{56,1,72,1});
            pack.put(80).put(Converter.int2ByteArrayNoFill((int)Protobuf.serialize(width)));
            pack.put(88).put(Converter.int2ByteArrayNoFill((int)Protobuf.serialize(heigth)));
            pack.put(new byte[]{96,4,106,5,50,54,54,53,54,112,0,120,3,-128,1,0});

            Packet pack2=new Packet();
            pack2.put(new byte[]{0,0,0,7,0,0}).put(Converter.int2ByteArray(pack.get().length+4));
            pack2.put(new byte[]{8,1,18,3,-104,1,1,16,1,26}).put(Converter.int2ByteArrayNoFill(pack.get().length));
            pack2.put(pack.get());
            byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,-120}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                    .put(new Tea().encrypt(pack2.get(),APP.getSession_key())).putTail().get();
            return ret;
        }else
            return null;
    }
    public static byte[] pack_0388_voice(long gn,byte[] bin){
        byte[] pb_gn=Protobuf.serialize_b(gn);
        byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
        byte[] md5=new Md5().b(bin);
        byte[] pb_size=Protobuf.serialize_b(bin.length);
        Packet pack=new Packet();
        pack.putZero(2).put(new byte[]{0,7}).putZero(3).put(Converter.int2ByteArrayNoFill(105+pb_gn.length+pb_myQQ.length+pb_size.length));
        pack.put(new byte[]{8,1,18,3,-104,1,3,8,1,16,3});
        pack.put(42).put(Converter.long2ByteArrayNoFill(99+pb_gn.length+pb_myQQ.length+pb_size.length));
        pack.put(8).put(pb_gn).put(16).put(pb_myQQ);
        pack.put(new byte[]{24,0,34,16}).put(md5).put(40).put(pb_size);
        pack.put(new byte[]{50,54,89,0,85,0,53,0,40,0,78,0,85,0,74,0,93,0,123,0,54,0,57,0,50,0,54,0,82,0,52,0,70,0,85,0,87,0,78,0,49,0,36,0,53,0,89,0,46,0,97,0,109,0,114,0});
        pack.put(new byte[]{56,1,72,3,82,4,50,0,0,54,88,0,96,3,104,1,112,1,120,2});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,-120}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }
    public static byte[] pack_0002_voice(long gn,byte[] bin,int seconds){
        byte[] gid=Converter.long2ByteArray(Converter.gn2Gid(gn));
        String guid=Converter.md52Guid(new Md5().b(bin))+".amr";
        Packet pack=new Packet();
        pack.put(42).put(gid);
        pack.put(new byte[]{1,80,0,1,1,0,0,0,0,0,0,0,77,83,71,0,0,0,0,0});
        pack.putTime().putRan(4).put(new byte[]{0,0,0,0,9,0,-122,0}).putFont();
        pack.put(new byte[]{10,0,-73,2,0,42}).put(guid.getBytes());
        pack.put(new byte[]{4,0,4,0,0,0,0,5,0,4,0,0,0,0,6,0,4,0,0,0,0,7,0,1,0,8,0,0,9,0,1,1,13,0,8,0,0});
        pack.put(Converter.int2ByteArray(seconds));
        pack.put(new byte[]{0,0,0,1,-1,0,92,22,54,32,57,50,107,65,49,0,32,32,32,32,32,32,32,48,32,32,32,32,32,32,32,48,32,32,32,32,32,32,32,48,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32});
        pack.put(guid.getBytes());
        pack.put(new byte[]{65,1,0,97,1,0,94,-27,-113,-111,-26,-99,-91,-28,-72,-128,-26,-99,-95,-24,-81,-83,-23,-97,-77,-17,-68,-116,-27,-67,-109,-27,-119,-115,-25,-119,-120,-26,-100,-84,-28,-72,-115,-26,-108,-81,-26,-116,-127,-26,-108,-74,-27,-112,-84,-17,-68,-116,-24,-81,-73,-27,-120,-80,32,104,116,116,112,58,47,47,105,109,46,113,113,46,99,111,109,-28,-72,-117,-24,-67,-67,-26,-100,-128,-26,-106,-80,-25,-119,-120,-26,-100,-84,81,81});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }


    public static byte[] pack_0352(long QQ,byte[] bin){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bin,0,bin.length);
        if(bitmap!=null){
            long width=bitmap.getWidth();
            long heigth=bitmap.getHeight();
            byte[] md5=new Md5().b(bin);
            Packet pack=new Packet();
            pack.put(new byte[]{0,0,0,7,0,0,0});
            byte[] pb_size=Protobuf.serialize_b(bin.length);
            byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
            byte[] pb_QQ=Protobuf.serialize_b(QQ);
            pack.put(Converter.int2ByteArrayNoFill(65+pb_size.length+pb_myQQ.length+pb_QQ.length));
            pack.put(new byte[]{8,1,18,3,-104,1,1,8,1,18});
            pack.put(Converter.int2ByteArrayNoFill(61+pb_size.length+pb_myQQ.length+pb_QQ.length));
            pack.put(8).put(pb_myQQ);
            pack.put(16).put(pb_QQ);
            pack.put(new byte[]{24,0,34,16}).put(md5);
            pack.put(40).put(pb_size);
            pack.put(new byte[]{50,26,82,0,55,0,71,0,75,0,82,0,57,0,80,0,88,0,51,0,90,0,80,0,78,0,53,0,56,1,72,0});
            pack.put(112).put(Converter.int2ByteArrayNoFill((int)Protobuf.serialize(width)));
            pack.put(120).put(Converter.int2ByteArrayNoFill((int)Protobuf.serialize(heigth)));
            byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,82}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                    .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
            return ret;
        }else
            return null;
    }
    /**
     * 撤回消息
     */
    public static byte[] pack_03F7(long gn,long withdarw_seq,long withdarw_id) {
        byte[] pb_gn = Protobuf.serialize_b(gn);
        byte[] pb_withdraw_seq = Protobuf.serialize_b(withdarw_seq);
        byte[] pb_withdraw_id = Protobuf.serialize_b(withdarw_id);
        Packet pack = new Packet();
        pack.put(new byte[]{0, 0, 0, 7, 0, 0, 0}).put((22+pb_gn.length+pb_withdraw_id.length+pb_withdraw_seq.length*2));
        pack.put(new byte[]{ 8, 1, 18, 3, -104, 1, 0, 8, 1, 16, 0, 24});
        pack.put(pb_gn);
        pack.put(34).put((pb_withdraw_seq.length + pb_withdraw_id.length + 2));
        pack.put(8).put(pb_withdraw_seq).put(16).put(pb_withdraw_id);
        pack.put(42).put((11 + pb_withdraw_seq.length)).put(new byte[]{8, 0});
        pack.put(18).put((7 + pb_withdraw_seq.length)).put(8).put(pb_withdraw_seq);
        pack.put(new byte[]{16, 0, 24, 1, 32, 0});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,-9}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }
    public static byte[] pack_0818(){
        byte[] encipher_key=ByteUtil.getRanByteArray(16);
        Packet pack=new Packet();
        pack.put(new byte[]{0,25,0,16,0,1}).put(Packet.PUB_NO);
        pack.put(new byte[]{0,0,1,20,0,29,1,3,0,25}).put(APP.getEcc().pub_key);
        pack.put(new byte[]{3,5,0,30,0,0,0,0,0,0,0,5,0,0,0,4,0,0,0,0,0,0,0,72,0,0,0,2,0,0,0,2,0,0,0,21,0,48,0,1,1});
        pack.putRan(4).put(new byte[]{0,16}).putRan(4);
        pack.put(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,2}).putRan(4);
        pack.put(new byte[]{0,16}).putRan(16);
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,24}).putRan(2).putZero(4).put(Packet.KEY_FIX_VER_03).put(encipher_key)
                .put(new Tea().encrypt(pack.get(),encipher_key)).putTail().get();
        return ret;
    }
    public static byte[] pack_0819(byte[] qrcode_k,byte[] token){
        Packet pack=new Packet();
        pack.put(new byte[]{0,25,0,16,0,1}).put(Packet.PUB_NO);
        pack.put(new byte[]{0,0,3,1,0,34,0,32}).put(qrcode_k);
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{8,25}).putRan(2).putZero(4).put(Packet.KEY_FIX_VER_03)
                .put(new byte[]{0,48,0,58,0,56}).put(token).put(new Tea().encrypt(pack.get(),UnPackDatagram._0819_encipher_key)).putTail().get();
        return ret;
    }

    /*public static byte[] pack_01B9_voice(long QQ,byte[] bin){
        String fileName="D:\\QQ\\"+APP.getQQ()+"\\Audio\\D44436CA-F6CF-4C71-8071-D57AF56F04C9.amr";
        byte[] b_fileName=fileName.getBytes();
        Packet pack=new Packet();
        pack.put(1).putQQ().put(Converter.long2ByteArray(QQ));
        pack.put(new byte[]{0,0,0,4,0,0,0,0,0,0,67,-71});
        pack.put(new byte[]{0,32}).put(new byte[]{109,-85,79,-128,-26,21,-123,-86,-25,80,43,-41,102,38,118,-32,-34,-92,-102,-33,-79,-42,-92,-25,-27,30,-120,90,-72,-111,-104,-93});
        pack.put(new byte[]{0,0,42,0,-43});
        pack.put(new byte[]{0,16}).put(new Md5().b(bin));
        pack.put(Converter.int2ByteArray(b_fileName.length)).put(b_fileName);
        pack.put(new byte[]{0,0,0,0,0,11,3,1,-1,1,8,0,4,0,0,0,1});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{1,-71}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }*/
    /**
     * 下线
     */
    public static byte[] pack_0062(){
        Packet pack=new Packet();
        pack.putHead().putVer().put(new byte[]{0,98}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},APP.getSession_key())).putTail();
        return pack.get();
    }
    public static byte[] pack_envelope(long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        byte[] myQQ=Converter.long2ByteArray(Protobuf.serialize(APP.getQQ()));
        byte[] pb_gn=Converter.long2ByteArray(Protobuf.serialize(gn));
        Packet pack1=new Packet();
        pack1.put(10);
        pack1.put((49+myQQ.length));
        pack1.put(8).put(myQQ).put(new byte[]{16,10});
        pack1.put(new byte[]{26,44}).put(APP.getP_skey_tenpay().getBytes());

        Packet pack2=new Packet();
        pack2.put(18).put(102);
        pack2.put(new byte[]{10,11}).putStr(Packet.QQ_VERSION);
        pack2.put(16).put(Packet.PB_VERSION);
        pack2.put(new byte[]{24,5,34,4,49,48,46,48});
        pack2.put(new byte[]{42,21}).put(new byte[]{87,105,110,100,111,119,115,32,49,48,32,69,110,116,101,114,112,114,105,115,101});
        pack2.put(new byte[]{58,32}).put(new byte[]{70,48,48,67,54,53,65,49,68,56,50,55,50,66,56,67,56,50,70,68,70,48,52,52,65,55,54,51,48,65,68,49});
        pack2.put(new byte[]{74,17,56,48,45,70,65,45,53,66,45,52,50,45,50,57,45,48,54}).put(new byte[]{88,0});

        int QQ_length=(APP.getQQ()+"").length();
        Packet pack3=new Packet();
        pack3.put(26).put((109+QQ_length+pb_gn.length));
        pack3.put(new byte[]{10,32}).put(envelope_p2);
        pack3.put(new byte[]{18,32}).put(envelope_p1);
        pack3.put(34).put(QQ_length).put((APP.getQQ()+"").getBytes());
        pack3.put(48).put(pb_gn);
        pack3.put(new byte[]{56,1,74,32}).put(envelope_p3).put(new byte[]{80,0});

        byte[] data1=pack1.get();
        data1=ByteUtil.subBytes(data1,2,data1.length-2);
        byte[] data3=pack3.get();
        data3=ByteUtil.subBytes(data3,2,data3.length-2);
        byte[] sig=computeSig(data1,data3);
        byte[] ret=new Packet().put(pack1.get()).put(pack2.get()).put(pack3.get()).put(new byte[]{-86,6,16}).put(sig).get();
        return ret;
    }

    public static byte[] pack_envelope_detail(long gn,byte[] envelope_p1,byte[] envelope_p2,byte[] envelope_p3){
        byte[] myQQ=Converter.long2ByteArray(Protobuf.serialize(APP.getQQ()));
        byte[] pb_gn=Converter.long2ByteArray(Protobuf.serialize(gn));
        Packet pack1=new Packet();
        pack1.put(10);
        pack1.put((49+myQQ.length));
        pack1.put(8).put(myQQ).put(new byte[]{16,10});
        pack1.put(new byte[]{26,44}).put(APP.getP_skey_tenpay().getBytes());

        Packet pack2=new Packet();
        pack2.put(18).put(102);
        pack2.put(new byte[]{10,11}).putStr(Packet.QQ_VERSION);
        pack2.put(16).put(Packet.PB_VERSION);
        pack2.put(new byte[]{24,5,34,4,49,48,46,48});
        pack2.put(new byte[]{42,21}).put(new byte[]{87,105,110,100,111,119,115,32,49,48,32,69,110,116,101,114,112,114,105,115,101});
        pack2.put(new byte[]{58,32}).put(new byte[]{70,48,48,67,54,53,65,49,68,56,50,55,50,66,56,67,56,50,70,68,70,48,52,52,65,55,54,51,48,65,68,49});
        pack2.put(new byte[]{74,17,56,48,45,70,65,45,53,66,45,52,50,45,50,57,45,48,54}).put(new byte[]{88,0});

        Packet pack3=new Packet();
        pack3.put(26).put((75+pb_gn.length));
        pack3.put(new byte[]{10,32}).put(envelope_p2);
        pack3.put(new byte[]{18,32}).put(envelope_p1);
        pack3.put(48).put(pb_gn);
        pack3.put(new byte[]{56,1,64,0,72,10});

        byte[] data1=pack1.get();
        data1=ByteUtil.subBytes(data1,2,data1.length-2);
        byte[] data3=pack3.get();
        data3=ByteUtil.subBytes(data3,2,data3.length-2);
        byte[] sig=computeSig(data1,data3);
        byte[] ret=new Packet().put(pack1.get()).put(pack2.get()).put(pack3.get()).put(new byte[]{-86,6,16}).put(sig).get();
        return ret;
    }

    private static byte[] computeSig(byte[] data1, byte[]data3){
        byte[] sigdata=new Packet().put(data1).put(data3).put(new byte[]{ 56, 50, 48, 53, 100, 104, 102, 57, 56, 49, 121, 53, 99, 57, 49, 107, 108 }).get();
        byte[] sig1=new Md5().b(sigdata);
        byte[] sig2=new Md5().b(new Packet().put(sig1).put(new byte[]{ 50, 54, 122, 99, 102, 52, 48, 102, 97, 101, 56, 101, 103, 104, 121, 112 }).get());
        return sig2;
    }

    /**
     * 点赞
     * @return
     */
    public static byte[] pack_03E3(long QQ){
        byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
        byte[] pb_targetQQ=Protobuf.serialize_b(QQ);
        Packet pack=new Packet();
        pack.putZero(3).put((pb_myQQ.length+11));
        pack.putZero(3).put((pb_myQQ.length+pb_targetQQ.length+8));
        pack.put(new byte[]{8,1,18}).put((pb_myQQ.length+7));
        pack.put(8).put(pb_myQQ);
        pack.put(new byte[]{16,-29,7,-104,1,0,8,-27,15,16,1});
        pack.put(34).put((pb_targetQQ.length+6));
        pack.put(88).put(pb_targetQQ);
        pack.put(new byte[]{96,-110,78,104,1});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,-29}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }

    //同意被拉进群
    public static byte[] pack_0359_agreeInviteMe(long gn){
        agreeInviteMeGn=gn;
        Packet pack=new Packet();
        pack.put(new byte[]{0,0,0,7,0,0,0,6,8,1,18,3,(byte)152,1,0,8,1,18,2,8,0});
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,89}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }

    //同意被拉进群
    public static byte[] pack_03E3_agreeInviteMe(byte[] timestamp){
        if(agreeInviteMeGn>0){
            byte[] pb_gn=Protobuf.serialize_b(agreeInviteMeGn);
            byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
            Packet pack=new Packet();
            pack.putZero(3).put(16).putZero(3).put(30).put(new byte[]{8,1});
            pack.put(18).put(pb_myQQ.length+7).put(8).put(pb_myQQ);
            pack.put(new byte[]{16,(byte)227,7,(byte)152,1,0});
            pack.put(new byte[]{8,(byte)130,18});
            pack.put(new byte[]{16,0});
            pack.put(34).put(18+pb_gn.length);
            pack.put(new byte[]{8,1});
            pack.put(18).put(14+pb_gn.length);
            pack.put(8).put(timestamp);
            pack.put(24).put(pb_gn);
            pack.put(new byte[]{32,2,72,19});
            byte[] ret=new Packet().putHead().putVer().put(new byte[]{3,-29}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_04)
                    .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
            return ret;
        }
        return null;
    }

    //同意/拒绝好友
    public static byte[] pack_00A8(long QQ,boolean isAgree){
        Packet pack=new Packet();
        String nick=QQAPI.getNick(QQ);
        if(nick==null || nick.equals(""))
            nick="error";
        if(isAgree){
            pack.put(3).put(Converter.long2ByteArray(QQ)).putZero(3);
            int nick_length=nick.getBytes().length;
            pack.put(Converter.int2ByteArray(nick_length+17)).put(new byte[]{0,9,0,0,0,0,0,0})
                    .put(Converter.int2ByteArray(nick_length)).put(nick.getBytes()).put(new byte[]{0,5,0,0,0,0,1});

        }else{
            pack.put(5).put(Converter.long2ByteArray(QQ));
            pack.put(new byte[]{0,0,0,0,17,0,101,0,0,0,0,0,0,0,0,0,5,0,0,0,0,1});
        }
        return new Packet().putHead().putVer().put(new byte[]{0,-88}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02)
                .put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();

    }

    //获取长消息
    public static byte[] pack_httpsGetLongMsg(String m_resid){
        byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
        int r;
        if(pb_myQQ.length>=5)
            r=128+new Random().nextInt(128);
        else
            r=new Random().nextInt(128);
        byte[] ran=Protobuf.serialize_b(r);
        Packet pack=new Packet();
        pack.put(new byte[]{40,0,0,0});

        pack.put(163+pb_myQQ.length+ran.length);
        pack.putZero(2);
        pack.put(new byte[]{0,80});
        pack.put(new byte[]{8,4});
        int len=142+pb_myQQ.length+ran.length;
        pack.put(34);
        pack.put(Protobuf.serialize_b(len));
        pack.put(8).put(pb_myQQ);
        pack.put(new byte[]{16,-1,13});
        pack.put(new byte[]{24,-115,10});
        pack.put(32).put(ran);
        pack.put(new byte[]{40}).put(Packet.PB_VERSION);
        pack.put(new byte[]{56,-127,-126,4});
        pack.put(new byte[]{64,-122,-46,1});
        pack.put(new byte[]{-120,1,1});
        pack.put(new byte[]{-110,1,104}).put(APP.getHttpconn_token());
        pack.put(new byte[]{-104,1,0});
        pack.put(new byte[]{-64,1,0});
        pack.put(new byte[]{-54,1,4,8,0,16,0});
        pack.put(new byte[]{50,14,8,1,18,10}).put(APP.getSkey().getBytes());
        pack.put(new Tea().encrypt(new Packet().put(new byte[]{10,68,10,64}).putStr(m_resid).put(new byte[]{16,3}).get(),APP.getHttpconn_key()));
        pack.put(41);
        return pack_httpconn(pack.get());
    }

    //提交长消息
    public static byte[] pack_httpsPostLongMsg(long gn,String msg){
        byte[] b=Gzip.gzip(pack_httpsPostLongMsg_pb(gn,msg));
        Packet pack3=new Packet();
        pack3.put(new byte[]{8,1,16,1,24,1,34});
        pack3.put(Protobuf.serialize_b(b.length+9));
        pack3.put(new byte[]{8,3,34});
        pack3.put(Protobuf.serialize_b(b.length));
        pack3.put(b);
        pack3.put(new byte[]{40,4,56,0});

        byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
        int r;//发送和接收相反
        if(pb_myQQ.length>=5)
            r=new Random().nextInt(128);
        else
            r=128+new Random().nextInt(128);
        byte[] ran=Protobuf.serialize_b(r);

        Packet pack2=new Packet();
        pack2.put(new byte[]{8,4});
        int len=142+pb_myQQ.length+ran.length;
        pack2.put(34);
        pack2.put(Protobuf.serialize_b(len));
        pack2.put(8).put(pb_myQQ);
        pack2.put(new byte[]{16,-1,13});
        pack2.put(new byte[]{24,-93,12});
        pack2.put(32).put(ran);
        pack2.put(new byte[]{40}).put(Packet.PB_VERSION);
        pack2.put(new byte[]{56,-127,-126,4});
        pack2.put(new byte[]{64,-122,-46,1});
        pack2.put(new byte[]{-120,1,1});
        pack2.put(new byte[]{-110,1,104}).put(APP.getHttpconn_token());
        pack2.put(new byte[]{-104,1,0});
        pack2.put(new byte[]{-64,1,0});
        pack2.put(new byte[]{-54,1,4,8,0,16,0});
        pack2.put(new byte[]{50,14,8,1,18,10}).put(APP.getSkey().getBytes());
        pack2.put(new Tea().encrypt(pack3.get(),APP.getHttpconn_key()));
        pack2.put(41);

        Packet pack=new Packet();
        pack.put(new byte[]{40,0,0,0});

        pack.put(163+pb_myQQ.length+ran.length);
        pack.putZero(2);
        pack.put(Converter.int2ByteArray(pack2.get().length-170));
        pack.put(pack2.get());
        return pack_httpconn(pack.get());
    }

    private static byte[] pack_httpsPostLongMsg_pb(long gn,String msg){
        longMsgId++;
        byte[] pb_gn=Protobuf.serialize_b(gn);
        byte[] pb_myQQ=Protobuf.serialize_b(APP.getQQ());
        String card=QQAPI.getGroupCard(gn,APP.getQQ());
        if(card==null)
            card="null";
        byte[] card_b=card.getBytes();

        byte[] b1_1=Protobuf.setVarint(1,pb_myQQ);
        byte[] b1_2=Protobuf.setVarint(2,pb_gn);
        byte[] b1_3=Protobuf.setVarint(3,82);
        byte[] b1_4=Protobuf.setVarint(5,Protobuf.serialize_b(longMsgId));//累加1
        byte[] b1_5=Protobuf.setVarint(6,Protobuf.serialize_b(System.currentTimeMillis()/1000));
        byte[] b1_6=Protobuf.setVarint(7,Protobuf.serialize_b(1000000000+(long)new Random().nextInt(300000000)*10));//随机10位数
        byte[] b1_7_1=Protobuf.setVarint(1,pb_gn);
        byte[] b1_7_2=Protobuf.setLength_delimited(4,card_b);
        byte[] b1_7=Protobuf.setLength_delimited(9,new Packet().put(b1_7_1).put(b1_7_2).get());
        byte[] b1_8=Protobuf.setLength_delimited(20,new byte[]{16,1});
        byte[] b1=new Packet().put(b1_1).put(b1_2).put(b1_3).put(b1_4).put(b1_5).put(b1_6).put(b1_7).put(b1_8).get();
        b1=Protobuf.setLength_delimited(1,b1);

        byte[] b2_1_1_1_1=Protobuf.setLength_delimited(1,msg.getBytes());
        byte[] b2_1_1_1=Protobuf.setLength_delimited(1,b2_1_1_1_1);
        byte[] b2_1_1=Protobuf.setLength_delimited(2,b2_1_1_1);//消息列表
        byte[] b2_msg=Protobuf.setLength_delimited(1,b2_1_1);
        byte[] b2=Protobuf.setLength_delimited(3,b2_msg);

        byte[] b=new Packet().put(b1).put(b2).get();
        byte[] ret=Protobuf.setLength_delimited(1,b);
        return ret;
    }

    private static byte[] pack_httpconn(byte[] data){
        Packet pack=new Packet();
        pack.putStr("POST /cgi-bin/httpconn HTTP/1.1");
        pack.putEnter();
        pack.putStr("Accept: */*");
        pack.putEnter();
        pack.putStr("User-Agent: "+APP.getUser_agent());
        pack.putEnter();
        pack.putStr("Connection: Keep-Alive");
        pack.putEnter();
        pack.putStr("Cache-Control: no-cache");
        pack.putEnter();
        pack.putStr("Accept-Encoding: gzip, deflate");
        pack.putEnter();
        pack.putStr("Content-Type: application/octet-stream");
        pack.putEnter();
        pack.putStr("Content-Length:"+data.length);
        pack.putEnter();
        pack.putEnter();
        pack.put(data);
        return pack.get();
    }

    public static byte[] get_0836_send_encrypt_key() {
        return _0836_send_encrypt_key;
    }

    public static void set_0836_send_encryptkey(byte[] _0836_send_encrypt_key) {
        PackDatagram._0836_send_encrypt_key = _0836_send_encrypt_key;
    }

    public static byte[] pack_get_private_0x30(long gn,long QQ){
        Packet pack=new Packet();
        pack.put(115).put(Converter.long2ByteArray(Converter.gn2Gid(gn))).put(Converter.long2ByteArray(QQ)).get();
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,2}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02).put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }

    public static byte[] pack_get_private_0x20(long QQ){
        Packet pack=new Packet();
        pack.put(new byte[]{1,66,0}).put(Converter.long2ByteArray(QQ)).get();
        byte[] ret=new Packet().putHead().putVer().put(new byte[]{0,-82}).putRan(2).putQQ().put(Packet.KEY_FIX_VER_02).put(new Tea().encrypt(pack.get(),APP.getSession_key())).putTail().get();
        return ret;
    }
}
