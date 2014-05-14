package com.weemo.sdk.helper.call;

import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent.Error;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.call.CallFragment.TouchType;
import com.weemo.sdk.helper.contacts.ContactsActivity;

/**
 * This is the activity in which calls will take place for phone devices.
 * In the manifest, we have declared this activity landscape blocked.
 * This is because we don't want to handle system orientation (like in tablets).
 * In Android phones, the cameras always works best in landscape mode (this is their default mode).
 *
 * We will handle ourselves the rotation of the ui buttons to match the device rotation (in the fragment).
 */
public class CallActivity extends Activity implements DialogInterface.OnCancelListener {

	/**
	 * The current call
	 */
	protected @Nullable WeemoCall call;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The callId must be provided in the intent that started this activity
		// If it is not, we finish the activity
		final int callId = getIntent().getIntExtra(ContactsActivity.EXTRA_CALLID, -1);
		if (callId == -1) {
			finish();
			return ;
		}

		// Weemo must be initialized before starting this activity
		// If it is not, we finish the activity
		final WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			finish();
			return ;
		}

		// The call with the given ID must exist before starting this activity
		// If it is not, we finish the activity
		this.call = weemo.getCall(callId);
		if (this.call == null) {
			finish();
			return ;
		}

		if (savedInstanceState == null) {
			((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(true);
		}

		setTitle(this.call.getContactDisplayName());

		// Add the call window fragment
		if (savedInstanceState == null) {
			getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, CallFragment.newInstance(callId, TouchType.SLIDE_CONTROLS_FULLSCREEN, getResources().getInteger(R.integer.camera_correction)))
				.commit();
		}

		// Register as event listener
		Weemo.eventBus().register(this);
	}

	@Override
	public void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		// When we leave this activity, we stop the video.
		if (this.call != null) {
			this.call.setVideoOut(null);
			this.call.setVideoIn(null);
		}

		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// We allow leaving this activity only if specified
		if (getIntent().getBooleanExtra("canComeBack", false)) {
			super.onBackPressed();
		}
	}

	/**
	 * This listener catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCallStatusChanged(final CallStatusChangedEvent event) {
		// First, we check that this event concerns the call we are monitoring
		if (event.getCall().getCallId() != this.call.getCallId()) {
			return ;
		}

		// If the call has ended, we finish the activity (as this activity is only for an active call)
		if (event.getCallStatus() == CallStatus.ENDED) {
			finish();
		}
	}

	/**
	 * This listener catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCanCreateCallChanged(final CanCreateCallChangedEvent event) {
		final Error error = event.getError();
		if (error == CanCreateCallChangedEvent.Error.CLOSED) {
			Toast.makeText(this, error.description(), Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		this.call.hangup();
	}

}
