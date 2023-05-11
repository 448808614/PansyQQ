package com.pansy.robot.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

object QRCodeUtil {

    fun readQRImage(bMap: Bitmap): String? {
        var contents: String? = null

        val intArray = IntArray(bMap.width * bMap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)

        val source = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()// use this otherwise ChecksumException
        try {
            val result = reader.decode(bitmap)
            contents = result.text
            //byte[] rawBytes = result.getRawBytes();
            //BarcodeFormat format = result.getBarcodeFormat();
            //ResultPoint[] points = result.getResultPoints();
            //Log.d("QRCode", contents)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }

        return contents
    }

    fun createQRCodeBitmap(content: String, width: Int, height: Int): Bitmap? {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null
        }
        try {
            /** 1.设置二维码相关配置  */
            val hints = Hashtable<EncodeHintType, String>()
            // 字符转码格式设置
            //if (!TextUtils.isEmpty(character_set)) {
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            //}
            // 容错率设置
            //if (!TextUtils.isEmpty(error_correction_level)) {
            hints[EncodeHintType.ERROR_CORRECTION] = "H"
            //}
            // 空白边距设置
            //if (!TextUtils.isEmpty(margin)) {
            hints[EncodeHintType.MARGIN] = "1"
            //}
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象  */
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = Color.WHITE// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象  */
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }

    }

}