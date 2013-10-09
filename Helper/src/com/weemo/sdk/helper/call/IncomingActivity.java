package com.weemo.sdk.helper.call;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.R;

public class IncomingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_incoming);
		
		setFinishOnTouchOutside(false);

		setTitle(getIntent().getStringExtra("displayName"));
		
		findViewById(R.id.answer).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				Weemo weemo = Weemo.instance();
				assert weemo != null;
				WeemoCall call = weemo.getCall(getIntent().getIntExtra("callId", 0));
				if (call != null) {
					call.resume();
					Log.i("NPEAVOID", "Starting CallActivity from Incoming");
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
				Weemo weemo = Weemo.instance();
				assert weemo != null;
				WeemoCall call = weemo.getCall(getIntent().getIntExtra("callId", 0));
				if (call != null)
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
		Weemo.getEventBus().register(this);
	}

	@Override
	protected void onStop() {
		// Unregister as event listener
		Weemo.getEventBus().unregister(this);

		// This should always be the last statement of onStop
		Weemo.onActivityStop();

		super.onStop();
	}

	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		if (e.getCall().getCallId() != getIntent().getIntExtra("callId", -1))
			return ;
		if (e.getCallStatus() == CallStatus.ENDED)
			finish();
	}

	@Override
	public void onBackPressed() {
		// Do nothing
	}
}
