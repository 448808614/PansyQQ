package com.pansy.robot.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.pansy.robot.R;

import project.pyp9536.wanxiang.util.ScreenUtil;

public class HorizonProgress extends View {
    private double mProgress;
    private Paint mPaint;
    private Paint mTextPaint;
    private int mStartColor = getResources().getColor(R.color.colorAccent);
    private int mEndColor = getResources().getColor(R.color.colorAccent);
    private int mBgColor = Color.GRAY;
    private float textYOffset;

    public HorizonProgress(Context context) {
        super(context);
    }


    public HorizonProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizonProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(int textSize) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mTextPaint=new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics=mTextPaint.getFontMetrics();
        textYOffset=-fontMetrics.ascent-(-fontMetrics.ascent+fontMetrics.descent)/2;
    }

    public void setProgress(double progress){
        if(progress>100) progress=100;
        mProgress = progress/100;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int textSize=(int)(h*0.55);
        if(textSize==0) textSize=40;
        init(textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        triPathDraw(canvas);
    }

    private void triPathDraw(Canvas canvas) {
        mPaint.setColor(mBgColor);
        int height = getHeight();
        int width = getWidth();
        RectF rectF = new RectF(0, 0, width, height);
        mPaint.setShader(null);
        canvas.drawRoundRect(rectF, height / 2, height / 2, mPaint);//先绘制背景
        float radius = height / 2;//左右半圆的半径
        double progressW = mProgress * width;//当前进度对应的长度
        RectF rectLeft = new RectF(0, 0, height, height);

        //增加渐变
        LinearGradient linearGradient = new LinearGradient(0, 0, (int)progressW, height, mStartColor,mEndColor, Shader.TileMode.CLAMP);
        mPaint.setShader(linearGradient);//设置shader
        if(progressW < radius){//当进度处于图1的时候
            double disW = radius - progressW;
            float angle = (float) Math.toDegrees(Math.acos(disW / radius));
            //绘制图1 弧AD对应的棕色区域，注意第三个参数设置为false
            // 表示绘制不经过圆心（即图一效果）的月牙部分，设置为true则绘制的是扇形，angle是绘制角度
            canvas.drawArc(rectLeft, 180 - angle, angle * 2, false, mPaint);
        }else if(progressW <= width - radius){//当进度处于图2的时候
            canvas.drawArc(rectLeft, 90, 180, true, mPaint);//绘制弧AD半圆
            RectF rectMid = new RectF(radius, 0, (float) progressW, height);
            canvas.drawRect(rectMid, mPaint);//绘制ABCD矩形进度
        }else{//图4对应部分
            canvas.drawArc(rectLeft, 90, 180, true, mPaint);//绘制左端半圆

            RectF rectMid = new RectF(radius , 0, width - radius, height);
            canvas.drawRect(rectMid, mPaint);//绘制中间的矩形部分

            //得到图四中F->C->B->E->F的闭合区域，注意此处是从头F为起点，减少坐标计算
            double disW = progressW - (width - radius);
            float angle = (float) Math.toDegrees(Math.acos(disW / radius));
            RectF rectRight = new RectF(width - height, 0, width, height);
            Path path = new Path();
            path.arcTo(rectRight, angle, 90 - angle);
            path.lineTo(width - radius, 0);
            path.arcTo(rectRight, 270, 90 - angle);
            path.close();
            canvas.drawPath(path, mPaint);//绘制path
        }
        canvas.drawText((int)(mProgress*100)+"%",getWidth()/2-mTextPaint.getTextSize(),getHeight()/2+textYOffset,mTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int defaultWidth = (int) (ScreenUtil.INSTANCE.getScreenWidth(getContext())*0.8);
        int defaultHeight =  ScreenUtil.INSTANCE.dp2px(getContext(),30);
        int width = getMeasureSize(defaultWidth, widthMeasureSpec);
        int height = getMeasureSize(defaultHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    private int getMeasureSize(int defaultSize, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if(mode == MeasureSpec.UNSPECIFIED){
            return defaultSize;
        }else if(mode == MeasureSpec.AT_MOST){
            return Math.min(defaultSize, size);
        }
        return size;
    }

    private int calGradientColor(float progress, int colorLeft, int colorRight) {
        int redS = Color.red(colorLeft);
        int greenS = Color.red(colorLeft);
        int blueS = Color.blue(colorLeft);
        int redE = Color.red(colorRight);
        int greenE = Color.green(colorRight);
        int blueE = Color.blue(colorRight);

        int dstRed = (int) (redS * (1 - progress) + progress * redE);
        int dstGreen = (int) (greenS * (1 - progress) + progress * greenE);
        int dstBlue = (int) (blueS * (1 - progress) + progress * blueE);
        return Color.argb(255, dstRed, dstGreen, dstBlue);
    }

}