// API.aidl
package com.pansy.robot;

// Declare any non-default types here with import statements

interface API {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void sendGroupMessage(long gn,String msg);
    void sendGroupXml(long gn,String xml);
    void sendGroupJson(long gn,String json);
    void sendGroupVoice(long gn,in byte[] voice,int seconds);
    void sendGroupVoiceUrl(long gn,String url,int seconds);
    void sendFriendMessage(long QQ,String msg);
    void sendFriendXml(long QQ,String xml);
    void sendFriendJson(long QQ,String json);
    void withdraw(long gn,long withdraw_seq,long withdraw_id);
    double robEnvelope(long gn,in byte[] envelope_p1,in byte[] envelope_p2,in byte[] envelope_p3);
    String shutup(long gn, long QQ, long seconds);
    String shutupAll(long gn,boolean isShutup);
    String kick(long gn,long QQ);
    String getNick(long QQ);
    String getGroupInfo(long gn);
    String getGroupMembers(long gn);
    String getGroupName(long gn);
    String getGroupCard(long gn,long QQ);
    String setGroupCard(long gn,long QQ,String card);
    String getFriendList();
    String getGroupList();
    String joinGroupDispose(long gn,long QQ,boolean agree,String refuse_reason);
    void agreeFriend(long QQ,boolean isAgree);
    long getQQ();
    String getCookies();
    String getGtk();
    void praise(long QQ);
    void agreeInviteMe(long gn);
    void log(String name,String msg);
    void setBubble(int bubbleId);
    int getBubble();
    byte[] getSessionKey();
    void sendUdp(in byte[] data);
    String getEnvelopeDetail(long gn,in byte[] envelope_p1,in byte[] envelope_p2,in byte[] envelope_p3);
    String getQzoneSkey();
    void sendPrivateMessage(long gn,long QQ,String msg);
}
