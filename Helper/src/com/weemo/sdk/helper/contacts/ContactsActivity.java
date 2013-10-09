package com.weemo.sdk.helper.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.helper.ChooseFragment;
import com.weemo.sdk.helper.ChooseFragment.ChooseListener;
import com.weemo.sdk.helper.call.CallActivity;
import com.weemo.sdk.helper.ConnectedService;
import com.weemo.sdk.helper.R;

public class ContactsActivity extends Activity implements ChooseListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(this, ConnectedService.class));
		
		if(savedInstanceState == null) {
			Weemo weemo = Weemo.instance();
			assert weemo != null;
			
			String uid = weemo.getToken();
			assert uid != null;

			String displayName = weemo.getDisplayName();

			boolean askForDisplayName = displayName.isEmpty();
			
			if (askForDisplayName)
				displayName = uid;
			
			getFragmentManager()
				.beginTransaction()
				.add(android.R.id.content, ChooseFragment.newInstance(getString(R.string.check), displayName))
				.commit();
		
			if (askForDisplayName) {
				String defaultName = "";
				if (ChooseFragment.ACCOUNTS.containsKey(uid))
					defaultName = ChooseFragment.ACCOUNTS.get(uid);
	            AskDisplayNameDialogFragment.newInstance(defaultName).show(getFragmentManager(), null);
			}
		}
		
		setTitleFromDisplayName();
	}
	
	@Override
	public void onChoose(String contactId) {
		ContactCheckDialogFragment.newInstance(contactId).show(getFragmentManager(), null);
	}
	
	public void askToCall(String contactId) {
		ContactCallDialogFragment.newInstance(contactId).show(getFragmentManager(), null);
	}

	public void makeCall(String contactId) {
		Weemo weemo = Weemo.instance();
		assert weemo != null;
		weemo.createCall(contactId);
	}
	
	private void setTitleFromDisplayName() {
		Weemo weemo = Weemo.instance();
		assert weemo != null;
		String displayName = weemo.getDisplayName();
		if (!displayName.isEmpty())
			setTitle(displayName);
		else {
			String uid = weemo.getToken();
			assert uid != null;
			setTitle("{" + uid + "}");
		}
	}
	
	public void setDisplayName(String displayName) {
		Weemo weemo = Weemo.instance();
		assert weemo != null;
		weemo.setDisplayName(displayName);
		startService(new Intent(this, ConnectedService.class));
		setTitleFromDisplayName();
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// This should always be the first statement of onStart
		Weemo.onActivityStart();

		// Register as event listener
		Weemo.getEventBus().register(this);
		
		Weemo weemo = Weemo.instance();
		assert weemo != null;
		((ChooseFragment) getFragmentManager().findFragmentById(android.R.id.content)).setEnabled(weemo.canCreateCall());

		WeemoCall call = weemo.getCurrentCall();
		if (call != null) {
			Log.i("NPEAVOID", "Starting CallActivity from Contacts");
			startActivity(
				new Intent(this, CallActivity.class)
					.putExtra("callId", call.getCallId())
			);
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(R.string.disconnect).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				Weemo.destroy();
				return true;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}

	@WeemoEventListener
	public void onCanCreateCallChanged(CanCreateCallChangedEvent e) {
		CanCreateCallChangedEvent.Error error = e.getError();
		if (error == null) {
			// We can create call
			((ChooseFragment) getFragmentManager().findFragmentById(android.R.id.content)).setEnabled(true);
		}
		else {
			switch (error) {
			case NETWORK_LOST:
				// The cannot create call
				((ChooseFragment) getFragmentManager().findFragmentById(android.R.id.content)).setEnabled(false);
				break;
			case SYSTEM_ERROR:
				Toast.makeText(this, error.description(), Toast.LENGTH_LONG).show();
				// intentional fallthrough
			case DESTROYED:
				stopService(new Intent(ContactsActivity.this, ConnectedService.class));
				finish();
				break;
			}
		}
	}
	
	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		if (e.getCallStatus() == CallStatus.PROCEEDING) {
			ContactCallingDialogFragment dialog = ContactCallingDialogFragment.newInstance(e.getCall().getCallId());
			dialog.setCancelable(false);
			dialog.show(getFragmentManager(), null);
		}
	}
}
