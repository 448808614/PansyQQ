package com.pansy.robot.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Converter {
    /**
     * 十六进制文本到字节数组
     * @param data
     * @return
     */
    public static byte[] hexStr2ByteArray(String data){
        if(data.equals("")) return null;
        String[] arr=data.split(" ");
        byte[] buf=new byte[arr.length];
        for(int i=0;i<arr.length;i++){
            buf[i]=hex2Byte(arr[i]);
        }
        return buf;
    }
    private static byte hex2Byte(String hex){
        if(hex.length()==2){
            byte high=(byte)(Character.digit(hex.charAt(0),16));
            byte low=(byte)(Character.digit(hex.charAt(1),16));
            return (byte)(high<<4|low);
            //return getHexInt(hex.charAt(0))*16+getHexInt(hex.charAt(1));
        }
        return 0;
    }
    /**
     * 字节数组到十六进制文本
     */
    public static String byteArray2HexStr(byte[] bytes){
        if(bytes==null)
            return "null";
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<bytes.length;i++){
            int a=(bytes[i]&0xff);
            String str=Integer.toHexString(a);
            if(a<16){
                str="0"+str;
            }
            sb.append(str+" ");
        }
        return sb.toString().toUpperCase().trim();
    }


    public static byte[] intArray2ByteArray(int[] arr){
        byte[] bytes=new byte[arr.length];
        for(int i=0;i<arr.length;i++){
            bytes[i]=(byte)arr[i];
        }
        return bytes;
    }

    /**
     * long转16进制文本，4个以上字节
     * @return
     */
    public static String long2Hex(long n){
        String str=Long.toHexString(n);
        StringBuilder sb=new StringBuilder();
        while(str.length()<8){
            str="0"+str;
        }
        for(int i=0;i<str.length()/2;i++ ){
            sb.append(str,i*2,i*2+2);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }
    /**
     * int转16进制文本，2个字节
     * @return
     */
    public static String int2Hex(long n){
        String str=Long.toHexString(n).toUpperCase();
        StringBuilder sb=new StringBuilder();
        while(str.length()<4){
            str="0"+str;
        }
        for(int i=0;i<2;i++ ){
            sb.append(str,i*2,i*2+2);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }


    /**
     * ip转long
     */
    /*public static long ip2Long(String ip){
        String[] arr=ip.split("\\.");
        long l=0;
        for(int i=0;i<arr.length;i++){
            int n=Integer.parseInt(arr[i]);
            l+=n<<(8*(arr.length-1-i));
        }
        return l;
    }*/

    /**
     * ip转byte数组
     */
    public static byte[] ip2ByteArray(String ip){
        String[] arr=ip.split("\\.");
        byte[] bytes=new byte[arr.length];
        for(int i=0;i<arr.length;i++){
            int n=Integer.parseInt(arr[i]);
            bytes[i]=(byte)n;
        }
        return bytes;
    }

    /**
     * byte数组转ip
     */
    public static String byteArray2IP(byte[] bytes){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<bytes.length;i++){
            if(i==bytes.length-1){
                sb.append((bytes[i]&0xff)+"");
            }else{
                sb.append((bytes[i]&0xff)+".");
            }
        }
        return sb.toString().toUpperCase();
    }

    /**
     * byte数组转long
     * @param
     * @return
     */
    public static long byteArray2Long(byte[] bytes){
        return Long.parseLong(byteArray2HexStr(bytes).replace(" ",""),16);
    }
    public static int byteArray2Int(byte[] bytes){
        return (int)Long.parseLong(byteArray2HexStr(bytes).replace(" ",""),16);
    }


    public static byte[] int2ByteArray(int n){
        //两个字节
        return hexStr2ByteArray(int2Hex(n));
    }
    public static byte[] int2ByteArrayNoFill(int n){
        //两个字节
        byte[] bytes=hexStr2ByteArray(int2Hex(n));
        //清除前面的0字节
        if(bytes[0]==0){
            int index=-1;
            for(int i=0;i<bytes.length;i++){
                if(bytes[i]==0)
                    index=i;
                else
                    break;
            }
            byte[] b=new byte[bytes.length-index-1];
            for(int i=0;i<b.length;i++){
                b[i]=bytes[index+1+i];
            }
            return b;
        }else{
            return bytes;
        }
    }
    public static byte[] long2ByteArray(long l){
        //四个个以上字节
        byte[] bytes= hexStr2ByteArray(long2Hex(l));
        return bytes;
    }
    public static byte[] long2ByteArrayNoFill(long l){
        byte[] bytes= hexStr2ByteArray(long2Hex(l));
        //清除前面的0字节
        if(bytes[0]==0){
            int index=-1;
            for(int i=0;i<bytes.length;i++){
                if(bytes[i]==0)
                    index=i;
                else
                    break;
            }
            byte[] b=new byte[bytes.length-index-1];
            for(int i=0;i<b.length;i++){
                b[i]=bytes[index+1+i];
            }
            return b;
        }else{
            return bytes;
        }
    }

    /**
     * 群号转gid
     * @param gn
     * @return
     */
    public static long gn2Gid(long gn){
        String gnStr=Long.toString(gn);
        int left=Integer.parseInt(gnStr.substring(0,gnStr.length()-6));
        String right="";
        String gidStr="";
        if(left>=1 && left<=10){
            right=gnStr.substring(gnStr.length()-6,gnStr.length());
            gidStr=left+202+right;
        }else if(left>=11 && left<=19){
            right=gnStr.substring(gnStr.length()-6,gnStr.length());
            gidStr=left+469+right;
        }else if(left>=20 && left<=66){
            left=Integer.parseInt((left+"").substring(0,1));
            right=gnStr.substring(gnStr.length()-7,gnStr.length());
            gidStr=left+208+right;
        }else if(left>=67 && left<=156){
            right=gnStr.substring(gnStr.length()-6,gnStr.length());
            gidStr=left+1943+right;
        }else if(left>=157 && left<=209){
            left=Integer.parseInt((left+"").substring(0,2));
            right=gnStr.substring(gnStr.length()-7,gnStr.length());
            gidStr=left+199+right;
        }else if(left>=210 && left<=309){
            left=Integer.parseInt((left+"").substring(0,2));
            right=gnStr.substring(gnStr.length()-7,gnStr.length());
            gidStr=left+389+right;
        }else if(left>=310 && left<=335){
            left=Integer.parseInt((left+"").substring(0,2));
            right=gnStr.substring(gnStr.length()-7,gnStr.length());
            gidStr=left+349+right;
        }else if(left>=336 && left<=386){
            left=Integer.parseInt((left+"").substring(0,3));
            right=gnStr.substring(gnStr.length()-6,gnStr.length());
            gidStr=left+2265+right;
        }else if(left>=387 && left<=500){
            left=Integer.parseInt((left+"").substring(0,2));
            right=gnStr.substring(gnStr.length()-7,gnStr.length());
            gidStr=left+349+right;
        }else if(left>500){
            return gn;
        }
        return Long.parseLong(gidStr);
    }

    /**
     * gid转群号
     * @param gid
     * @return
     */
    public static long gid2Gn(long gid){
        String gidstr=Long.toString(gid);
        String gnStr="";
        if(Integer.parseInt(gidstr.substring(0,3))>500){
            return gid;
        }
        int left=Integer.parseInt(gidstr.substring(0,gidstr.length()-6));
        String right="";
        if(left>=203 && left<=212){
            right=gidstr.substring(gidstr.length()-6,gidstr.length());
            gnStr=left-202+right;
        }else if(left>=480 && left<=488){
            right=gidstr.substring(gidstr.length()-6,gidstr.length());
            gnStr=left-469+right;
        }else if(left>=2100 && left<=2146){
            left=Integer.parseInt((left+"").substring(0,3));
            right=gidstr.substring(gidstr.length()-7,gidstr.length());
            gnStr=left-208+right;
        }else if(left>=2010 && left<=2099){
            right=gidstr.substring(gidstr.length()-6,gidstr.length());
            gnStr=left-1943+right;
        }else if(left>=2147 && left<=2199){
            left=Integer.parseInt((left+"").substring(0,3));
            right=gidstr.substring(gidstr.length()-7,gidstr.length());
            gnStr=left-199+right;
        }else if(left>=2601 && left<=2651){
            left=Integer.parseInt((left+"").substring(0,4));
            right=gidstr.substring(gidstr.length()-6,gidstr.length());
            gnStr=left-2265+right;
        }else if(left>=4100 && left<=4199){
            left=Integer.parseInt((left+"").substring(0,3));
            right=gidstr.substring(gidstr.length()-7,gidstr.length());
            gnStr=left-389+right;
        }else if(left>=3800 && left<=3825){
            left=Integer.parseInt((left+"").substring(0,3));
            right=gidstr.substring(gidstr.length()-7,gidstr.length());
            gnStr=left-349+right;
        }else if(left>=3877 && left<=3990){
            left=Integer.parseInt((left+"").substring(0,3));
            right=gidstr.substring(gidstr.length()-7,gidstr.length());
            gnStr=left-349+right;
        }
        if(gnStr.equals(""))
            return gid;
        else
            return Long.parseLong(gnStr);
    }
    public static String md52Guid(byte[] md5){
        String str=byteArray2HexStr(md5);
        StringBuilder sb=new StringBuilder();
        str=str.replace(" ","");
        sb.append("{");
        sb.append(str,0,8);
        sb.append("-");
        sb.append(str,8,12);
        sb.append("-");
        sb.append(str,12,16);
        sb.append("-");
        sb.append(str, 16, 20);
        sb.append("-");
        sb.append(str,str.length()-12,str.length());
        sb.append("}");
        return sb.toString();
    }

    //二进制转10进制
    public static long binary2Long(String bin){
        long l= 0;
        for (int i=bin.length()-1;i>=0;i--){
            int b=Integer.parseInt(bin.substring(i,i+1));
            long a=b*(long) Math.pow(2,bin.length()-i-1);
            if(bin.length()==64 && i==0 && b==1)//补码
                a=a*-1-1;
            l+=a;
        }
        return l;
    }

    //10进制转二进制，不足八位补0
    public static String long2Binary(long l){
        String s=Long.toBinaryString(l);
        if(s.length()<8){
            int count=8-s.length();
            for (int i=0;i<count;i++)
                s="0"+s;
        }
        return s;

    }



}
