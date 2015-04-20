package com.delorme.inreachapp;

import java.util.List;

import org.servalproject.sam.R;
import com.delorme.inreachapp.utils.AutoScrollTextView;
import com.delorme.inreachapp.utils.LogEventHandler;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;

/**
 * An activity for for viewing the events from an inReach
 * in a scroll text view.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class DeviceLogActivity extends Activity implements LogEventHandler.Listener
{
   /**
    * Called when the activity is created
    * 
    * @author Eric Semle
    * @since inReachApp (07 May 2012)
    * @version 1.0
    * @bug AND-1009
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
       super.onCreate(savedInstanceState);
       
       setContentView(R.layout.log_activity);
       
       m_textView = (AutoScrollTextView)findViewById(R.id.debug_webView);
   }
   
   /**
    * Called when the activity is resumed.
    * 
    * @author Eric Semle
    * @since inReachApp (07 May 2012)
    * @version 1.0
    * @bug AND-1009
    */
   @Override
   public void onResume()
   {
       super.onResume();
       
       final LogEventHandler logger = LogEventHandler.getInstance();
       
       // update the auto scroll text view with the newest events
       final List<String> events = logger.getEvents();
       for (int i = m_eventCount; i < events.size(); ++i)
       {
           onNewEvent(events.get(i));
       }
       
       // listen for new events
       logger.setListener(this);
   }
   
   /**
    * Called when the activity is paused.
    * 
    * @author Eric Semle
    * @since inReachApp (07 May 2012)
    * @version 1.0
    * @bug AND-1009
    */
   @Override
   public void onPause()
   {
       super.onPause();
       
       // stop listening for events
       LogEventHandler logger = LogEventHandler.getInstance();
       logger.setListener(null);
   }
   
   /**
    * Invoked when the Log Event Handler receives a new event.
    * The event is appended to the end of the scrolling text view.
    * 
    * @param event A string that contains HTML and describes the event.
    * 
    * @author Eric Semle
    * @since inReachApp (07 May 2012)
    * @version 1.0
    * @bug AND-1009
    */
   @Override
   public void onNewEvent(final String event)
   {
       // This is invoked from the main thread, so it
       // is okay to append the text without a runnable
       m_textView.appendText(Html.fromHtml(event));
           
       // increment the event count
       ++m_eventCount;
   }
   
   //! A count of the number of events that have been added to the textview
   private int m_eventCount = 0;
      
   //! An auto scrolling text view
   private AutoScrollTextView m_textView = null;
}