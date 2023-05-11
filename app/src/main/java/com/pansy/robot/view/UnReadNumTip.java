package com.pansy.robot.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.pansy.robot.R;

import project.pyp9536.wanxiang.util.ScreenUtil;


//未读消息控件
public class UnReadNumTip extends View {
    //private int radius;//圆半径
    private int circleX;//圆心x坐标
    private int circleY;//圆心y坐标
    private int mWidth;
    private int mHeight;
    private String text;//文字
    private int textSize;//文字大小
    private float textYOffset;//文字Y坐标偏移
    Paint circlePaint;//圆画笔
    Paint textPaint;//文字画笔

    public UnReadNumTip(Context context) {
        super(context);
    }

    public UnReadNumTip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    public UnReadNumTip(Context context, AttributeSet attrs,int defStyle) {
        super(context, attrs,defStyle);
    }

    public void init(AttributeSet attrs){
        TypedArray typedArray=getContext().obtainStyledAttributes(attrs,R.styleable.UnReadNumTip);
        text=typedArray.getString(R.styleable.UnReadNumTip_unReadNum);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width_mode=MeasureSpec.getMode(widthMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height_mode=MeasureSpec.getMode(heightMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        if(width!=0 && height!=0){
            if(width_mode==MeasureSpec.AT_MOST && height_mode==MeasureSpec.AT_MOST){
                if(Integer.parseInt(text)<10)
                    widthMeasureSpec=MeasureSpec.makeMeasureSpec(ScreenUtil.INSTANCE.dp2px(getContext(),20),MeasureSpec.EXACTLY);
                else
                    widthMeasureSpec=MeasureSpec.makeMeasureSpec(ScreenUtil.INSTANCE.dp2px(getContext(),30),MeasureSpec.EXACTLY);

                heightMeasureSpec=MeasureSpec.makeMeasureSpec(ScreenUtil.INSTANCE.dp2px(getContext(),20),MeasureSpec.EXACTLY);
            }else{
                if(Integer.parseInt(text)>=10)
                    widthMeasureSpec=MeasureSpec.makeMeasureSpec((int)(width*1.5),MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=w;
        mHeight=h;
        circleX=w/2;
        circleY=h/2;
        textSize=(int)(Math.min(w,h)*0.7);
        initCirclePaint();
        initTextPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try{
            if(!TextUtils.isEmpty(text) && !text.equals("0")){
                if(Integer.parseInt(text)<10){
                    canvas.drawCircle(circleX,circleY,circleX,circlePaint);
                    if(Integer.parseInt(text)>99) text="99";
                    canvas.drawText(text,circleX,circleY+textYOffset,textPaint);
                }else{
                    RectF rectf=new RectF();
                    rectf.left=0;
                    rectf.top=0;
                    rectf.right=mWidth;
                    rectf.bottom=mHeight;
                    canvas.drawOval(rectf,circlePaint);
                    canvas.drawRoundRect(rectf,mHeight/2,mHeight/2,circlePaint);
                    if(Integer.parseInt(text)>99) text="99";
                    canvas.drawText(text,mWidth/2,mHeight/2+textYOffset,textPaint);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void initCirclePaint(){
        circlePaint=new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setAntiAlias(true);
    }
    private void initTextPaint(){
        textPaint=new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics=textPaint.getFontMetrics();
        textYOffset=-fontMetrics.ascent-(-fontMetrics.ascent+fontMetrics.descent)/2;
    }


    public void setText(String text){
        this.text=text;
    }

    public void setUnRead(int num){
        this.text=num+"";
        requestLayout();
        //invalidate();
    }


    public String getText( ){
        return text;
    }
}
