package com.example.smartlabiot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCCore;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCDoorBellManager;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCDoorbell;
import com.thingclips.smart.android.camera.sdk.bean.ThingDoorBellCallModel;
import com.thingclips.smart.android.camera.sdk.callback.ThingSmartDoorBellObserver;
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.AbsP2pCameraListener;
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OperationDelegateCallBack;
import com.thingclips.smart.camera.middleware.p2p.IThingSmartCameraP2P;
import com.thingclips.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.thingclips.smart.camera.middleware.widget.ThingCameraView;
import com.thingclips.smart.sdk.bean.DeviceBean;

public class DoorBellView extends AppCompatActivity {
	private static final String TAG = "DOORBELL_CAMERA";
	private String deviceId;
	IThingSmartCameraP2P mCameraP2P = null;
	IThingIPCCore cameraInstance;
	ThingCameraView mVideoView;
	SimpleDraweeView simpleDraweeView;
	AbsP2pCameraListener absP2pCameraListener;
	
	IThingIPCDoorBellManager doorBellInstance = ThingIPCSdk.getDoorbell().getIPCDoorBellManagerInstance();
	
	IThingIPCDoorbell doorbell = ThingIPCSdk.getDoorbell();
	
	ThingDoorBellCallModel mCallModel;
	
	private final ThingSmartDoorBellObserver observer = new ThingSmartDoorBellObserver() {
		@Override
		public void doorBellCallDidReceivedFromDevice(ThingDoorBellCallModel callModel, DeviceBean deviceModel) {
			mCallModel = callModel;
			Log.d(TAG, "doorBellCallDidReceivedFromDevice: DoorBell Pressed! from in app observer!");
		}
		
		@Override
		public void doorBellCallDidAnsweredByOther(ThingDoorBellCallModel callModel) {
		}
		
		@Override
		public void doorBellCallDidCanceled(ThingDoorBellCallModel callModel, boolean isTimeOut) {
		}
		
		@Override
		public void doorBellCallDidHangUp(ThingDoorBellCallModel callModel) {
		}
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_door_bell_view);
		doorBellInstance.addObserver(observer);
		deviceId = getIntent().getStringExtra("deviceId");
		if (!deviceId.isEmpty()){
			Log.d(TAG, "onCreate: Device Id is "+deviceId);
			if (doorbell !=null){
				doorbell.wirelessWake(deviceId);
			}
			initViews();
			mainWorkFlows();
		}
		else {
			Log.d(TAG, "onCreate: Device ID can't able to get!");
			finish();
		}
	}
	public void initViews(){
//		simpleDraweeView = findViewById(R.id.simpleDraweeVieww);
//		if(simpleDraweeView == null){
//			Log.d(TAG, "initViews: SimpleDraweeView is null");
//		}
	}
	public void mainWorkFlows(){
		 cameraInstance = ThingIPCSdk.getCameraInstance();
		if (cameraInstance != null) {
			mCameraP2P = cameraInstance.createCameraP2P(deviceId);
			simpleDraweeView = findViewById(R.id.simpleDraweeVieww);
			mVideoView = findViewById(R.id.camera_video_view);
			mVideoView.setViewCallback(new AbsVideoViewCallback() {
				@Override
				public void onCreated(Object view) {
					super.onCreated(view);
					// The callback to invoke when view rendering is finished.
					if (null != mCameraP2P){
						mCameraP2P.generateCameraView(view);
					}
				}
			});
			mVideoView.createVideoView(deviceId);
			absP2pCameraListener = new AbsP2pCameraListener() {
				@Override
				public void onSessionStatusChanged(Object camera, int sessionId, int sessionStatus) {
					super.onSessionStatusChanged(camera, sessionId, sessionStatus);
					// If sessionStatus = -3 (timeout) or  -105 (failed authentication), we recommend that you initiate a reconnection. Make sure to avoid an infinite loop.
					Log.d(TAG, "onSessionStatusChanged: // If sessionStatus = -3 (timeout) or  -105 (failed authentication), we recommend that you initiate a reconnection. Make sure to avoid an infinite loop.");
					Log.d(TAG, "onSessionStatusChanged: sessionStatus = "+sessionStatus+" " +
							"SessionId = "+sessionId+" Camera Object = "+camera);
				}
			};
			if (null != mCameraP2P){
				mCameraP2P.registerP2PCameraListener(absP2pCameraListener);
				mCameraP2P.connect(deviceId,1, new OperationDelegateCallBack() {
					@Override
					public void onSuccess(int sessionId, int requestId, String data) {
						// A P2P connection is created.
						Log.d(TAG, "onSuccess: A P2P connection is created.");
						mCameraP2P.startPreview(4,new OperationDelegateCallBack() {
							@Override
							public void onSuccess(int sessionId, int requestId, String data) {
								// Live video streaming is started.
								Log.d(TAG, "onSuccess: Live video streaming is started.");
							}
							
							@Override
							public void onFailure(int sessionId, int requestId, int errCode) {
								// Failed to start live video streaming.
								Log.d(TAG, "onFailure: Failed to start live video streaming.");
							}
						});
						
					}
					
					@Override
					public void onFailure(int sessionId, int requestId, int errCode) {
						// Failed to create a P2P connection.
						Log.d(TAG, "onFailure: Failed to create a P2P connection.");
					}
				});
				
			}
		}
		
	}
}