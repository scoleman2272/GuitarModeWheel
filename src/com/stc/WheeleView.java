/*****************************************************************
WheeleView

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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

/**
 * @author Scott Coleman
 * 
 * Main view implementation. View contains one scale which is displayed
 * along the circumference of the "wheel". Each note of the scale is represented
 * by a NoteView object. If the user selects a "note" on the scale, that note
 * is played in the media player 
 * 
 * The distance from one note to the next on the wheele is indicated by a graphic
 * located between the notes (see below). Note that to make the distance
 * an integer half steps are referred to as distance of "1", and whole
 * steps are a distance of "2".
 * 
 *  half step  /\
 *  whole step /--\
 *  
 *  Notes which are selected are indicated in bold font
 *  
 *  Modes of a scale are indicated by rotating the entire wheel by one note
 *  e.g. Ionian rotate right one = Locrian
 *  
 *  Rotation is done via the touch screen. Note that while I used to actually 
 *  rotate the canvas I realized that the same effect can be had by
 *  simply changing the angle used to calculate screen positions.
 *  
 *  Note that there is one wrinkle to this. When the graphics are rotated in the
 *  view, each individual graphic muse be rotated so it appears in correct relation 
 *  to the two notes it sits between.
 */
public class WheeleView extends SurfaceView implements SurfaceHolder.Callback
{
	private static final String TAG = "WheeleView";
	private static final int TOUCH_RESPONSE_SIZE = 30;
	
    private TextView mStatusText; // Currently not used

    /**
     * Main thread used to process graphics
     */
    private WheeleThread thread;

    /**
     * Number of pieces to divide the wheel into
     */
    int mNumSteps;
    
    /**
     * How much angle to increment for each step 
     *  i.e 360/mNumSteps;
     */
    float mAngleIncrement;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
//	  private final float TRACKBALL_SCALE_FACTOR = 36.0f;

	float previousViewRotate = -1;
	float viewRotate = 0;
	boolean redraw = false;
	private float mPreviousX;
    private float mPreviousY;
    boolean mTouchLocked = false;
    private Drawable mHalfStepGraphicImage;
    private Drawable mWholeStepGraphicImage;
    private int mSelectedItemNum;
    private int mPreviousSelectedItemNum;
//      private StaffView mStaffView;
    private FretboardView mFretboardView;
    private Scale mScale;
    MidiPlayer midiFile;
    

	/*
     * State-tracking constants - Currently unused
     */
    public static final int STATE_PAUSE = 2;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_READY = 6;
    

    private static final String KEY_X = "mX";
//    private static final String KEY_Y = "mY";

    /**
     * Bitmap for wheel view backdrop
     */
    // private Bitmap mBackgroundImage
    private int mCanvasHeight = 1;
    private int mCanvasWidth = 1;

    private float mCenterX = 0;
    private float mCenterY = 0;
    private float mOutsideRadius = 0;

    /**
     * Location for note name on the circumference of the wheel
     */
    private MyPoint mNamePoint;
    
    /**
     * Location for the graphic indicating distance between notes
     * (half/whole)
     */
    private MyPoint mGraphicPoint;

    /**
     * Location of viewe title (currently not used)
     */
    private MyPoint mTitlePoint;
    
    /** Message handler used by thread to interact with TextView */
    private Handler mHandler;  // Currently unused

    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mSelectedTextPaint;

    private int mMode; // Currently unused

    /** Indicate whether the surface has been created & is ready to draw */
    private boolean mRun = false;

    /**
     * Handle to the surface manager object we interact with
     */
    private SurfaceHolder mSurfaceHolder;    

	TextView mTextViewMode;

    
	public void setTextViewMode(TextView mTextViewMode) {
		this.mTextViewMode = mTextViewMode;
	}

	/**
	 * @param context - Resource context
	 * @param attrs - View attributes
	 */
	public WheeleView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		
		
        // Create a Midi player to play back notes
		midiFile = new MidiPlayer(context);
        
		// register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        // Get the resources for the distance graphics
        // Note that these are selected by using an index
        // 1 - half step, 2 - whole step
        // This way objects don't need to carry around a drawable
        mHalfStepGraphicImage = context.getResources().getDrawable(R.drawable.halfstepgraphic);
        mWholeStepGraphicImage = context.getResources().getDrawable(R.drawable.wholestepgraphic);
     
