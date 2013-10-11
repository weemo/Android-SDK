package com.weemo.sdk.helper;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.AuthenticatedEvent;
import com.weemo.sdk.event.global.ConnectedEvent;
import com.weemo.sdk.helper.ChooseFragment.ChooseListener;
import com.weemo.sdk.helper.contacts.ContactsActivity;

public class ConnectActivity extends Activity implements ChooseListener {

	private boolean hasLoggedIn = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Weemo weemo = Weemo.instance();
		if (weemo != null && weemo.isAuthenticated()) {
			// TODO: Unset this and investigate deadlock
			hasLoggedIn = true;
			startActivity(
				new Intent(this, ContactsActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
				);
			finish();
		}
		
		// This activity starts with a LoadingFragment
		if (savedInstanceState == null) {
			LoadingDialogFragment dialog = LoadingDialogFragment.newFragmentInstance(getString(R.string.connection_title), getString(R.string.connection_text));
			dialog.setCancelable(false);
			dialog.show(getFragmentManager(), "dialog");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// This should always be the first statement of onStart
		Weemo.onActivityStart();

		// Register as event listener
		Weemo.getEventBus().register(this);

		// Initialize Weemo, can be called multiple times
		Weemo.initialize(getString(R.string.weemo_mobileAppId), this);
	}

	@Override
	protected void onStop() {
		// Unregister as event listener
		Weemo.getEventBus().unregister(this);

		// This should always be the last statement of onStop
		Weemo.onActivityStop();

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// Stops Weemo if this activity is destroyed with hasLoggedIn is true,
		// this means it is destroyed after CallActivity being displayed.
		// However, if hasLoggedIn is false, it means that the activity is destroyed
		// because the user has got out of the application without logging in.
		// In this case we need to stop the Weemo engine
		if (!hasLoggedIn)
			Weemo.destroy();
		
		super.onDestroy();
	}
	
	// This is called by the ChoseFragment when user clicks on "login" button
	@Override
	public void onChoose(String chose) {
		Weemo weemo = Weemo.instance();
		// Weemo must be instanciated at this point
		assert weemo != null;

		// Start authentication
		boolean ok = weemo.authenticate(chose, Weemo.UserType.INTERNAL);
		if (ok) {
			LoadingDialogFragment dialog = LoadingDialogFragment.newFragmentInstance(chose, getString(R.string.authentication_title));
			dialog.setCancelable(false);
			dialog.show(getFragmentManager(), "dialog");
		}
		else
			Toast.makeText(this, R.string.incorrect_userid, Toast.LENGTH_SHORT).show();
	}

	// This listener will be called when a ConnectedEvent occurs because:
	// 1. It is annotated with @WeemoEventListener
	// 2. It takes one argument which type is ConnectedEvent
	// 3. It's object has been registered with Weemo.getEventBus().register(this) in onResume()
	@WeemoEventListener
	public void onConnected(ConnectedEvent e) {
		ConnectedEvent.Error error = e.getError();
		
		if (error != null) {
			// There was an error, so we display its English description
			Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
			finish();
			return ;
		}
		
		// If there is no error, everything went normal, show login fragment
		Log.i("=====", "LAUNCHING LOGIN");
		DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
		if (dialog != null)
			dialog.dismiss();
        getFragmentManager().beginTransaction().replace(android.R.id.content, ChooseFragment.newInstance(getString(R.string.log_in))).commit();
	}

	// This listener will be called when an AuthenticatedEvent occurs because:
	// 1. It is annotated with @WeemoEventListener
	// 2. It takes one argument which type is AuthenticatedEvent
	// 3. It's object has been registered with Weemo.getEventBus().register(this) in onResume()
	@WeemoEventListener
	public void onAuthenticated(AuthenticatedEvent e) {
		AuthenticatedEvent.Error error = e.getError();
		
		if (error != null) {
			// There was an error, so we display its English description as a toast and go back to login fragment
			Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
			DialogFragment dialog = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
			if (dialog != null)
				dialog.dismiss();
			return ;
		}
		
		// If there is no error, everything went normal, go to call activity
		Log.i("=====", "LAUNCHING CALL");

		// If error is null, then auth went ok, then there is a UserID
		hasLoggedIn = true;
		startActivity(new Intent(this, ContactsActivity.class));
		finish();
	}


}
