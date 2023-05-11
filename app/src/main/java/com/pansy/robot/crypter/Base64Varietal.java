package com.pansy.robot.crypter;

import android.util.Base64;

public class Base64Varietal {
    private final static char[] chars={
            '+',',','=','F','z',
            'E','Y','u','2','p','/','0','X','W','q',
            '-','-', '-','@', '-','-','-',
            'P', '1', 'C', 'd', 'Z', 'L', 'm', '8', 'y', 'J', 'A', 'o', 'Q', '7', 't', 'x', '3', '9', 'l', 'R', 'S', 'B', 'e', 'G', '5', 'H',
            '-','-', '-','-', '-','-',
            'D', '6', 'v', 'I', 'j', 'M', 'w', '.', 'K', 'n', '4', 'i', 'N', 'r', 'b', 'c', 'g', 's', 'U', 'V', 'f', 'T', 'O', 'k', 'h', 'a',
    };

    public static String enc(String str){
        str=Base64.encodeToString(str.getBytes(), Base64.DEFAULT).trim();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<str.length();i++){
            int a=str.charAt(i);
            sb.append(chars[a-43]);
        }
        return sb.toString();
    }
    public static String dec(String str){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<str.length();i++){
            char c=str.charAt(i);
            int index=10;
            for(int j=0;j<chars.length;j++){
                if(chars[j]==c) {
                    index = j;
                    break;
                }
            }
            sb.append((char)(index+43));

        }
        return new String(Base64.decode(sb.toString().getBytes(), Base64.DEFAULT));
    }
}
