package com.weemo.sdk.helper.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.connect.ConnectedService;

/**
 * Fragment displaying an error and closing its activity when the user has acknowledged it
 */
public class ErrorFragment extends Fragment implements View.OnClickListener {

	/** Fragment required string argument key: the text of the error */
	private static final String ARG_TEXT = "text";

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param text The text of the error
	 * @return The created fragment
	 */
	public static ErrorFragment newInstance(final String text) {
		final ErrorFragment fragment = new ErrorFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_TEXT, text);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_error, container, false);

		final TextView errorText = (TextView) root.findViewById(R.id.errorText);
		errorText.setText(getArguments().getString(ARG_TEXT));

		final Button close = (Button) root.findViewById(R.id.close);
		close.setOnClickListener(this);

		return root;
	}

	@Override
	public void onClick(final View view) {
		Weemo.disconnect();
		getActivity().stopService(new Intent(getActivity(), ConnectedService.class));
		getActivity().finish();
	}
}
