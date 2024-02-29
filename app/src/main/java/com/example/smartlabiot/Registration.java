package com.example.smartlabiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IResultCallback;

public class Registration extends AppCompatActivity {
	private static final String TAG = "REGISTRATION";
	private TextView etRegisterEmail,etVerifyCode,etRegisterPass;
	private Button btnRegister,btnGetCode;
	private String email,pass,verificationCode;
//	boolean isCodeSent = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		initViews();
		btnRegister.setVisibility(View.INVISIBLE);
		btnGetCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				email = etRegisterEmail.getText().toString();
				Log.d(TAG, "onClick: "+email);
				if(email.isEmpty()){
					Toast.makeText(Registration.this, "Enter Your Email!",Toast.LENGTH_SHORT).show();
				}else {
					sendVerificationCode();
//					if (isCodeSent){
//						btnRegister.setVisibility(View.VISIBLE);
//					}
				}
			}
		});
		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pass = etRegisterPass.getText().toString();
				verificationCode = etVerifyCode.getText().toString();
				if (pass.isEmpty() && verificationCode.isEmpty()){
					Toast.makeText(Registration.this, "Enter the Verification Code!",
							Toast.LENGTH_SHORT).show();
				}
				else {
					registration();
				}
			}
		});
		
	}
	public void initViews(){
		etRegisterEmail = findViewById(R.id.etRegisterEmail);
		etRegisterPass = findViewById(R.id.etRegisterPass);
		etVerifyCode = findViewById(R.id.etVerifyCode);
		btnRegister = findViewById(R.id.btnRegister);
		btnGetCode = findViewById(R.id.btnGetCode);
	}
	public void registration(){
		// Registers an account with an email address and a password.
		ThingHomeSdk.getUserInstance().registerAccountWithEmail("880", email,pass,verificationCode,
				new IRegisterCallback() {
			@Override
			public void onSuccess(User user) {
				Toast.makeText(Registration.this, "Registered successfully.", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onSuccess: Registration Successful. Here is User info! "+user.toString());
				Intent intent = new Intent(Registration.this, HomeScreen.class);
				startActivity(intent);
				finish();
			}
			
			@Override
			public void onError(String code, String error) {
				Toast.makeText(Registration.this, "code: " + code + "error:" + error,
						Toast.LENGTH_SHORT).show();
			}
		});
	}
	public void sendVerificationCode(){
		// Returns a verification code to an email address.
		ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(email, "", "880", 1,
				new IResultCallback() {
			@Override
			public void onError(String code, String error) {
				Toast.makeText(Registration.this, "code: " + code + "error:" + error,
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onError: You got an Error! "+error);
			}
			
			@Override
			public void onSuccess() {
//				isCodeSent = true;
				btnRegister.setVisibility(View.VISIBLE);
				Toast.makeText(Registration.this, "Verification code returned successfully.",
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Verification Code Sent Successfully!");
				btnGetCode.setVisibility(View.INVISIBLE);
			}
		});
	}
}