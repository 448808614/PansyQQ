package com.pansy.robot.struct;

import android.graphics.Bitmap;

public class Plugin {
    public String url;//插件下载的url
    public String packageName;
    public String name;
    public String brief;
    public String author;
    public String version;
    public int download;//下载次数
    public Bitmap icon;
    public boolean enable;
    public boolean hasNewVersion;
    public boolean forceUpdate;
    public boolean is_aidl;
    public Plugin(String url,String packageName,String name,String brief,String author,String version,Bitmap icon,boolean enable){
        this.url=url;
        this.packageName=packageName;
        this.name=name;
        this.brief=brief;
        this.author=author;
        this.version=version;
        this.icon=icon;
        this.enable=enable;
        this.download=0;
        this.hasNewVersion=false;
        this.forceUpdate=false;
        this.is_aidl=false;
    }
}
