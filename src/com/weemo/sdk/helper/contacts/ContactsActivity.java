package com.weemo.sdk.helper.contacts;

import javax.annotation.CheckForNull;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.helper.DemoAccounts;
import com.weemo.sdk.helper.HelperApplication;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.call.CallActivity;
import com.weemo.sdk.helper.call.CallControl;
import com.weemo.sdk.helper.call.CallFragment;
import com.weemo.sdk.helper.call.CallFragment.TouchType;
import com.weemo.sdk.helper.connect.ConnectedService;
import com.weemo.sdk.helper.fragment.ChooseFragment;
import com.weemo.sdk.helper.fragment.ChooseFragment.ChooseListener;
import com.weemo.sdk.helper.util.UIUtils;

/**
 * This is the main activity when the user is connected.
 * From this activity, the user can:
 *  - Poll for the status of a remote contact
 *  - Call a remote contact.
 *
 * When a call starts, this activity will :
 *  - Start the CallActivity if we are using a phone or a small tablet
 *  - Display the CallFragment and handle the call itself if we are using a 10 inch or bigger tablet
 *
 * It is also that activity that starts and stops the ConnectedService.
 * This service will run as long as the user is connected.
 */
public class ContactsActivity extends Activity implements ChooseListener, OnCancelListener {

	/** Key of optional extra int for intent: call identifier if there is one taking place */
	public static final String EXTRA_CALLID = "callId";

	/** Key of optional extra boolean for intent: Whether to pick up a call */
	public static final String EXTRA_PICKUP = "pickup";

	/** Log tag for Log.* */
	private static final String TAG = "ContactsActivity";

	/** STATIC value to retain the connected user id */
	public @CheckForNull static String currentUid; // = null;

	/** Whether the user is in checked mode (meaning that a userid will be checked before being called) or not */
	protected boolean checkedMode; // = false;

	/**
	 * Drawer toggle, null if there is no drawer (in phones)
	 */
	private @CheckForNull ActionBarDrawerToggle drawerToggle;

	/**
	 * Drawer, null if there is no drawer (in phones)
	 */
	protected @CheckForNull DrawerLayout drawer;

	/**
	 * Starts the call view if there is a call currently taking place
	 * or starts the service to listen for call events if not.
	 */
	private void startCallOrService() {
		HelperApplication.cancelCountDown();
		WeemoEngine weemo = Weemo.instance();
		assert weemo != null;

		// If there is a call currently going on,
		// it's probably because the user has clicked in the notification after going on its device home.
		// In which case we redirect him to the CallActivity
		WeemoCall call = weemo.getCurrentCall();

		if (call == null) {
			if (getIntent().getBooleanExtra(EXTRA_PICKUP, false)) {
				final int callId = getIntent().getIntExtra(EXTRA_CALLID, -1);
				call = weemo.getCall(callId);
				if (call != null) {
					startCallWindow(call);
				}
			}
			else {
				// This activity can only be launched if the user is connected
				// Therefore, we launch the ConnectedService
				startService(new Intent(this, ConnectedService.class));
			}
		}
		else {
			startCallWindow(call);
		}
	}

	/**
	 * Initializes this activity meaning that this activity is being built, not re-built.
	 *
	 * @param weemo The Weemo engine
	 */
	private void initialize(WeemoEngine weemo) {
		// Ensure that there is a connected user
		if (currentUid == null) {
			Log.e(TAG, "ContactsActivity was started while Weemo is not authenticated");
			finish();
			return ;
		}

		// Display the contact fragment
		getFragmentManager()
			.beginTransaction()
			.add(R.id.contact_list, ChooseFragment.newInstance(getResources().getString(this.checkedMode ? R.string.check : R.string.call), currentUid))
			.commit();

		// If we need to ask for the display name, shows the apropriate popup
		if (weemo.getDisplayName().isEmpty()) {
			
			if(!TextUtils.isEmpty(HelperApplication.overrideDisplayName)) {
				setDisplayName(HelperApplication.overrideDisplayName);
				if(!TextUtils.isEmpty(HelperApplication.overrideCallee)) {
					onChoose(HelperApplication.overrideCallee);
				}
			} else {
				String defaultName = DemoAccounts.getDeviceName(false);
				if (DemoAccounts.ACCOUNTS.containsKey(currentUid)) {
					defaultName = DemoAccounts.ACCOUNTS.get(currentUid);
				}
	            AskDisplayNameDialogFragment.newInstance(defaultName).show(getFragmentManager(), null);
			}
		}

		startCallOrService();
	}

