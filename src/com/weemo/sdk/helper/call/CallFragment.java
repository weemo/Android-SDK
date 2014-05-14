package com.weemo.sdk.helper.call;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.ReceivingVideoChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent.Error;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.fragment.LoadingDialogFragment;
import com.weemo.sdk.view.WeemoVideoInFrame;
import com.weemo.sdk.view.WeemoVideoOutPreviewFrame;

/**
 * This is the fragment that displays a call video views.
 *
 * It uses the {@link CallControl} view to control the call
 */
public class CallFragment extends Fragment implements OnSystemUiVisibilityChangeListener, OnTouchListener {

	/** fragment argument key */
	private static final int CONTROL_DELAY = 4000;

	/** fragment argument key */
	private static final String ARG_CALLID = "callId";

	/** fragment argument key */
	private static final String ARG_SHOW_CONTROL = "show_control";

	/** fragment argument key */
	private static final String ARG_SLIDE_CONTROL = "slide_control";

	/** fragment argument key */
	private static final String ARG_FULLSCREEN = "fullscreen";

	/** fragment argument key */
	private static final String ARG_CORRECTION = "correction";

	/** UI (main) thread handler */
	private @Nullable Handler handler;

	/** Runnable that will hide the call control bar */
	private @Nullable Runnable hideControl;

	/** Runnable that will show the call control bar */
	private @Nullable Runnable showControl;

	/** number of pixels in 20dp (so we don't have to recalculate this every time) */
	private float dp20;

	/** The call control bar */
	protected @Nullable CallControl controls;

	/** The call to display */
	protected @CheckForNull WeemoCall call;

	/** Frame (declared in the XML) that will contain video OUT */
	protected @Nullable WeemoVideoOutPreviewFrame videoOutFrame;
	
    ControlDisplayListener displayListener;

    public static interface ControlDisplayListener {
        void onShow();
        void onHide();
    }

	/**
	 * Behaviour expected from this fragment on touch regarding the call control bar
	 */
	public enum TouchType {
		/** Nothing happens. This fragment does have a call control bar. You are responsible for controlling the call */
		NO_CONTROLS                (false, false, false),

		/** The control bar is displayed and stays in place */
		STATIC_CONTROLS            (true,  false, false),

		/** The control bar is displayed and is hidden after {@link #CONTROL_DELAY}, it reappears on touch */
		SLIDE_CONTROLS             (true,  true,  false),

		/** The control bar is displayed and is hidden after {@link #CONTROL_DELAY}, it reappears on touch.
		 * When the control bar is hidden, the activity is set to fullscreen */
		SLIDE_CONTROLS_FULLSCREEN  (true,  true,  true );

		/** Whether or not to display the control bar */
		public final boolean show_control;

		/** Whether or not the control bar slides away */
		public final boolean slide_control;

		/** Whether or not, when the control bar is hidden, the activity is set to fullscreen */
		public final boolean fullscreen;

