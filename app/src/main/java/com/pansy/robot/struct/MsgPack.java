package com.pansy.robot.struct;

import android.support.annotation.NonNull;

public class MsgPack implements Comparable<MsgPack>{
    public int num;
    public int id;
    public int seq;
    public String msg;
    public MsgPack(int num,int id,int seq,String msg){
        this.num=num;
        this.id=id;
        this.seq=seq;
        this.msg=msg;
    }

    @Override
    public int compareTo(@NonNull MsgPack o) {
        if(this.id==o.id)
            return(this.seq-o.seq);
        return 0;
    }
}
