package org.servalproject.succinctdata;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.magdaaproject.sam.RCLauncherActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
 
public class SuccinctDataQueueDbAdapter {
 
 public static final String KEY_ROWID = "_id";
 public static final String KEY_PREFIX = "prefix";
 public static final String KEY_FORM = "form";
 public static final String KEY_TIMESTAMP = "timestamp";
 public static final String KEY_SUCCINCTDATA = "succinctdata";
 public static final String KEY_XMLDATA = "xmldata";
 
 private static final String TAG = "SuccinctDataQueueDbAdapter";
 private DatabaseHelper mDbHelper;
 private SQLiteDatabase mDb;
 
 private static final String DATABASE_NAME = "SuccinctDataQueue";
 private static final String SQLITE_TABLE = "QueuedMessages";
 private static final int DATABASE_VERSION = 3;
 
 private final Context mCtx;
 
 private static final String DATABASE_CREATE =
  "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
  KEY_ROWID + " integer PRIMARY KEY autoincrement," +
  KEY_PREFIX + "," +
  KEY_FORM + "," +
  KEY_TIMESTAMP + "," +
  KEY_SUCCINCTDATA + "," +
  KEY_XMLDATA + "," +
  " UNIQUE (" + KEY_PREFIX +"));";
 
 private static class DatabaseHelper extends SQLiteOpenHelper {
 
  DatabaseHelper(Context context) {
   super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
 
 
  @Override
  public void onCreate(SQLiteDatabase db) {
   Log.w(TAG, DATABASE_CREATE);
   db.execSQL(DATABASE_CREATE);
  }
 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
   Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
     + newVersion + ", which will destroy all old data");
   db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
   onCreate(db);
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
 
 public static String getCurrentTimeStamp(){
	 try {

		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

		 return currentTimeStamp;
	    	} catch (Exception e) {
	        e.printStackTrace();

	        return null;
	    }
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
  if (inputText == null  ||  inputText.length () == 0)  {
   mCursor = mDb.query(SQLITE_TABLE, new String[] {KEY_ROWID,
     KEY_PREFIX, KEY_FORM, KEY_TIMESTAMP, KEY_SUCCINCTDATA, KEY_XMLDATA}, 
     null, null, null, null, null);
 
  }
  else {
   mCursor = mDb.query(true, SQLITE_TABLE, new String[] {KEY_ROWID,
     KEY_PREFIX, KEY_FORM, KEY_TIMESTAMP, KEY_SUCCINCTDATA, KEY_XMLDATA}, 
     KEY_PREFIX + " like '%" + inputText + "%'", null,
     null, null, null, null);
  }
  if (mCursor != null) {
   mCursor.moveToFirst();
  }
  return mCursor;
 
 }
 
 public long getMessageQueueLength() {
	 String query = "Select count(*) from "+ SQLITE_TABLE;
	 SQLiteStatement statement = mDb.compileStatement(query);
	 long count = statement.simpleQueryForLong();
	 return count;
 }
 
 public Cursor fetchAllMessages() {
 
  Cursor mCursor = mDb.query(SQLITE_TABLE, new String[] {KEY_ROWID,
    KEY_PREFIX, KEY_FORM, KEY_TIMESTAMP, KEY_SUCCINCTDATA, KEY_XMLDATA}, 
    null, null, null, null, null);
 
  if (mCursor != null) {
   mCursor.moveToFirst();
  }
  return mCursor;
 }

public void delete(String piece) {
	// Delete message using piece text as key
	mDb.delete(SQLITE_TABLE, "SUCCINCTDATA=?", new String[] {piece});
	RCLauncherActivity.set_message_queue_length(this.getMessageQueueLength());
}
 

	
}
