package com.pansy.robot.crypter;
import com.pansy.robot.utils.Converter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5加密类
 */
public class Md5 {
	private static String[] e={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};

	public String a(String hash){
		//String var1=new String(hash);
		//ba(var1,hash);		
		MessageDigest md5=null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		byte[] bytes=md5.digest(hash.getBytes());
		return c(bytes);
	}
	public byte[] b(byte[] bytes){
		MessageDigest md5=null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return md5.digest(bytes);
	}
	public String d(String str){
		return a(str).toUpperCase();
	}
	private static String c(byte[] bytes){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<bytes.length;i++){
			sb.append(a(bytes[i]));
		}
		return sb.toString();
	}
	
	private static String a(byte b){
		//System.out.println(b);
		int i=(int)b;
		if(i<0){
			
			i=i+256;
			//System.out.println(b+","+b2);
		}
		int var1=i/16;
		int var2=i%16;
		StringBuilder sb=new StringBuilder();
		String var3=e[var1];
		sb.append(var3);
		String var4=e[var2];
		sb.append(var4);
		return sb.toString();
		//return null;
	}

}
