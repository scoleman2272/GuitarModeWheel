package com.stc;



import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.stc.WheeleView.WheeleThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// TODO Fix where octave shifts in keys other than C (outside circle)
// TODO Fix issues with sleep on/off (See SpinnerActivity)
// TODO implement other patterns
// TODO Check all scales
// TODO Fix Landscape view
// TODO Implement About box (and versioning)
// TODO Main screen icon
// TODO Implement versioning



// Low level want list
// TODO Add play button to automatically play whole scale
// TODO Implement Landscape mode
// TODO Chang scale selector to spinner (or something easier

 	
/**
 * @author Scott Coleman
 *
 * Main activity
 */
public class ModeWheele extends Activity 
{
    private static final int MENU_TOGGLE_SCALE = Menu.FIRST+100;
	private static final String TAG = "BFDemo";
    
    
    private WheeleView mWheeleView;
    private StaffView mStaffView;
    private FretboardView mFretboardView;
    private WheeleThread mWheeleThread;

	private String mApplicationVersion = "";	
    
    /**
     * ArrayAdapter connects the spinner widget to array-based data.
     */
    protected ArrayAdapter<CharSequence> mAdapter;
    
    
	
    /* (non-Javadoc)
     * Called when the activity is first created
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);  
        
        mStaffView = (StaffView) findViewById(R.id.staff); 
        mFretboardView = (FretboardView) findViewById(R.id.fretboard); 
        
        mWheeleView = (WheeleView) findViewById(R.id.wheele);   
        mWheeleView.SetStaffView(mStaffView);
        mWheeleView.SetFretboardView(mFretboardView);

  	  	Scale scale = new Scale("Ionian", "C");
  	  	scale.setInitialStringAndFret(6,1); // This also sets octave
  	  	mWheeleView.SetScale(scale);
        
//  	  	mWheeleView.setTextView((TextView) findViewById(R.id.TextView01));        
        mWheeleThread = mWheeleView.getThread();   
        mWheeleThread.doStart();
        
//        TextView fred = (TextView)findViewById(R.id.TextView01);
//        fred.setText("hi there this is a testr");
//        fred.setVisibility(View.VISIBLE);
        
        
//        AdView adView = (AdView) this.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//            .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")
//            .build();
//        adView.loadAd(adRequest);        
        
        registerForContextMenu(mWheeleView);
        
        mFretboardView.setmParentActivity(this);

        
        Spinner spinner = (Spinner) findViewById(R.id.Spinner01);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.ScaleNotes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);    
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        logVersion();
        
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")
            .build();
        adView.loadAd(adRequest);        
        
        
    }    	

    /**
     * Called when another view detects that a new note has been selected
     * 
     * @param noteView - The note that was selected
     */
    public void SelectionChanged(NoteView noteView)
    {
    	mWheeleView.SelectionChanged(noteView);
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		  MenuInflater inflater = getMenuInflater();
		  inflater.inflate(R.menu.options_menu, menu);		
	}



	/* (non-Javadoc)
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
			PackageInfo info = packageManager.getPackageInfo(this.getPackageName(), 0);			
			mApplicationVersion = info.versionName;
			Log.i(TAG, "BioHeart Application Version: " + mApplicationVersion);
		} 
		catch (NameNotFoundException e) {
			   	Log.e(TAG, e.toString());
		}        
    }
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		  switch (item.getItemId()) 
		  {
		  		case R.id.help:
		    		Intent intent1 = new Intent(this, InstructionsActivity.class);
		    		startActivity(intent1);	 		  			
		  			return true;
		  
			    case R.id.toggleScaleSelector:
			    	Spinner spinner = (Spinner) findViewById(R.id.Spinner01);
			        if (spinner == null) return true;
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

	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
	    {
	    	// Make sure we have initialized wheeleView
	    	if (mWheeleView == null) return;
	    	
	    	String root = parent.getItemAtPosition(pos).toString();
	    	Scale scale = new Scale("Ionian", root);
	  	  	scale.setInitialStringAndFret(6,1); // This also sets octave			        
	        mWheeleView.SetScale(scale);
	        mWheeleView.ResetRotation();
	        mWheeleView.redraw = true;
	        mWheeleView.invalidate();	    	
	    	Toast.makeText(parent.getContext(), "You have chosen scale: " +  root, Toast.LENGTH_LONG).show();
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}    
}