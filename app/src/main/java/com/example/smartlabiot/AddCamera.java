package com.example.smartlabiot;

import static android.graphics.Color.BLACK;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
	String wifiName, wifiPass;
	private Button btnGenQrImg;
	private long homeId;
	private ThingCameraActivatorBuilder builder;
	private IThingCameraDevActivator mThingActivator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_camera);
		homeId = getIntent().getLongExtra("homeId",-1);
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
				if (!wifiName.isEmpty() && !wifiPass.isEmpty()){
					if (mThingActivator != null){
						mThingActivator.createQRCode(); // The result is returned by the callback of `onQRCodeSuccess`.
						mThingActivator.start();
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
						builderActivate(token);
					}
					
					@Override
					public void onFailure(String s, String s1) {
						Log.d(TAG, "onFailure: Token is not able to generate! "+s1);
					}
				});
		
	}
	public void builderActivate(String token){
		builder = new ThingCameraActivatorBuilder()
				.setContext(this)
				.setSsid(wifiName)
				.setPassword(wifiPass)
				.setToken(token)
				.setTimeOut(1000)
				.setListener(new IThingSmartCameraActivatorListener() {
					@Override
					public void onQRCodeSuccess(String qrcodeUrl) {
						// The URL used to generate a QR code.
						Log.d(TAG, "onQRCodeSuccess: // The URL used to generate a QR code.");
						try {
							Bitmap qrCodeBitmap = createQRCode(qrcodeUrl, 512); // Assuming createQRCode method is defined in your class
							// Create an ImageView to display the QR code
							ImageView imageView = new ImageView(AddCamera.this);
							imageView.setImageBitmap(qrCodeBitmap);
							
							// Create an AlertDialog.Builder
							AlertDialog.Builder builder = new AlertDialog.Builder(AddCamera.this);
							builder.setTitle("QR Code"); // Set dialog title
							builder.setView(imageView); // Set the ImageView as the view of the dialog
							
							// Create and show the AlertDialog
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
					}
					
					@Override
					public void onActiveSuccess(DeviceBean devResp) {
						// The device is paired.
						Log.d(TAG, "onActiveSuccess: // The device is paired. "+devResp.getName());
					}
				});
		
		mThingActivator  = ThingHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
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