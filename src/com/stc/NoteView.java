package com.stc;

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
