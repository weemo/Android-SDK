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
import android.util.Log;
import android.util.TypedValue;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.event.call.ReceivingVideoChangedEvent;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.view.WeemoVideoInFrame;
import com.weemo.sdk.view.WeemoVideoOutPreviewFrame;

@SuppressWarnings("deprecation")
public class CallActivity extends Activity {

	private @Nullable ImageView toggleIn;
	private @Nullable ImageView muteOut;
	private @Nullable ImageView video;
	private @Nullable ImageView videoToggle;
	private @Nullable ImageView hangup;
	
	private int correction;
	private boolean isSpeakerphoneOn;
	
	private @Nullable AudioManager audioManager;

	private @Nullable OrientationEventListener oel;
	
	private @Nullable BroadcastReceiver br;
	
	private @Nullable WeemoVideoOutPreviewFrame videoOutFrame;
	private @Nullable WeemoVideoInFrame videoInFrame;

	private void setSpeakerphoneOn(boolean on) {
		audioManager.setSpeakerphoneOn(on);
		toggleIn.setImageResource(on ? R.drawable.call_device_access_volume_on : R.drawable.call_device_access_volume_muted);
		isSpeakerphoneOn = on;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("NPEAVOID", "CallActivity.onCreate");
		
		setContentView(R.layout.activity_call);
		
		final int callId = getIntent().getIntExtra("callId", -1);
		if (callId == -1) {
			finish();
			return ;
		}

		Weemo weemo = Weemo.instance();
		assert weemo != null;
		final WeemoCall call = weemo.getCall(callId);
		if (call == null) {
			finish();
			return ;
		}

		videoOutFrame = (WeemoVideoOutPreviewFrame) findViewById(R.id.video_out);
		call.setVideoOut(videoOutFrame);

		videoInFrame = (WeemoVideoInFrame) findViewById(R.id.video_in);
		videoInFrame.setDisplayFollowDeviceOrientation(true);
		call.setVideoIn(videoInFrame);

		call.videoStart();
		
		setTitle(call.getContactDisplayName());
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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
		
		br = new BroadcastReceiver() {
			@Override public void onReceive(Context context, Intent intent) {
				setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());
			}
		};
		
		toggleIn = (ImageView) findViewById(R.id.toggle_in);
		toggleIn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				setSpeakerphoneOn(!isSpeakerphoneOn);
			}
		});

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

		video = (ImageView) findViewById(R.id.video);
		video.setOnClickListener(new OnClickListener() {
			boolean video = false;
			@Override public void onClick(View v) {
				video = !video;
				if (video) {
					call.videoStart();
					videoToggle.setVisibility(View.VISIBLE);
				}
				else {
					call.videoStop();
					videoToggle.setVisibility(View.GONE);
				}
			}
		});

		videoToggle = (ImageView) findViewById(R.id.video_toggle);
		videoToggle.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				call.toggleVideoSource();
			}
		});

		hangup = (ImageView) findViewById(R.id.hangup);
		hangup.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				call.hangup();
			}
		});

		setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());

		switch (getWindowManager().getDefaultDisplay().getRotation()) {
			case Surface.ROTATION_0:   correction = 0;   break ;
			case Surface.ROTATION_90:  correction = 90;  break ;
			case Surface.ROTATION_180: correction = 180; break ;
			case Surface.ROTATION_270: correction = 270; break ;
		}

		setVideoOutFrameDimensions(call.isReceivingVideo());
	}

	private void animate(View view, String property, float current, int angle) {
		if (angle - current > 180)
			ObjectAnimator.ofFloat(view, property, current + 360).setDuration(0).start();
		else if (current - angle > 180)
			ObjectAnimator.ofFloat(view, property, current - 360).setDuration(0).start();
		
		ObjectAnimator.ofFloat(view, property, angle).start();
	}

	private void setOrientation(int orientation) {
		animate(toggleIn,    "rotation", toggleIn.getRotation(),    orientation);
		animate(muteOut,     "rotation", muteOut.getRotation(),     orientation);
		animate(video,       "rotation", video.getRotation(),       orientation);
		animate(videoToggle, "rotation", videoToggle.getRotation(), orientation);
		animate(hangup,      "rotation", hangup.getRotation(),      orientation);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// This should always be the first statement of onStart
		Weemo.onActivityStart();

		// Register as event listener
		Weemo.getEventBus().register(this);

		if (oel.canDetectOrientation())
			oel.enable();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		filter.setPriority(0);
		registerReceiver(br, filter);
	}

	@Override
	protected void onStop() {
		// Unregister as event listener
		Weemo.getEventBus().unregister(this);

		oel.disable();

		unregisterReceiver(br);
		
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
	
	@WeemoEventListener
	public void onReceivingVideoChanged(ReceivingVideoChangedEvent e) {
		if (e.getCall().getCallId() != getIntent().getIntExtra("callId", -1))
			return ;
		setVideoOutFrameDimensions(e.isReceivingVideo());
	}
	
	@Override
	public void onBackPressed() {
		// do nothing
	}
	
}
