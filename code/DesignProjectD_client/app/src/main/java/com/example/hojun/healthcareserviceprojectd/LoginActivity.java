package com.example.hojun.healthcareserviceprojectd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_SIGN_UP = 1;
    private SocketManager socketManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        socketManager = SocketManager.getSocketManagerInstance();
        if (socketManager.isRunning == false) {
            Thread t = new Thread(socketManager);
            t.start();
        }


        Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            JSONObject json = new JSONObject();
                            json.put("request", "Login");
                            json.put("id", ((EditText) findViewById(R.id.IDText)).getText());
                            json.put("pwd", ((EditText) findViewById(R.id.PWDText)).getText());
                            socketManager.sendMsg(json.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }

        });

        Button btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                intent.putExtra("tag","FromLoginActivity");
                startActivityForResult(intent, LoginActivity.REQUEST_SIGN_UP);
            }
        });


        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiver, makeUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiver, makeUpdateIntentFilter());

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiver, makeUpdateIntentFilter());
    }

    private final BroadcastReceiver StatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(SocketManager.CLIENTLOGINCOMPLETE)) {

                Intent activityIntent = new Intent(LoginActivity.this, UserHomeActivity.class);
                activityIntent.putExtra("jsonData", mIntent.getStringExtra("jsonData"));
                startActivity(activityIntent);
                finish();
            }

            if (action.equals(SocketManager.DOCTORLOGINCOMPLETE)) {
                Intent activityIntent = new Intent(LoginActivity.this, DoctorHomeActivity.class);
                startActivity(activityIntent);
                finish();
            }

            if (action.equals(SocketManager.TRAINERLOGINCOMPLETE)) {
                Intent activityIntent = new Intent(LoginActivity.this, TrainerHomeActivity.class);
                startActivity(activityIntent);
                finish();
            }

            if(action.equals(SocketManager.CLIENTLOGINDENY)){
                new android.app.AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Wrong Account!")
                        .setMessage("Please Check!")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }


        }
    };

    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketManager.CLIENTLOGINCOMPLETE);
        intentFilter.addAction(SocketManager.DOCTORLOGINCOMPLETE);
        intentFilter.addAction(SocketManager.TRAINERLOGINCOMPLETE);
        intentFilter.addAction(SocketManager.CLIENTLOGINDENY);

        return intentFilter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SIGN_UP:

                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e("XXX", ignore.toString());
        }
    }


}
