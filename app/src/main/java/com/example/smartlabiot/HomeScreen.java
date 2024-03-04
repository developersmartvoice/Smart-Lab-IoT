package com.example.smartlabiot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback;
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback;
import com.thingclips.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeScreen extends AppCompatActivity {
	private static final String TAG = "HOME_SCREEN";
	private User user;
	private ImageButton btnAddDevice;
	private TextView etHomeScreenTxt;
	private List<HomeBean> homeBeansGlobal;
//	private long homeId;
	private String homeName;
	private HomeBean beanGlobal;
	private List<DeviceBean> deviceBeanList;
	private List<String> rooms = new ArrayList<>(Arrays.asList("Living Room", "Master Bedroom", "Second Bedroom",
			"Dining Room", "Kitchen","Study Room"));
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
		intiViews();
		getUserInfo();
		mainWorkFlow();
	}
	
	public void getUserInfo(){
		user = ThingHomeSdk.getUserInstance().getUser();
		assert user != null;
		Log.d(TAG, "onCreate: To fetch the user information "+user.getUsername());
//		user.getUsername();
	}
	public void intiViews(){
		etHomeScreenTxt = findViewById(R.id.etHomeScreenTxt);
		btnAddDevice = findViewById(R.id.btnAddDevice);
	}
	public void mainWorkFlow(){
		etHomeScreenTxt.setText("Hello, "+user.getUsername());
		checkExistingHomeList();
		btnAddDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeScreen.this, AddDevice.class);
				intent.putExtra("homeId", beanGlobal.getHomeId());
				startActivity(intent);
			}
		});
	}
	public void checkExistingHomeList(){
		ThingHomeSdk.getHomeManagerInstance().queryHomeList(new IThingGetHomeListCallback() {
			@Override
			public void onSuccess(List<HomeBean> homeBeans) {
				// do something
				Log.d(TAG, "onSuccess: Checking is there any existing home available. "+homeBeans);
				homeBeansGlobal = homeBeans;
				if(homeBeansGlobal.isEmpty()){
					createHome();
				}else{
					checkHomeDetails();
				}
				
			}
			@Override
			public void onError(String errorCode, String error) {
				// do something
				Log.d(TAG, "onError: Error on checking home beans. "+error);
			}
		});
		
	}
	public void checkHomeDetails(){
		long homeId = homeBeansGlobal.get(0).getHomeId();
		ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(new IThingHomeResultCallback() {
			@Override
			public void onSuccess(HomeBean bean) {
				// do something
				beanGlobal = bean;
				homeName = beanGlobal.getName();
				homeNameTextSet1();
				checkDevice();
			}
			@Override
			public void onError(String errorCode, String errorMsg) {
				// do something
			}
		});
		
	}
	public void createHome() {

			Log.d(TAG, "createHome: Need to create a home");
			
			// Inflate the dialog layout
			View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_home, null);
			
			// Initialize views from the dialog layout
			EditText editTextHomeName = dialogView.findViewById(R.id.editTextHomeName);
			Button buttonCreateHome = dialogView.findViewById(R.id.buttonCreateHome);
			
			// Create an AlertDialog.Builder
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(dialogView);
			builder.setCancelable(false);
			
			// Create and show the AlertDialog
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
			
			// Set onClickListener for the button
			buttonCreateHome.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Get the entered home name from EditText
					homeName = editTextHomeName.getText().toString();
					if (!homeName.isEmpty()){
						ThingHomeSdk.getHomeManagerInstance().createHome(homeName, 0, 0, "",
								rooms, new IThingHomeResultCallback() {
							@Override
							public void onSuccess(HomeBean bean) {
								// do something
								Log.d(TAG, "onSuccess: Home created successfully! "+bean.getHomeId());
								beanGlobal = bean;
								homeBeansGlobal.add(bean);
								homeNameTextSet1();
								checkDevice();
							}
							@Override
							public void onError(String errorCode, String errorMsg) {
								// do something
							}
						});
					}
					else {
						Toast.makeText(HomeScreen.this,"Enter home name!",Toast.LENGTH_SHORT).show();
					}
					// Perform actions to create home using homeName
					// For now, let's just dismiss the dialog
					alertDialog.dismiss();
				}
			});
	}
	
	public void homeNameTextSet1(){
		TextView textView = new TextView(this);
		textView.layout(10,10,10,10);
		textView.setText("Home name is: "+homeName);
		textView.setTextSize(16);
		// Set ID for the TextView
//		textView.setId();
		textView.setTextColor(getResources().getColor(R.color.white));
		textView.setTypeface(Typeface.MONOSPACE);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL; // Set layout gravity to center horizontal
		textView.setLayoutParams(layoutParams);
		LinearLayout containerLayout = findViewById(R.id.homeScreenLinearlayout);
		containerLayout.addView(textView);
	}
	public void checkDevice() {
		deviceBeanList = beanGlobal.getDeviceList();
		Log.d(TAG, "checkDevice: "+deviceBeanList.size());
		if (!deviceBeanList.isEmpty()) {
			for (DeviceBean device : deviceBeanList) {
				// Create card dynamically for each device
				createCardForDevice(device);
			}
		} else {
			// Handle case when deviceBeanList is empty
		}
	}
	
	private void createCardForDevice(DeviceBean device) {
		// Create a new card view programmatically
		CardView cardView = new CardView(this);
		// Set card view properties as needed
		cardView.setCardElevation(8);
		cardView.setRadius(16);
		cardView.setContentPadding(16, 16, 16, 16);
		cardView.setClickable(true); // Make the card clickable
		
		// Create an image view to display the device icon on the left side
		ImageView iconImageView = new ImageView(this);
		// Set icon image from URL using Picasso
		Picasso.get().load(device.getIconUrl()).into(iconImageView); // Change R.drawable.device_icon to your actual icon resource
		// Set layout parameters for the icon image view to align it to the start (left) side
		LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		iconLayoutParams.gravity = Gravity.START;
		iconImageView.setLayoutParams(iconLayoutParams);
		// Add icon image view to the card view
		cardView.addView(iconImageView);
		
		// Create a text view to display device information
		TextView textView = new TextView(this);
		// Set text view properties
		textView.setText(device.getName()); // Assuming getName() returns device name
		textView.setTextSize(16);
		// Set text view to be aligned to the end (right side) of the card
		LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		textView.setLayoutParams(textLayoutParams);
		textView.setGravity(Gravity.END); // Align text to the end (right side)
		// Add the text view to the card view
		cardView.addView(textView);
		
		// Create a text view to display the online/offline status
		TextView statusTextView = new TextView(this);
		statusTextView.setTextSize(12);
		// Set layout parameters to align the status text to the end (right side) and bottom of the card
		FrameLayout.LayoutParams statusLayoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT
		);
		statusLayoutParams.gravity = Gravity.END | Gravity.BOTTOM;
		statusTextView.setLayoutParams(statusLayoutParams);
		// Set status text and background color based on device status
		if (device.getIsOnline()) {
			statusTextView.setText("Online");
			statusTextView.setBackgroundColor(Color.GREEN); // Set background color to green for online
		} else {
			statusTextView.setText("Offline");
			statusTextView.setBackgroundColor(Color.RED); // Set background color to red for offline
		}
		// Add the status text view to the card view
		cardView.addView(statusTextView);
		
		// Add click listener to the card view
		cardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Handle card click event here
				// You can perform any action you want when the card is clicked
				// For example, open a new activity or show a dialog
				if (device.getName().toLowerCase().equals("JZ Doorbell".toLowerCase())){
					Intent intent = new Intent(HomeScreen.this, DoorBellView.class);
					intent.putExtra("deviceId", device.getDevId()); // Pass devResp as an
					startActivity(intent);
				}
			}
		});
		
		// Add the card view to the layout (assuming you have a LinearLayout with id "containerLayout")
		LinearLayout containerLayout = findViewById(R.id.homeScreenLinearlayout);
		containerLayout.addView(cardView);
	}
	
}