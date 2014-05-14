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

/**
 * This fragments checks if a remote contact is available.
 * Displays a loading dialog while checking.
 */
public class ContactCheckDialogFragment extends DialogFragment {

	/** Fragment required int argument key: the contact ID to check */
	private static final String ARG_CONTACTID = "contactId";

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param contactId The Contact IDentifier to check
	 * @return The created fragment
	 */
	public static ContactCheckDialogFragment newInstance(final String contactId) {
		final ContactCheckDialogFragment fragment = new ContactCheckDialogFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_CONTACTID, contactId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Register as event listener
		Weemo.eventBus().register(this);

		// Asks weemo engine to do the check
		// The answer of this check will be provided through a StatusEvent
		final WeemoEngine weemo = Weemo.instance();
		assert weemo != null;
		weemo.getStatus(getArguments().getString(ARG_CONTACTID));
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(getArguments().getString(ARG_CONTACTID));
		dialog.setMessage(getString(R.string.check));
		return dialog;
	}

	@Override
	public void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	/**
	 * This listener catches StatusEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is StatusEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onStatus(final StatusEvent event) {
		// First, we check that the status event affects the remote user we asked.
		// In other terms, we check that this is the answer to the question we asked
		if (!event.getUserID().equals(getArguments().getString(ARG_CONTACTID))) {
			return ;
		}

		// If the remote contact can be called, we propose the user to do so.
		// If it can't, we display a toast saying so.
		if (event.canBeCalled()) {
			ContactCallDialogFragment.newInstance(getArguments().getString(ARG_CONTACTID)).show(getFragmentManager(), null);
		}
		else {
			Toast.makeText(getActivity(), R.string.unreachable, Toast.LENGTH_SHORT).show();
		}

		dismiss();
	}
}
