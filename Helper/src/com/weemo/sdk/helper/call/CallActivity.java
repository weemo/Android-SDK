package com.weemo.sdk.helper.call;

import javax.annotation.Nullable;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoCall.VideoProfile;
import com.weemo.sdk.WeemoCall.VideoSource;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.event.call.ReceivingVideoChangedEvent;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.view.WeemoVideoInFrame;
import com.weemo.sdk.view.WeemoVideoOutPreviewFrame;

/*
 * This is the activity in which calls will take place.
 * In the manifest, we have declared this activity landscape blocked.
 * This is because we don't want to handle system orientation.
 * In Android, the cameras always work best in landscape mode (this is their default mode).
 * 
 * We will handle ourselves the rotation of the ui buttons to match the device rotation.
 * 
 * This activity exposes different buttons that control the current call.
 * It uses the Weemo API to control everything that relates to the call, the video and the audio OUT.
 * 
 * Weemo does not exposes api to control audio IN.
 * This activity uses Android's AudioManager to control everything that relates to audio IN.
 */
@SuppressWarnings("deprecation")
public class CallActivity extends Activity {

	// Buttons
	private @Nullable ImageView toggleIn;
	private @Nullable ImageView muteOut;
	private @Nullable ImageView video;
	private @Nullable ImageView videoToggle;
	private @Nullable ImageView hangup;
	private @Nullable ToggleButton hdToggle;
	
	// This is the correction for the OrientationEventListener.
	// It allows portrait devices (like phones) and landscape devices (like tablets)
	// to have the same orientation result.
	private int correction;
	
	// Whether or not the speakerphone is on.
	// This is use to toggle
	private boolean isSpeakerphoneOn;

	// The audio manager used to control audio
	private @Nullable AudioManager audioManager;

	// Used to rotate UI elements according to device orientation
	private @Nullable OrientationEventListener oel;
	
	// Used to receive Intent.ACTION_HEADSET_PLUG, which is when the headset is (un)plugged
	private @Nullable BroadcastReceiver br;
	
	// Both frames (declared in the XML) that will contain video OUT and IN
	private @Nullable WeemoVideoOutPreviewFrame videoOutFrame;
	private @Nullable WeemoVideoInFrame videoInFrame;

	/*
	 * Uses AudioManager to route audio to Speakerphone or not
	 */
	private void setSpeakerphoneOn(boolean on) {
		audioManager.setSpeakerphoneOn(on);
		toggleIn.setImageResource(on ? R.drawable.call_device_access_volume_on : R.drawable.call_device_access_volume_muted);
		isSpeakerphoneOn = on;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The callId must be provided in the intent that started this activity
		// If it is not, we finish the activity
		final int callId = getIntent().getIntExtra("callId", -1);
		if (callId == -1) {
			finish();
			return ;
		}

		// Weemo must be initialized before starting this activity
		// If it is not, we finish the activity
		WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			finish();
			return ;
		}

		// The call with the given ID must exist before starting this activity
		// If it is not, we finish the activity
		final WeemoCall call = weemo.getCall(callId);
		if (call == null) {
			finish();
			return ;
		}
		
		setContentView(R.layout.activity_call);

		// Get the OUT frame from the inflated view and set the call to use it
		videoOutFrame = (WeemoVideoOutPreviewFrame) findViewById(R.id.video_out);
		call.setVideoOut(videoOutFrame);

		// Get the IN frame from the inflated view and set the call to use it
		// We set its display to follow device orientation because we have blocked the device rotation
		videoInFrame = (WeemoVideoInFrame) findViewById(R.id.video_in);
		videoInFrame.setDisplayFollowDeviceOrientation(true);
		call.setVideoIn(videoInFrame);

