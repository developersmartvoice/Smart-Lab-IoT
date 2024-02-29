package com.example.smartlabiot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

public class HomeScreen extends AppCompatActivity {
	private static final String TAG = "HOME_SCREEN";
	private User user;
	private TextView etHomeScreenTxt;
	
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
	}
	public void mainWorkFlow(){
		etHomeScreenTxt.setText("Hello, "+user.getUsername());
	}
}