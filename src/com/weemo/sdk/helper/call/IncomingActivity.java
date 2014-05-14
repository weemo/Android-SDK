package com.weemo.sdk.helper.call;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.HelperApplication;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.contacts.ContactsActivity;

/**
 * This activity is shown when a call is incoming.
 * It is created by the service, so, it can be created while the app is in the background.
 *
 * It displays a dialog that asks the user if he accepts or rejects the call.
 */
public class IncomingActivity extends Activity {

	/** Key of required extra string for intent: default display name */
	public static final String EXTRA_DISPLAYNAME = "displayName";

	/** Key of required extra int for intent: call identifier */
	public static final String EXTRA_CALLID = "callId";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		unlockDevice();
		
		HelperApplication.cancelCountDown();
		
		setContentView(R.layout.weemo_activity_incoming);

		setFinishOnTouchOutside(false);

		setTitle(getIntent().getStringExtra(EXTRA_DISPLAYNAME));

		findViewById(R.id.answer).setOnClickListener(new OnClickListener() {
			@Override public void onClick(final View arg0) {
				// get the call by its id (provided to the activity in the intent by the service)
				final WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				final WeemoCall call = weemo.getCall(getIntent().getIntExtra(EXTRA_CALLID, -1));
				if (call != null) {
					// Accepts the call
					call.resume();

					startActivity(
						new Intent(IncomingActivity.this, ContactsActivity.class)
						.putExtra(ContactsActivity.EXTRA_PICKUP, true)
						.putExtra(ContactsActivity.EXTRA_CALLID, call.getCallId())
						.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP)
					);
				}
				finish();
			}
		});

		findViewById(R.id.decline).setOnClickListener(new OnClickListener() {
			@Override public void onClick(final View arg0) {
				// get the call by its id (provided to the activity in the intent by the service)
				final WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				final WeemoCall call = weemo.getCall(getIntent().getIntExtra(EXTRA_CALLID, -1));
				if (call != null) {
					// Rejects the call
					call.hangup();
				}
				finish();
			}
		});

		// Register as event listener
		Weemo.eventBus().register(this);
	}

	
	/**
	 * Wakeup and unlock the device
	 */
	private void unlockDevice() {
		// This function is not completely working with Activity themed as a Dialog
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		// We could use KeyguardManager instead but it requires permissions...
		//		<uses-permission android:name="android.permission.WAKE_LOCK" />
		//	    <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
		//
		//	PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		//	WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), HelperApplication.class.getSimpleName());
		//	wakeLock.acquire();
		//	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		//	KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		//	KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(HelperApplication.class.getSimpleName());
		//	keyguardLock.disableKeyguard();
	}


	@Override
	protected void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	/**
	 * This listener catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCallStatusChanged(final CallStatusChangedEvent event) {
		// First, we check that this event affects the call we are currently watching
		if (event.getCall().getCallId() != getIntent().getIntExtra(EXTRA_CALLID, -1)) {
			return ;
		}
		// If the call has ended, we end this activity as it cannot be accepted anymore
		if (event.getCallStatus() == CallStatus.ENDED) {
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		// Do nothing
	}
}
