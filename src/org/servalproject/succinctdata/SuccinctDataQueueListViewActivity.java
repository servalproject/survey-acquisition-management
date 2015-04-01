package org.servalproject.succinctdata;

import org.servalproject.sam.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
 
public class SuccinctDataQueueListViewActivity extends Activity  {

	 
	 private SuccinctDataQueueDbAdapter dbHelper;
	 private SimpleCursorAdapter dataAdapter;
	 private BroadcastReceiver myReceiver = null;
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.succinctdata_queue_listview);
	 
	  dbHelper = new SuccinctDataQueueDbAdapter(this);
	  dbHelper.open();

	  onResume(savedInstanceState);
	 }
	 
	 public void onResume(Bundle savedInstanceState) {
		 //Generate ListView from SQLite Database
		 displayListView();

		 myReceiver = new BroadcastReceiver() {
			 @Override
			 public void onReceive(Context context, Intent intent) {
				 Handler refresh = new Handler(Looper.getMainLooper());
				 refresh.post(new Runnable() {
				     public void run()
				     {
				    	 displayListView();
				     }
				 });
			 }
			
		 };
		 
		 IntentFilter f;
	     f = new IntentFilter("SD_MESSAGE_QUEUE_UPDATED");
	     f.addAction("SD_MESSAGE_QUEUE_UPDATED");
	     LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, f);
	 }
	 
	 public void onPause(Bundle savedInstanceState) {
		 unregisterReceiver(myReceiver);
	 }
	  
	 private void displayListView() {
	 	 
	  Cursor cursor = dbHelper.fetchAllMessages();
	 
	  // The desired columns to be bound
	  String[] columns = new String[] {
	    SuccinctDataQueueDbAdapter.KEY_FORM,
	    SuccinctDataQueueDbAdapter.KEY_TIMESTAMP,
	    SuccinctDataQueueDbAdapter.KEY_SUCCINCTDATA
	  };
	 
	  // the XML defined views which the data will be bound to
	  int[] to = new int[] { 
	    R.id.form,
	    R.id.timestamp,
	    R.id.succinctdata,
	  };
	 
	  // create the adapter using the cursor pointing to the desired data 
	  //as well as the layout information
	  dataAdapter = new SimpleCursorAdapter(
	    this, R.layout.succinctdata_message_info, 
	    cursor,columns, 
	    to
	    );
	 
	  ListView listView = (ListView) findViewById(R.id.succinctDataQueueListView);
	  // Assign adapter to ListView
	  listView.setAdapter(dataAdapter);
	 
	  listView.invalidate();	  
	 
	  listView.setOnItemClickListener(new OnItemClickListener() {
	   @Override
	   public void onItemClick(AdapterView<?> listView, View view, 
	     int position, long id) {
	   // Get the cursor, positioned to the corresponding row in the result set
	   Cursor cursor = (Cursor) listView.getItemAtPosition(position);
	 
	   // XXX Item was clicked on
	 
	   }
	  });
	 	   
	  dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {
	         public Cursor runQuery(CharSequence constraint) {
	             return dbHelper.fetchSuccinctDataByName(constraint.toString());
	         }
	     });
	 
	 }
}	
	
