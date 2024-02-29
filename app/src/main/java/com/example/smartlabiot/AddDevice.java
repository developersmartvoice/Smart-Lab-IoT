package com.example.smartlabiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AddDevice extends AppCompatActivity {
	private Button btnCamera;
	private long homeId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_device);
		homeId = getIntent().getLongExtra("homeId",-1);
		initViews();
		mainWorkFlow();
	}
	public void initViews(){
		btnCamera = findViewById(R.id.btnCamera);
	}
	public void mainWorkFlow(){
		btnCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddDevice.this, AddCamera.class);
				intent.putExtra("homeId",homeId);
				startActivity(intent);
				finish();
			}
		});
	}
}