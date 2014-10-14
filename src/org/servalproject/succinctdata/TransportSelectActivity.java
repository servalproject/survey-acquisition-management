package org.servalproject.succinctdata;

import org.servalproject.sam.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TransportSelectActivity extends Activity implements OnClickListener {

	@Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.transport_select_layout);

       Button mButton = (Button) findViewById(R.id.transport_select_cellulardata);
       mButton.setOnClickListener(this);
       
       mButton = (Button) findViewById(R.id.transport_select_sms);
       mButton.setOnClickListener(this);
       
       mButton = (Button) findViewById(R.id.transport_select_inreach);
       mButton.setOnClickListener(this);

       mButton = (Button) findViewById(R.id.transport_cancel);
       mButton.setOnClickListener(this);
}
	
	public void onClick(View view) {

            Intent mIntent;

            // determine which button was touched
            switch(view.getId()){
            }
    }

}
