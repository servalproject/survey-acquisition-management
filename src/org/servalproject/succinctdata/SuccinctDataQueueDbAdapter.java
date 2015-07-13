package org.servalproject.succinctdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.magdaaproject.sam.RCLauncherActivity;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuccinctDataQueueDbAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_PREFIX = "prefix";
	public static final String KEY_FORM = "form";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_SUCCINCTDATA = "succinctdata";
	public static final String KEY_XMLDATA = "xmldata";
	public static final String KEY_HASH = "thinghash";
	public static final String COL_STATUS = "status";
	public static final String COL_INREACH_ID = "inreach_id";

	public static final String STATUS_SMS_QUEUED = "SMS_QUEUED";
	public static final String STATUS_SMS_SENT = "SMS_SENT";
	public static final String STATUS_INREACH_QUEUED = "INREACH_QUEUED";
	public static final String STATUS_INREACH_SENT = "INREACH_SENT";

	private static final String TAG = "SuccinctDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "SuccinctDataQueue";
	private static final String SQLITE_TABLE = "QueuedMessages";
	private static final String SQLITE_DEDUP_TABLE = "SentThings";

	private static final int DATABASE_VERSION = 5;

	private final Context mCtx;

	private static final String DATABASE_CREATE =
			"CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
					KEY_ROWID + " integer PRIMARY KEY autoincrement," +
					KEY_PREFIX + "," +
					KEY_FORM + "," +
					KEY_TIMESTAMP + "," +
					KEY_SUCCINCTDATA + "," +
					KEY_XMLDATA + "," +
					COL_STATUS + "," +
					COL_INREACH_ID + "," +
					" UNIQUE (" + KEY_PREFIX + "));";

	private static final String DATABASE_DEDUP_CREATE =
			"CREATE TABLE if not exists " + SQLITE_DEDUP_TABLE + " (" +
					KEY_ROWID + " integer PRIMARY KEY autoincrement," +
					KEY_HASH + "," +
					" UNIQUE (" + KEY_HASH + "));";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}


		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.w(TAG, DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_DEDUP_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion );
			switch (oldVersion) {
				case 4:
					db.execSQL("Alter table " + SQLITE_TABLE + " add column " + KEY_XMLDATA);
					db.execSQL("Alter table " + SQLITE_TABLE + " add column " + COL_STATUS);
					db.execSQL("Alter table " + SQLITE_TABLE + " add column " + COL_INREACH_ID);
			}
		}
	}

	public SuccinctDataQueueDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public SuccinctDataQueueDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	public static String getCurrentTimeStamp() {
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

			return currentTimeStamp;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	// The following function is from: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String stringHash(String s) {
		try {
			byte[] b = s.getBytes("iso-8859-1");
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			md.update(b, 0, b.length);
			byte[] sha1hash = md.digest();
			return bytesToHex(sha1hash);
		} catch (Exception e) {
			return null;
		}
	}

	public long rememberThing(String thing) {
		String md5sum = stringHash(thing);

		// Mark this record as having been queued so that we don't queue it again
		ContentValues dedup = new ContentValues();
		if (md5sum != null) {
			dedup.put(KEY_HASH, md5sum);
			return mDb.insert(SQLITE_DEDUP_TABLE, null, dedup);
		} else return -1L;

	}

	public boolean isThingNew(String thing) {
		String md5sum = stringHash(thing);

		Cursor cursor = mDb.rawQuery("SELECT " + KEY_HASH + " FROM " + SQLITE_DEDUP_TABLE + " WHERE " + KEY_HASH + " = '" + md5sum + "'", null);
		if (cursor.getCount() > 0)
			return false;
		else
			return true;
	}

	public long createQueuedMessage(String prefix, String succinctData, String formNameAndVersion,
									String xmlData) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PREFIX, prefix);
		initialValues.put(KEY_FORM, prefix);
		initialValues.put(KEY_TIMESTAMP, getCurrentTimeStamp());
		initialValues.put(KEY_SUCCINCTDATA, succinctData);
		initialValues.put(KEY_XMLDATA, xmlData);

		return mDb.insert(SQLITE_TABLE, null, initialValues);
	}

	public Cursor fetchSuccinctDataByName(String inputText) throws SQLException {
		Log.w(TAG, inputText);
		Cursor mCursor = null;
		if (inputText == null || inputText.length() == 0) {
			mCursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID,
							KEY_PREFIX, KEY_FORM, KEY_TIMESTAMP, KEY_SUCCINCTDATA, KEY_XMLDATA},
					null, null, null, null, null);

		} else {
			mCursor = mDb.query(true, SQLITE_TABLE, new String[]{KEY_ROWID,
							KEY_PREFIX, KEY_FORM, KEY_TIMESTAMP, KEY_SUCCINCTDATA, KEY_XMLDATA},
					KEY_PREFIX + " like '%" + inputText + "%'", null,
					null, null, null, null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public Cursor getMessageCounts() {
		return mDb.rawQuery("SELECT "+COL_STATUS+", count(*) from "+SQLITE_TABLE, null);
	}

	public Cursor fetchAllMessages() {

		Cursor mCursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID,
						KEY_PREFIX, KEY_FORM, KEY_TIMESTAMP, KEY_SUCCINCTDATA, KEY_XMLDATA, COL_STATUS, COL_INREACH_ID},
				null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public void delete(long rowId) {
		// Delete message using row id as key
		mDb.delete(SQLITE_TABLE, KEY_ROWID +" = ? ", new String[]{Long.toString(rowId)});
	}

	public void update(long rowId, ContentValues values) {
		update(KEY_ROWID +" = ? ", new String[]{Long.toString(rowId)}, values);
	}

	public void update(String where, String cols[], ContentValues values) {
		mDb.update(SQLITE_TABLE, values, where, cols);
	}
}
