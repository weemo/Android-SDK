package com.weemo.sdk.helper;

import java.io.IOException;

import javax.annotation.CheckForNull;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallCreatedEvent;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.call.IncomingActivity;
import com.weemo.sdk.helper.contacts.ContactsActivity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/*
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

	/*
	 * Player used to play ringing sound
	 */
	@CheckForNull MediaPlayer player;
	
	/*
	 * This is not a binded service
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/*
	 * Set the background notification
	 */
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
	
	/*
	 * Set the normal presence notification
	 * (Get the required parameters and call presenceNotification()
	 */
	private void normalPresenceNotification() {
		WeemoEngine weemo = Weemo.instance();
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

	/*
	 * Start notification on service start
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		normalPresenceNotification();
		
		return super.onStartCommand(intent, flags, startId);
	}

	/*
	 * Registers itself as a listener when created
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		Weemo.eventBus().register(this);
		
		try {
			AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.ring);
			player = new MediaPlayer();
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			player.setAudioStreamType(AudioManager.STREAM_RING);
			player.prepare();
			player.setLooping(true);
		}
		catch (IOException e) {
			e.printStackTrace();
			player = null;
		}
	}
	
	/*
	 * Unregisters itself as a listener when destroyed
	 */
	@Override
	public void onDestroy() {
		Weemo.eventBus().unregister(this);

		super.onDestroy();
	}

	/*
	 * This listener method catches CallCreatedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallCreatedEvent
	 * 3. It's service object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 */
	@WeemoEventListener
	public void onCallCreated(CallCreatedEvent e) {
		presenceNotification(android.R.drawable.ic_menu_call, e.getCall().getContactDisplayName(), "");
	}
	
	/*
	 * This listener method catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's service object has been registered with Weemo.getEventBus().register(this) in onCreate()
	 */
	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		// If the ringing sound is playing, it means that we have gone from RINGING to whatever-but-RINGING
		// In which case we stop the ringing
		if (player != null && player.isPlaying())
			player.stop();
		switch(e.getCallStatus()) {
		case ENDED:
			normalPresenceNotification();
			break ;
		case RINGING:
			// Starts the ringing
			if (player != null)
				player.start();
			// Starts pickup dialog activity
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