	/**
	 * Initializes the drawer.
	 * Will do nothing in phone mode.
	 *
	 * @param weemo The Weemo engine
	 */
	private void initializeDrawer(WeemoEngine weemo) {
		this.drawer = (DrawerLayout) findViewById(R.id.drawer);
		if (this.drawer == null) {
			return ;
		}

		this.drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		this.drawerToggle = new ActionBarDrawerToggle(
			this,
			this.drawer,
			R.drawable.ic_navigation_drawer,
			R.string.contacts_open,
			R.string.contacts_close) {
			// TODO
		};

		assert this.drawer != null;
		this.drawer.setDrawerListener(this.drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        WeemoCall currentCall = weemo.getCurrentCall();
        if (currentCall == null) {
        	assert this.drawer != null;
			this.drawer.openDrawer(Gravity.START);
        }
        else {
        	putCallControls(currentCall);
        }
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contacts);

		UIUtils.forceOverflowMenu(this);

		// Ensure that Weemo is initialized
		final WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			Log.e(TAG, "ContactsActivity was started while Weemo is not initialized");
			stopService(new Intent(this, ConnectedService.class));
			finish();
			return ;
		}

		if (savedInstanceState == null) {
			initialize(weemo);
		}

		initializeDrawer(weemo);

		setTitleFromDisplayName();