		setTitle(call.getContactDisplayName());
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// This will call setOrientation each time the device orientation have changed
		// This allows us to display the control ui buttons in the correct orientation
		oel = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			int lastOrientation = -1;
			@Override public void onOrientationChanged(int orientation) {
				if (orientation > 45 && orientation <= 135)
					orientation = 270;
				else if (orientation > 135 && orientation <= 225)
					orientation = 180;
				else if (orientation > 225 && orientation <= 315)
					orientation = 90;
				else if (orientation > 315 || orientation <= 45)
					orientation = 0;
				orientation = (orientation + 360 - correction) % 360;
				if (lastOrientation != orientation) {
					setOrientation(orientation);
					lastOrientation = orientation;
				}
			}
		};
		
		// Simple brodcast receiver that will call setSpeakerphoneOn when receiving an intent
		// It will be registered for Intent.ACTION_HEADSET_PLUG intents
		br = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());
			}
		};
		
		// Button that toggles audio route
		toggleIn = (ImageView) findViewById(R.id.toggle_in);
		toggleIn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				setSpeakerphoneOn(!isSpeakerphoneOn);
			}
		});

		// Button that toggles audio OUT mute
		muteOut = (ImageView) findViewById(R.id.mute_out);
		muteOut.setOnClickListener(new OnClickListener() {
			boolean mute = false;
			@Override public void onClick(View v) {
				mute = !mute;
				if (mute) {
					call.audioMute();
					muteOut.setImageResource(R.drawable.call_device_access_mic_muted);
				}
				else {
					call.audioUnMute();
					muteOut.setImageResource(R.drawable.call_device_access_mic_on);
				}
			}
		});

		// Button that toggles sending video
		// Note that we also toggle the videoOutFrame visibility
		video = (ImageView) findViewById(R.id.video);
		video.setOnClickListener(new OnClickListener() {
			boolean video = false;
			@Override public void onClick(View v) {
				video = !video;
				if (video) {
					videoOutFrame.setVisibility(View.VISIBLE);
					call.videoStart();
					videoToggle.setVisibility(View.VISIBLE);
				}
				else {
					videoOutFrame.setVisibility(View.GONE);
					call.videoStop();
					videoToggle.setVisibility(View.GONE);
				}
			}
		});

		// Button that toggles sending video source
		videoToggle = (ImageView) findViewById(R.id.video_toggle);
		videoToggle.setOnClickListener(new OnClickListener() {
			boolean front = true;
			@Override public void onClick(View v) {
				front = !front;
				call.setVideoSource(front ? VideoSource.FRONT : VideoSource.BACK);
			}
		});

		// Button that hangs up the call
		hangup = (ImageView) findViewById(R.id.hangup);
		hangup.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				call.hangup();
			}
		});

		// Button that toggles if we want to receive HD video or not
		hdToggle = (ToggleButton) findViewById(R.id.hd_toggle);
		hdToggle.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				boolean hd = hdToggle.isChecked();
				call.setInVideoProfile(hd ? VideoProfile.HD : VideoProfile.SD);
			}
		});
		
		// By default, we set the audio IN route according to isWiredHeadsetOn
		setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());

		// Get the correction for the OrientationEventListener
		switch (getWindowManager().getDefaultDisplay().getRotation()) {
			case Surface.ROTATION_0:   correction = 0;   break ;
			case Surface.ROTATION_90:  correction = 90;  break ;
			case Surface.ROTATION_180: correction = 180; break ;
			case Surface.ROTATION_270: correction = 270; break ;
		}

		// Sets the camera preview dimensions according to whether or not the remote contact has started his video
		setVideoOutFrameDimensions(call.isReceivingVideo());
	}

	/*
	 * Sets the dimension preview dimensions according to whether or not we are receiving video.
	 * If we are, we need to have a small preview.
	 * If we are not, the preview needs to fill the space.
	 */
	private void setVideoOutFrameDimensions(boolean isReceivingVideo) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		LayoutParams params = videoOutFrame.getLayoutParams();
		if (isReceivingVideo) {
			params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32 * 4, metrics);
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24 * 4, metrics);
		}
		else {
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.MATCH_PARENT;
		}
		
		videoOutFrame.setLayoutParams(params);
	}

	/*
	 * Animate the property of an object.
	 * may first add or remove 360 degrees to the property
	 * to ensure that the property will rotate in the right direction
	 */
	private void animate(View view, String property, float current, int angle) {
		if (angle - current > 180)
			ObjectAnimator.ofFloat(view, property, current + 360).setDuration(0).start();
		else if (current - angle > 180)
			ObjectAnimator.ofFloat(view, property, current - 360).setDuration(0).start();
		
		ObjectAnimator.ofFloat(view, property, angle).start();
	}

	// Sets orientation of all UI elements
	// This is called by the OrientationEventListener
	private void setOrientation(int orientation) {
		animate(toggleIn,    "rotation", toggleIn.getRotation(),    orientation);
		animate(muteOut,     "rotation", muteOut.getRotation(),     orientation);
		animate(video,       "rotation", video.getRotation(),       orientation);
		animate(videoToggle, "rotation", videoToggle.getRotation(), orientation);
		animate(hangup,      "rotation", hangup.getRotation(),      orientation);
		animate(hdToggle,    "rotation", hdToggle.getRotation(),    orientation);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// This should always be the first statement of onStart
		Weemo.onActivityStart();

		// Register as event listener
		Weemo.eventBus().register(this);

		// Start listening for orientation changes
		if (oel.canDetectOrientation())
			oel.enable();
		
		// Register the BrodcastReceiver to detect headset connection change
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		filter.setPriority(0);
		registerReceiver(br, filter);
	}

	@Override
	protected void onStop() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		// We do not need to listen for orientation change while we are in the background
		// Beside, not stoping this will generate a leak when the activity is destroyed
		oel.disable();

		// Same as the line above
		unregisterReceiver(br);
		
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
		// First, we check that this event concerns the call we are monitoring
		if (e.getCall().getCallId() != getIntent().getIntExtra("callId", -1))
			return ;
		
		// If the call has ended, we finish the activity (as this activity is only for an active call)
		if (e.getCallStatus() == CallStatus.ENDED)
			finish();
	}
	
	/*
	 * This listener catches ReceivingVideoChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is ReceivingVideoChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 */
	@WeemoEventListener
	public void onReceivingVideoChanged(ReceivingVideoChangedEvent e) {
		// First, we check that this event concerns the call we are monitoring
		if (e.getCall().getCallId() != getIntent().getIntExtra("callId", -1))
			return ;
		
		// Sets the camera preview dimensions according to whether or not the remote contact has started his video
		setVideoOutFrameDimensions(e.isReceivingVideo());
	}
	
	@Override
	public void onBackPressed() {
		// do nothing
	}
	
}
