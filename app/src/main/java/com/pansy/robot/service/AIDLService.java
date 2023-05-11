package com.pansy.robot.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.pansy.robot.API;
import com.pansy.robot.APP;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.PackDatagram;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.protocol.SendQQMessage;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AIDLService extends Service {
    private static ExecutorService threadPool=Executors.newCachedThreadPool();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private API.Stub mBinder=new API.Stub() {

        @Override
        public void sendGroupMessage(long gn, String msg) throws RemoteException {
            SendQQMessage.sendGroupMessage(gn,msg);
            QQAPI.log_("aidl","群消息发送>>"+msg,0);
            SendService.send_count++;
            SendService.notifyNotification();
        }

        @Override
        public void sendGroupXml(long gn, String xml) throws RemoteException {
            SendQQMessage.sendGroupXml(gn,xml);
            QQAPI.log_("aidl","群xml发送>>"+xml,0);
            SendService.send_count++;
            SendService.notifyNotification();
        }

        @Override
        public void sendGroupJson(long gn, String json) throws RemoteException {
            SendQQMessage.sendGroupJson(gn,json);
            QQAPI.log_("aidl","群json发送>>"+json,0);
            SendService.send_count++;
            SendService.notifyNotification();
        }

        @Override
        public void sendGroupVoice(long gn, byte[] voice, int seconds) throws RemoteException {
            SendQQMessage.sendGroupVoice(gn,voice,seconds);
        }

        @Override
        public void sendGroupVoiceUrl(long gn, String url, int seconds) throws RemoteException {
            SendQQMessage.sendGroupVoice(gn,url,seconds);
        }

        @Override
        public void sendFriendMessage(long QQ, String msg) throws RemoteException {
            SendQQMessage.sendFriendMessage(QQ,msg);
            QQAPI.log_("aidl","好友消息发送>>"+msg,0);
            SendService.send_count++;
            SendService.notifyNotification();
        }

        @Override
        public void sendFriendXml(long QQ, String xml) throws RemoteException {
            SendQQMessage.sendFriendXml(QQ,xml);
            QQAPI.log_("aidl","好友xml发送>>"+xml,0);
            SendService.send_count++;
            SendService.notifyNotification();
        }

        @Override
        public void sendFriendJson(long QQ, String json) throws RemoteException {
            SendQQMessage.sendFriendJson(QQ,json);
            QQAPI.log_("aidl","好友json发送>>"+json,0);
            SendService.send_count++;
            SendService.notifyNotification();
        }

        @Override
        public void withdraw(long gn, long withdraw_seq, long withdraw_id) throws RemoteException {
            PCQQ.withdraw(gn,withdraw_seq,withdraw_id);
        }

        @Override
        public double robEnvelope(final long gn, final byte[] envelope_p1, final byte[] envelope_p2, final byte[] envelope_p3) throws RemoteException {
            Future<Double> future=threadPool.submit(()-> QQAPI.robEnvelope(gn,envelope_p1,envelope_p2,envelope_p3));
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public String shutup(final long gn, final long QQ, final long seconds) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                QQAPI.shutup(gn,QQ,seconds)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String shutupAll(final long gn, final boolean isShutup) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                    QQAPI.shutupAll(gn,isShutup)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String kick(final long gn, final long QQ) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                     QQAPI.kick(gn,QQ)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getNick(final long QQ) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                     QQAPI.getNick(QQ)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getGroupInfo(final long gn) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                QQAPI.getGroupInfo(gn)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getGroupMembers(final long gn) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                     QQAPI.getGroupMembers(gn)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        public String getGroupName(final long gn) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                     QQAPI.getGroupName(gn)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getGroupCard(final long gn, final long QQ) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                     QQAPI.getGroupCard(gn,QQ)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String setGroupCard(final long gn, final long QQ, final String card) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                    QQAPI.setGroupCard(gn,QQ,card)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getFriendList() throws RemoteException {
            Future<String> future=threadPool.submit(()->
                    QQAPI.getFriendList()
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String getGroupList() throws RemoteException {
            Future<String> future=threadPool.submit(()->
                QQAPI.getGroupList()
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public String joinGroupDispose(final long gn, final long QQ, final boolean agree,String refuse_reason) throws RemoteException {
            Future<String> future=threadPool.submit(()->
                    QQAPI.joinGroupDispose(gn,QQ,agree,refuse_reason)
            );
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void agreeFriend(long QQ, boolean isAgree) throws RemoteException {
            PCQQ.agreeFriend(QQ,isAgree);
        }

        @Override
        public long getQQ() throws RemoteException {
            return APP.getQQ();
        }

        @Override
        public String getCookies() throws RemoteException {
            return APP.getCookies();
        }

        @Override
        public String getGtk() throws RemoteException {
            return APP.getGtk();
        }

        @Override
        public void praise(long QQ) throws RemoteException {
            PCQQ.praise(QQ);
        }

        @Override
        public void agreeInviteMe(long gn) throws RemoteException {
            PCQQ.send(PackDatagram.pack_0359_agreeInviteMe(gn));
        }

        @Override
        public void log(String name, String msg) throws RemoteException {
            QQAPI.log_(name,msg,0);
        }

        @Override
        public void setBubble(int bubbleId) throws RemoteException {
            SendQQMessage.bubbleId=bubbleId;
        }

        @Override
        public int getBubble() throws RemoteException {
            return SendQQMessage.bubbleId;
        }

        @Override
        public byte[] getSessionKey() throws RemoteException {
            return APP.getSession_key();
        }

        @Override
        public void sendUdp(byte[] data) throws RemoteException {
            PCQQ.send(data);
        }

        @Override
        public String getEnvelopeDetail(final long gn, final byte[] envelope_p1, final byte[] envelope_p2, final byte[] envelope_p3) throws RemoteException {
            Future<String> future=threadPool.submit(()-> QQAPI.getEnvelopeDetail(gn,envelope_p1,envelope_p2,envelope_p3));
            try {
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        public String getQzoneSkey(){
            return APP.getQzone_skey();
        }

        @Override
        public void sendPrivateMessage(long gn,long QQ, String msg) throws RemoteException {
            QQAPI.log_("aidl","私聊消息发送>>"+msg,0);
            SendQQMessage.sendPrivateMessage_(gn,QQ,msg);
        }
    };
}
