package com.pansy.robot.protocol;

import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.Converter;
import java.util.Arrays;

public class Protobuf {
    public static byte[] serialize_b(long l){
        return Converter.long2ByteArrayNoFill(serialize(l));
    }
    public static long serialize(long l){
        String bin=Long.toBinaryString(l);
        StringBuilder sb=new StringBuilder();
        while(bin.equals("")==false){
            String s="0000000"+bin;
            sb.append("1"+s.substring(s.length()-7,s.length()));
            if(bin.length()>=7)
                bin=bin.substring(0,bin.length()-7);
            else
                bin="";
        }
        sb.replace(sb.length()-8,sb.length()-7,"0");
        //Long r=Long.parseLong(sb.toString(),2);
        //自带的二进制转long不能超过19位，只能自己写一个
        long r=Converter.binary2Long(sb.toString());
        return r;
    }
    public static long unSerialize(long l){
        String bin=Long.toBinaryString(l);
        if(bin.length()<8){
            bin="00000000"+bin;
            bin=bin.substring(bin.length()-8,bin.length());
        }
        String temp="";
        while(bin.length()>0){
            temp=bin.substring(1,8)+temp;
            bin=bin.substring(8,bin.length());
        }
        //long r=Long.parseLong(temp,2);
        long r=Converter.binary2Long(temp);
        return r;
    }
    public static boolean hasNext(byte b){
        String bin=Integer.toBinaryString(b & 0xff);
        if(bin.length()<8){
            bin="00000000"+bin;
            bin=bin.substring(bin.length()-8,bin.length());
        }
        return bin.substring(0,1).equals("1");
    }

    //根据标签分析值
    public static byte[] analy(byte[] data,int[] tree){
        for (int i=0;i<tree.length;i++){
            if(ByteUtil.byteIndexOf(data,Converter.int2ByteArrayNoFill(tree[i]))==-1){
                return new byte[]{0};
            }
        }
        byte[] find=Converter.int2ByteArrayNoFill(tree[0]);
        int i=0;
        while(i<data.length){
            int a=data[i] & 0xff;
            byte m[];
            if(hasNext(data[i])){
                //a不变，i+1
                m=ByteUtil.subBytes(data,i,2);
                i++;
            }else
                m=new byte[]{data[i]};
            String b=Converter.long2Binary(a);
            String wire_type=b.substring(5);

            if(wire_type.equals("000")){//varint
                int j=i;
                while (j<data.length){
                    j++;
                    int a1=data[j] & 0xff;
                    String b1=Converter.long2Binary(a1);
                    String f1=b1.substring(0,1);
                    if(f1.equals("0"))//结束标志
                        break;
                }

                if(Arrays.equals(find,m)){
                    byte[] data2=ByteUtil.subBytes(data,i+1,j-i);
                    if(tree.length>1)
                        return analy(data2,ByteUtil.subInts(tree,1,tree.length-1));
                    else
                        return data2;
                }
                i+=(j-i)+1;
                continue;
            }else if(wire_type.equals("010")) {//length_delimited
                int length_delimited=0;
                if(hasNext(data[i+1])){
                    length_delimited=(int)unSerialize(Converter.byteArray2Long(ByteUtil.subBytes(data,i+1,2)));
                    i++;
                }else{
                    length_delimited=data[i+1] & 0xff;
                }

                if(Arrays.equals(find,m)){
                    byte[] data2=ByteUtil.subBytes(data,i+2,length_delimited);
                    if(tree.length>1)
                        return analy(data2,ByteUtil.subInts(tree,1,tree.length-1));
                    else
                        return data2;
                }
                i+=length_delimited+2;
                continue;
            }else if(wire_type.equals("001")){//FIXED64
                i+=65;
                continue;
            }else if(wire_type.equals("101")){//FIXED32
                i+=33;
                continue;
            }else if(wire_type.equals("111")) {//unknow
                i+=33;
                continue;
            }else if(wire_type.equals("110")) {//unknow
                i+=33;
                continue;
            }else{//unknow
                i+=33;
                continue;
            }

        }
        return new byte[]{0};
    }

    public static long get(byte[] data,int[] tree){
        return unSerialize(Converter.byteArray2Long(analy(data,tree)));
    }

    //根据序号分析值
    public static byte[] analyBySeq(byte[] data,int seq){
        int curSeq=0;
        int i=0;
        while(i<data.length){
            int a=data[i] & 0xff;
            if(hasNext(data[i]))
                i++;
            String b=Converter.long2Binary(a);
            String wire_type=b.substring(5);

            if(wire_type.equals("000")){//varint
                int j=i;
                while (j<data.length){
                    j++;
                    int a1=data[j] & 0xff;
                    String b1=Converter.long2Binary(a1);
                    String f1=b1.substring(0,1);
                    if(f1.equals("0"))//结束标志
                        break;
                }
                if(curSeq==seq){
                    byte[] data2=ByteUtil.subBytes(data,i+1,j-i);
                    return data2;
                }else
                    curSeq++;
                i+=(j-i)+1;
                continue;
            }else if(wire_type.equals("010")) {//length_delimited
                int length_delimited=0;
                if(hasNext(data[i+1])){
                    length_delimited=(int)unSerialize(Converter.byteArray2Long(ByteUtil.subBytes(data,i+1,2)));
                    i++;
                }else{
                    length_delimited=data[i+1] & 0xff;
                }

                if(curSeq==seq){
                    byte[] data2=ByteUtil.subBytes(data,i+2,length_delimited);
                    return data2;
                }else
                    curSeq++;
                i+=length_delimited+2;
                continue;
            }else if(wire_type.equals("001")){//FIXED64
                i+=65;
                curSeq++;
                continue;
            }else if(wire_type.equals("101")){//FIXED32
                i+=33;
                curSeq++;
                continue;
            }else if(wire_type.equals("111")) {//unknow
                i+=33;
                curSeq++;
                continue;
            }else if(wire_type.equals("110")) {//unknow
                i+=33;
                curSeq++;
                continue;
            }else{//unknow
                i+=33;
                curSeq++;
                continue;
            }

        }
        return new byte[]{0};
    }

    public static byte[] setVarint(int key,byte[] value){
        String bin=Long.toBinaryString(key);
        bin+="000";
        return new Packet().put(Converter.long2ByteArrayNoFill(Converter.binary2Long(bin))).put(value).get();
    }

    public static byte[] setVarint(int key,long value){
        return setVarint(key,Converter.long2ByteArrayNoFill(value));
    }

    public static byte[] setLength_delimited(int key,byte[] value){
        String bin=Long.toBinaryString(key);
        //还有问题
        if(bin.length()<=4){
            bin+="010";
        }else{
            bin="1"+bin.substring(1)+"010"+"00000001";
        }
        return new Packet().put(Converter.long2ByteArrayNoFill(Converter.binary2Long(bin))).put(serialize_b(value.length)).put(value).get();
    }

    public static byte[] setLength_delimited(int key,long value){
        return setLength_delimited(key,Converter.long2ByteArrayNoFill(value));
    }

}
