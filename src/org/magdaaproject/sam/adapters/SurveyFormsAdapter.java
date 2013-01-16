/*
 * Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the MaGDAA SAM Software
 *
 * MaGDAA SAM Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.magdaaproject.sam.adapters;

import org.magdaaproject.sam.SurveyFormsActivity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

/**
 * manage the mapping between data and view for the list of event survey forms
 * @author techxplorer
 *
 */
public class SurveyFormsAdapter extends SimpleCursorAdapter {
	
	/*
	 * private class level variables
	 */
	private String[] from;
	private int[] to;
	private SurveyFormsActivity parent;
	

	/*
	 * standard constructor
	 */
	public SurveyFormsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		
		this.from = from;
		this.to = to;
		
		// cast the context to the appropriate type so the methods we expect are available
		this.parent = (SurveyFormsActivity) context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		// populate button text and set onclick handler
		Button mButton = (Button) view.findViewById(to[0]);
		mButton.setText(cursor.getString(cursor.getColumnIndex(from[0])));
		
		mButton.setOnClickListener(parent);
		mButton.setTag(cursor.getPosition());
		
	}
	 
	
}
