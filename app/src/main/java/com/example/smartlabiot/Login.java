package com.example.smartlabiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity {
    private static final String TAG = "LOGIN";
    private String email,pass;
    private TextView txtRegisterBtn,etLoginEmail,etLoginPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etLoginEmail.getText().toString();
                pass = etLoginPassword.getText().toString();
                if (email.isEmpty() && pass.isEmpty()){
                    Toast.makeText(Login.this, "Fill up the login form!", Toast.LENGTH_SHORT).show();
                }
                else {
                    login();
                }
            }
        });
        txtRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });
    }
    public void initView(){
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegisterBtn = findViewById(R.id.txtRegisterBtn);
    }
    public void login(){
        // Enables login to the app with the email address and password.
        ThingHomeSdk.getUserInstance().loginWithEmail("880", email, pass,
                new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(Login.this, "Logged in with Username: ",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onSuccess: Login is Success! Here is the user info! "+user);
                Intent intent = new Intent(Login.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
            
            @Override
            public void onError(String code, String error) {
                Toast.makeText(Login.this, "code: " + code + "error:" + error, Toast.LENGTH_SHORT).show();
            }
        });
        
    }
}