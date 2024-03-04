package com.example.smartlabiot;

import static android.graphics.Color.BLACK;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCCore;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.builder.ThingCameraActivatorBuilder;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingCameraDevActivator;
import com.thingclips.smart.sdk.api.IThingSmartCameraActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;

import java.util.Hashtable;

public class AddCamera extends AppCompatActivity {
	private static final String TAG = "ADD_CAMERA";
	private EditText etWifiName,etWifiPass;
	String wifiName, wifiPass,tokenGlobal;
	private Button btnGenQrImg;
	private long homeId;
	private ThingCameraActivatorBuilder builder;
	private IThingCameraDevActivator mThingActivator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_camera);
		homeId = getIntent().getLongExtra("homeId",-1);
		Log.d(TAG, "onCreate: Home Id "+homeId);
		initViews();
		if (homeId != -1){
			generateToken();
			mainWorkFlow();
		}
		else {
			Log.d(TAG, "HomeId is -1!");
			finish();
		}
	}
	public void initViews(){
		etWifiName = findViewById(R.id.etWifiName);
		etWifiPass = findViewById(R.id.etWifiPass);
		btnGenQrImg = findViewById(R.id.btnGenQrImg);
	}
	
	public void mainWorkFlow(){
		btnGenQrImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wifiName = etWifiName.getText().toString();
				wifiPass = etWifiPass.getText().toString();
				Log.d(TAG, "onClick: Wifi "+wifiName+" Wifi Pass "+wifiPass);
				if (!wifiName.isEmpty() && !wifiPass.isEmpty()){
					Log.d(TAG, "onClick: Dhukse");
					builderActivate();
					if (mThingActivator != null){
						Log.d(TAG, "onClick: Ekhane o dhukse");
						mThingActivator.createQRCode(); // The result is returned by the callback of `onQRCodeSuccess`.
						if(mThingActivator == null){
							Toast.makeText(AddCamera.this, "Wifi Config In Progress.",
									Toast.LENGTH_SHORT).show();
						}
						else{
								// The builder will be activate for searching devices
								mThingActivator.start();
								Log.d("QR CODE","activator Started!");
						}
					}
					else {
						Log.d(TAG, "onClick: Wifi Configuration Progressing");
						Toast.makeText(AddCamera.this,"Wifi Configuration Progressing",
								Toast.LENGTH_SHORT).show();
					}
				}
				else {
					Log.d(TAG, "onClick: Enter Wifi name and Password!");
					Toast.makeText(AddCamera.this, "Enter Wifi name and Password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
	}
	public void generateToken(){
		ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId,
				new IThingActivatorGetToken() {
					
					@Override
					public void onSuccess(String token) {
						Log.d(TAG, "onSuccess: Token is generated! "+token);
//						builderActivate(token);
						tokenGlobal = token;
					}
					
					@Override
					public void onFailure(String s, String s1) {
						Log.d(TAG, "onFailure: Token is not able to generate! "+s1);
					}
				});
		
	}
	public void builderActivate(){
		Log.d(TAG, "builderActivate: ");
		builder = new ThingCameraActivatorBuilder()
				.setContext(this)
				.setSsid(wifiName)
				.setPassword(wifiPass)
				.setToken(tokenGlobal)
				.setTimeOut(1000)
				.setListener(new IThingSmartCameraActivatorListener() {
					@Override
					public void onQRCodeSuccess(String qrcodeUrl) {
						// The URL used to generate a QR code.
						Log.d(TAG, "onQRCodeSuccess: // The URL used to generate a QR code.");
						try {
							// Inside onQRCodeSuccess method
							AlertDialog.Builder builder = new AlertDialog.Builder(AddCamera.this);
							View dialogView = getLayoutInflater().inflate(R.layout.custom_qr_dialog, null);
							builder.setView(dialogView);
							
							TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
							ImageView imageView = dialogView.findViewById(R.id.qr_image_view);
							
							titleTextView.setText("QR Code");
							
							Bitmap qrCodeBitmap = createQRCode(qrcodeUrl, 512); // Assuming createQRCode method is defined in your class
							imageView.setImageBitmap(qrCodeBitmap);
							
							AlertDialog alertDialog = builder.create();
							alertDialog.show();
							
						} catch (WriterException e) {
							e.printStackTrace();
							// Handle QR code generation error
							Log.d(TAG, "onQRCodeSuccess: // Handle QR code generation error");
						}
					}
					
					@Override
					public void onError(String errorCode, String errorMsg) {
						// Failed to pair the device.
						Log.d(TAG, "onError: // Failed to pair the device. "+errorMsg);
						mThingActivator.stop();
					}
					
					@Override
					public void onActiveSuccess(DeviceBean devResp) {
						// The device is paired.
						mThingActivator.stop();
						Log.d(TAG, "onActiveSuccess: // The device is paired. "+devResp.getName());
						Log.d(TAG, "onActiveSuccess: The Device info is: "+devResp);
						IThingIPCCore cameraInstance = ThingIPCSdk.getCameraInstance();
						if (cameraInstance != null) {
							boolean isIt = cameraInstance.isIPCDevice(devResp.devId);
							if(isIt){
								Log.d(TAG, "True");
								int type = cameraInstance.getP2PType(devResp.devId);
								Log.d(TAG, "onActiveSuccess: Type is: "+type);
								Intent intent = new Intent(AddCamera.this, DoorBellView.class);
								intent.putExtra("deviceId", devResp.devId); // Pass devResp as an
								// extra
								// with key "deviceBean"
								startActivity(intent);
								finish();
							}else {
								Log.d(TAG, "False");
							}
						}
					}
				});
		
		mThingActivator  = ThingHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
		Log.d(TAG, "builderActivate: mThingActivatorCalled");
	}
	
	public static Bitmap createQRCode(String url, int widthAndHeight)
			throws WriterException {
		Hashtable hints = new Hashtable();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		hints.put(EncodeHintType.MARGIN,0);
		BitMatrix matrix = new MultiFormatWriter().encode(url,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
}