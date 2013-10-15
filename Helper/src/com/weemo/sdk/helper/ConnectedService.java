package com.weemo.sdk.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallCreatedEvent;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.call.IncomingActivity;
import com.weemo.sdk.helper.contacts.ContactsActivity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
public class ConnectedService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void presenceNotification(int icon, String title, String message) {
		Intent intent = new Intent(this, ContactsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification n = new NotificationCompat.Builder(this)
			.setContentTitle(title)
			.setContentText(message)
			.setSmallIcon(icon)
			.setContentIntent(pendingIntent)
			.build()
		;
		startForeground(42, n);
	}
	
	private void normalPresenceNotification() {
		Weemo weemo = Weemo.instance();
		assert weemo != null;
		String displayName = weemo.getDisplayName();
		if (displayName != null)
			displayName = weemo.getUserId();
		assert displayName != null;
		presenceNotification(
				R.drawable.ic_launcher,
				getString(R.string.app_name),
				displayName
			);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		normalPresenceNotification();
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Weemo.getEventBus().register(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Weemo.getEventBus().unregister(this);
	}

	@WeemoEventListener
	public void onCallCreated(CallCreatedEvent e) {
		presenceNotification(android.R.drawable.ic_menu_call, e.getCall().getContactDisplayName(), "");
	}
	
	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		switch(e.getCallStatus()) {
		case ENDED:
			normalPresenceNotification();
			break ;
		case RINGING:
			startActivity(
				new Intent(this, IncomingActivity.class)
					.putExtra("displayName", e.getCall().getContactDisplayName())
					.putExtra("callId", e.getCall().getCallId())
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			);
			break ;
		default:
			break ;
		}
	}
	
}
