package com.weemo.sdk.helper.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.VideoProfile;
import com.weemo.sdk.WeemoCall.VideoSource;
import com.weemo.sdk.helper.R;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * View that displays call controls and that applies those controls to a {@link WeemoCall}
 *
 * It uses the Weemo API to control everything that relates to the call, the video and the audio OUT.
 *
 * Weemo does not exposes api to control audio IN.
 * This fragment uses Android's AudioManager to control everything that relates to audio IN.
 */
public class CallControl extends LinearLayout implements OnClickListener {

	/** The call to control */
	private WeemoCall call;

	/** The audio manager used to control audio */
	protected AudioManager audioManager;

	/** Used to receive Intent.ACTION_HEADSET_PLUG, which is when the headset is (un)plugged */
	private BroadcastReceiver headsetPlugReceiver;

	/** Whether the view should render for a Holo.Light or Holo.Dark */
	public enum Style { /** Holo.Dark */ DARK, /** Holo.Light */ LIGHT }

	/** The speakers button */
	protected ImageButton speakers;

	/** The micro button */
	protected ImageButton micro;

	/** The video button */
	protected ImageButton video;

	/** The frontBack button */
	protected ImageButton frontBack;

	/** The SD/HD button */
	protected ImageButton sdHd;

	/** The hangup button */
	protected ImageButton hangup;

	/**
	 * Initializes this view (called from constructors)
	 *
	 * @param style The style to use
	 */
	private void init(Style style) {
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.weemo_layout_callcontrol, this);

		this.speakers  = (ImageButton) findViewById(R.id.item_speakers);
		this.micro     = (ImageButton) findViewById(R.id.item_micro);
		this.video     = (ImageButton) findViewById(R.id.item_video);
		this.frontBack = (ImageButton) findViewById(R.id.item_front_back);
		this.sdHd      = (ImageButton) findViewById(R.id.item_sd_hd);
		this.hangup    = (ImageButton) findViewById(R.id.item_hangup);

		CheatSheet.setup(this.speakers);
		CheatSheet.setup(this.micro);
		CheatSheet.setup(this.video);
		CheatSheet.setup(this.frontBack);
		CheatSheet.setup(this.sdHd);
		CheatSheet.setup(this.hangup);

		switch (style) {
		case DARK:
			this.speakers  .setImageResource(R.drawable.weemo_ic_speakers_dark);
			this.micro     .setImageResource(R.drawable.weemo_ic_mic_dark);
			this.video     .setImageResource(R.drawable.weemo_ic_video_dark);
			this.frontBack .setImageResource(R.drawable.weemo_ic_action_switch_video_dark);
			this.sdHd      .setImageResource(R.drawable.weemo_ic_quality_dark);
			break;
		case LIGHT:
		default:
			this.speakers  .setImageResource(R.drawable.weemo_ic_speakers_light);
			this.micro     .setImageResource(R.drawable.weemo_ic_mic_light);
			this.video     .setImageResource(R.drawable.weemo_ic_video_light);
			this.frontBack .setImageResource(R.drawable.weemo_ic_action_switch_video_light);
			this.sdHd      .setImageResource(R.drawable.weemo_ic_quality_light);
			break;
		}

		this.speakers.setOnClickListener(this);
		this.micro.setOnClickListener(this);
		this.video.setOnClickListener(this);
		this.frontBack.setOnClickListener(this);
		this.sdHd.setOnClickListener(this);
		this.hangup.setOnClickListener(this);

		if (!isInEditMode()) {
			this.audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
			if (Camera.getNumberOfCameras() < 2) {
				this.frontBack.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Get the {@link Style} from the XML attributes and initializes accordingly
	 *
	 * @param attrs XML attributes
	 */
	public void initAttr(AttributeSet attrs) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CallControl, 0, 0);

		try {
			init(a.getInt(R.styleable.CallControl_style, 0) == 0 ? Style.DARK : Style.LIGHT);
		}
		finally {
			a.recycle();
		}
	}

