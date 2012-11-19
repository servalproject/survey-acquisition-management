/*
 * Copyright (C) 2012 The MaGDAA Project
 *
 * This file is part of the MaGDAA MEM Software
 *
 * MaGDAA MEM Software is free software; you can redistribute it and/or modify
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
package org.magdaaproject.sam.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * main class for managing the creation of, and access to, the database
 */
public class MainDatabaseHelper extends SQLiteOpenHelper {

	/*
	 * public class level constants
	 */

	/**
	 * name of the actual database file
	 */
	public static final String DB_NAME = "magdaa-sam.db";

	/**
	 * current version of the database file
	 */
	public static final int DB_VERSION = 1;
	
	/*
	 * private class level constants
	 */
	private static final String sConfigsTableCreate = "CREATE TABLE " +
			ConfigsContract.Table.TABLE_NAME + " ( " +
			ConfigsContract.Table._ID + " INTEGER PRIMARY KEY, " +
			ConfigsContract.Table.TITLE + " TEXT, " + 
			ConfigsContract.Table.DESCRIPTION + " TEXT, " +
			ConfigsContract.Table.VERSION + " TEXT, " +
			ConfigsContract.Table.AUTHOR + " TEXT, " +
			ConfigsContract.Table.AUTHOR_EMAIL + " TEXT, " +
			ConfigsContract.Table.GENERATED_DATE + " TEXT)";
	
	private static final String sCategoriesTableCreate = "CREATE TABLE " +
			CategoriesContract.Table.TABLE_NAME + " ( " +
			CategoriesContract.Table._ID + " INTEGER PRIMARY KEY, " +
			CategoriesContract.Table.CONFIG_ID + " INTEGER, " +
			CategoriesContract.Table.CATEGORY_ID + " INTEGER, " +
			CategoriesContract.Table.TITLE + " TEXT, " + 
			CategoriesContract.Table.DESCRIPTION + " DESCRIPTION, " +
			CategoriesContract.Table.ICON + " BLOB)";
	
	private static final String sFormsTableCreate = "CREATE TABLE " +
			FormsContract.Table.TABLE_NAME + " (" +
			FormsContract.Table._ID + " INTEGER PRIMARY KEY, " +
			FormsContract.Table.FORM_ID + " INTEGER, " +
			FormsContract.Table.CATEGORY_ID + " INTEGER, " +
			FormsContract.Table.TITLE + " TEXT, " +
			FormsContract.Table.DESCRIPTION + " TEXT, " +
			FormsContract.Table.XFORMS_FILE + " TEXT, " +
			FormsContract.Table.ICON + " BLOB)";

	/**
	 * constructs a new MainDatabaseHelper object
	 * 
	 * @param context the context in which the database should be constructed
	 */
	MainDatabaseHelper(Context context) {
		// context, database name, factory, db version
		super(context, DB_NAME, null, DB_VERSION);
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		// create the table and index
		db.execSQL(sConfigsTableCreate);
		db.execSQL(sCategoriesTableCreate);
		db.execSQL(sFormsTableCreate);
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// add code to update the database if necessary
	}
}
