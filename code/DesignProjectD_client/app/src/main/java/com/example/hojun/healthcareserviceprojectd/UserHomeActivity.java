package com.example.hojun.healthcareserviceprojectd;

import android.app.Activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by HoJun on 2017-10-15.
 */

public class UserHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int EDIT_USER_INFO = 2;
    public static final String TAG = "HealthCare";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 107;

    private String userID;
    private JSONObject jsonUserData = null;
    private HomeFragment myHomeFragment = null;
    private BoardFragment myBoardFragment = null;

    /**
     * Singleton Socket Variable
     */
    private SocketManager socketManager;

    /**
     * SwipeView Variables
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private int[] tabIcons = {
            R.drawable.health, R.drawable.board, R.drawable.talk
    };
    private String[] tabValues = {
            "Health Info", "Board", "Counsel"
    };


    /**
     * Bluethooth Variables
     */
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private boolean rtThreadFlag; //rt를 실행시키는 조건을 확인하는 변수
    private reConnectionThread rt; //워터클과의 연결이 끊겼을때 재연결을 위한 변수

    /**
     * SmartCup Variables
     */
    private SmartCupBLE smartCupBLE = null;
    private static String hexRep = "0123456789ABCDEF"; //16진수를 10진수로 변환하기위한 변수
    private byte[] value;
    private String deviceAddress;
    private int totalMassOfDrink = 0; //현재 섭취한 총 물의 양을 저장하는 변수

    /**
     * Smart Scale Variables
     */
    public ArrayList<ScaleRecord> scaleRecords = new ArrayList<ScaleRecord>();
    private GlucoseBLE glucoseBLE;

    /**
     * Smart Gluecose Variables
     */
    public ArrayList<GlucoseRecord> glucoseRecords = new ArrayList<GlucoseRecord>();
    private ScaleBLE scaleBLE;


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Signout) {
            // Handle the camera action
            this.userID = null;
            this.jsonUserData = null;
            Intent intent = new Intent(UserHomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_Modify_info) {
            Intent intent = new Intent(UserHomeActivity.this, SignUpActivity.class);
            intent.putExtra("tag", "FromUserHomeActivity");
            startActivityForResult(intent, UserHomeActivity.EDIT_USER_INFO);
        } else if (id == R.id.nav_BLEStart) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    super.run();

                    //2glucoseBLE.Suspend();
                    scaleBLE.Suspend();

                    //2glucoseBLE.Resume();
                    scaleBLE.Resume();


                    try {
                        sleep(30000);
                        //2glucoseBLE.Suspend();
                        scaleBLE.Suspend();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            t.start();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //2glucoseBLE.Suspend();
        scaleBLE.Suspend();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.user_home);
        socketManager = SocketManager.getSocketManagerInstance();


        // Get Data From Intent which is sended by LoginActivity
        Intent fromLogin = getIntent();
        try {
            jsonUserData = new JSONObject((fromLogin.getExtras().getString("jsonData")));
            Log.d("XXX", jsonUserData.toString());
            this.userID = jsonUserData.getString("id");
            //TODO set health variables using variable jsonUserData


        } catch (JSONException e) {
            Toast.makeText(UserHomeActivity.this, "서버와의 통신에서 에러 발생하여 종료합니다.", Toast.LENGTH_LONG);
            finish();
            e.printStackTrace();
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        // Set FloatingActionButton for Writing Post
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater factory = LayoutInflater.from(UserHomeActivity.this);

                final View textEntryView = factory.inflate(R.layout.post_item, null);
                //text_entry is an Layout XML file containing two text field to display in alert dialog

                final EditText postTitle = (EditText) textEntryView.findViewById(R.id.post_title);
                final EditText postContent = (EditText) textEntryView.findViewById(R.id.post_content);

                postTitle.setText("", TextView.BufferType.EDITABLE);
                postContent.setText("", TextView.BufferType.EDITABLE);
                postTitle.setHint("Title");
                postContent.setHint("Content");


                final AlertDialog.Builder alert = new AlertDialog.Builder(UserHomeActivity.this);
                alert.setIcon(R.drawable.board).setTitle(
                        "Writing Posts:").setView(
                        textEntryView).setPositiveButton("Enter",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                JSONObject json = new JSONObject();
                                try {
                                    json.put("request", "UpdatePost");
                                    json.put("id", UserHomeActivity.this.userID);
                                    json.put("title", postTitle.getText());
                                    json.put("content", postContent.getText());

                                    SocketManager.JsonSubmitThread jsonSubmitThread = new SocketManager.JsonSubmitThread(json.toString());
                                    jsonSubmitThread.start();

                                    myBoardFragment.getAdapter().getList().add(0, new BoardFragment.BoardItem(postTitle.getText().toString(), postContent.getText().toString()));
                                    myBoardFragment.getAdapter().notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // User clicked OK so do some stuff
                            }
                        }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                // User clicked cancel so do some stuff

                            }
                        });
                alert.show();

            }
        });

        setToolbarANDLayout();


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        // Set Navigation View Variables
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //TODO ReconnectThread 사용할지 말지 다시 확인해볼것!
        //rtThreadFlag = true;
        //rt = new reConnectionThread();

        //start BLE Setting
        service_init();

        //2glucoseBLE = new GlucoseBLE(this);
        //2glucoseBLE.start();

        scaleBLE = new ScaleBLE(this);
        scaleBLE.start();

        //Register BroadCaster
        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiver, makeUpdateIntentFilter());


    }


    public void setToolbarANDLayout() {

        // Set Tab and Slide Event Listener
        this.tabLayout = (TabLayout) findViewById(R.id.tabs);
        this.tabLayout.addTab(this.tabLayout.newTab().setText(tabValues[0]));
        this.tabLayout.addTab(this.tabLayout.newTab().setText(tabValues[1]));
        this.tabLayout.addTab(this.tabLayout.newTab().setText(tabValues[2]));

        this.tabLayout.getTabAt(0).setIcon(this.tabIcons[0]);
        this.tabLayout.getTabAt(1).setIcon(this.tabIcons[1]);
        this.tabLayout.getTabAt(2).setIcon(this.tabIcons[2]);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.userhome_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ble:

                Intent intent = new Intent(UserHomeActivity.this, BLEDeviceListActivity.class);
                startActivityForResult(intent, REQUEST_SELECT_DEVICE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + smartCupBLE);
                    //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    smartCupBLE.connect(deviceAddress);

                }
                break;
            case EDIT_USER_INFO:

                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(smartCupServiceConnection);
    }


    private final BroadcastReceiver StatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(SocketManager.RECIEVECOMPLETE)) {
                Log.d("XXX", "rece");
                return;
            }

            if (action.equals(SmartCupBLE.ACTION_GATT_CONNECTED)) {
                //rtThreadFlag = true;
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        setTitle("HealthCare-BLE Connecting");

//                        glucoseBLE.Resume();
//                        scaleBLE.Resume();
                    }
                });


                return;
            }

            if (action.equals(SmartCupBLE.ACTION_GATT_DISCONNECTED)) {
                /*
                Log.d("XXX", "연결끊김");
                if (deviceAddress != null) {
                    if (rtThreadFlag) {
                        Log.d("XXX", "재연결쓰레드가 쉬는중이라 재실행");
                        rt = new reConnectionThread();
                        rt.start();
                        rtThreadFlag = false;
                    }
                }*/
                setTitle("HealthCare");

                return;
            }

            if (action.equals(SmartCupBLE.ACTION_GATT_SERVICES_DISCOVERED)) {
                smartCupBLE.enableTXNotification();
            }

            /**/
            if (action.equals(SmartCupBLE.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(SmartCupBLE.EXTRA_DATA);

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = bytesToHex(txValue);


                            if (text.substring(0, 2).equals("D2")) {//watercle이 물을 섭취했다고 event를 보내는 경우
                                smartCupBLE.writeRXCharacteristic(value); // 물의 음용량을 요청하는 명령어 전달
                            }
                            if (text.substring(0, 2).equals("D1")) { //물의 음용량을 watercle이 전송해주는 event가 발생한 경우 mobile에서도 누적 음용량을 갱신해서 ui에 update한다
                                int Mass_of_Water = hexToDecimal(text.substring(4, 8));
                                Log.d("TAG", String.valueOf(Mass_of_Water));
                                //((TextView) findViewById(R.id.Mass_of_Water)).setText(String.valueOf(Mass_of_Water) + "ml");

                                Log.d("XXX", "??");

                                /*
                                if(!glucoseRecords.isEmpty()){
                                    GlucoseRecord glucoseRecord = glucoseRecords.get(glucoseRecords.size()-1);
                                    Log.d("XXX","?"+String.valueOf(glucoseRecord.glucoseData) );
                                    ((TextView) findViewById(R.id.Blood_Glucose)).setText(String.valueOf(glucoseRecord.glucoseData));
                                    glucoseRecords.clear();
                                }

                                 if(!scaleRecords.isEmpty()){
                                 ScaleRecord scaleRecord = scaleRecords.get(scaleRecords.size()-1);
                        json.put("scale_weight",scaleRecord.weight);
                        json.put("scale_BMR",scaleRecord.BMR);
                        json.put("scale_bone",scaleRecord.bone);
                        json.put("scale_fat",scaleRecord.fat);
                        json.put("scale_muscle",scaleRecord.muscle);
                        json.put("scale_visfat",scaleRecord.visfat);
                        json.put("scale_water",scaleRecord.water);
                        scaleRecords.clear();
                    }

                                */

                                //TODO 계산된 물 음용량을 서버로 전송
                                /*
                                if (serverConnected)
                                    netService.sendMassOfWater(Mass_of_Drink - totalMassOfDrink);//사용자가 한번 마신 음용량을 서버로 전송
                                */
                                totalMassOfDrink = Mass_of_Water; //누적 음용량을 갱신
                                updateWaterInfo(totalMassOfDrink);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            if (action.equals(SocketManager.DIAGNOSISRESULT)) {
                try {
                    JSONObject json = new JSONObject(intent.getExtras().getString("jsonData"));
                    showBasicNotification(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }
    };


    /**
     * Function About Create Notification
     */
    public void showBasicNotification(JSONObject json) throws JSONException {
        NotificationCompat.Builder mBuilder = createNotification(json);
        mBuilder.setContentIntent(createPendingIntent());

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    /**
     * Function About Create Notification
     */
    private PendingIntent createPendingIntent() {
        Intent resultIntent = new Intent(this, UserHomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(UserHomeActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    /**
     * Function About Create Notification
     */
    private NotificationCompat.Builder createNotification(JSONObject json) throws JSONException {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.diagnosis);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("Diabetes diagnosis arrived")
                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (json.get("result").equals("positive"))
            builder.setContentText("Diagnosis: Positive");
        else
            builder.setContentText("Diagnosis: Negative");

        return builder;
    }


    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketManager.RECIEVECOMPLETE);
        intentFilter.addAction(SmartCupBLE.ACTION_GATT_CONNECTED);
        intentFilter.addAction(SmartCupBLE.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(SmartCupBLE.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(SmartCupBLE.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(SmartCupBLE.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(SocketManager.DIAGNOSISRESULT);
        intentFilter.addAction(SocketManager.CONSULTLIST);
        intentFilter.addAction(SocketManager.NONCONSULTLIST);
        intentFilter.addAction(SocketManager.ASKRESULT);
        intentFilter.addAction(SocketManager.SENDMESSAGE);
        return intentFilter;
    }

    void checkPermission() {
        if (ActivityCompat.checkSelfPermission(UserHomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserHomeActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            Log.d("XXX", "권한요청");
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(UserHomeActivity.this, "권한 사용을 동의해주셔야 합니다.", Toast.LENGTH_LONG).show();
                    this.finish();
                }
            }
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    myHomeFragment = HomeFragment.newInstance(0, (UserHomeActivity.this.jsonUserData));
                    return myHomeFragment;
                case 1:
                    try {
                        myBoardFragment = BoardFragment.newInstance(1, jsonUserData.getJSONArray("board").toString());
                        return myBoardFragment;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                case 2:
                    try {
                        return CounsulFragment.newInstance(2, jsonUserData.getJSONArray("ConsultList").toString(), jsonUserData.getJSONArray("NonConsultList").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ;
                    }
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    //SmartCup service connected/disconnected
    private ServiceConnection smartCupServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            smartCupBLE = ((SmartCupBLE.LocalBinder) rawBinder).getService();
            UserHomeActivity.this.value = smartCupBLE.get_WaterRequestCommand();
            Log.d(TAG, "onServiceConnected mService= " + smartCupBLE);
            if (!smartCupBLE.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            smartCupBLE = null;
        }
    };

    private void service_init() {

        Intent bindIntent = new Intent(this, SmartCupBLE.class);
        bindService(bindIntent, smartCupServiceConnection, Context.BIND_AUTO_CREATE);

    }


    private class reConnectionThread extends Thread {
        private boolean flag = true;

        public void Stop_flag() {
            flag = false;
        }

        public void run() {
            Log.d(TAG, "Thread start ");

            while (!(smartCupBLE.isConnected) && flag) {
                if (deviceAddress != null) {
                    smartCupBLE.connect(deviceAddress);
                    Log.d(TAG, "Thread working ");
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
            Log.d(TAG, "Thread end ");
        }
    }

    //hex array method for converting byte array
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static int hexToDecimal(String hex) {

        int counter = hex.length() - 1;
        int sum = 0;

        for (char c : hex.toCharArray()) {
            int i = hexRep.indexOf(c);
            sum = (int) (sum + (Math.pow(16, counter)) * i);
            counter--;
        }

        return sum;
    }

    public void updateWaterInfo(int totalMassOfDrink) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("request", "UpdateWater");
        json.put("id", jsonUserData.get("id"));
        json.put("totalMassOfDrink", totalMassOfDrink);
        ((TextView) findViewById(R.id.Mass_of_Water)).setText(String.valueOf(totalMassOfDrink) + "ML");

        String notification = createNotification("water", totalMassOfDrink);
        ((TextView) findViewById(R.id.Health_Notification)).setText(notification);

        SocketManager.JsonSubmitThread jsonSubmitThread = new SocketManager.JsonSubmitThread(json.toString());
        jsonSubmitThread.start();

    }

    public void updateGlucoseInfo() throws JSONException {
        GlucoseRecord glucoseRecord = glucoseRecords.get(glucoseRecords.size() - 1);
        ((TextView) findViewById(R.id.Blood_Glucose)).setText(String.valueOf(glucoseRecord.glucoseData));
        JSONObject json = new JSONObject();
        json.put("request", "UpdateGlucose");
        json.put("id", jsonUserData.get("id"));
        json.put("bloodGlucose", glucoseRecord.glucoseData);


        String notification = createNotification("glucose", (int)glucoseRecord.glucoseData);
        ((TextView) findViewById(R.id.Health_Notification)).setText(notification);

        SocketManager.JsonSubmitThread jsonSubmitThread = new SocketManager.JsonSubmitThread(json.toString());
        jsonSubmitThread.start();
        glucoseRecords.clear();
    }

    public void updateScaleInfo() {
        JSONObject json = new JSONObject();
        ScaleRecord scaleRecord = scaleRecords.get(scaleRecords.size() - 1);
        try {
            json.put("request", "UpdateScale");
            json.put("id", jsonUserData.get("id"));
            json.put("scale_weight", String.valueOf(scaleRecord.weight));
            //json.put("scale_BMR", scaleRecord.BMR);
            json.put("scale_bone", String.valueOf(scaleRecord.bone));
            json.put("scale_fat", String.valueOf(scaleRecord.fat));
            json.put("scale_muscle", String.valueOf(scaleRecord.muscle));
            //json.put("scale_visfat", scaleRecord.visfat);
            //json.put("scale_water", scaleRecord.water);
            scaleRecords.clear();

            UserHomeActivity.this.myHomeFragment.setDetailScale(json);

            final String notification = createNotification("scale", 73);


            final String weight = String.valueOf(scaleRecord.weight) + "KG";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.Scale_Weight)).setText(weight);
                            ((TextView) findViewById(R.id.Health_Notification)).setText(notification);
                        }
                    });
                }
            }).start();


            SocketManager.JsonSubmitThread jsonSubmitThread = new SocketManager.JsonSubmitThread(json.toString());
            jsonSubmitThread.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String createNotification(String type, int value) {
        if (type.equals("water")) {
            if (value < 2000)
                return "Degydration, please drink more water";
            else if (value >= 2000 && value < 2500)
                return "Normal water Intake!";
            else if (value >= 2500)
                return "OverHydration!, you drink enough water";
        } else if (type.equals("glucose")) {
            if (value < 50)
                return "Dangerously Low Blood Glucose!";
            else if (value < 70 && value >= 50)
                return "Low Blood Glucose!";
            else if (value < 120 && value >= 70)
                return "Low Blood Glucose!";
            else if (value < 180 && value >= 120)
                return "Normal Blood Glucose!";
            else if (value < 280 && value >= 180)
                return "High Blood Glucose!";
            else if (value >= 280)
                return "Dangerously High Blood Glucose!";
        } else if (type.equals("scale")) {
            if (value < 65)
                return "Underweight!";
            else if (value >= 65 && value < 75)
                return "Normal Weight!";
            else if (value >= 75)
                return "Over Weight!";
        }
        return null;
    }

    public JSONObject getJsonUserInfo() {
        return this.jsonUserData;
    }
}
