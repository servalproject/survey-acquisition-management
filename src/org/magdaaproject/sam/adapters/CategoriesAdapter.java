/*
 * Copyright (C) 2013 The Serval Project
 * Portions Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the Serval SAM Software, a fork of the MaGDAA SAM software
 * which is located here: https://github.com/magdaaproject/survey-acquisition-management
 *
 * Serval SAM Software is free software; you can redistribute it and/or modify
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

import java.util.HashMap;

import org.servalproject.sam.R;
import org.magdaaproject.utils.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CategoriesAdapter extends SimpleCursorAdapter {
	
	/*
	 * private class level variables
	 */
	private String[] from;
	private int[] to;
	private OnClickListener parent;
	private String iconPath;
	private HashMap<String, Bitmap> iconCache;

	/*
	 * standard constructor
	 */
	public CategoriesAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		
		// store reference to index variables
		this.from = from;
		this.to = to;
		
		this.parent = (OnClickListener) context;
		
		iconPath = Environment.getExternalStorageDirectory().getPath();
		iconPath += context.getString(R.string.system_file_path_icons);
		
		iconCache = new HashMap<String, Bitmap>();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		// add the title and description
		TextView mTextView = (TextView) view.findViewById(to[0]);
		mTextView.setText(cursor.getString(cursor.getColumnIndex(from[1])));
		
		mTextView = (TextView) view.findViewById(to[1]);
		mTextView.setText(cursor.getString(cursor.getColumnIndex(from[2])));
		
		// get the bitmap representation of the icon, either from cache
		// or from the file system
		String mIconFileName = cursor.getString(cursor.getColumnIndex(from[3]));
		
		Bitmap mIconBitmap = null;
		
		if(iconCache.containsKey(mIconFileName) == true) {
			mIconBitmap = iconCache.get(mIconFileName);
		} else {
			String mFullIconPath = iconPath + mIconFileName;
			
			if(FileUtils.isFileReadable(mFullIconPath) == true) {
				mIconBitmap = BitmapFactory.decodeFile(mFullIconPath);
			}
		}
		
		// use the bitmap to replace the default image if available
		if(mIconBitmap != null) {
			ImageView mImageView = (ImageView) view.findViewById(to[2]);
			mImageView.setImageBitmap(mIconBitmap);
		}
		
		view.setOnClickListener(parent);
		view.setTag(
				cursor.getString(cursor.getColumnIndex(from[0])) 
				+ "|" + cursor.getString(cursor.getColumnIndex(from[1])));	
	}
}