	/**
	 * @see View#View(Context, AttributeSet, int)
	 */
	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	public CallControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAttr(attrs);
	}

	/**
	 * @see View#View(Context, AttributeSet)
	 */
	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	public CallControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttr(attrs);
	}

	/**
	 * Construct a new CallControl view with the given style
	 *
	 * @param context The context to create the view
	 * @param style The style to use
	 */
	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	public CallControl(Context context, Style style) {
		super(context);
		if (style == null) {
			initAttr(null);
		}
		else {
			init(style);
		}
	}

	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	/**
	 * Set the call to control
	 *
	 * @param call The call to control
	 */
	public void setCall(WeemoCall call) {
		this.call = call;

		if (!this.audioManager.isSpeakerphoneOn()) {
			this.speakers.setActivated(true);
		}
		if (!this.call.isSendingAudio()) {
			this.micro.setActivated(true);
		}
		if (!this.call.isSendingVideo()) {
			this.video.setActivated(true);
			this.frontBack.setVisibility(View.GONE);
		}
		this.sdHd.setActivated(this.call.getVideoInProfile() == VideoProfile.HD);
	}

	@Override
	public void onClick(View v) {
		if (this.call != null)
			v.setActivated(!v.isActivated());
			switch (v.getId()) {
			case R.id.item_speakers:
				this.audioManager.setSpeakerphoneOn(!v.isActivated());
				break ;
			case R.id.item_micro:
				if (v.isActivated()) {
					this.call.audioMute();
				}
				else {
					this.call.audioUnMute();
				}
				break ;
			case R.id.item_video:
				if (v.isActivated()) {
					this.call.videoStop();
					this.frontBack.setVisibility(View.GONE);
				}
				else {
					this.call.videoStart();
					if (Camera.getNumberOfCameras() > 1) {
						this.frontBack.setVisibility(View.VISIBLE);
					}
				}
				break ;
			case R.id.item_front_back:
				this.call.setVideoSource(v.isActivated() ? VideoSource.BACK : VideoSource.FRONT);
				break ;
			case R.id.item_hangup:
				this.call.hangup();
				break ;
			case R.id.item_sd_hd:
				this.call.setInVideoProfile(v.isActivated() ? VideoProfile.HD : VideoProfile.SD);
				break;
			default:
				throw new UnsupportedOperationException("Unknown button");
			}
	}

	/**
	 * Class used to store {@link CallControl}'s state
	 */
	static class SavedState extends BaseSavedState {

		/**
		 * @see android.view.View.BaseSavedState#BaseSavedState(Parcelable)
		 */
		public SavedState(Parcelable superState) {
			super(superState);
		}

		/**
		 * Whether or not buttons are activated :
		 * [0] = item_speakers
		 * [1] = item_micro
		 * [2] = item_video
		 * [3] = item_sd_hd
		 */
		boolean[] activated = {};

		/**
		 * @see android.view.View.BaseSavedState#BaseSavedState(Parcel)
		 */
		protected SavedState(Parcel in) {
			super(in);
			this.activated = new boolean[4];
			in.readBooleanArray(this.activated);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeBooleanArray(this.activated);
		}

		/**
		 * required field that makes Parcelables from a Parcel
		 */
		@SuppressWarnings("hiding")
		public static final Parcelable.Creator<SavedState> CREATOR =
			new Parcelable.Creator<SavedState>() {
				@Override
				public SavedState createFromParcel(Parcel in) {
					return new SavedState(in);
				}

				@Override
				public SavedState[] newArray(int size) {
					return new SavedState[size];
				}
			};
	}


	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState state = new SavedState(superState);

		state.activated = new boolean[] {
			this.speakers.isActivated(),
			this.micro.isActivated(),
			this.video.isActivated(),
			this.sdHd.isActivated()
		};

		return state;
	}

	@Override
	public void onRestoreInstanceState(Parcelable parcelable) {
		if (!(parcelable instanceof SavedState)) {
			super.onRestoreInstanceState(parcelable);
			return;
		}

		SavedState state = (SavedState) parcelable;
		super.onRestoreInstanceState(state.getSuperState());

		this.speakers.setActivated(state.activated[0]);
		this.micro.setActivated(state.activated[1]);
		this.video.setActivated(state.activated[2]);
		if (this.video.isActivated() || Camera.getNumberOfCameras() < 2) {
			this.frontBack.setVisibility(View.GONE);
		}
		this.sdHd.setActivated(state.activated[3]);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		this.headsetPlugReceiver = new BroadcastReceiver() {
			@Override public void onReceive(final Context context, final Intent intent) {
				@SuppressWarnings("deprecation")
				boolean isOn = !CallControl.this.audioManager.isWiredHeadsetOn();
				CallControl.this.audioManager.setSpeakerphoneOn(isOn);
				CallControl.this.speakers.setActivated(!isOn);
			}
		};
		getContext().registerReceiver(this.headsetPlugReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}

	@Override
	protected void onDetachedFromWindow() {
		getContext().unregisterReceiver(this.headsetPlugReceiver);

		super.onDetachedFromWindow();
	}
}
