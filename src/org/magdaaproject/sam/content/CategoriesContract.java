/*
 * Copyright (C) 2012 The MaGDAA Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the The MaGDAA Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SERVAL PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.magdaaproject.sam.content;

import android.net.Uri;
import android.provider.BaseColumns;

public class CategoriesContract {
	
	/**
	 * path component of the URI
	 */
	public static final String CONTENT_URI_PATH = "categories";

	/**
	 * content URI for the locations data
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + ItemsContentProvider.AUTHORITY + "/" + CONTENT_URI_PATH);

	/**
	 * content type for a list of items
	 */
	public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd." + ItemsContentProvider.AUTHORITY + "." + CONTENT_URI_PATH;

	/**
	 * content type for an individual item
	 */
	public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd." + ItemsContentProvider.AUTHORITY + "." + CONTENT_URI_PATH;

	/**
	 * table definition
	 */
	public static final class Table implements BaseColumns {

		/**
		 * name of the database table
		 */
		public static final String TABLE_NAME = CategoriesContract.CONTENT_URI_PATH;
		
		/**
		 * unique id column
		 */
		public static final String _ID = BaseColumns._ID;
		
		/**
		 * the unique config id
		 */
		public static final String CONFIG_ID = "config_id";
		
		/**
		 * category id column
		 */
		public static final String CATEGORY_ID = "category_id";
		
		/**
		 * category title column
		 */
		public static final String TITLE = "title";
		
		/**
		 * category description column
		 */
		public static final String DESCRIPTION = "description";
		
		/**
		 * category icon column
		 */
		public static final String ICON = "icon";
	}
}