        // Create new thread to handle the graphics processing
        thread = new WheeleThread(holder, context, new Handler() 
        {
            @Override
            public void handleMessage(Message m) 
            {
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });        
        
        setFocusable(true); // make sure we get key events
	}

    /**
     * @author Scott Coleman
     * Utility class to carry position of elements not only
     * in Cartesian coordinates but polar as well.
     * Since were dealing with a circlt.
     */
    class MyPoint extends PointF
    {
    	float r;
    	float omega;

    	/**
    	 * Amount to offset X from x calculated from polar coordinates.
    	 * This is done because some elements we don't want
    	 * dead center (like labels, etc.)
    	 * 
    	 */
    	float offsetX = 0;

    	/**
    	 * Amount to offset Y from y calculated from polar coordinates
    	 * This is done because some elements we don't want
    	 * dead center (like labels, etc.)
    	 */
    	float offsetY = 0;

    	/**
    	 * Calculates Cartesian coordinates from polar.
    	 * @param _r - radius
    	 * @param _omega angle
    	 */
    	public void setPolar(float _r, float _omega)
    	{
    		r = _r;
    		omega = _omega;
    		x = (float) (r * Math.cos(omega)) + offsetX;
    		y = (float) (r * Math.sin(omega)) + offsetY;
    	}

    	MyPoint(float _x, float _y)
    	{
    		super(_x, _y);
    		offsetX = (float) x;  offsetY = (float) y;
    	}

    	/**
    	 * Sets XY amount to offset XY from coordinates calculated
    	 * from polar coordinates 
    	 * @param x
    	 * @param y
    	 * @see offsetX
    	 * @see offsetY
    	 */
    	void SetOffset(double x, double y)
    	{
    		offsetX = (float) x;  offsetY = (float) y;
    	}
    	
    }
	
	/**
	 * @return main thread associated with view
	 */
	public WheeleThread getThread() 
	{
        return thread;
    }	
	
    /**
     * @param textView - text view to associate with view
     *  Current not used
     */
    public void setTextView(TextView textView) 
    {
        mStatusText = textView;
    }

