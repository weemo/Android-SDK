package com.weemo.sdk.helper.contacts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent.Error;
import com.weemo.sdk.helper.R;

/**
 * Displays a confirmation dialog asking if the user want to call a remote contact.
 * This DialogFragment should only be called after the remote contact availability has been checked.
 */
public class ContactCallDialogFragment extends DialogFragment {

	/** Fragment required int argument key: the contact ID to call */
	private static final String ARG_CONTACTID = "contactId";

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param contactId The Contact IDentifier to call
	 * @return The created fragment
	 */
	public static ContactCallDialogFragment newInstance(final String contactId) {
		final ContactCallDialogFragment fragment = new ContactCallDialogFragment();
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
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(getArguments().getString(ARG_CONTACTID));
		dialog.setPositiveButton(R.string.call, new OnClickListener() {
			@Override public void onClick(final DialogInterface paramDialogInterface, final int paramInt) {
				// When the users clicks on the "OK" button,
				// We start the call
				final WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				weemo.createCall(getArguments().getString(ARG_CONTACTID));
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		return dialog.create();
	}

	@Override
	public void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	/**
	 * This listener catches CanCreateCallChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CanCreateCallChangedEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCanCreateCallChanged(final CanCreateCallChangedEvent event) {
		final Error error = event.getError();
		// If there is an error, it means the user can not create a call anymore.
		// This means that this popup should not be shown anymore.
		if (error != null) {
			Toast.makeText(getActivity(), error.description(), Toast.LENGTH_SHORT).show();
			dismiss();
		}
	}

}
