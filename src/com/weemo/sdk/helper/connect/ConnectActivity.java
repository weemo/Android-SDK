package com.weemo.sdk.helper.connect;

import org.acra.ACRA;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.WeemoEngine.UserType;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.AuthenticatedEvent;
import com.weemo.sdk.event.global.ConnectedEvent;
import com.weemo.sdk.helper.HelperApplication;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.contacts.ContactsActivity;
import com.weemo.sdk.helper.fragment.ChooseFragment;
import com.weemo.sdk.helper.fragment.ChooseFragment.ChooseListener;
import com.weemo.sdk.helper.fragment.ErrorFragment;
import com.weemo.sdk.helper.fragment.InputFragment;
import com.weemo.sdk.helper.fragment.InputFragment.InputListener;
import com.weemo.sdk.helper.fragment.LoadingDialogFragment;
import com.weemo.sdk.helper.util.UIUtils;
import com.weemo.sdk.impl.MUCLConstants;

/**
 * This is the first activity being launched.
 * Its role is to handle connection and authentication of the user.
 */
public class ConnectActivity extends Activity implements InputListener, ChooseListener {

    /** Regex to validate the AppID */
    private static final String REGEX_APP_ID = "^[^\\s#%&+]+$";
    
	/** Whether or not the current user has successfully logged in */
	private boolean hasLoggedIn; // = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UIUtils.forceOverflowMenu(this);

		// Checks if Weemo is already initialized and authenticated.
		// If it is, it is probably because the user clicked on the service notification.
		// In which case, the user is redirected to the second screen
		final WeemoEngine weemo = Weemo.instance();
		if (weemo != null && weemo.isAuthenticated()) {
			this.hasLoggedIn = true;
			startActivity(
				new Intent(this, ContactsActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
				);
			finish();
			return ;
		}
		
		HelperApplication.detectOverrideData(getIntent());

		// This activity starts with a LoadingFragment (which makes the user wait while Weemo is connecting)
		// The connection is started in onStart, after registering the listener
		// (so that the ConnectedEvent can't be launched while we are not yet listening).
		if (savedInstanceState == null) {
			if(!TextUtils.isEmpty(HelperApplication.overrideAppID)) {
				onInput(HelperApplication.overrideAppID);
			} else {
				InputFragment.show(getString(R.string.weemo_appId), getFragmentManager());
			}
		}

		// Register the activity as event listener
		Weemo.eventBus().register(this);

		// Initialize Weemo, can be called multiple times
	}

	@Override
	protected void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		// If this activity is destroyed with hasLoggedIn is true,
		// this means it is destroyed after CallActivity being displayed.
		// However, if hasLoggedIn is false, it means that the activity is destroyed
		// because the user has got out of the application without logging in.
		// In this case we need to stop the Weemo engine.
		// If an orientation change event if occuring, do not disconnect.
		if (!this.hasLoggedIn && !isChangingConfigurations()) {
			Weemo.disconnect();
		}

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
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

	/**
	 * This is called by the InputFragment when user clicks on "OK" button
	 * from the dialog that asks him for the appId
	 *
	 * @param appId The Application Identifier entered by the user
	 */
	@Override
	public void onInput(final String appId) {
        if (TextUtils.isEmpty(appId) || !appId.matches(REGEX_APP_ID)) {
			return ;
		}

		InputFragment.hide(getFragmentManager());
		LoadingDialogFragment.show(getString(R.string.connection_title), getString(R.string.connection_text), null, getFragmentManager());
		
		ACRA.getErrorReporter().putCustomData(HelperApplication.ACRA_CUSTOM_APP_ID, appId);
		Weemo.initialize(appId, this);
	}

	/**
	 * This is called by the ChoseFragment when user clicks on "login" button after selecting a login.
	 *
	 * @param userId The entered User Identifier
	 */
	@Override
	public void onChoose(final String userId) {
		ChooseFragment.hide(getFragmentManager());

		final WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			getFragmentManager().beginTransaction().replace(android.R.id.content, ErrorFragment.newInstance("Connection lost")).commitAllowingStateLoss();
			return ;
		}

		LoadingDialogFragment.show(userId, getString(R.string.authentication_title), null, getFragmentManager());

		// Start authentication with the userId chosen by the user and the deviceType.
		ContactsActivity.currentUid = userId;
		final int deviceType = getResources().getBoolean(R.bool.isTablet) ? MUCLConstants.DeviceType.DEVICETYPE_APAD : MUCLConstants.DeviceType.DEVICETYPE_APHONE;
		UserType userType = WeemoEngine.UserType.INTERNAL;
		ACRA.getErrorReporter().putCustomData(HelperApplication.ACRA_CUSTOM_USER_ID, userId);
		ACRA.getErrorReporter().putCustomData(HelperApplication.ACRA_CUSTOM_DEVICE_TYPE, getResources().getBoolean(R.bool.isTablet) ? "APAD" : "APHONE");
		ACRA.getErrorReporter().putCustomData(HelperApplication.ACRA_CUSTOM_USER_TYPE, userType.name());
		weemo.authenticate(userId, userType, deviceType);
	}

	/**
	 * This listener catches ConnectedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is ConnectedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onConnected(final ConnectedEvent event) {
		final ConnectedEvent.Error error = event.getError();

		// Stop the loading dialog
		LoadingDialogFragment.hide(getFragmentManager());

		// If there is an error, this means that connection failed
		// So we display the English description of the error
		if (error != null) {
			getFragmentManager().beginTransaction().replace(android.R.id.content, ErrorFragment.newInstance(error.description())).commitAllowingStateLoss();
			return ;
		}

		if(!TextUtils.isEmpty(HelperApplication.overrideUID)) {
			onChoose(HelperApplication.overrideUID);
		} else {
			// If there is no error, everything went normal, connection succeeded.
			if (getResources().getBoolean(R.bool.isTablet)) {
				ChooseFragment.show(getString(R.string.log_in), getFragmentManager());
			}
			else {
				getFragmentManager().beginTransaction().replace(android.R.id.content, ChooseFragment.newInstance(getString(R.string.log_in))).commitAllowingStateLoss();
			}
		}
		
	}

	/**
	 * This listener catches AuthenticatedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is AuthenticatedEvent
	 * 3. It's activity object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onAuthenticated(final AuthenticatedEvent event) {
		final AuthenticatedEvent.Error error = event.getError();
		LoadingDialogFragment.hide(getFragmentManager());
		
		// If there is an error, this means that authentication failed
		// So we display the English description of the error
		// We then go back to the login fragment so that authentication can be tried again
		if (error != null) {
			if (error == AuthenticatedEvent.Error.BAD_APIKEY) {
				getFragmentManager().beginTransaction().replace(android.R.id.content, ErrorFragment.newInstance(error.description())).commitAllowingStateLoss();
				return ;
			}

			Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();

			return ;
		}

		// If there is no error, everything went normal, go to call activity
		ACRA.getErrorReporter().putCustomData(HelperApplication.ACRA_CUSTOM_INSTALL_ID, String.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getInt("INSTALL_ID", -1)));
		this.hasLoggedIn = true;
		startActivity(new Intent(this, ContactsActivity.class));
		finish();
	}
}
