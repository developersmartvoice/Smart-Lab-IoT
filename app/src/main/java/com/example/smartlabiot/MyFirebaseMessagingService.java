package com.example.smartlabiot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IResultCallback;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	private final static String TAG = "Firebase Messaging";
	private static final String CHANNEL_ID = "1";
	
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getFrom());
		
		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
			
//			if (/* Check if data needs to be processed by long running job */ true) {
//				// For long-running tasks (10 seconds or more) use WorkManager.
////				scheduleJob();
//			} else {
//				// Handle message within 10 seconds
////				handleNow();
//			}
//			send
			
		}
		
		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
		}
		
		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}
	@Override
	public void onNewToken(@NonNull String token) {
		Log.d(TAG, "Refreshed token: " + token);
		
		// If you want to send messages to this application instance or
		// manage this apps subscriptions on the server side, send the
		// FCM registration token to your app server.
		ThingHomeSdk.getPushInstance().registerDevice(token, "fcm",
				new IResultCallback() {
					@Override
					public void onError(String code, String error) {
					}
					
					@Override
					public void onSuccess() {
						Log.d(TAG, "onSuccess: Device Registered with Firebase Token! old");
					}
				});
		sendRegistrationToServer(token);
	}
	
	public void sendRegistrationToServer(String token){
		// Create an explicit intent for an Activity in your app.
		Intent intent = new Intent(this, MyFirebaseMessagingService.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_launcher_background)
				.setContentTitle("Notification")
				.setContentText("Hello world")
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				// Set the intent that fires when the user taps the notification.
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);
	}
	
}
