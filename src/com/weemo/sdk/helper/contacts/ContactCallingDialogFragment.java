package com.weemo.sdk.helper.contacts;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.R;

/**
 * This fragments displays a loading dialog while calling a remote contact.
 * When the call is established, it starts the CallActivity.
 */
public class ContactCallingDialogFragment extends DialogFragment {

	/** Fragment required int argument key: the contact ID who's calling */
	private static final String ARG_CALLID = "callId";

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param callId The ID of the call to answer (or not)
	 * @return The created fragment
	 */
	public static ContactCallingDialogFragment newInstance(final int callId) {
		final ContactCallingDialogFragment fragment = new ContactCallingDialogFragment();
		final Bundle args = new Bundle();
		args.putInt(ARG_CALLID, callId);
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
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(getArguments().getString("contactId"));
		dialog.setMessage(getString(R.string.calling));
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new OnClickListener() {
			@Override public void onClick(final DialogInterface paramDialogInterface, final int paramInt) {
				final WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				final WeemoCall call = weemo.getCall(getArguments().getInt(ARG_CALLID));
				if (call != null) {
					call.hangup();
				}
			}
		});
		return dialog;
	}

	@Override
	public void onDestroy() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	/**
	 * This listener catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onStart()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCallStatusChanged(final CallStatusChangedEvent event) {
		// We check that the call affected is the one we are monitoring
		if (event.getCall().getCallId() != getArguments().getInt(ARG_CALLID)) {
			return ;
		}

		// If the call is now ENDED, it means that the remote user has either:
		// - disconnected
		// - refused the call
		if (event.getCallStatus() == CallStatus.ENDED) {
			dismiss();
		}

		// If the call is now ACTIVE, it is now taking place
		// we therefore start the CallActivity
		else if (event.getCallStatus() == CallStatus.ACTIVE) {
			((ContactsActivity)getActivity()).startCallWindow(event.getCall());
			dismissAllowingStateLoss();
		}
	}
}