		/**
		 * Constructor
		 *
		 * @param show_control Whether or not to display the control bar
		 * @param slide_control Whether or not the control bar slides away
		 * @param fullscreen Whether or not, when the control bar is hidden, the activity is set to fullscreen
		 */
		private TouchType(boolean show_control, boolean slide_control, boolean fullscreen) {
			this.show_control = show_control;
			this.slide_control = slide_control;
			this.fullscreen = fullscreen;
		}
	}

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param callId The ID of the call to display
	 * @param touchType Behaviour expected from this fragment on touch regarding the call control bar
	 * @param correction The correction of the camera orientation (should be 90 on portrait devices, 0 on landscape devices)
	 * @return The fragment
	 */
	public static CallFragment newInstance(int callId, TouchType touchType, int correction) {
		CallFragment fragment = new CallFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_CALLID, callId);
		args.putBoolean(ARG_SHOW_CONTROL, touchType.show_control);
		args.putBoolean(ARG_SLIDE_CONTROL, touchType.slide_control);
		args.putBoolean(ARG_FULLSCREEN, touchType.fullscreen);
		args.putInt(ARG_CORRECTION, correction);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Shows the call control bars
	 * Does nothing if the fragment is not configured to hide the call control bar
	 */
	protected void showCallControls() {
		if (!getArguments().getBoolean(ARG_SLIDE_CONTROL)) {
			return ;
		}

		if (getArguments().getBoolean(ARG_FULLSCREEN)) {
			getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		if (CallFragment.this.displayListener != null) {
			CallFragment.this.displayListener.onShow();
		}

		animateView(true, this.controls);
	}

	/**
	 * Hides the call control bars
	 * Does nothing if the fragment is not configured to hide the call control bar
	 */
	protected void hideCallControls() {
		if (!getArguments().getBoolean(ARG_SLIDE_CONTROL)) {
			return ;
		}

		if (CallFragment.this.displayListener != null) {
			CallFragment.this.displayListener.onHide();
		}

		animateView(false, this.controls);
		
		if (getArguments().getBoolean(ARG_FULLSCREEN) && getActivity() != null && getActivity().getWindow() != null) {
			getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}
	
	
	private void animateView(boolean enter, final View view) {
		if(view == null){
			return;
		}
		
		if(enter){
			view.setVisibility(View.VISIBLE);
		}
		
		AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), enter ? R.animator.weemo_callcontrol_enter : R.animator.weemo_callcontrol_exit);
		set.setTarget(view);
		if (!enter) {
			set.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
						view.setVisibility(View.GONE);
				}
			});
		}
		set.start();
	}
	

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.handler = new Handler();
		this.hideControl = new Runnable() { // I miss Java8... this.removeControl = this::hide;
			@Override public void run() { hideCallControls(); }
		};
		this.showControl = new Runnable() {
			@Override public void run() { showCallControls(); }
		};

		if (getArguments().getBoolean(ARG_FULLSCREEN) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
		getActivity().getWindow().getDecorView().setOnTouchListener(this);

		this.dp20 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

		postControlRemove();

		final WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			throw new UnsupportedOperationException("Cannot use the CallFragment if Weemo is not initialized");
		}

		final int callId = getArguments().getInt(ARG_CALLID);
		this.call = weemo.getCall(callId);

		Weemo.eventBus().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View root = inflater.inflate(R.layout.weemo_fragment_call, container, false);
		this.controls = (CallControl) root.findViewById(R.id.call_control);

		if (!getArguments().getBoolean(ARG_SHOW_CONTROL, true)) {
			this.controls.setVisibility(View.GONE);
		}

		if (this.call == null) {
			return root;
		}

		this.controls.setCall(this.call);

		this.videoOutFrame = (WeemoVideoOutPreviewFrame) root.findViewById(R.id.video_out);
		this.videoOutFrame.setDeviceCorrection(getArguments().getInt(ARG_CORRECTION));
		assert this.call != null;
		this.call.setVideoOut(this.videoOutFrame);

		final WeemoVideoInFrame videoInFrame = (WeemoVideoInFrame) root.findViewById(R.id.video_in);
		this.call.setVideoIn(videoInFrame);

		this.videoOutFrame.post(new Runnable() {
			@Override public void run() {
				assert CallFragment.this.call != null;
				setVideoOutFrameDimensions(CallFragment.this.call.isReceivingVideo());
			}
		});

		return root;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(this.call != null) {
			this.handler.removeCallbacks(this.hideControl);
			getActivity().invalidateOptionsMenu();
		}
	}

	@Override
	public void onDestroy() {
		this.handler.removeCallbacksAndMessages(null);

		if (getArguments().getBoolean(ARG_FULLSCREEN)) {
			getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);
		}

		Weemo.eventBus().unregister(this);

		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onDestroy();
	}

	/**
	 * Post the removal of the control bar after {@link #CONTROL_DELAY}
	 * Does nothing if the fragment is not configured to hide the call control bar
	 */
	void postControlRemove() {
		if (!getArguments().getBoolean(ARG_SLIDE_CONTROL)) {
			return ;
		}

		this.handler.removeCallbacks(this.hideControl);
		this.handler.postDelayed(this.hideControl, CONTROL_DELAY);
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
			showCallControls();
			postControlRemove();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!getArguments().getBoolean(ARG_SLIDE_CONTROL)) {
			return false;
		}
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			if(this.controls.getVisibility() != View.VISIBLE || this.controls.getAlpha() != 1){
				showCallControls();
			}
			this.handler.removeCallbacks(this.hideControl);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			postControlRemove();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * Hides or show the call control bar
	 * @param offset 1 completely hides it, 0 shows it completely
	 */
	public void setControlOut(float offset) {
		if (offset < 0.01) {
			postControlRemove();
		}
		else {
			this.handler.removeCallbacksAndMessages(null);
		}

		this.controls.setTranslationY(-(this.dp20) * offset);
		this.controls.setAlpha(1.0f - offset);
	}

	/**
	 * Sets the dimension preview dimensions according to whether or not we are receiving video.
	 * If we are, we need to have a small preview.
	 * If we are not, the preview needs to fill the space.
	 *
	 * @param isReceivingVideo Whether or not we are receiving video
	 */
	protected void setVideoOutFrameDimensions(final boolean isReceivingVideo) {
		final DisplayMetrics metrics = new DisplayMetrics();
		if (getActivity() == null || getActivity().getWindowManager() == null || getActivity().getWindowManager().getDefaultDisplay() == null) {
			return;
		}
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		final ViewGroup.LayoutParams params = this.videoOutFrame.getLayoutParams();
		if (isReceivingVideo) {
			params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32 * 4, metrics);
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32 * 4, metrics);
		}
		else {
			params.width = ViewGroup.LayoutParams.MATCH_PARENT;
			params.height = ViewGroup.LayoutParams.MATCH_PARENT;
		}

		if (getView().getWidth() > getView().getHeight()) {
			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		else if (getView().getHeight() > getView().getWidth()) {
			params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		}

		this.videoOutFrame.setLayoutParams(params);
	}

	/**
	 * This listener catches ReceivingVideoChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is ReceivingVideoChangedEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onReceivingVideoChanged(final ReceivingVideoChangedEvent event) {
		// First, we check that this event concerns the call we are monitoring
		if (this.call == null || event.getCall().getCallId() != this.call.getCallId()) {
			return ;
		}

		// Sets the camera preview dimensions according to whether or not the remote contact has started his video
		setVideoOutFrameDimensions(event.isReceivingVideo());
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
		if (error == CanCreateCallChangedEvent.Error.NETWORK_LOST) {
			LoadingDialogFragment.show(null, "Reconnecting", "Cancel", getFragmentManager());
		}
		else if (error == null) {
			LoadingDialogFragment.hide(getFragmentManager());
		}
	}
}
