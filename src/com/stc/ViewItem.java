package com.stc;

import android.graphics.drawable.Drawable;

public class ViewItem
{
	String title;
	String label;
	String majorMinor;
	int halfWholeStepGraphic;
	String selectedText;
	Drawable selectedGraphic;
	int screenPosX;
	int screenPosY;
	boolean selected;
	int string;
	int fret;
	
	ViewItem(String _title, String _majorMinor, String _label, int _halfWholeStepGraphic, String _selectedText, Drawable _selectedGraphic)
	{
		title = _title;
		label = _label;
		majorMinor = _majorMinor;
		halfWholeStepGraphic = _halfWholeStepGraphic;
		selectedText = _selectedText;
		selectedGraphic = _selectedGraphic;
		selected = false;
	}

	public ViewItem(ViewItem viewItem)
	{
		this.title = viewItem.title;
		this.label = viewItem.label;
		this.majorMinor = viewItem.majorMinor;
		this.halfWholeStepGraphic = viewItem.halfWholeStepGraphic;
		this.selectedText = viewItem.selectedText;
		this.selectedGraphic = viewItem.selectedGraphic;
		this.screenPosX = viewItem.screenPosX;
		this.screenPosY = viewItem.screenPosY;
		this.selected = viewItem.selected;
		this.string = viewItem.string;
		this.fret = viewItem.fret;
	}

	
	
}
