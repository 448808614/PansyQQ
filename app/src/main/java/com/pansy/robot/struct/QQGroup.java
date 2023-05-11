package com.pansy.robot.struct;
//QQ群信息类
public class QQGroup {
    public long gn;
    public String name;
    public boolean open;//群是否启用
    public QQGroup(long gn,String name,boolean open){
        this.gn=gn;
        this.name=name;
        this.open=open;
    }
}
