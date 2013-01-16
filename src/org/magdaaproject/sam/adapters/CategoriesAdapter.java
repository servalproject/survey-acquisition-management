/*
 * Copyright (C) 2012 The MaGDAA Project
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

import org.magdaaproject.sam.R;

import android.content.Context;
import android.database.Cursor;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class CategoriesAdapter extends SimpleCursorAdapter {
	
	/*
	 * private class level variables
	 */
	private String[] from;
	private int[] to;
	private Context context;
	private OnClickListener parent;
	private String template;

	/*
	 * standard constructor
	 */
	public CategoriesAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		
		// store reference to index variables
		this.from = from;
		this.to = to;
		
		this.context = context;
		this.parent = (OnClickListener) context;
		
		template = context.getString(R.string.launcher_ui_btn_categories);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		// populate button text and set onclick handler
		Button mButton = (Button) view.findViewById(to[0]);
		String mButtonLabel = String.format(
				template, 
				cursor.getString(cursor.getColumnIndex(from[1])),
				cursor.getString(cursor.getColumnIndex(from[2]))
			);
		
		Log.i("info-1", template);
		Log.i("info-2", mButtonLabel);
				
		mButton.setText(Html.fromHtml(mButtonLabel));
		
		mButton.setOnClickListener(parent);
		mButton.setTag(
				cursor.getInt(cursor.getColumnIndex(from[0]))
			);
	}
}
