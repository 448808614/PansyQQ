package com.pansy.robot.struct;

//QQ消息类
public class QQMessage {
    public QQMessage(){}
    public QQMessage(long gn,long QQ,String msg,String time,int type){
        this.gn=gn;
        this.QQ=QQ;
        //this.nick=nick;
        this.msg=msg;
        this.time=time;
        this.type=type;
    }
    public long gn;
    public long QQ;
    //public String nick;
    public String msg;
    public String time;
    public int type;//消息类型
}
