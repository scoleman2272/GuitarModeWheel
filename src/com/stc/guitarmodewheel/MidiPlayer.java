/*****************************************************************
MidiPlayer

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import com.stc.R;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MidiPlayer
{
	private static final String TAG = "MidiPlayer";
	String mFileName;
	Context mContext;
	File mFilePath;	
	private Hashtable<String, Integer> mStringNoteToMidiNote;
	byte[] mMidiData;
	boolean mReady = false;
	MediaPlayer mMediaPlayer;
	
	public MidiPlayer(Context mContext)
	{
		InputStream is;
	    mFilePath = mContext.getFilesDir();

	    // Get base midi data from file
	    try {
	        is = mContext.getResources().openRawResource(R.raw.c);
	        mMidiData = new byte[is.available()];
	        is.read(mMidiData);
	        is.close();
	        mReady = true;	        
	    }
		catch (Exception e2)	{
			Log.w(TAG, "Error reading Midi file" + e2.toString());
		}	    
		
		mStringNoteToMidiNote = new Hashtable<String, Integer>();	
		mStringNoteToMidiNote.put("C", 48);
		mStringNoteToMidiNote.put("C#", 49);
		mStringNoteToMidiNote.put("D", 50);
		mStringNoteToMidiNote.put("D#", 51);
		mStringNoteToMidiNote.put("E", 52);
		mStringNoteToMidiNote.put("F", 53);
		mStringNoteToMidiNote.put("F#", 54);
		mStringNoteToMidiNote.put("G", 55);
		mStringNoteToMidiNote.put("G#", 56);
		mStringNoteToMidiNote.put("A", 57);
		mStringNoteToMidiNote.put("A#", 58);
		mStringNoteToMidiNote.put("B", 59);
		
		this.mContext = mContext;

	}

	public void PlayNote(NoteView noteView)
	{
		String note = noteView.name;
		// Make sure we have Midi data
		if (!mReady)
			return;
	    // Get midi note (key)
	    int midiNote = mStringNoteToMidiNote.get(note);
	    // Adjust for octive
	    int octavesToShift = 4 - noteView.octave;
	    int notesToShift = octavesToShift * 12;
	    midiNote -= notesToShift;
	    
	    File file = new File(mFilePath, "note.midi");

        // Put in our own midi key
        mMidiData[0x175] = (byte) midiNote;
        mMidiData[0x17A] = (byte) midiNote;

        
	    try {
	        OutputStream os = new FileOutputStream(file);
	        
	        os.write(mMidiData);
	        os.close();	        
	        
	    	try
			{
	    		File file1 = new File(mFilePath, "note.midi");
	            FileInputStream fis = new FileInputStream(file1);
	    		
	            Cleanup();
	            mMediaPlayer = new MediaPlayer();
//		    	String s = file.getCanonicalPath();
//				mp.setDataSource(s);					// This should work but for some reason it doesn't
	            mMediaPlayer.setDataSource(fis.getFD());
	            mMediaPlayer.prepare();
	            mMediaPlayer.setVolume(200,200);
	            mMediaPlayer.start();
				fis.close();
				
	    		
	    		
			} catch (Exception e2)
			{
		        Log.w(TAG, "Error reading " + file.getPath() + " - " + e2.toString());
			}
	        
	    } catch (IOException e) {
	        Log.w(TAG, "Error writing " + file, e);
	    }	    
	}
	
	void Cleanup()
	{
		try
		{
			if (mMediaPlayer != null)
			{
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		} catch (Exception e)
		{
	        Log.w(TAG, e.toString());
		}
	}

}
