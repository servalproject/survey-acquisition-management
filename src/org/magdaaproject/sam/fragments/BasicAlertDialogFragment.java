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
package org.magdaaproject.sam.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * a basic alert dialog fragment showing a title, description and an OK button
 * 
 * use the setArguments(Bundle) method to pass a bundle with the following two items
 * "message" for the message of the dialog
 * "title" for the title of the message
 */
public class BasicAlertDialogFragment extends DialogFragment {
	
	/*
	 * (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		if(savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		
		String mMessage = savedInstanceState.getString("message");
		String mTitle = savedInstanceState.getString("title");
		
		// create and return the dialog
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
		
		mBuilder.setMessage(mMessage)
		.setCancelable(false)
		.setTitle(mTitle)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return mBuilder.create();
		
	}
	

}
