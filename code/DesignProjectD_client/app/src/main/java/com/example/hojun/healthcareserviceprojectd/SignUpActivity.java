package com.example.hojun.healthcareserviceprojectd;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HoJun on 2017-10-16.
 */

public class SignUpActivity extends Activity {
    private SocketManager socketManager;

    private Button btnSubmit;
    private Button btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        socketManager = SocketManager.getSocketManagerInstance();

        btnSubmit = (Button) findViewById(R.id.btnSignUpInfoSubmit);
        btnCancel = (Button) findViewById(R.id.btnSignUpCancel);

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiver, makeUpdateIntentFilter());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();

                EditText userId = (EditText) findViewById(R.id.clientID);
                EditText userPassword = (EditText) findViewById(R.id.clientPassword);
                EditText userName = (EditText) findViewById(R.id.clientName);
                EditText userHeight = (EditText) findViewById(R.id.clientHeight);
                EditText userWeight = (EditText) findViewById(R.id.clientWeight);
                int typeID = ((RadioGroup) findViewById(R.id.clientGender)).getCheckedRadioButtonId();
                String userGender = ((RadioButton) findViewById(typeID)).getText().toString();


                try {
                    if (getIntent().getExtras().getString("tag").equals("FromLoginActivity"))
                        json.put("request", "SignUp");
                    else
                        json.put("request", "EditInfo");

                    json.put("id", userId.getText().toString());
                    json.put("pwd", userPassword.getText().toString());
                    json.put("name", userName.getText().toString());
                    json.put("height", userHeight.getText().toString());
                    json.put("weight", userWeight.getText().toString());
                    json.put("gender", userGender);
                    SocketManager.JsonSubmitThread jsonSubmitThread = new SocketManager.JsonSubmitThread(json.toString());
                    jsonSubmitThread.start();
                    Log.d("XXX", "?");
                    Log.d("XXX", json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private final BroadcastReceiver StatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            //*********************//
            if (action.equals(SocketManager.SIGNUPCONFIRM)) {

                new android.app.AlertDialog.Builder(SignUpActivity.this)
                        .setTitle("Sign Up Complete")
                        .setMessage("Please Sign In!")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();

            } else if(action.equals(SocketManager.EditInfoConfirm)){
                Toast.makeText(getApplicationContext(),"Successful", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketManager.SIGNUPCONFIRM);
        intentFilter.addAction(SocketManager.EditInfoConfirm);

        return intentFilter;
    }

}
