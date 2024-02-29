package com.example.smartlabiot;

import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.thingclips.smart.home.sdk.ThingHomeSdk;


public class MainActivity extends AppCompatActivity {
	
	private final String TAG = "SDK_INIT";
	private static final String CHANNEL_ID = "1";
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter bluetoothAdapter;
	// Declare the launcher at the top of your Activity/Fragment:
	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					// FCM SDK (and your app) can post notifications.
					createNotificationChannel();
				} else {
					// TODO: Inform user that that your app will not show notifications.
				}
			});
	
	private void askNotificationPermission() {
		// This is only necessary for API level >= 33 (TIRAMISU)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
					PackageManager.PERMISSION_GRANTED) {
				// FCM SDK (and your app) can post notifications.
			} else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
				// TODO: display an educational UI explaining to the user the features that will be enabled
				//       by them granting the POST_NOTIFICATION permission. This UI should provide the user
				//       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
				//       If the user selects "No thanks," allow the user to continue without notifications.
			} else {
				// Directly ask for the permission
				requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		
		ThingHomeSdk.init(getApplication(), "f8u9jmfumvpcjwvhcd3d", "smmysf9xas79nhfjcdqhtscpdvpesmv5");
		
		// Check if the app has BLUETOOTH and BLUETOOTH_CONNECT permissions
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
				!= PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
						!= PackageManager.PERMISSION_GRANTED) {
			// Permission is not granted, request it
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT},
					REQUEST_ENABLE_BT);
			
		} else {
			// Permission is granted, proceed to check and enable Bluetooth
			checkAndEnableBluetooth();
		}
		
		askNotificationPermission();
		
		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
//							Log.w(TAG, "Fetching FCM registration token failed", task.getException());
							Log.d(TAG, "onComplete: Token registration failed!");
							return;
						}
						
						// Get new FCM registration token
						String token = task.getResult();
						
						// Log and toast
//						String msg = getString(R.string.msg_token_fmt, token);token
//						Log.d(TAG, msg);
//						Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
						Log.d(TAG, "onComplete: The firebase token is: "+token);
					}
				}
		);
		// Delay starting the next activity
		
		
	}
	
	// This method will be called when the permission dialog is dismissed
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission granted, proceed to check and enable Bluetooth
				checkAndEnableBluetooth();
			} else {
				// Permission denied, show a toast or handle it as appropriate
				Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show();
				finish(); // Finish the activity or handle it as appropriate
			}
		}
	}
	
	// Method to check and enable Bluetooth
	private void checkAndEnableBluetooth() {
		// Initialize BluetoothAdapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			// Device doesn't support Bluetooth
			Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// Check if Bluetooth is enabled
		if (!bluetoothAdapter.isEnabled()) {
			// Bluetooth is not enabled, request to enable it
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			// Bluetooth is enabled
			// Proceed with your logic here
			handler();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				// Bluetooth is enabled by the user
				// Proceed with your logic here
			} else {
				// User denied enabling Bluetooth or an error occurred
				Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	private void createNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is not in the Support Library.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "Notification";
			String description = "Notification Channel";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this.
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}
	
	public void handler() {
		
		// Start the next activity
//				Intent intent = new Intent(MainActivity.this, Login.class);
//				startActivity(intent);
//				finish(); // Optional, depends on your requirements
		if (ThingHomeSdk.getUserInstance().isLogin()) {
			Intent intent = new Intent(MainActivity.this, HomeScreen.class);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(MainActivity.this, Login.class);
			startActivity(intent);
			finish();
		}
	}
}