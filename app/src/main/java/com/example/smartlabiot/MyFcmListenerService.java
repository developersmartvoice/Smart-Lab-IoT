package com.example.smartlabiot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thingclips.smart.android.camera.sdk.bean.ThingDoorBellCallModel;
import com.thingclips.smart.android.camera.sdk.callback.ThingSmartDoorBellObserver;
import com.thingclips.smart.sdk.bean.DeviceBean;

import java.util.HashMap;

public class MyFcmListenerService extends FirebaseMessagingService {
	public static final String TAG = "MyFcmListenerService";
	public static final String CHANNEL_ID = "my_channel_01";
	public static HashMap<String, Long> pushTimeMap = new HashMap<>();
	private ThingSmartDoorBellObserver observer;
	private ThingDoorBellCallModel callModel;
	private DeviceBean deviceModel;
	
	// Constructor or initialization method where you set up the observer and other objects
	public MyFcmListenerService() {
		// Initialize your observer
		observer = new ThingSmartDoorBellObserver() {
			@Override
			public void doorBellCallDidReceivedFromDevice(ThingDoorBellCallModel callModel, DeviceBean deviceModel) {
				// Implement this method as needed
				Log.d(TAG, "doorBellCallDidReceivedFromDevice: Jani na ki korlam kintu kichu ekta korlam!");
			}
			
			@Override
			public void doorBellCallDidAnsweredByOther(ThingDoorBellCallModel callModel) {
				// Implement this method as needed
			}
			
			@Override
			public void doorBellCallDidCanceled(ThingDoorBellCallModel callModel, boolean isTimeOut) {
				// Implement this method as needed
			}
			
			@Override
			public void doorBellCallDidHangUp(ThingDoorBellCallModel callModel) {
				// Implement this method as needed
			}
		};
		
		// Initialize other objects as needed
	}
	
	@Override
	public void onMessageReceived(RemoteMessage message) {
		Log.d(TAG, "FCM message received: " + message.getData().toString());
		
		// Access your observer and callModel objects here
		if (observer != null && callModel != null) {
			// Trigger actions in your observer based on the FCM message
			// For example:
			observer.doorBellCallDidReceivedFromDevice(callModel, deviceModel);
			showNotification(message.getData().get("title"),message.getData().get("message"));
		}
	}



	
	private void showNotification(String title, String message) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.drawable.baseline_alarm_24)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		
		// Create a NotificationManager object
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Check if the Android version is Oreo or higher to create a notification channel
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "MyChannel";
			String description = "Channel description";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system
			notificationManager.createNotificationChannel(channel);
		}
		
		// Display the notification
		notificationManager.notify(1, builder.build());
	}
}
