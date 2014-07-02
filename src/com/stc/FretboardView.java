/*****************************************************************
FretboardView

Copyright (C) 2014 Scott T Coleman

Eclipse Public License 1.0 (EPL-1.0)

This library is free software; you can redistribute it and/or
modify it under the terms of the Eclipse Public License as
published by the Free Software Foundation, version 1.0 of the 
License.

The Eclipse Public License is a reciprocal license, under 
Section 3. REQUIREMENTS iv) states that source code for the 
Program is available from such Contributor, and informs licensees 
how to obtain it in a reasonable manner on or through a medium 
customarily used for software exchange.

Post your updates and modifications to our GitHub or email to 
scoleman2012@gmail.com.

This library is distributed WITHOUT ANY WARRANTY; without 
the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the Eclipse Public License 1.0 (EPL-1.0)
for more details.
 
You should have received a copy of the Eclipse Public License
along with this library; if not, 
visit http://www.opensource.org/licenses/EPL-1.0

*****************************************************************/
package com.stc;

import java.util.Enumeration;
import java.util.Vector;







import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

public class FretboardView extends View
{
	
	private static final int TOUCH_RESPONSE_SIZE = 30;
	private Paint   mLinePaint;	
	private Paint   mTextPaint;	
	private Paint   mSelectedNotePaint;	
	private Paint   mNotePaint;	
	private Paint   mRootPaint;	
    private float   mX;	
    float mHeight = 0;
    float mWidth = 0;
    
    int mNumStrings = 6;
    int mMaxNumRelativeFrets = 7;
    float mStringSpacing;
    float mFretSpacing;
    float mFretOffset;
    float mStringOffset;
    float mNoteSize;
    
    int mFirstString;
	int mString = 6;
	int mRelativeFret = 1;
	int mAbsoluteFret = 1;
	int mNumOnString = 0;
	int mMaxNumOnString = 3;
	int mNumStringsTouched = 0;
	int mOctave = 4;					// Default to 4th octave
	int mAbsoluteStartFret;

    
    public ModeWheele mParentActivity = null; 
    
   
	public void setmParentActivity(ModeWheele mParentActivity)
	{
		this.mParentActivity = mParentActivity;
	}

	float[] mStringPosiotions;
    float[] mFretPosiotions;
    
    // Note that we must store the notes outside of the provided scale because we may iterate through the scale a couple of times to fill the fretboard
    private Vector<NoteView> mNotes;
    private Scale mScale;
    private Context mContext;
    
    
	public FretboardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(0x80000000);
		mLinePaint.setStyle(Paint.Style.STROKE);	
		
		mNotePaint = new Paint();
		mNotePaint.setAntiAlias(true);
		mNotePaint.setColor(0x80000000);
		mNotePaint.setStyle(Paint.Style.FILL);	
		
        mSelectedNotePaint = new Paint();
        mSelectedNotePaint.setAntiAlias(true);
		mLinePaint.setColor(0x80000000);
		mLinePaint.setStyle(Paint.Style.STROKE);	

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
      //  mTextPaint.setTextSize(14);
      //  mTextPaint.setTypeface(Typeface.SERIF);		
        
        mTextPaint.setTextSize(30);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);    
        
        mTextPaint.setColor(0x80000000);
	

        mStringPosiotions = new float[mNumStrings];
        mFretPosiotions = new float[mMaxNumRelativeFrets];
		mNotes = new Vector<NoteView>();

		// Plot some test notes
