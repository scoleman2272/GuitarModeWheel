/*****************************************************************
NoteView

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

import android.graphics.drawable.Drawable;

public class NoteView
{
	String title;
	String name;
	String majorMinor;
	int halfWholeStepGraphic;
	String selectedText;
	Drawable selectedGraphic;
	int screenPosX;
	int screenPosY;
	boolean selected;
	int string;
	int fret;
	int octave;
	

	NoteView()
	{
		title = "";
		name = "";
		majorMinor = "";
		halfWholeStepGraphic = 0;
		selectedText = "";
		selectedGraphic = null;
		selected = false;
		octave = 4;						// Default to octave 4
	}
	
	NoteView(String _title, String _majorMinor, String _label, int _halfWholeStepGraphic, String _selectedText, Drawable _selectedGraphic)
	{
		title = _title;
		name = _label;
		majorMinor = _majorMinor;
		halfWholeStepGraphic = _halfWholeStepGraphic;
		selectedText = _selectedText;
		selectedGraphic = _selectedGraphic;
		selected = false;
		octave = 4;						// Default to octave 4
	}

	public NoteView(NoteView noteView)
	{
		this.title = noteView.title;
		this.name = noteView.name;
		this.majorMinor = noteView.majorMinor;
		this.halfWholeStepGraphic = noteView.halfWholeStepGraphic;
		this.selectedText = noteView.selectedText;
		this.selectedGraphic = noteView.selectedGraphic;
		this.screenPosX = noteView.screenPosX;
		this.screenPosY = noteView.screenPosY;
		this.selected = noteView.selected;
		this.string = noteView.string;
		this.fret = noteView.fret;
		this.octave = noteView.octave;
	}
}
