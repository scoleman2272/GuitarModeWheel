/*****************************************************************
ModeWheele

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
package com.stc.guitarmodewheel;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.stc.R;
import com.stc.guitarmodewheel.WheeleView.WheeleThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// TODO: variable width frets
// 

// TODO: still issue with selecing single note then rotating
// TODO Fix where octave shifts in keys other than C (outside circle)
// TODO Fix issues with sleep on/off (See SpinnerActivity)
// TODO implement other patterns
// TODO Check all scales
// TODO: make icon background invisible
// DONE: fix MixoLydian
// DONE: select notes when neww scale is first chosen
// DONE: reposition mode indicator text
// DONE: Notes are left over when a new note from the wheel view is selected

// DONE Implement versioning
// DONE Implement About box (and versioning)
// DONE: Make fret indication change when new scale is chosen
// DONE: When a new mode is chosen highlight starting point
// DONE: Fixed logging tags
// DONE Main screen icon
// DONE Change scale selector to spinner (or something easier

// Low level want list
// TODO Add play button to automatically play whole scale
// TODO Implement Landscape mode

/**
 * @author Scott Coleman
 * 
 *         Main activity
 */
public class ModeWheele extends Activity {
	private static final int MENU_TOGGLE_SCALE = Menu.FIRST + 100;
	private static final String TAG = "ModeWheele";

	private WheeleView mWheeleView;
	private StaffView mStaffView;
	private FretboardView mFretboardView;
	private WheeleThread mWheeleThread;

	private String mApplicationVersion = "";
	private TextView mTextViewMode;	

	/**
	 * ArrayAdapter connects the spinner widget to array-based data.
	 */
	protected ArrayAdapter<CharSequence> mAdapter;

	/*
	 * (non-Javadoc) Called when the activity is first created
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mStaffView = (StaffView) findViewById(R.id.staff);
		mFretboardView = (FretboardView) findViewById(R.id.fretboard);

		mTextViewMode = (TextView)findViewById(R.id.textViewMode);
		
		
		mWheeleView = (WheeleView) findViewById(R.id.wheele);
		mWheeleView.SetStaffView(mStaffView);
		mWheeleView.SetFretboardView(mFretboardView);
		mWheeleView.setTextViewMode(mTextViewMode);

		Scale scale = new Scale("Ionian", "C");
		scale.setInitialStringAndFret(6, 1); // This also sets octave
		mWheeleView.SetScale(scale);

		// mWheeleView.setTextView((TextView) findViewById(R.id.TextView01));
		mWheeleThread = mWheeleView.getThread();
		mWheeleThread.doStart();

		// TextView fred = (TextView)findViewById(R.id.TextView01);
		// fred.setText("hi there this is a testr");
		// fred.setVisibility(View.VISIBLE);

		// AdView adView = (AdView) this.findViewById(R.id.adView);
		// AdRequest adRequest = new AdRequest.Builder()
		// .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		// .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")
		// .build();
		// adView.loadAd(adRequest);

		registerForContextMenu(mWheeleView);


		
		mFretboardView.setmParentActivity(this);

		Spinner spinner = (Spinner) findViewById(R.id.Spinner01);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.ScaleNotesLong, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		logVersion();

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE").build();
		adView.loadAd(adRequest);

	}

	/**
	 * Called when another view detects that a new note has been selected
	 * 
	 * @param noteView
	 *            - The note that was selected
	 */
	public void SelectionChanged(NoteView noteView) {
		mWheeleView.SelectionChanged(noteView);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	/**
	 * Retrieves the application version and writes it to the log file
	 */
	private void logVersion() {
		try {
			PackageManager packageManager = this.getPackageManager();
			PackageInfo info = packageManager.getPackageInfo(
					this.getPackageName(), 0);
			mApplicationVersion = info.versionName;
			Log.i(TAG, "Application Version: " + mApplicationVersion);
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.help:
			Intent intent1 = new Intent(this, InstructionsActivity.class);
			startActivity(intent1);
			return true;

		case R.id.toggleScaleSelector:
			Spinner spinner = (Spinner) findViewById(R.id.Spinner01);
			if (spinner == null)
				return true;
			if (spinner.getVisibility() == View.GONE)
				spinner.setVisibility(View.VISIBLE);
			else
				spinner.setVisibility(View.GONE);
			return true;

		case R.id.about:

			String content = "Scott Coleman\n\n";
			content += "Guitar Mode Wheel\n";
			content += "Application Version: " + mApplicationVersion + "\n";

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("About");
			alert.setMessage(content);
			alert.show();

			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// Make sure we have initialized wheeleView
			if (mWheeleView == null)
				return;

			
			String root = parent.getItemAtPosition(pos).toString();
			// if we're using the long form ten remove "scale of: "
			if (root.contains("Scale of: ")) {
				root = root.substring("Scale of: ".length());	
			}
			
			Scale scale = new Scale("Ionian", root);
			scale.setInitialStringAndFret(6, 1); // This also sets octave

	    	//String noteName = scale.mItems.elementAt(0).name;	
	    	scale.mItems.elementAt(0).selected = true;	    	
			
			// Use this to draw neck manually (Not used anymore!)
//			ImageView imageView = getImageForScale(scale);
//			View v = new ImageView(getBaseContext());
//			
//			imageView.setX(0);
//			imageView.setY(-200);
//			imageView.setMaxWidth(50);
//			FrameLayout layout = (FrameLayout) findViewById(R.id.FrameLayout01);
//			FrameLayout.LayoutParams iParams = (android.widget.FrameLayout.LayoutParams) layout.getLayoutParams();
//			FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(600,600);
//			fp.setMargins(75,135,300,300);
//			imageView.setLayoutParams(fp);
//			
//			layout.addView(imageView);
			
			// Use this to jsut the replace the one in the layout
			ImageView imageView = (ImageView) findViewById(R.id.imageViewNeck);
			setImageForScale(scale, imageView);
			
			
			
			mWheeleView.SetScale(scale);
			mWheeleView.ResetRotation();
			mWheeleView.redraw = true;
			mWheeleView.invalidate();
			Toast.makeText(parent.getContext(),
					"You have chosen scale: " + root, Toast.LENGTH_LONG).show();
			
    		mFretboardView.invalidate();
			
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}
	
	ImageView setImageForScale(Scale scale, ImageView imageView) {
		
		imageView.setImageResource(R.drawable.neck);
		
		switch (scale.mAbsoluteStartFret) {
		case 0:
			imageView.setImageResource(R.drawable.neck0);
			break;
		case 1:
			imageView.setImageResource(R.drawable.neck1);
			break;
		case 2:
			imageView.setImageResource(R.drawable.neck2);
			break;
		case 3:
			imageView.setImageResource(R.drawable.neck3);
			break;
		case 4:
			imageView.setImageResource(R.drawable.neck4);
			break;
		case 5:
			imageView.setImageResource(R.drawable.neck5);
			break;
		case 6:
			imageView.setImageResource(R.drawable.neck6);
			break;
		case 7:
			imageView.setImageResource(R.drawable.neck7);
			break;
		case 8:
			imageView.setImageResource(R.drawable.neck8);
			break;
		case 9:
			imageView.setImageResource(R.drawable.neck9);
			break;
		case 10:
			imageView.setImageResource(R.drawable.neck10);
			break;
		case 11:
			imageView.setImageResource(R.drawable.neck11);
			break;
		default:
			int i = 0;
			int j = i;
			
		}
		return imageView;
	}
	
}