    // ---------------------------------------------------------------
    // The next few methods pass overrides to thread graphics handler
    // ---------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see android.view.View#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        return thread.doKeyDown(keyCode, msg);
    }

    /* (non-Javadoc)
     * @see android.view.View#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        return thread.doKeyUp(keyCode, msg);
    }

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height)
	{
        thread.setSurfaceSize(width, height);
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		
		 if(thread.getState()== Thread.State.TERMINATED)
		 {
			 

		        thread = new WheeleThread(getHolder(), getContext(), new Handler() 
		        {
		            @Override
		            public void handleMessage(Message m) 
		            {
		                mStatusText.setVisibility(m.getData().getInt("viz"));
		                mStatusText.setText(m.getData().getString("text"));
		            }
		        });        
			 
//		      thread = new LunarThread(mHolder, mContext, mHandler);
		      thread.setRunning(true);
		      thread.start();
		      redraw = true;
		  }else 
		  {
		        // start the thread here so that we don't busy-wait in run()
		        // waiting for the surface to be created
		        thread.setRunning(true);
		        thread.start();
		  }
	}

	
	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) 
        {
            try 
            {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
	}
	
	  @Override public boolean onTouchEvent(MotionEvent e) 
	  {
	    float x = e.getX();
	    float y = e.getY();
	    switch (e.getAction()) {
		    case MotionEvent.ACTION_DOWN:
		      	for (int i = 0; i < mNumSteps; i++) {
	            	// Get item to Check on this iteration
	            	NoteView noteView = (NoteView) mScale.mItems.elementAt(i);
	            	if ((Math.abs(noteView.screenPosX - x) < TOUCH_RESPONSE_SIZE ) && (Math.abs(noteView.screenPosY - y) < TOUCH_RESPONSE_SIZE )) {
		        		mScale.unSelectAll();
	            		noteView.selected = true;
	    		    	midiFile.PlayNote(noteView);
	    		    }
	            }
		      	
		    	break;
		    case MotionEvent.ACTION_UP:

		    	// Snap it to a detent
		    	float correction = viewRotate % mAngleIncrement;
		    	viewRotate -= correction;
		    	
		    	if (correction > mAngleIncrement/2) {
			    	viewRotate += mAngleIncrement;
		    	}
		    	
		    	mSelectedItemNum = (int)( viewRotate / mAngleIncrement);
		    	mSelectedItemNum = mScale.mItems.size() - mSelectedItemNum;
		    	if (mSelectedItemNum > mScale.mItems.size() - 1) 
		    		mSelectedItemNum = 0;

		    	if (mPreviousSelectedItemNum != mSelectedItemNum) {
		    		mPreviousSelectedItemNum = mSelectedItemNum;
		    		Log.d(TAG, "new selected item");
	        		mScale.unSelectAll();
			    	mScale.mItems.elementAt(mSelectedItemNum).selected = true;
		    	}
		    	
		    	String noteName = mScale.mItems.elementAt(mSelectedItemNum).name;
		    	mScale.mRoot = noteName;
			    mFretboardView.SetScale(mScale);              
		    	
        		redraw = true;
        		invalidate();
        		mFretboardView.invalidate();		    	

				break;
		    
		    case MotionEvent.ACTION_MOVE:
		        float dx = x - mPreviousX;
		        float dy = y - mPreviousY;
	
		        if (Math.abs(x - mCenterX) < 50) {
		        	
			        if (y > mCenterY) {
				        viewRotate -= dx * TOUCH_SCALE_FACTOR;
			        }
			        else {
				        viewRotate += dx * TOUCH_SCALE_FACTOR;
			        }
		        }
		        else {
		        	
			        if (x > mCenterX) {
				        viewRotate += dy * TOUCH_SCALE_FACTOR;
			        }
			        else {
				        viewRotate -= dy * TOUCH_SCALE_FACTOR;
			        }
		        }
		        
		        if (viewRotate >= 359) viewRotate -= 360;
		        if (viewRotate < 0 ) viewRotate += 360;
	        
	    }

	    mPreviousX = x;
	    mPreviousY = y;
	    return true;
	}        
	  
	  void SetStaffView(StaffView staffView)
	  {
//		  mStaffView = staffView;
		  
	  }
	  void SetFretboardView(FretboardView FretboardView)
	  {
		  mFretboardView = FretboardView;
	  }
	  
	  public void SelectionChanged(NoteView noteView)
	  {
		  // Clear all but the selected note
			Enumeration<NoteView> scaleElements = mScale.mItems.elements();
			while (scaleElements.hasMoreElements())
			{
				NoteView n = (NoteView)scaleElements.nextElement();
				if (n.name.compareTo(noteView.name) == 0)
				{
					n.selected = true;
    		    	midiFile.PlayNote(noteView);
				}
				else
				{
					n.selected = false;
				}
			}		  
		  
  		redraw = true;
		invalidate();
	  }
	  
	  
	  public void SetScale(Scale scale) {
		  mScale = scale;
		  mNumSteps = mScale.mItems.size();
	      mAngleIncrement = 360/mNumSteps;
	      mSelectedItemNum = 0;
	        
	      mFretboardView.SetScale(scale);
	  }
	  
		
	  public void ResetRotation() {
		  previousViewRotate = -1;
		  viewRotate = 0;
	  }
	
    /**
     * @author Scott Coleman
     * 
     * Most of the work for the Wheel view is done via this thread. 
     * this allows "animation" of scale playback, and advanced graphics
     * effects such as rotation with momentum.
     * 
     * Note that currently none of this extra functionality is used
     * so this thread could be eliminated for efficiency if needed.
     */
    class WheeleThread extends Thread {



        /**
         * Thread constructor
         * @param surfaceHolder - Used to share canvas with main view 
         * @param context - Contains the view resources
         * @param handler - Used to send messages back to the view wich owns the thread
         */
        public WheeleThread(SurfaceHolder surfaceHolder, Context context,Handler handler) 
        {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
          //  mContext = context;

            // Disable background bitmap for now
            // Resources res = context.getResources();
            // mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.wheele1);

            // Create paint objects for screen elements
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setARGB(255, 0, 0, 0);
            mLinePaint.setStyle(Paint.Style.STROKE);
            
            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTextSize(50);
            mTextPaint.setTypeface(Typeface.SERIF);    
            mTextPaint.setTextAlign(Paint.Align.CENTER);            

            mSelectedTextPaint = new Paint();
            mSelectedTextPaint.setAntiAlias(true);
            mSelectedTextPaint.setTextSize(50);
            mSelectedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);    
            mSelectedTextPaint.setTextAlign(Paint.Align.CENTER);            

        }
        
        /**
         * Convert degrees to radians
         * TODO: fix for efficiency
         * @param degrees
         * @return
         */
        float degToRad(double degrees)
        {
        	return (float) (degrees * Math.PI / 180);
        }
        
        /**
         * Start up the view thread
         */
        public void doStart() 
        {
            synchronized (mSurfaceHolder) 
            {
            	// Set locations for screen elements
            	mCenterX = mCanvasWidth / 2; 
            	mCenterY = mCanvasHeight / 2; 
            	mOutsideRadius = Math.min(mCenterX, mCenterY);
            	
                // Create polar coordinate pointer for each of the elements
                mNamePoint = new MyPoint(mCenterX, mCenterY);
                mGraphicPoint = new MyPoint(mCenterX, mCenterY);
                mTitlePoint = new MyPoint(mCenterX, mCenterY);
//                mLastTime = System.currentTimeMillis() + 100;
                setState(STATE_RUNNING);
            }
        }

        /**
         * Currently not used
         */
        public void pause() 
        {
            synchronized (mSurfaceHolder) 
            {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        /**
         * Restores view from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         * 
         * @param savedState Bundle containing the view state
         */
        public synchronized void restoreState(Bundle savedState) 
        {
            synchronized (mSurfaceHolder) 
            {
                setState(STATE_PAUSE);
                mCenterX = (float) savedState.getDouble(KEY_X);
            }
        }

        /* (non-Javadoc)
         * Main thread loop
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                
          	  	if (previousViewRotate != viewRotate || redraw == true)
          	  	{
          	  		redraw = false;
              	  	previousViewRotate = viewRotate;      	  	
                    try {
                        c = mSurfaceHolder.lockCanvas(null);
                        synchronized (mSurfaceHolder) 
                        {
                        	// Previous version of app rotated the view here
                        	// realized I didn't have to do this
                        	// but want to retain the code for reference
                        	
                            //Matrix matrix =  c.getMatrix();
                            //matrix.setRotate(viewRotate, mCenterX, mCenterY);
                            //c.setMatrix(matrix);
                        	
                      	  	c.drawColor(0xffffffff);
                            doDraw(c);
                        }
                    } finally 
                    {
                        // do this in a finally so that if an exception is thrown
                        // during the above, we don't leave the Surface in an
                        // inconsistent state
                        if (c != null) 
                        {
                            mSurfaceHolder.unlockCanvasAndPost(c);
                        }
                    }
          	  		
          	  	}
            }
        }

        /**
         * Dump view state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         * 
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) 
        {
            synchronized (mSurfaceHolder) 
            {
                if (map != null) {
                    map.putDouble(KEY_X, Double.valueOf(mCenterX));
                }
            }
            return map;
        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         * 
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) 
        {
            mRun = b;
        }

        /**
         * Sets the mode. That is, whether we are running, paused, etc..
         * 
         *  Currently unused
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setState(int mode) 
        {
            synchronized (mSurfaceHolder) 
            {
                setState(mode, null);
            }
        }

        /**
         * Sets the viewmode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * Currently unused
         * 
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) 
        {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) 
            {
                mMode = mode;

//                // Again, not used but save for reference
//              if (mMode == STATE_RUNNING) 
//                {
//                    Message msg = mHandler.obtainMessage();
//                    Bundle b = new Bundle();
//                    b.putString("text", "");
//                    b.putInt("viz", View.INVISIBLE);
//                    msg.setData(b);
//                    mHandler.sendMessage(msg);
//              } 
//              else 
//              {
//                    Resources res = mContext.getResources();
//                    CharSequence str = "";
//                    if (mMode == STATE_READY)
//                        str = res.getText(R.string.mode_ready);
//
//                    if (message != null) 
//                	  {
//                        str = message + "\n" + str;
//                    }
//
//                    Message msg = mHandler.obtainMessage();
//                    Bundle b = new Bundle();
//                    b.putString("text", str.toString());
//                    b.putInt("viz", View.VISIBLE);
//                    msg.setData(b);
//                    mHandler.sendMessage(msg);
//                }
            } // End synchronized (mSurfaceHolder) 
        }

        /**
         * Callback invoked when the surface dimensions change.
         * We need to recalculate our screen element positions
         * @param width
         * @param height
         */
        public void setSurfaceSize(int width, int height) 
        {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) 
            {
                mCanvasWidth = width;
                mCanvasHeight = height;
            	mCenterX = mCanvasWidth / 2; 
            	mCenterY = mCanvasHeight / 2; 
            	mOutsideRadius = Math.min(mCenterX, mCenterY);
                mNamePoint.SetOffset(mCenterX, mCenterY);
                mGraphicPoint.SetOffset(mCenterX, mCenterY);
                mTitlePoint.SetOffset(mCenterX, mCenterY);

                // don't forget to resize the background image
                //  mBackgroundImage = mBackgroundImage.createScaledBitmap(mBackgroundImage, width, height, true);
            }
        }

        /**
         * Resumes from a pause
         * Currently not used
         */
        public void unpause() 
        {
            // Move the real time clock up to now
//            synchronized (mSurfaceHolder) 
//            {
//                mLastTime = System.currentTimeMillis() + 100;
            //}
            setState(STATE_RUNNING);
        }

        /**
         * Handles a key-down event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true
         */
        boolean doKeyDown(int keyCode, KeyEvent msg) 
        {
            synchronized (mSurfaceHolder) 
            {
                return false;
            }
        }

        /**
         * Handles a key-up event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true if the key was handled and consumed, or else false
         */
        boolean doKeyUp(int keyCode, KeyEvent msg) 
        {
            boolean handled = false;
   
            synchronized (mSurfaceHolder) {
                
            	if (mMode == STATE_RUNNING) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_SPACE) {
                    	//                      setFiring(false);
                    	//                handled = true;
                    } 
                }
            }

            return handled;
        }

        /**
         * Main paint routine
         * @param canvas - Canvac to paint to
         */
        private void doDraw(Canvas canvas) 
        {
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            //canvas.drawBitmap(mBackgroundImage, 0, 0, null);

        	// Divide the circle mNumSteps parts
        	// for each part draw the appropriate note view elements
        	for (int i = 0; i < mNumSteps; i++)
            {
            	float omega = i * mAngleIncrement - 90;
          	  	omega += viewRotate;
          	  	
            	// Get note to draw on this iteration
            	NoteView noteView = (NoteView) mScale.mItems.elementAt(i);
            	Drawable graphic;
        		graphic = mWholeStepGraphicImage;
            	if (noteView.halfWholeStepGraphic == Scale.HALF_STEP) 
            		graphic = mHalfStepGraphicImage;
            	
            	// Draw graphic
            	mGraphicPoint.setPolar(mOutsideRadius * 7 / 8, degToRad(omega  + mAngleIncrement/2 ));
                int graphicWidth = graphic.getIntrinsicWidth();
                int graphicHeight = graphic.getIntrinsicHeight();
                int left = (int) mGraphicPoint.x - graphicWidth / 2;
                int top = (int) mGraphicPoint.y - graphicHeight / 2;
                int right = left +  graphicWidth;
                int bottom = top +  graphicHeight;
	            canvas.save();
	            canvas.rotate((float) (omega + 90  + mAngleIncrement/2 ) , mGraphicPoint.x, mGraphicPoint.y);
	            graphic.setBounds(left, top, right, bottom);
	            graphic.draw(canvas);
	            canvas.restore();
	            
            	// Draw note name
            	mNamePoint.setPolar(mOutsideRadius * 6 / 8, degToRad(omega));

            	if (noteView.selected)
            		canvas.drawText(noteView.name, mNamePoint.x, mNamePoint.y + 8, mSelectedTextPaint);	
            	else
            		canvas.drawText(noteView.name, mNamePoint.x, mNamePoint.y + 8, mTextPaint);
            	
            	// Save screen position of label for touch detection
            	noteView.screenPosX = (int) mNamePoint.x;
            	noteView.screenPosY = (int) mNamePoint.y;
            }
        	
        	
        	try
			{
				// Draw Root and Mode
				NoteView noteView = (NoteView) mScale.mItems.elementAt(mSelectedItemNum);
				final String ident = new String(mScale.mRoot + " (" +  noteView.title + ") " + noteView.majorMinor);

				mTextViewMode.post(new Runnable() {

					@Override
					public void run() {
						mTextViewMode.setText(ident);						
					}
					
				});

	    		
	    		
//				canvas.drawText(ident, mCenterX - 20, mCanvasHeight - 20, mTextPaint);
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
        	
        	
        	
        	
        	
        }


    } // End Thread 
	
      
}
