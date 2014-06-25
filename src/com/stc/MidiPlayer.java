package com.stc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MidiPlayer
{
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
			Log.w("ExternalStorage", "Error reading Midi file" + e2.toString());
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
		        Log.w("ExternalStorage", "Error reading " + file.getPath() + " - " + e2.toString());
			}
	        
	    } catch (IOException e) {
	        Log.w("", "Error writing " + file, e);
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
	        Log.w("", e.toString());
		}
	}

}