/*****************************************************************
ViewItem

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
