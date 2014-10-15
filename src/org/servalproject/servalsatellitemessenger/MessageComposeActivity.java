package org.servalproject.servalsatellitemessenger;

import java.util.Date;

import org.servalproject.sam.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delorme.inreachapp.*;
import com.delorme.inreachcore.InReachManager;
import com.delorme.inreachcore.OutboundMessage;

public class MessageComposeActivity extends BaseTabActivity implements OnClickListener {

    private static final String TAG = null;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_compose);
        
		// get the thread id from the intent
		Intent mIntent = getIntent();
		
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(this);
		
		if (Intent.ACTION_SENDTO.equals(mIntent.getAction())) {
			Uri uri = mIntent.getData();
			String recipient = null;
			String messageBody = null;
			
			Log.v(TAG, "Received " + mIntent.getAction() + " " + uri.toString());
			if (uri != null) {
				if (uri.getScheme().equals("sms")
						|| uri.getScheme().equals("smsto")) {
					recipient = uri.getSchemeSpecificPart();
					recipient = recipient.trim();
					
					Log.v(TAG, "Parsed recipient " + recipient);
					
					final TextView recipientView = (TextView) findViewById(R.id.editText1);
					recipientView.setText(recipient);
					
					messageBody = mIntent.getStringExtra("sms_body");
					
					Log.v(TAG, "Parsed body " + messageBody);
					
					final TextView bodyView = (TextView) findViewById(R.id.editText2);
					bodyView.setText(messageBody);
					
				}
			}
		}
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.message_compose, menu);
        return true;
    }


	@Override
	public void onClick(View v) {
		// We only have one button, so just do stuff here.
		
        final InReachManager manager = getInReachManager();
        
        if (manager == null)
            return;
        
        // This is the "meat and potatoes" setup.
        final OutboundMessage message = new OutboundMessage();
        // Use PPAC free form for all free text or reference point messages.
        message.setAddressCode(OutboundMessage.AC_FreeForm);
        // This specific type of message is free text.
        message.setMessageCode(OutboundMessage.MC_FreeTextMessage);
        
        // This is kind of hacky, but this sample application uses
        // a static variable to increment the message identifier.
        // Another option would be to drive this with a table in a
        // database with an auto increment primary key. The table could
        // store when the message is successfully sent.
        message.setIdentifier(++ms_messageIdentifier);
        
		final TextView recipientView = (TextView) findViewById(R.id.editText1);
		final TextView bodyView = (TextView) findViewById(R.id.editText2);
        
        // Set an address to send it too.
        message.addAddress(recipientView.getText().toString());
        // What do we want to say?
        message.setText(bodyView.getText().toString());
        // The date and time the message was created.
        message.setDate(new Date());
        
        // fill in the location information. this is the location the
        // message was created at.
        if (!setMessageLocation(message))
        {
            // The location was not set. You could
            // - send anyways
            // - queue and wait for a location
            // - use the inReach location (if supported)
        }
        
        // queue the message for sending
        if (!manager.sendMessage(message))
        {
            Toast.makeText(this,
                "Failed to send free text message",
                Toast.LENGTH_LONG).show();
        }
        else
        {
            // view the event log
            finish();
        }        
        
	}
    
    /**
     * This static long is used to generate a unique identifier
     * for each message.
     */
    private static long ms_messageIdentifier = 0;
}
