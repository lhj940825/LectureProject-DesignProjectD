package com.example.hojun.healthcareserviceprojectd;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by HoJun on 2017-10-15.
 */

public class SocketManager extends Application implements Runnable {

    private static SocketManager socketManager = new SocketManager();

    public final static String HOST = "163.180.116.125";
    public final static int PORT = 3000;
    public static boolean isRunning = false;
    private static Socket socket;

    private InputStream objectInBytes;
    private ObjectInputStream ois;
    private OutputStream sendingBytes;
    private ObjectOutputStream oos;


    public final static String CLIENTLOGINCOMPLETE = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.ClientLoginComplete";
    public final static String CLIENTLOGINDENY = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.ClientLoginDENY ";
    public final static String DOCTORLOGINCOMPLETE = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.DoctorLoginComplete";
    public final static String TRAINERLOGINCOMPLETE = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.TrainerLoginComplete";
    public final static String RECIEVECOMPLETE = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.LoginComplete";
    public final static String SIGNUPCONFIRM =  "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.SIGNUPCONFIRM";
    public final static String EditInfoConfirm = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.EditInfoConfirm";
    public final static String DIAGNOSISRESULT = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.DiagnosisResult";
    public final static String CONSULTLIST = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.CONSULTLIST";
    public final static String NONCONSULTLIST = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.NONCONSULTLIST";
    public final static String ASKRESULT = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.ASKRESULT";
    public final static String SENDMESSAGE = "com.nordicsemi.com.example.hojun.healthcareserviceprojectd.SENDMESSAGE";

    public static class JsonSubmitThread extends Thread{
        private String JsonDataTOSubmit;
        public JsonSubmitThread(){

        }
        public JsonSubmitThread(String JsonDataTOSubmit){
            this.JsonDataTOSubmit = JsonDataTOSubmit;
        }
        @Override
        public void run() {
            super.run();
            try {
                socketManager.sendMsg(this.JsonDataTOSubmit);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static SocketManager getSocketManagerInstance() {
        return socketManager;
    }



    public static Socket getSocket() throws IOException {
        if (socket == null)
            socket = new Socket();

        if (!socket.isConnected())
            socket.connect(new InetSocketAddress(HOST, PORT));

        return socket;
    }

    @Override
    public void run() {

        try {
            this.isRunning = true;
            sendingBytes = SocketManager.getSocket().getOutputStream();
            oos = new ObjectOutputStream(sendingBytes);

            objectInBytes = SocketManager.getSocket().getInputStream();
            ois = new ObjectInputStream(objectInBytes);
        } catch (IOException e) {

        }

        while (true) {
            try {
                Object obj = ois.readObject();

                if (obj instanceof String) {
                    JSONObject inputJson = new JSONObject((String) obj);
                    processJSONData(inputJson);


                }
                //Message msg = Message.obtain(mHandler, 0, 0, 0, receivedmsg);
                //mHandler.sendMessage(msg);

            } catch (IOException e) {

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void processJSONData(JSONObject inputJson) throws JSONException {

        if (inputJson.get("response").equals("LoginConfirm")) {
            broadcastUpdate(SocketManager.CLIENTLOGINCOMPLETE, inputJson.toString());
        }else if(inputJson.get("response").equals("LoginDeny")){
            broadcastUpdate(SocketManager.CLIENTLOGINDENY , inputJson.toString());
        } else if(inputJson.get("response").equals("SignUpConfirm")){
            broadcastUpdate(SocketManager.SIGNUPCONFIRM, inputJson.toString());
        } else if(inputJson.get("response").equals("EditInfoConfirm")){
            broadcastUpdate(SocketManager.EditInfoConfirm, inputJson.toString());
        } else if(inputJson.get("response").equals("DiagnosisDiabetes")){
            broadcastUpdate(SocketManager.DIAGNOSISRESULT, inputJson.toString());
        } else if(inputJson.get("response").equals("ConsultList")) {
            broadcastUpdate(SocketManager.CONSULTLIST, inputJson.toString());
        } else if(inputJson.get("response").equals("NonConsultList")) {
            broadcastUpdate(SocketManager.NONCONSULTLIST, inputJson.toString());
        } else if(inputJson.get("response").equals("AskResult")) {
            broadcastUpdate(SocketManager.ASKRESULT, inputJson.toString());
        } else if(inputJson.get("response").equals("SendMessage")) {
            broadcastUpdate(SocketManager.SENDMESSAGE, inputJson.toString());
        }


    }

    public void sendMsg(String msg) throws IOException {
        oos.writeObject(msg);
        oos.reset();
    }

    public static void closeSocket() throws IOException {
        if (socket != null)
            socket.close();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, String notificationInfo) {
        final Intent intent = new Intent(action);
        intent.putExtra("jsonData", notificationInfo);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
