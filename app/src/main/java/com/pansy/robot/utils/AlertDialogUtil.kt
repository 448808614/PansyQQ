package com.pansy.robot.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View

object AlertDialogUtil {
    fun show(context: Context, title:String?, message:String?, items:Array<String>?,itemsListener:DialogInterface.OnClickListener?,view: View?,pt:String?,ptListener: DialogInterface.OnClickListener?,neg:String?,negListener: DialogInterface.OnClickListener?,neu:String?,neuListener: DialogInterface.OnClickListener?,canCancel:Boolean){
        val builder=AlertDialog.Builder(context)
        if(title!=null)
            builder.setTitle(title)
        if(message!=null)
            builder.setMessage(message)
        if(items!=null)
            builder.setItems(items,itemsListener)
        if(view!=null)
            builder.setView(view)
        if(pt!=null && ptListener!=null)
            builder.setPositiveButton(pt,ptListener)
        if(neg!=null)
            if(negListener!=null)
                builder.setNegativeButton(neg,negListener)
            else
                builder.setNegativeButton(neg,{dialog,which->})
        if(neu!=null && neuListener!=null)
            builder.setNeutralButton(neu,neuListener)
        val dialog=builder.create()
        dialog.setCanceledOnTouchOutside(canCancel)
        dialog.show()
    }
}