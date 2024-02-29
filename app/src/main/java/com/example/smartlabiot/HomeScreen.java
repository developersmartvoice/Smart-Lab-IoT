package com.example.smartlabiot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
				finish();
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
				createHome();
			}
			@Override
			public void onError(String errorCode, String error) {
				// do something
				Log.d(TAG, "onError: Error on checking home beans. "+error);
			}
		});
		
	}
	public void createHome() {
		if (homeBeansGlobal.isEmpty()) {
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
		} else {
			beanGlobal = homeBeansGlobal.get(0);
			homeName = beanGlobal.getName();
			homeNameTextSet1();
			checkDevice();
		}
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
		// For example, you can set card elevation, corner radius, padding, etc.
		cardView.setCardElevation(8);
		cardView.setRadius(16);
		cardView.setContentPadding(16, 16, 16, 16);
		
		// Create a text view to display device information
		TextView textView = new TextView(this);
		// Set text view properties
		textView.setText(device.getName()); // Assuming getName() returns device name
		textView.setTextSize(16);
		
		// Add the text view to the card view
		cardView.addView(textView);
		
		// Add the card view to the layout (assuming you have a LinearLayout with id "containerLayout")
		LinearLayout containerLayout = findViewById(R.id.homeScreenLinearlayout);
		containerLayout.addView(cardView);
		
		// Optionally, you can set click listeners or other properties for the card view or text view
	}
	
}