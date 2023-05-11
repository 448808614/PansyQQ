package com.yy.runscore.Util

import android.content.Context
import android.widget.Toast

object ToastUtil{
    fun toast(context: Context,str:String){
        Toast.makeText(context,str,Toast.LENGTH_LONG).show()
    }
}