package com.pansy.robot.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.Manifest.permission
import android.Manifest.permission.READ_PHONE_STATE
import android.app.Activity
import android.support.v4.app.ActivityCompat
import android.hardware.usb.UsbDevice.getDeviceId
import android.content.pm.PackageManager
import android.graphics.Color
import android.telephony.TelephonyManager
import com.pansy.robot.crypter.Md5


object PhoneUtil{
    fun getManufacturer(): String {
        return Build.MANUFACTURER
    }

    fun getId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getIMEI(context: Context): String {
        try {
            //实例化TelephonyManager对象
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            //获取IMEI号
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                var imei: String?
                if(Build.VERSION.SDK_INT>=26)
                    imei=telephonyManager.imei
                else
                    imei=telephonyManager.deviceId
                if (imei == null)
                    imei = Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID).trim { it <= ' ' }
                return imei
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID).trim { it <= ' ' }
    }

    fun getDeviceId(context:Context) :String{
        val str = Build.BOARD + "|" + Build.BOOTLOADER + "|" + Build.DEVICE + "|" + Build.DISPLAY + "|" + Build.BRAND +"|"+Build.MODEL +  "|" + Build.FINGERPRINT + "|" + Build.HARDWARE + "|" + Build.HOST + "|" + Build.ID + "|" + Build.MANUFACTURER + "|" + Build.PRODUCT + "|" + Build.TAGS + "|" + Build.TYPE
        return Md5().d(str).toLowerCase()
        /*var androidId = Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID).trim()
        var serial=Build.SERIAL.trim()
        if(androidId==null || serial==null || androidId.length+serial.length<16){
            return "null"
        }
        androidId=(androidId+serial).substring(0,16)
        return androidId*/
    }
}