//		PlotNote("", 2,2);
//		PlotNote("", 4,2);
		
		
        
	}

	@Override
	public void draw(Canvas canvas)
	{
		
		
//		this.get
        canvas.drawColor(Color.WHITE);
        float x = mX;
        x = 0;
        float y = 0;        
        
        Paint p = mLinePaint;   
        // Draw strings
        for (int i = 0; i < mNumStrings; i++)
        {
        	canvas.drawLine(x, mStringPosiotions[i], mWidth, mStringPosiotions[i], p);         	
        }
        // Draw Frets
        for (int i = 0; i < mMaxNumRelativeFrets; i++)
        {
        	canvas.drawLine(mFretPosiotions[i],y + mStringOffset , mFretPosiotions[i], mHeight - mStringOffset, p);         	
        }
        
        // Draw fret indication
        canvas.drawText(Integer.toString(mAbsoluteStartFret), 0, mHeight, mTextPaint);        
        
        
        // Draw notes on fretboard
		Enumeration<NoteView> e = mNotes.elements();
		while (e.hasMoreElements())
		{
			NoteView note = (NoteView)e.nextElement();
			
			int fret = note.fret - 1;;
			int string = note.string - 1;
			if ((fret>= 0 && fret < mMaxNumRelativeFrets) && (string >= 0 && string < mNumStrings))
			{
				x = mFretPosiotions[note.fret - 1] + mFretOffset;
				y = mStringPosiotions[note.string - 1];
				note.screenPosX = (int) x;
				note.screenPosY = (int) y;
				
				if (note.selected)
				{
					canvas.drawCircle(x, y, mNoteSize, mSelectedNotePaint);
				}
				else
				{
					canvas.drawCircle(x, y, mNoteSize, mNotePaint);
				}
				// If this is the root draw a square to show it
				if (note.name.compareTo(mScale.mRoot) == 0)
				{
					float left = x - mNoteSize;
					float right = x + mNoteSize;
					float top = y - mNoteSize;
					float bottom = y + mNoteSize;
					canvas.drawRect(left, top, right, bottom, mLinePaint);
				}
			}
		}		

		super.draw(canvas);
	}

	public void SetScale(Scale scale)
	{
		boolean moreNotes = true;
		mString = scale.mStartString;
		mRelativeFret = scale.mStartRelativeFret;
		mOctave = scale.mStartOctave;
		mAbsoluteStartFret = scale.mAbsoluteStartFret;
		
		// Make sure to start fresh in case we're just changing scales
		mNotes.clear();		
		mString = 6;
		mRelativeFret = 1;		
		
		mScale = scale;		// Save the provided scale so we can reference the global parameters (root, mode)		
		
		Enumeration<NoteView> e = scale.mItems.elements();
		mFirstString = mString;
		
		// Set the octave from the first note in the scale
		NoteView n = (NoteView)scale.mItems.elementAt(0);
		//mOctave = n.octave;
		
		// note that the scale will be repeated across the fretboard, so it may be necessary
		// to repeat the iteration through the scale while filling the fretboard.
		while (moreNotes)
		{
			if (!e.hasMoreElements())
			{
				// We've wrapped around an octave
				//mOctave++;
				e = scale.mItems.elements();
			}
			// We need to create a brand new note to insert into the fretboard so each fretboard note
			// can have it's own string/fret combination.
			NoteView note = (NoteView)e.nextElement();
			NoteView newNote = new NoteView(note);
			
			newNote.string = mString;
			newNote.fret = mRelativeFret;
			//newNote.octave = mOctave;
			newNote.octave = scale.getOctave(mString, mRelativeFret + scale.mAbsoluteStartFret );
			mNotes.add(newNote);					
			moreNotes = AdvanceFretAndString(newNote);
		}
	}
	
	private boolean AdvanceFretAndString(NoteView note)
	{
		mRelativeFret += note.halfWholeStepGraphic;
		mNumOnString++;	
		if (mRelativeFret >= mMaxNumRelativeFrets  || mNumOnString >= mMaxNumOnString)
		{
			mRelativeFret -= note.halfWholeStepGraphic; // Move the fret back to where it was
			mNumOnString = 0;
			mString--;
			if (mString <= 0)
			{
				mString = mNumStrings;
				if (mString == mFirstString)
					return false;
			}

			// Now figure out where to move the fret
			// By changing string we have advanced 5 (or 4 for string 2) steps, so 5 - note.halfWholeStepGraphic
			// is the number we need to change by
			int stepsMoved = (mString == 2) ? 4:5;
			int stepsToMove = stepsMoved - note.halfWholeStepGraphic;
			mRelativeFret -= stepsToMove;			
		}
		return true;
	}
	
	public void PlotNote(String note, int string, int fret)
	{
  	  NoteView v = new NoteView("", "", note, 0, null, null);
  	  v.string = string;
  	  v.fret = fret;
  	  mNotes.add(v);			
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
        mX = w * 0.5f;  // remember the center of the screen		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		mHeight =  (float) ((float) View.MeasureSpec.getSize(heightMeasureSpec) / 2);
//	    mWidth = (float) ((float) View.MeasureSpec.getSize(widthMeasureSpec) / 2.5);
	    mWidth = (float) ((float) View.MeasureSpec.getSize(widthMeasureSpec) / 2.0);
		
	    
		setMeasuredDimension((int) mWidth,(int) mHeight);

		mStringSpacing = mHeight /(mNumStrings);
		mStringOffset= mStringSpacing / 2;
		mFretSpacing = mWidth /(mMaxNumRelativeFrets - 1);
		mNoteSize = mFretSpacing / 4;	
		mFretOffset = mFretSpacing / 2;
		
        for (int i = 0; i < mNumStrings; i++)
        {
            mStringPosiotions[i] = (mStringSpacing * i) + mStringOffset;             
        }
        for (int i = 0; i < mMaxNumRelativeFrets; i++)
        {
            mFretPosiotions[i] = mFretSpacing * i;             
        }
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
	    float x = e.getX();
	    float y = e.getY();
	    switch (e.getAction()) {
		    case MotionEvent.ACTION_DOWN:
				Enumeration<NoteView> notesElements = mNotes.elements();
				while (notesElements.hasMoreElements())
				{
					NoteView noteView = (NoteView)notesElements.nextElement();
					if ((Math.abs(noteView.screenPosX - x) < TOUCH_RESPONSE_SIZE ) && (Math.abs(noteView.screenPosY - y) < TOUCH_RESPONSE_SIZE ))
					{
						noteView.selected = true;
//	    		    	midiFile.PlayNote(noteView.name);
//	            		redraw = true;
						invalidate();
//	            		mFretboardView.invalidate();
				        Log.v("one", new String(" found: " + noteView.name));
				        
				        // Now select the note in the original scale so it shows up in the parent view
						Enumeration<NoteView> scaleElements = mScale.mItems.elements();
						while (scaleElements.hasMoreElements())
						{
							NoteView n = (NoteView)scaleElements.nextElement();
							if (n.name.compareTo(noteView.name) == 0)
							{
								n.selected = true;
								if (mParentActivity != null)
									mParentActivity.SelectionChanged(noteView);							
							}
						}

				        
				        
				    }	
					else
					{
						noteView.selected = false;
					}
				}
				
		    	break;
		    case MotionEvent.ACTION_UP:


				break;

		    
		    
		    
		    case MotionEvent.ACTION_MOVE:
		    	break;
	        
	    }
        Log.v("one", new String(" found: " + "End"));

		return true;
		//return super.onTouchEvent(e);
	}



}
