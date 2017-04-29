package org.servalproject.succinctdata;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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
 public static final String KEY_HASH = "thinghash";
 
 public static final String KEY_RECORD = "badrecord";

 
 private static final String TAG = "SuccinctDataQueueDbAdapter";
 private DatabaseHelper mDbHelper;
 private SQLiteDatabase mDb;
 
 private static final String DATABASE_NAME = "SuccinctDataQueue";
 private static final String SQLITE_TABLE = "QueuedMessages";
 private static final String SQLITE_DEDUP_TABLE = "SentThings";
 private static final String SQLITE_BADRECORD_TABLE = "IndigestibleRecords";
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
  " UNIQUE (" + KEY_PREFIX +"));";
 
 private static final String DATABASE_DEDUP_CREATE =
		  "CREATE TABLE if not exists " + SQLITE_DEDUP_TABLE + " (" +
		  KEY_ROWID + " integer PRIMARY KEY autoincrement," +
		  KEY_HASH + "," +
		  " UNIQUE (" + KEY_HASH +"));";

 private static final String DATABASE_BADRECORD_CREATE =
		  "CREATE TABLE if not exists " + SQLITE_BADRECORD_TABLE + " (" +
		  KEY_ROWID + " integer PRIMARY KEY autoincrement," +
		  KEY_FORM + "," +
		  KEY_RECORD + ");";
		  
 
 private static class DatabaseHelper extends SQLiteOpenHelper {
 
  DatabaseHelper(Context context) {
   super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
 
 
  @Override
  public void onCreate(SQLiteDatabase db) {
   Log.w(TAG, DATABASE_CREATE);
   db.execSQL(DATABASE_CREATE);
   db.execSQL(DATABASE_DEDUP_CREATE);
   db.execSQL(DATABASE_BADRECORD_CREATE);
  }
 
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
   Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
     + newVersion + ", which will destroy all old data");
   db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
   db.execSQL("DROP TABLE IF EXISTS " + SQLITE_DEDUP_TABLE);
   db.execSQL("DROP TABLE IF EXISTS " + SQLITE_BADRECORD_TABLE);
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
 
 // The following function is from: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
 final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
 public static String bytesToHex(byte[] bytes) {
     char[] hexChars = new char[bytes.length * 2];
     for ( int j = 0; j < bytes.length; j++ ) {
         int v = bytes[j] & 0xFF;
         hexChars[j * 2] = hexArray[v >>> 4];
         hexChars[j * 2 + 1] = hexArray[v & 0x0F];
     }
     return new String(hexChars);
 }
 
 public static String stringHash(String s)
 {
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
 
 public static String normalise(String thing)
 {
	 String normalised = thing.replaceAll("<lastsubmittime>.*</lastsubmittime>", "<lastsubmittime></lastsubmittime>");
	 normalised = normalised.replaceAll("<lastsubmittime>.*</lastsubmittime>", "<lastsubmittime></lastsubmittime>");
	 normalised = normalised.replaceAll("<longitude>.*</longitude>","<longitude></longitude>");
	 normalised = normalised.replaceAll("<latitude>.*</latitude>","<latitude></latitude>");
	 return normalised;
 }
 
 public long rememberThing(String thing)
 {
	 String md5sum = stringHash(normalise(thing));
	  
	  // Mark this record as having been queued so that we don't queue it again
	  ContentValues dedup = new ContentValues();
	  if (md5sum != null) {
		  dedup.put(KEY_HASH, md5sum);
		  if (mDb.insert(SQLITE_DEDUP_TABLE, null, dedup) == -1L) 
			  return -1L;
		  else
			  return 0L;
	  } else return -1L;

 }
 
 
 
 public boolean isThingNew(String thing)
 {
	 String normalised = normalise(thing);
	 
	 String md5sum = stringHash(normalised);
	  
	 Cursor cursor = mDb.rawQuery("SELECT "+ KEY_HASH +" FROM " + SQLITE_DEDUP_TABLE + " WHERE "+ KEY_HASH +" = '" + md5sum + "'", null);	 
	 if (cursor.getCount() > 0) 
		 return false; 
	 else 
		 return true;
 }
 
 public long createQueuedMessage(String prefix, String succinctData, String formNameAndVersion,
		 String xmlData) {
 
  // Do not queue if we have previously queued this records
	 if (!isThingNew(xmlData)) 
		 return 0;
	 
  ContentValues initialValues = new ContentValues();
  initialValues.put(KEY_PREFIX, prefix);
  initialValues.put(KEY_FORM, prefix);
  initialValues.put(KEY_TIMESTAMP, getCurrentTimeStamp());
  initialValues.put(KEY_SUCCINCTDATA, succinctData);
  initialValues.put(KEY_XMLDATA, xmlData);
  
  long result = mDb.insert(SQLITE_TABLE, null, initialValues);  
  
  RCLauncherActivity.enqueuedPiece();
  
  return 0;
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
	
	// First, get the xmlData that the piece is part of, and see how many other pieces refer to the same
	// record. When none are left, then we can record this record as having been sent, and ignore it in
	// future by storing its hash in the SentThings database table.
	// (the query to find out if this is the last piece of this record should be possible as a nested query)
	// XXX - This is not yet implemented. 		 
		 
	mDb.delete(SQLITE_TABLE, "SUCCINCTDATA=?", new String[] {piece});
	RCLauncherActivity.set_message_queue_length(this.getMessageQueueLength());
}
 
public long logBadRecord(String form, String record) {

	 
 ContentValues initialValues = new ContentValues();
 initialValues.put(KEY_FORM, form);
 initialValues.put(KEY_RECORD, record);

 try {
 long result = mDb.insert(SQLITE_BADRECORD_TABLE, null, initialValues);
 } catch (Exception e) {
 
 }
  
 return 0;
}

public void deleteBadRecord(String form, String record) {
	
	mDb.delete(SQLITE_BADRECORD_TABLE, KEY_FORM+"=? AND "+ KEY_RECORD+"=?", new String[] {form,record});
}

public Cursor fetchAllBadRecords() {
	  Cursor mCursor = mDb.query(SQLITE_BADRECORD_TABLE, new String[] {KEY_ROWID,
			    KEY_FORM, KEY_RECORD}, 
			    null, null, null, null, null);
			 
			  if (mCursor != null) {
			   mCursor.moveToFirst();
			  }
			  return mCursor;
}


	
}
