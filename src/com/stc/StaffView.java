package com.stc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class StaffView extends View
{
	private Paint   mLinePaint;	
	private Paint   mPaint;	
    private float   mX;	
    int mHeight = 0;
    int mWidth = 0;
    
	public StaffView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(0x800000FF);
		mLinePaint.setStyle(Paint.Style.STROKE);	
		
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(30);
        mPaint.setTypeface(Typeface.SERIF);		

	}

	@Override
	public void draw(Canvas canvas)
	{
        canvas.drawColor(Color.WHITE);
        float x = mX;
        float y = 0;        
        
        Paint p = mLinePaint;        
        //p.setColor(0x80FF0000);
        canvas.drawLine(x, y, x, y+200, p);        
        
        
        
		super.draw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		//w = w / 4;
		//h = h / 4;
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
        mX = w * 0.5f;  // remember the center of the screen		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int _height = View.MeasureSpec.getSize(heightMeasureSpec);
	    int _width = View.MeasureSpec.getSize(widthMeasureSpec);
		
//		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(_width/4,_height/4);
//		setMeasuredDimension(100,100);
		// TODO Auto-generated method stub
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	
}
