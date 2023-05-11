package com.pansy.robot.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ListView
import kotlinx.android.synthetic.main.dialog_imitate_ios.*
import com.pansy.robot.R
import project.pyp9536.wanxiang.util.ScreenUtil
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintLayout
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import java.lang.Thread.sleep


//仿ios对话框
class ImitateIosDialog(val mContext: Context) : Dialog(mContext){
    private var mTitle:String?=null
    private var mContent:String?=null
    private var mConfrimText:String?=null
    private var mCancelText:String?=null
    private var mNeedInput=false
    private var mIsPassword=false
    var mInputString:String?=null
    private var mTitleColor:Int?=null
    private var mConfirmColor:Int?=null
    private var mCancelColor:Int?=null
    private var mContentColor:Int?=null
    private var mNoAutoCancel=false
    private var mHideContent=false
    private var mHideTitle=false
    private var mHideCancel=false
    private var mHideConfirm=false
    private var mHideV1=false
    private var mView:View?=null
    private var mInputOnlyNumber=false
    private var mWidth:Int?=null
    private var mSeconds:Int=-1

    private var mOnConfirmListener:(()->Unit)?=null
    private var mOnCancelListener:(()->Unit)?=null
    fun setOnConfirmListener(listener:()->Unit){
        this.mOnConfirmListener=listener
    }
    fun setOnCancelListener(listener:()->Unit){
        this.mOnCancelListener=listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_imitate_ios)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //设置透明度
        val a=this.window.attributes
        a.alpha=0.8f
        a.width=(ScreenUtil.getScreenWidth(mContext)*0.8).toInt()
        if(mWidth!=null) a.width=mWidth!!
        this.window.attributes=a
        initView()
    }

    fun setTitle(title:String?){
        if(title!=null)
           mTitle=title
    }

    fun setConfirmText(confirmText:String?){
        if(confirmText!=null)
            mConfrimText=confirmText
    }

    fun setCancelText(cancelText:String?){
        if(cancelText!=null)
            mCancelText=cancelText
    }


    fun setTitleColor(color:Int){
        mTitleColor=color
    }

    fun setConfirmColor(color:Int){
        mConfirmColor=color
    }

    fun setCancelColor(color:Int){
        mCancelColor=color
    }

    fun setContentColor(color:Int){
        mContentColor=color
    }

    fun setContent(content:String?){
        if(content!=null)
            mContent=content
    }

    fun hideContent(){
        mHideContent=true
    }

    fun hideTitle(){
        mHideTitle=true
    }

    fun hideCancel(){
        mHideCancel=true
    }

    fun hideConfirm(){
        mHideConfirm=true
    }
    fun hideV1(){
        mHideV1=true
    }

    fun needInput(isPassword:Boolean){
        mNeedInput=true
        mIsPassword=isPassword
    }

    fun setInputString(str:String?){
        mInputString=str
    }

    fun noAutoDismiss(){
        mNoAutoCancel=true
    }

    fun addView(view:View){
        mView=view
    }

    fun inputOnlyNumber(){
        mInputOnlyNumber=true
    }

    fun setWidth(width:Int){
        mWidth=width
    }

    fun setCountdown(seconds:Int){
        mSeconds=seconds
    }

    fun addItem(context:Context,item: Array<String>,listener: (p:Int) -> Unit){
        val lv=ListView(context)
        lv.layoutParams=(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        lv.adapter=ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, item)
        setListViewHeightBasedOnChildren(lv)
        addView(lv)
        lv.setOnItemClickListener { parent, view, position, id ->
            listener.invoke(position)
            this.dismiss()
        }
    }

    fun setListViewHeightBasedOnChildren(listView: ListView) {
        // 获取ListView对应的Adapter
        val listAdapter = listView.adapter ?: return
        var totalHeight = 0
        var i = 0
        val len = listAdapter.count
        while (i < len) {
            //listAdapter.getCount()返回数据项的数目
            val listItem = listAdapter.getView(i, null, listView)
            // 计算子项View 的宽高 listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.minimumHeight
            i++
        }
        val params = listView.layoutParams
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        params.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
        //params.height最后得到整个ListView完整显示需要的高度
        listView.layoutParams = params
    }

    fun initView() {
        tv_title.text = mTitle ?: "提示"
        tv_content.text = mContent ?: "内容"
        tv_confirm.text = mConfrimText ?: "确定"
        tv_cancel.text = mCancelText ?: "取消"
        if (mHideContent) tv_content.visibility = View.GONE
        if (mHideTitle) tv_title.visibility = View.GONE
        if (mHideCancel) {
            tv_cancel.visibility = View.GONE
            tv_confirm.setBackgroundResource(R.drawable.dialog_imitate_ios_tv_confirm_bg2)
        }
        if (mHideConfirm) {
            tv_confirm.visibility = View.GONE
            tv_cancel.setBackgroundResource(R.drawable.dialog_imitate_ios_tv_confirm_bg2)
            v2.visibility = View.GONE
        }
        if (mHideV1){
            v1.visibility = View.INVISIBLE
            val lp =(v1.layoutParams) as  ConstraintLayout.LayoutParams
            lp.topMargin = ScreenUtil.dp2px(mContext, 10f)
            v1.layoutParams=lp
        }
        if(mTitleColor!=null) tv_title.setTextColor(mTitleColor!!)
        if(mContentColor!=null) tv_content.setTextColor(mContentColor!!)
        if(mConfirmColor!=null) tv_confirm.setTextColor(mConfirmColor!!)
        if(mCancelColor!=null) tv_cancel.setTextColor(mCancelColor!!)
        if(mView!=null) {
            ll.visibility=View.VISIBLE
            ll.addView(mView)
        }

        if (mNeedInput) {
            edt.visibility= View.VISIBLE
            if(mIsPassword)
                edt.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            if(mInputOnlyNumber)
                edt.inputType=InputType.TYPE_CLASS_NUMBER
        }
        if (mInputString!=null) edt.setText(mInputString)

        tv_confirm.setOnClickListener{
            if(mSeconds>-1) return@setOnClickListener
            if(!mNoAutoCancel)
                this.dismiss()
            if(mNeedInput) mInputString=edt.text.toString()
            mOnConfirmListener?.invoke()
        }
        tv_cancel.setOnClickListener{
            if(!mNoAutoCancel)
                this.dismiss()
            if(mNeedInput) mInputString=edt.text.toString()
            mOnCancelListener?.invoke()
        }

        if(mSeconds>-1){
            if(mConfrimText==null) mConfrimText=tv_confirm.text.toString()
            if(mConfirmColor==null) mConfirmColor=Color.parseColor("#4782ef")
            mNoAutoCancel=true
            tv_confirm.text=tv_confirm.text.toString()+"（$mSeconds）"
            tv_confirm.isClickable=false
            tv_confirm.setTextColor(Color.GRAY)


            Thread {
                while (mSeconds>-1) {
                    Handler(Looper.getMainLooper()).post{
                        tv_confirm.text="${mConfrimText}（$mSeconds）"
                        mSeconds--
                    }
                    sleep(1000)
                }
                mNoAutoCancel=false
                Handler(Looper.getMainLooper()).post{
                    tv_confirm.text=mConfrimText
                    tv_confirm.setTextColor(mConfirmColor!!)
                    tv_confirm.isClickable=true
                }
            }.start()

        }
    }


}
