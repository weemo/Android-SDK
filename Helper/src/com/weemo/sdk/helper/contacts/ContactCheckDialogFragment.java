package com.weemo.sdk.helper.contacts;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.StatusEvent;
import com.weemo.sdk.helper.R;

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
		Weemo.getEventBus().register(this);
		
		// Ask if contactId can be called
		Weemo weemo = Weemo.instance();
		assert weemo != null;
		weemo.getStatus(getArguments().getString("contactId"));
	}

	@Override
	public void onStop() {
		// Unregister as event listener
		Weemo.getEventBus().unregister(this);

		super.onStop();
	}
	
	@WeemoEventListener
	public void onStatus(StatusEvent e) {
		// TODO: Reactivate this
		if (!e.getUserID().equals(getArguments().getString("contactId")))
			return ;

		if (e.canBeCalled())
			((ContactsActivity) getActivity()).askToCall(getArguments().getString("contactId"));
		else
			Toast.makeText(getActivity(), R.string.unreachable, Toast.LENGTH_SHORT).show();

		dismiss();
	}
}
