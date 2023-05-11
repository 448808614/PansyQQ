package com.pansy.robot.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.pansy.robot.R;


public class SwitchButton extends View implements View.OnTouchListener {

    private boolean isChoose=false;

    private boolean isChecked;

    private boolean onSlip=false;

    private float down_x,now_x;

    private Rect btn_off,btn_on;

    private boolean isChangeOn=false;

    private boolean isInterceptOn=false;

    private OnChangedListener onChangedListener;

    private Bitmap bg_on,bg_off,slip_btn;

    public SwitchButton(Context context){
        super(context);

    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //if(w>h*1.7) w=(int)(h*1.7);//保持比例
        super.onSizeChanged(w, h, oldw, oldh);
        init(w,h);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w=getMeasuredWidth();
        int h=getMeasuredHeight();

        if(w>0 && h>0) {
            //heightMeasureSpec=MeasureSpec.makeMeasureSpec(h,MeasureSpec.EXACTLY);
            if(w>h*1.7)
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(h * 1.7),MeasureSpec.EXACTLY);
            else
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);

            if(h>w/1.7)
                heightMeasureSpec=MeasureSpec.makeMeasureSpec((int)(w / 1.7),MeasureSpec.EXACTLY);
            else
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init(int w, int h){
        bg_on=BitmapFactory.decodeResource(getResources(),R.drawable.nim_slide_toggle_on);
        bg_off=BitmapFactory.decodeResource(getResources(),R.drawable.nim_slide_toggle_off);
        slip_btn=BitmapFactory.decodeResource(getResources(),R.drawable.nim_slide_toggle);
        //图片压缩
      /* bg_on=bg_on.createScaledBitmap(bg_on,(int)(bg_on.getWidth()/1.5f),(int)(bg_on.getHeight()/1.5f),true);
        bg_off=bg_off.createScaledBitmap(bg_off,(int)(bg_off.getWidth()/1.5f),(int)(bg_off.getHeight()/1.5f),true);
        slip_btn=slip_btn.createScaledBitmap(slip_btn,(int)(slip_btn.getWidth()/1.5f),(int)(slip_btn.getHeight()/1.5f),true);*/

        bg_on=bg_on.createScaledBitmap(bg_on,w,h,true);
        bg_off=bg_off.createScaledBitmap(bg_off,w,h,true);
        slip_btn=slip_btn.createScaledBitmap(slip_btn,w/2,h,true);


        btn_off=new Rect(0,0,slip_btn.getWidth(),slip_btn.getHeight());
        btn_on=new Rect(bg_off.getWidth()-slip_btn.getWidth(),0,bg_off.getWidth(),slip_btn.getHeight());
        setOnTouchListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Matrix matrix=new Matrix();
        Paint paint=new Paint();
        float x;
        if(now_x<(bg_on.getWidth()/2)){
            x=now_x-slip_btn.getWidth()/2;
            canvas.drawBitmap(bg_off,matrix,paint);
        }else{
            x=bg_on.getWidth()-slip_btn.getWidth()/2;
            canvas.drawBitmap(bg_on,matrix,paint);
        }
        if(onSlip){
            if(now_x>=bg_on.getWidth()){
                x=bg_on.getWidth()-slip_btn.getWidth()/2;
            }else if(now_x<0) {
                x = 0;
            }else{
                x=now_x-slip_btn.getWidth()/2;
            }
        }else{
            if(isChoose){
                x=btn_on.left;
                canvas.drawBitmap(bg_on,matrix,paint);
            }else{
                x=btn_off.left;
            }
        }
        if(isChecked){
            canvas.drawBitmap(bg_on,matrix,paint);
            x=btn_on.left;
            isChecked=!isChecked;
        }

        if(x<0){
            x=0;
        }else if(x>bg_on.getWidth()-slip_btn.getWidth()){
            x=bg_on.getWidth()-slip_btn.getWidth();
        }
        canvas.drawBitmap(slip_btn,x,0,paint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean old=isChoose;
        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
                now_x=event.getX();
                break;
            case MotionEvent.ACTION_DOWN:
                if(event.getX()>bg_on.getWidth()||event.getY()>bg_on.getHeight()){
                    return false;
                }
                onSlip=true;
                down_x=event.getX();
                now_x=down_x;
                break;
            case MotionEvent.ACTION_CANCEL:
                onSlip=false;
                boolean choose=isChoose;
                if(now_x>=(bg_on.getWidth()/2)){
                    now_x=bg_on.getWidth()-slip_btn.getWidth()/2;
                    isChoose=true;
                }else{
                    now_x=now_x-slip_btn.getWidth()/2;
                    isChoose=false;
                }
                if(isChangeOn && (choose!=isChoose)){
                    onChangedListener.onChanged(this,isChoose);
                }
                break;
            case MotionEvent.ACTION_UP:
                onSlip=false;
                boolean lastChoose=isChoose;
                if(event.getX()>=(bg_on.getWidth()/2)){
                    now_x=bg_on.getWidth()-slip_btn.getWidth()/2;
                    isChoose=true;
                }else{
                    now_x=now_x-slip_btn.getWidth()/2;
                    isChoose=false;
                }
                if(lastChoose==isChoose){
                    if(event.getX()>=bg_on.getWidth()/2){
                        now_x=0;
                        isChoose=false;
                    }else{
                        now_x=bg_on.getWidth()-slip_btn.getWidth()/2;
                        isChoose=true;
                    }
                }
                if(isChangeOn){
                    onChangedListener.onChanged(this,isChoose);
                }
                break;
        }
        if(!old && isInterceptOn){
            isChoose=false;
        }else{
            invalidate();
        }
        return true;
    }

    public void setOnChangedListener(OnChangedListener listener){
        isChangeOn=true;
        onChangedListener=listener;
    }

    public interface OnChangedListener{
        void onChanged(View v, boolean checkState);
    }

    public void setCheck(boolean isChecked){
        this.isChecked=isChecked;
        isChoose=isChecked;
        if(isChecked==false){
            now_x=0;
        }
        invalidate();
    }

    public boolean isChoose() {
        return this.isChoose;
    }

    public boolean getCheck() {
        return this.isChecked;
    }

    public void setInterceptState(boolean isIntercept){
        isInterceptOn=isIntercept;
    }

}