		// Register as event listener
		Weemo.eventBus().register(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (this.drawerToggle != null) {
			this.drawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.drawerToggle != null) {
			this.drawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (this.drawerToggle != null && this.drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		final WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			finish();
			return ;
		}

		// If there is a call currently going on,
		// it's probably because the user has clicked in the notification after going on its device home.
		// In which case we redirect him to the CallActivity
		WeemoCall call = weemo.getCurrentCall();
		if (call != null) {
			startCallWindow(call);
		}
		if (intent.getBooleanExtra(EXTRA_PICKUP, false)) {
			final int callId = intent.getIntExtra(EXTRA_CALLID, -1);
			call = weemo.getCall(callId);
			if (call != null) {
				startCallWindow(call);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		HelperApplication.cancelCountDown();

		final WeemoEngine weemo = Weemo.instance();
		assert weemo != null;

		if (weemo.getCurrentCall() == null) {
			// Enables or disable the call button according to weemo.canCreateCall()
			((ChooseFragment) getFragmentManager().findFragmentById(R.id.contact_list)).setEnabled(weemo.canCreateCall());
		}

		// If we were in background mode, we got back to foreground
		if (weemo.isInBackground()) {
			weemo.goToForeground();
		}
	}

	@Override
	protected void onStop() {
		HelperApplication.startCountDown();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putBoolean("checkedMode", this.checkedMode);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.checkedMode = savedInstanceState.getBoolean("checkedMode", false);
		WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		if (weemo.getCurrentCall() == null) {
			removeCallFragment();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(R.string.disconnect)
			.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override public boolean onMenuItemClick(final MenuItem item) {
					Weemo.disconnect();
                    stopService(new Intent(getApplicationContext(), ConnectedService.class));
					return true;
				}
			})
		;

		menu.add(R.string.checked_mode)
			.setCheckable(true)
			.setChecked(this.checkedMode)
			.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override public boolean onMenuItemClick(final MenuItem item) {
					ContactsActivity.this.checkedMode = !item.isChecked();
					item.setChecked(ContactsActivity.this.checkedMode);
					final String text = getResources().getString(item.isChecked() ? R.string.check : R.string.call);
					((ChooseFragment) getFragmentManager().findFragmentById(R.id.contact_list)).setButtonText(text);
					return true;
				}
			})
		;

		menu.add(R.string.send_report)
			.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override public boolean onMenuItemClick(final MenuItem item) {
					HelperApplication.sendReport();
					return true;
				}
			})
		;

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * When the user has chosen someone to check, starts the dialog fragment that will make the check
	 */
	@Override
	public void onChoose(final String contactId) {
		if (this.checkedMode) {
			ContactCheckDialogFragment.newInstance(contactId).show(getFragmentManager(), null);
		}
		else {
			final WeemoEngine weemo = Weemo.instance();
			assert weemo != null;
			weemo.createCall(contactId);
		}
	}

	/**
	 * Set the activity title according to the display name
	 * If there is no display name, then we use the user ID
	 */
	private void setTitleFromDisplayName() {
		final WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		final String displayName = weemo.getDisplayName();
		if (displayName.isEmpty()) {
			setTitle("{" + currentUid + "}");
		}
		else {
			setTitle(displayName);
		}
	}

	/**
	 * Displays the call window.
	 * If we are using a 10 inch or bigger tablet, shows the Call fragment in this activity
	 * Otherwise, shows the activity that displays the call window fullscreen.
	 *
	 * @param call The call to display
	 */
	protected void startCallWindow(final WeemoCall call) {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				final View display = findViewById(R.id.contact_display);
				ChooseFragment chooseFragment = (ChooseFragment)(getFragmentManager().findFragmentById(R.id.contact_list));
				if(chooseFragment != null){
					chooseFragment.setEnabled(false);
				}
				if (display == null) {
					startActivity(
						new Intent(ContactsActivity.this, CallActivity.class)
							.putExtra(EXTRA_CALLID, call.getCallId())
					);
				}
				else {
					showCallFragment(call);
				}
			}
		});
	}

	/**
	 * Set the display name
	 *
	 * @param displayName The new display name
	 */
	public void setDisplayName(final String displayName) {
		final WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		weemo.setDisplayName(displayName);

		// Tell the service the user has changed display name, so the service can update the notification
		startService(new Intent(this, ConnectedService.class));
		setTitleFromDisplayName();
	}

	/**
	 * This listener method catches CanCreateCallChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CanCreateCallChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCanCreateCallChanged(final CanCreateCallChangedEvent event) {
		final CanCreateCallChangedEvent.Error error = event.getError();

		// If there is an error, the action we'll take depends on the error
		if (error != null) {
			switch (error) {
			// This is a loss of network. We cannot create call, but the network should be back anytime soon.
			// We disable the call button.
			case NETWORK_LOST:
				((ChooseFragment) getFragmentManager().findFragmentById(R.id.contact_list)).setEnabled(false);
				break;

			// This is either a system error or the Weemo engine was destroyed (by the user when he clicks disconnect)
			// We print the error message and finish the activity, the service (and the application along)
			case SIP_NOK:
			case CLOSED:
			default:
				Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
				stopService(new Intent(ContactsActivity.this, ConnectedService.class));
				finish();
				break;
			}
			return ;
		}

		final WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		if (weemo.getCurrentCall() == null) {
			// If there is no error, we can create call
			// We enable the call button.
			((ChooseFragment) getFragmentManager().findFragmentById(R.id.contact_list)).setEnabled(true);
		}
	}

	/**
	 * Only for tablets: will put the {@link CallControl} view inside the action bar
	 *
	 * @param call The call to control
	 */
	protected void putCallControls(WeemoCall call) {
		CallControl callControl = new CallControl(this, CallControl.Style.DARK);
		LayoutTransition lt = new LayoutTransition();
		callControl.setLayoutTransition(lt);
		callControl.setCall(call);
		int width = (int) getResources().getDimension(R.dimen.weemo_call_control_width);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
		callControl.setLayoutParams(layoutParams);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setCustomView(callControl);
	}

	/**
	 * Only for tablets: will show the call fragment
	 *
	 * @param call The call display
	 */
	protected void showCallFragment(final WeemoCall call) {
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.contact_display, CallFragment.newInstance(call.getCallId(), TouchType.NO_CONTROLS, getResources().getInteger(R.integer.camera_correction)))
		.commit();
		if (ContactsActivity.this.drawer != null) {
			ContactsActivity.this.drawer.closeDrawer(Gravity.START);
		}
		putCallControls(call);
	}

	/**
	 * Remove the call fragment, if any
	 */
	private void removeCallFragment() {
		((ChooseFragment)(getFragmentManager().findFragmentById(R.id.contact_list))).setEnabled(true);
		final View display = findViewById(R.id.contact_display);
		if (display != null) {
			final Fragment fragment = getFragmentManager().findFragmentById(R.id.contact_display);
			if (fragment != null) {
				getFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
			}
			if (this.drawer != null) {
				this.drawer.openDrawer(Gravity.START);
			}
			getActionBar().setDisplayShowCustomEnabled(false);
			getActionBar().setCustomView(null);
		}
	}

	/**
	 * This listener method catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCallStatusChanged(final CallStatusChangedEvent event) {
		// If there's a call whose status is newly to PROCEEDING, this means the user has initiated an outgoing call
		// and that this call is currently ringing on the remote user's device.
		// In which case, we show the ContactCallingDialogFragment that will monitor the babysteps of this newborn call
		if (event.getCallStatus() == CallStatus.PROCEEDING) {
			final ContactCallingDialogFragment dialog = ContactCallingDialogFragment.newInstance(event.getCall().getCallId());
			dialog.setCancelable(false);
			getFragmentManager().beginTransaction().add(dialog, null).commitAllowingStateLoss();
		}

		// If a call has ended and we are in tablet mode, we need to remove the call window fragment.
		if (event.getCallStatus() == CallStatus.ENDED) {
			removeCallFragment();
		}
	}

	@Override
    public void onCancel(DialogInterface dialog) {
        WeemoCall currentCall = Weemo.instance().getCurrentCall();
        if (currentCall != null) {
            currentCall.hangup();
        }
    }

	/**
	 * We only allow going back if there are no calls going on.
	 * That way, if we are in a tablet, the call fragment will never be destroyed during a call.
	 * Note that it could, but destroying the call fragment would stop the video out.
	 */
	@Override
	public void onBackPressed() {
		final WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		if (weemo.getCurrentCall() == null) {
			super.onBackPressed();
		}
	}
}
