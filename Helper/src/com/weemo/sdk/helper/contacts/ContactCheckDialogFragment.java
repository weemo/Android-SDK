package com.weemo.sdk.helper.contacts;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.StatusEvent;
import com.weemo.sdk.helper.R;

/*
 * This fragments checks if a remote contact is available.
 * Displays a loading dialog while checking.
 */
public class ContactCheckDialogFragment extends DialogFragment {
	
	public static ContactCheckDialogFragment newInstance(String contactId) {
		ContactCheckDialogFragment fragment = new ContactCheckDialogFragment();
		Bundle args = new Bundle();
		args.putString("contactId", contactId);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(getArguments().getString("contactId"));
		dialog.setMessage(getString(R.string.check));
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Register as event listener
		Weemo.eventBus().register(this);
		
		// Asks weemo engine to do the check
		// The answer of this check will be provided through a StatusEvent
		WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		weemo.getStatus(getArguments().getString("contactId"));
	}

	@Override
	public void onStop() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onStop();
	}
	
	/*
	 * This listener catches StatusEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is StatusEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onStart()
	 */
	@WeemoEventListener
	public void onStatus(StatusEvent e) {
		// First, we check that the status event affects the remote user we asked.
		// In other terms, we check that this is the answer to the question we asked
		if (!e.getUserID().equals(getArguments().getString("contactId")))
			return ;

		// If the remote contact can be called, we propose the user to do so.
		// If it can't, we display a toast saying so.
		if (e.canBeCalled())
			ContactCallDialogFragment.newInstance(getArguments().getString("contactId")).show(getFragmentManager(), null);
		else
			Toast.makeText(getActivity(), R.string.unreachable, Toast.LENGTH_SHORT).show();

		dismiss();
	}
}
