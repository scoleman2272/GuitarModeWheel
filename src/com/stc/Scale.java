/*****************************************************************
Scale

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

import java.util.Vector;

public class Scale
{
	public static final int SCALE_IONIAN  = 0;
	public static final int HALF_STEP  = 1;
	public static final int WHOLE_STEP  = 2;
	int mKeyIndex;
	int mModeIndex;
	int mStartRelativeFret = 1;
	int mStartString = 6;
	int mStartOctave = 4;
	int mAbsoluteStartFret = 1;
	Vector<NoteView> mItems;
	String mRoot;
	String mMode;
	
	
	public Scale(String mode, String root)
	{
		mMode = mode;
		mRoot = root;
		mItems = new Vector<NoteView>();
		// Find starting point
		mKeyIndex = getIndexOf(root, chromaticScaleSharps);
		mModeIndex = getIndexOf(mode, modes);
		
		if (mKeyIndex != -1 && mModeIndex != -1 )
		{
			int chromaticScaleIndex = mKeyIndex;		
			int currentModeIndex = mModeIndex;		
			for (int i = 0; i < 7; i++)
			{
		    	  NoteView v = new NoteView(modes[currentModeIndex], majorMinor[currentModeIndex], 
		    			   chromaticScaleSharps[chromaticScaleIndex], ionianSteps[currentModeIndex], null, null);
		    	  mItems.add(v);		
		    	  chromaticScaleIndex += ionianSteps[i];
		    	  currentModeIndex++;
			}
		}	
		
	}

	// For the next tables just repeat the entries twice to allow for starting a scale in the middle
	// (we could also just wrap the index below
	public static final String[] chromaticScaleSharps = {
			"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
			"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
			};
	public static final String[] modes = {
			"Ionian", "Dorian", "Phrygian", "Lydian", "MixoLydian", "Aoliian", "Locrian",
			"Ionian", "Dorian", "Phrygian", "Lydian", "MixoLydian", "Aoliian", "Locrian"
			};

	public static final String[] majorMinor = {
			"M", "m", "m", "M", "M", "m", "m",
			"M", "m", "m", "M", "M", "m", "m",
			};

	public static final String[] string1 = {
			"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
	};
	
	public static final String[][] stringsFrets = {
			{"E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#"},
			{"B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#"},
			{"G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"},
			{"D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#"},
			{"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"},
			{"E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#"},
	};
	
	public static final int[][] octaves = {
			{5,5,5,5,5,5,5,5,6,6,6,6},
			{4,5,5,5,5,5,5,5,5,5,5,5},
			{4,4,4,4,4,5,5,5,5,5,5,5},
			{4,4,4,4,4,4,4,4,4,4,5,5},
			{3,3,3,4,4,4,4,4,4,4,4,4},
			{3,3,3,3,3,3,3,3,4,4,4,4},
	};
	
	
	public static final int[] ionianSteps = {
			WHOLE_STEP,WHOLE_STEP,HALF_STEP,WHOLE_STEP,WHOLE_STEP,WHOLE_STEP,HALF_STEP,
			WHOLE_STEP,WHOLE_STEP,HALF_STEP,WHOLE_STEP,WHOLE_STEP,WHOLE_STEP,HALF_STEP
			};

	public static final int[] offsetForStrings = {4, 9, 2, 7, 11, 4};
	
	
	public void setInitialStringAndFret(int string, int fret)
	{
		mStartString = string;
		mAbsoluteStartFret = fret;
		if (mStartString > 0 && mStartString <= 6)
		{
			mStartString -= 1;
			String[] fretsForString = stringsFrets[mStartString];
			mAbsoluteStartFret = getIndexOf(mRoot, fretsForString);
			if (mAbsoluteStartFret != -1)
			{
				mStartOctave = octaves[mStartString][mAbsoluteStartFret];
			}
			else
			{
				// Defaults in case of error
				mStartOctave = 4; 
				mAbsoluteStartFret = 0;				
				
			}
		}
	}
	
	public int getOctave(int string, int fret)
	{
		mStartOctave = 4;
		string--;
		fret--;
		if (mStartString > 0 && mStartString <= 6)
		{
			if (fret < 12)
				mStartOctave = octaves[string][fret];
			else
				mStartOctave = octaves[string][fret - 12] + 1;
		}
		return mStartOctave;
	}
	
	public NoteView getPosition(String noteName, int string)
	{
		NoteView noteView = new NoteView();
		if (string > 0 && string <= 6)
		{
			int fret = getIndexOf(noteName, stringsFrets[string]);
			if (fret == -1) fret = 0;
			int octave = octaves[string][fret];
			noteView.fret = fret;
			noteView.octave = octave;
			noteView.name = noteName;
			noteView.string = string;
			
		}
	
		return noteView;
	}
	private int getIndexOf(String item, String[] items)
	{
		int result = -1;
		int max = items.length;
		for (int i = 0; i < max; i++)
		{
			if (items[i].compareTo(item) == 0)
				return i;
		}
		return result;
	}
	

}
