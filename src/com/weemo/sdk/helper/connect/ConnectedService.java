package com.weemo.sdk.helper.connect;

import javax.annotation.CheckForNull;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallCreatedEvent;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.call.IncomingActivity;
import com.weemo.sdk.helper.contacts.ContactsActivity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This is a service that will manage the notification saying that you are connected or that you are having a call.
 * It is also this service that will show the "you are called" popup when the user receives an incoming call.
 *
 * This service is supposed to be started upon connection and stopped upon disconnection.
 *
 * By default, it sets a service background notification that just says that the user is connected.
 *
 * It the listens for:
 *  - CallCreatedEvent to change the notification to it's call state
 *  - CallStatusChangedEvent with status ENDED to put the notification back to it's normal state
 *  - CallStatusChangedEvent with status RINGING to show the "you are called" popup
 */
@SuppressFBWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
public class ConnectedService extends Service {
	
	/** Ringtone used to notify incomming calls */
	protected @CheckForNull Ringtone ringtone;
	
	/** Handler used to stop ringtone */
	private @CheckForNull Handler handler;
	
	/** Runnable used to stop ringtone */
	private @CheckForNull Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (ConnectedService.this.ringtone != null && ConnectedService.this.ringtone.isPlaying()) {
				ConnectedService.this.ringtone.stop();
			}
		}
	};

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	/**
	 * Set the background notification
	 *
	 * @param icon The icon of the notification
	 * @param title The title of the notification
	 * @param message The message of the notification
	 */
	private void presenceNotification(final int icon, final String title, final String message) {
		final Intent intent = new Intent(this, ContactsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		final Notification notification = new NotificationCompat.Builder(this)
			.setContentTitle(title)
			.setContentText(message)
			.setSmallIcon(icon)
			.setContentIntent(pendingIntent)
			.build()
		;
		startForeground(42, notification);
	}

	/**
	 * Set the normal presence notification
	 * (Get the required parameters and call presenceNotification()
	 */
	private void normalPresenceNotification() {
		final WeemoEngine weemo = Weemo.instance();
		if (weemo == null) {
			return ;
		}
		final String displayName = weemo.getDisplayName();
		presenceNotification(
				R.drawable.ic_launcher,
				getString(R.string.app_name),
				displayName
			);
	}

	/**
	 * Start notification on service start
	 */
	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		normalPresenceNotification();

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Registers itself as a listener when created
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		this.handler = new Handler();
		Weemo.eventBus().register(this);
	}

	/**
	 * Unregisters itself as a listener when destroyed
	 */
	@Override
	public void onDestroy() {
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	/**
	 * This listener method catches CallCreatedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallCreatedEvent
	 * 3. It's service object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCallCreated(final CallCreatedEvent event) {
		presenceNotification(android.R.drawable.ic_menu_call, event.getCall().getContactDisplayName(), "");
	}

	/**
	 * This listener method catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's service object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 *
	 * @param event The event
	 */
	@WeemoEventListener
	public void onCallStatusChanged(final CallStatusChangedEvent event) {
		// If the ringing sound is playing, it means that we have gone from RINGING to whatever-but-RINGING
		// In which case we stop the ringing
		this.handler.removeCallbacks(this.runnable);
		if (this.ringtone != null && this.ringtone.isPlaying()) {
			this.ringtone.stop();
		}

		switch (event.getCallStatus()) {
		case ENDED:
			normalPresenceNotification();
			break;
		case RINGING:
			if (this.ringtone == null) {
				this.ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
			}
			if (this.ringtone != null) {
				this.ringtone.play();
				this.handler.postDelayed(this.runnable, 30000);
			}
			
			// Starts pickup dialog activity
			startActivity(
				new Intent(this, IncomingActivity.class)
					.putExtra(IncomingActivity.EXTRA_DISPLAYNAME, event.getCall().getContactDisplayName())
					.putExtra(IncomingActivity.EXTRA_CALLID, event.getCall().getCallId())
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			);
			break ;
		default:
			break ;
		}
	}

}
