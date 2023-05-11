package project.pyp9536.wanxiang.util

import android.content.Context
import android.util.DisplayMetrics

/**
 *
 * 作用:dp和pi的转换 工具类
 */
object ScreenUtil {

    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun sp2px(context: Context, sp: Float): Int {
        val dm = context.resources.displayMetrics
        return (sp * dm.scaledDensity + 0.5f).toInt()
    }

    fun getScreenWidth(context: Context): Int {
        val dm = context.resources.displayMetrics
        return dm.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val dm = context.resources.displayMetrics
        return dm.heightPixels
    }

}
