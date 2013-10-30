package com.weemo.sdk.helper.call;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.R;

/*
 * This activity is shown when a call is incoming.
 * It is created by the service, so, it can be created while the app is in the background.
 * 
 * It displays a dialog that asks the user if he accepts or rejects the call.
 */
public class IncomingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_incoming);
		
		setFinishOnTouchOutside(false);

		setTitle(getIntent().getStringExtra("displayName"));
		
		findViewById(R.id.answer).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				// get the call by its id (provided to the activity in the intent by the service)
				WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				WeemoCall call = weemo.getCall(getIntent().getIntExtra("callId", 0));
				if (call != null) {
					// Accepts the call
					call.resume();
					
					startActivity(
						new Intent(IncomingActivity.this, CallActivity.class)
							.putExtra("callId", call.getCallId())
					);
				}
				finish();
			}
		});

		findViewById(R.id.decline).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				// get the call by its id (provided to the activity in the intent by the service)
				WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				WeemoCall call = weemo.getCall(getIntent().getIntExtra("callId", 0));
				if (call != null)
					// Rejects the call
					call.hangup();
				finish();
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// This should always be the first statement of onStart
		Weemo.onActivityStart();

		// Register as event listener
		Weemo.eventBus().register(this);
	}

	@Override
	protected void onStop() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		// This should always be the last statement of onStop
		Weemo.onActivityStop();

		super.onStop();
	}

	/*
	 * This listener catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 */
	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		// First, we check that this event affects the call we are currently watching
		if (e.getCall().getCallId() != getIntent().getIntExtra("callId", -1))
			return ;
		// If the call has ended, we end this activity as it cannot be accepted anymore
		if (e.getCallStatus() == CallStatus.ENDED)
			finish();
	}

	@Override
	public void onBackPressed() {
		// Do nothing
	}
}
