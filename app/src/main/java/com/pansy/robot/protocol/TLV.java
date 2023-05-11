package com.pansy.robot.protocol;

import com.pansy.robot.utils.Converter;

public class TLV {
    public static byte[] put_tlv(int tag,byte[] value){
        Packet pack=new Packet();
        pack.put(tag);
        pack.put(Converter.int2ByteArray(value.length));
        pack.put(value);
        return pack.get();
    }
}
