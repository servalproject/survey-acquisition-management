/*
 * Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the MaGDAA Library Software
 *
 * MaGDAA Library Software is free software; you can redistribute it and/or modify
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
package org.magdaaproject.utils.serval;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * a utility class which exposes methods for interacting with the Serval Mesh software
 */
public class ServalUtils {

	/*
	 * public class level constants
	 */
	
	/**
	 * identify the package name of the Serval Mesh software
	 */
	public static final String SERVAL_MESH_PACKAGE_NAME = "org.servalproject";
	
	/**
	 * check to see if the Serval Mesh software is installed
	 * 
	 * @param context a context object used to gain access to system resources
	 * @return true if the Serval Mesh software is installed
	 */
	public static boolean isServalMeshInstalled(Context context) {
		
		try {
			context.getPackageManager().getApplicationInfo(SERVAL_MESH_PACKAGE_NAME, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}

		return true;
	}
}
