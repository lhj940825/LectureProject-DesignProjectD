package com.example.hojun.healthcareserviceprojectd;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;


/**
 * Created by HoJun on 2017-11-11.
 */


class Const {

    public final static int BLE_SCAN_DURATION = 5000;

    public final static String INTENT_BLE_EXTRA_DATA = "com.isens.standard.ble.BLE_EXTRA_DATA";
    public final static String INTENT_BLE_CONNECT_DEVICE = "com.isens.standard.ble.BLE_CONNECTED_DEVICE";
    public final static String INTENT_BLE_BONDED = "air.SmartLog.android.ble.BLE_BONDED";
    public final static String INTENT_BLE_BOND_NONE = "air.SmartLog.android.ble.BLE_BOND_NONE";
    public final static String INTENT_BLE_DEVICECONNECTED = "air.SmartLog.android.ble.BLE_DEVICECONNECTED";
    public final static String INTENT_BLE_DEVICEDISCONNECTED = "air.SmartLog.android.ble.BLE_DEVICEDISCONNECTED";
    public final static String INTENT_BLE_SERVICEDISCOVERED = "air.SmartLog.android.ble.BLE_SERVICEDISCOVERED";
    public final static String INTENT_BLE_ERROR = "air.SmartLog.android.ble.BLE_ERROR";
    public final static String INTENT_BLE_DEVICENOTSUPPORTED = "air.SmartLog.android.ble.BLE_DEVICENOTSUPPORTED";
    public final static String INTENT_BLE_OPERATESTARTED = "air.SmartLog.android.ble.BLE_OPERATESTARTED";
    public final static String INTENT_BLE_OPERATECOMPLETED = "air.SmartLog.android.ble.BLE_OPERATECOMPLETED";
    public final static String INTENT_BLE_OPERATEFAILED = "air.SmartLog.android.ble.BLE_OPERATEFAILED";
    public final static String INTENT_BLE_OPERATENOTSUPPORTED = "air.SmartLog.android.ble.BLE_OPERATENOTSUPPORTED";
    public final static String INTENT_BLE_DATASETCHANGED = "air.SmartLog.android.ble.BLE_DATASETCHANGED";
    public final static String INTENT_BLE_READ_SERIALNUMBER = "air.SmartLog.android.ble.BLE_READ_SERIALNUMBER";
    public final static String INTENT_BLE_READ_MANUFACTURER = "air.SmartLog.android.ble.BLE_READ_MANUFACTURER";
    public final static String INTENT_BLE_READ_SOFTWARE_REV = "air.SmartLog.android.ble.BLE_READ_SOFTWARE_REVISION";
    public final static String INTENT_BLE_RACPINDICATIONENABLED = "air.SmartLog.android.ble.BLE_RACPINDICATIONENABLED";
    public final static String INTENT_BLE_SEQUENCECOMPLETED = "air.SmartLog.android.ble.BLE_SEQUENCECOMPLETED";
    public final static String INTENT_BLE_REQUEST_COUNT = "air.SmartLog.android.ble.BLE_REQUESTCOUNT";
    public final static String INTENT_BLE_READCOMPLETED = "air.SmartLog.android.ble.BLE_READCOMPLETED";
    public final static String INTENT_BLE_READAFTER = "air.SmartLog.android.ble.BLE_READAFTER";


    //Service
    public final static UUID BLE_SERVICE_GLUCOSE = UUID.fromString("");
    public final static UUID BLE_SERVICE_DEVICE_INFO = UUID.fromString("");
    public final static UUID BLE_SERVICE_CUSTOM = UUID.fromString("");
    //Characteristic
    public final static UUID BLE_CHAR_GLUCOSE_SERIALNUM = UUID.fromString("");
    public final static UUID BLE_CHAR_SOFTWARE_REVISION = UUID.fromString("");
    public final static UUID BLE_CHAR_GLUCOSE_MEASUREMENT = UUID.fromString("");
    public final static UUID BLE_CHAR_GLUCOSE_CONTEXT = UUID.fromString("");
    public final static UUID BLE_CHAR_GLUCOSE_RACP = UUID.fromString("");
    public final static UUID BLE_CHAR_CUSTOM_TIME = UUID.fromString("");
    public final static UUID BLE_CHAR_CUSTOM_TIME_MC = UUID.fromString("");

    //Descriptor
    public final static UUID BLE_DESCRIPTOR_DESCRIPTOR = UUID.fromString("");
}

class GlucoseRecord { //0: default value
    public int sequenceNumber = 0;
    public long time = 0; // glucose time
    public double glucoseData = 0; // glucose value
    public int flag_cs = 0; // 1: control solution
    public int flag_hilow = 0; //-1: low, 1: high
    public int flag_context = 0; //1: ble context data (complete data)

    public int flag_meal = 0; //-1: before meal, 1: after meal
    public int flag_fasting = 0; //1: fasting
    public int flag_ketone = 0; //1: ketone
    public int flag_nomark = 0; //no-mark
    public int timeoffset = 0; //time offset

    @Override
    public boolean equals(Object obj) {
        return time==((GlucoseRecord)obj).time
                &&sequenceNumber==((GlucoseRecord)obj).sequenceNumber
                && glucoseData==((GlucoseRecord)obj).glucoseData;
    }
}

public class GlucoseBLE extends Thread {
    public final static double GLUCOSE_CONV = 18.016;   // conversion factor between mg/dL and mmol/L (mg/dL = mmol/L * 18.016)
    public final static int PERMISSION_REQUEST_CODE = 100;
    private static final boolean DEVICE_IS_BONDED = true;
    private static final boolean DEVICE_NOT_BONDED = false;
    private static final int REQUEST_ENABLE_BT = 2;
    private final static int OP_CODE_REPORT_STORED_RECORDS = 1;
    private final static int OP_CODE_DELETE_STORED_RECORDS = 2;
    private final static int OP_CODE_REPORT_NUMBER_OF_RECORDS = 4;
    private final static int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
    private final static int OP_CODE_RESPONSE_CODE = 6;
    private final static int COMPLETE_RESULT_FROM_METER = 192;
    private final static int OPERATOR_ALL_RECORDS = 1;
    private final static int OPERATOR_GREATER_OR_EQUAL_RECORDS = 3;
    private final static int OPERATOR_LATEST_RECORDS = 6;
    private final static int FILTER_TYPE_SEQUENCE_NUMBER = 1;
    private final static int RESPONSE_SUCCESS = 1;
    private final static int RESPONSE_OP_CODE_NOT_SUPPORTED = 2;
    private final static int RESPONSE_NO_RECORDS_FOUND = 6;
    private final static int RESPONSE_ABORT_UNSUCCESSFUL = 7;
    private final static int RESPONSE_PROCEDURE_NOT_COMPLETED = 8;
    private final static int SOFTWARE_REVISION_BASE = 1, SOFTWARE_REVISION_1 = 1, SOFTWARE_REVISION_2 = 0; //base: custom profile version
    private int _number;


    private final SparseArray<GlucoseRecord> mRecords = new SparseArray<GlucoseRecord>();
    //private final String address = "24:71:89:2A:80:11";
    private final String address = "24:71:89:2A:78:A7";
    private final String TAG = "GlucoseBLE";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic glucoseMeasurementCharacteristic;
    private BluetoothGattCharacteristic glucoseMeasurementContextCharacteristic;
    private BluetoothGattCharacteristic RACPCharacteristic;
    private BluetoothGattCharacteristic deviceSerialCharacteristic;
    private BluetoothGattCharacteristic deviceSoftwareRevisionCharacteristic;
    private BluetoothGattCharacteristic customTimeCharacteristic;
    private Handler mHandler;

    private boolean isRunning=false;
    private boolean isOn = false;
    private boolean isMgdl=true;

    private String _serial_text, _software_ver;
    private int _version_1;

    private Context context;

    public void InitCharacteristic(){
        glucoseMeasurementCharacteristic=null;
        glucoseMeasurementContextCharacteristic=null;
        RACPCharacteristic=null;
        deviceSerialCharacteristic=null;
        deviceSoftwareRevisionCharacteristic=null;
        customTimeCharacteristic=null;
    }

    GlucoseBLE(Context _context){
        context = _context;
    }

    public void Init() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG,"어댑터 NULL");
            return;
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            //블루투스를 지원하지 않거나 켜져있지 않으면 장치를끈다.
            Log.d(TAG,"블투 꺼져있음");
            return;
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void run(){
        _number=0;
        isOn=true;

        while(isOn) {
            try{
                while(!isRunning) {
                    Thread.sleep(3000);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            //Log.d(TAG, "SCANNING..");
            InitCharacteristic();
            Init();

            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.d(TAG,"기기연결 안대쬬");
                //isRunning = false;
                continue;
            }
            if (bluetoothManager != null && bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG,"연결 안대쬬!");
                //isRunning = false;
                continue;
            }

            bluetoothGatt = device.connectGatt(context, false, mGattCallBack);
            Log.d(TAG, "연결요청 완료!");
            //isRunning=true;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public void Stop(){
        isOn=false;
    }
    public void Suspend() {isRunning=false;}
    public void Resume(){isRunning=true;}

    private final BluetoothGattCallback mGattCallBack= new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(isRunning && newState== BluetoothProfile.STATE_CONNECTED){
                Log.d(TAG,"Connected!");
                isRunning=false;
                gatt.discoverServices();
            }
            else if(!isRunning && newState== BluetoothProfile.STATE_DISCONNECTED){
                Log.d(TAG,"Disconnected!");
                gatt.close();
                isRunning=true;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                InitCharacteristic();
                for (BluetoothGattService service : gatt.getServices()) {
                    if (Const.BLE_SERVICE_GLUCOSE.equals(service.getUuid())) { //1808
                        glucoseMeasurementCharacteristic = service.getCharacteristic(Const.BLE_CHAR_GLUCOSE_MEASUREMENT); //2A18
                        glucoseMeasurementContextCharacteristic = service.getCharacteristic(Const.BLE_CHAR_GLUCOSE_CONTEXT); //2A34
                        RACPCharacteristic = service.getCharacteristic(Const.BLE_CHAR_GLUCOSE_RACP);//2A52
                    } else if (Const.BLE_SERVICE_DEVICE_INFO.equals(service.getUuid())) { //180A
                        deviceSerialCharacteristic = service.getCharacteristic(Const.BLE_CHAR_GLUCOSE_SERIALNUM);//2A25
                        deviceSoftwareRevisionCharacteristic = service.getCharacteristic(Const.BLE_CHAR_SOFTWARE_REVISION); //2A28
                    } else if (Const.BLE_SERVICE_CUSTOM.equals(service.getUuid())) {//FFF0
                        customTimeCharacteristic = service.getCharacteristic(Const.BLE_CHAR_CUSTOM_TIME);//FFF1
                        if (customTimeCharacteristic != null)
                            gatt.setCharacteristicNotification(customTimeCharacteristic, true);
                    }
                }

                if (glucoseMeasurementCharacteristic == null || RACPCharacteristic == null) {
                    Log.d(TAG,"NOT SUPPORTED DEVICE!");
                    return;
                }

                if (deviceSoftwareRevisionCharacteristic != null) {
                    readDeviceSoftwareRevision(gatt);
                }
                //Log.d(TAG,"Service discovered!");
            }
            else
            {
                Log.d(TAG,"DOWNLOAD ERROR!");
                return;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (Const.BLE_CHAR_SOFTWARE_REVISION.equals(characteristic.getUuid())) { // 2A28
                    String[] revisions = characteristic.getStringValue(0).split("\\.");
                    _version_1 = Integer.parseInt(revisions[0]);
                    _software_ver = characteristic.getStringValue(0);
                    if (deviceSerialCharacteristic != null) {
                        gatt.readCharacteristic(deviceSerialCharacteristic);
                    }

                    //Log.d(TAG,"BLE_CHAR_SOFTWARE_REVISION!:" + _software_ver);
                }else if (Const.BLE_CHAR_GLUCOSE_SERIALNUM.equals(characteristic.getUuid())) { //2A25
                    if(RACPCharacteristic == null) return;

                    gatt.setCharacteristicNotification(RACPCharacteristic, true);
                    final BluetoothGattDescriptor descriptor = RACPCharacteristic.getDescriptor(Const.BLE_DESCRIPTOR_DESCRIPTOR);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                   Log.d(TAG,"BLE_CHAR_GLUCOSE_SERIALNUM!");
                }

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            super.onCharacteristicChanged(gatt, characteristic);
            final UUID uuid = characteristic.getUuid();
            Log.d(TAG,"test1");
            if (Const.BLE_CHAR_CUSTOM_TIME.equals(uuid)) { //FFF1
                Log.d(TAG,"BLE_CHAR_CUSTOM_TIME");
                int offset = 0;
                final int opCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset += 2; // skip the operator

                if (opCode == OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE) { // 05: time result
                    //broadcastUpdate(Const.INTENT_BLE_REQUEST_COUNT, "");
                }
            }else if (Const.BLE_CHAR_GLUCOSE_MEASUREMENT.equals(uuid)) { //2A18
                //Log.d(TAG,"BLE_CHAR_GLUCOSE_MEASUREMENT");
                int offset = 0;
                final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset += 1;

                final boolean timeOffsetPresent = (flags & 0x01) > 0;
                final boolean typeAndLocationPresent = (flags & 0x02) > 0;
                final boolean sensorStatusAnnunciationPresent = (flags & 0x08) > 0;
                final boolean contextInfoFollows = (flags & 0x10) > 0;

                int sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                boolean isSavedData = true;
                GlucoseRecord record = mRecords.get(sequenceNumber);
                if (record == null) {
                    record = new GlucoseRecord();
                    isSavedData = false;
                }
                record.sequenceNumber = sequenceNumber;
                record.flag_context = 0;
                offset += 2;

                final int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset + 0);
                final int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
                final int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3);
                final int hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4);
                final int minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5);
                final int seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6);
                offset += 7;

                final Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day, hours, minutes, seconds);
                record.time = calendar.getTimeInMillis() / 1000;

                if (timeOffsetPresent) {
                    record.timeoffset = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
                    record.time = record.time + (record.timeoffset * 60);
                    offset += 2;
                }

                if (typeAndLocationPresent) {
                    byte[] value = characteristic.getValue();
                    int glucoseValue = (int) bytesToFloat(value[offset], value[offset + 1]);
                    if(isMgdl) {
                        record.glucoseData = glucoseValue;
                    } else{
                        record.glucoseData = Double.parseDouble(String.valueOf(Math.round(10 * (double) glucoseValue / GLUCOSE_CONV) / 10.0));
                    }
                    final int typeAndLocation = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
                    int type = (typeAndLocation & 0xF0) >> 4;
                    record.flag_cs = type == 10 ? 1 : 0;
                    offset += 3;
                }

                if (sensorStatusAnnunciationPresent) {
                    int hilow = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                    if (hilow == 64) record.flag_hilow = -1;//lo
                    if (hilow == 32) record.flag_hilow = 1;//hi

                    offset += 2;
                }

                if (contextInfoFollows == false) {
                    record.flag_context = 1;
                }

                try {
                    if (isSavedData == false)
                        mRecords.put(record.sequenceNumber, record);
                } catch (Exception e) {
                }

                final GlucoseRecord glucoseRecord = record;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String str_hilow = "-";
                        if (glucoseRecord.flag_hilow == -1) str_hilow = "Lo";
                        else if (glucoseRecord.flag_hilow == 1) str_hilow = "Hi";

                        mRecords.put(glucoseRecord.sequenceNumber, glucoseRecord);
                        if (!contextInfoFollows) {
                            Log.d(TAG,"### : " + glucoseRecord.sequenceNumber + "," + " glucose: " + glucoseRecord.glucoseData +
                                    "," + " date: " + getDate(glucoseRecord.time) + "," + " timeoffset: " + glucoseRecord.timeoffset +
                                    "," + " hilo: " + str_hilow + "\n\n");

                        }
                        //TODO Update 함수 호출
                        ((UserHomeActivity)context).glucoseRecords.add(glucoseRecord);
                        try {
                            ((UserHomeActivity)context).updateGlucoseInfo();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG,"값"+glucoseRecord+"전송");
                    }
                });

            }else if (Const.BLE_CHAR_GLUCOSE_CONTEXT.equals(uuid)) { //2A34
                //Log.d(TAG,"BLE_CHAR_GLUCOSE_CONTEXT");


            }else if (Const.BLE_CHAR_GLUCOSE_RACP.equals(uuid)) { // Record Access Control Point characteristic 2A52
                Log.d(TAG,"BLE_CHAR_GLUCOSE_RACP");
                int offset = 0;
                final int opCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                offset += 2; // skip the operator

                if (opCode == COMPLETE_RESULT_FROM_METER) { //C0
                    final int requestedOpCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset - 1);
                    switch (requestedOpCode) {
                        case RESPONSE_SUCCESS: //01
                            //broadcastUpdate(Const.INTENT_BLE_READCOMPLETED, "");
                            //mBluetoothGatt.writeCharacteristic(characteristic);
                            break;
                        case RESPONSE_OP_CODE_NOT_SUPPORTED: //02
                            break;
                    }
                } else if (opCode == OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE) { // 05
                    if (bluetoothGatt == null || RACPCharacteristic == null) {
                        Log.d(TAG,"OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE ERROR!");
                        //broadcastUpdate(Const.INTENT_BLE_ERROR, getResources().getString(R.string.ERROR_CONNECTION_STATE_CHANGE));
                        return;
                    }

                    //clear(); //레코드 비움
                    //broadcastUpdate(Const.INTENT_BLE_OPERATESTARTED, "");

                    _number=characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);

                    offset += 2;
                    Log.d(TAG,"TOTAL: "+ _number);
                    getAllRecords();

                } else if (opCode == OP_CODE_RESPONSE_CODE) { // 06
                    final int subResponseCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                    final int responseCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);
                    offset += 2;

                    switch (responseCode) {
                        case RESPONSE_SUCCESS:
                            break;
                        case RESPONSE_NO_RECORDS_FOUND: //06000106
                            // android 6.0.1 issue - app disconnect send
                            //mBluetoothGatt.writeCharacteristic(characteristic);
                            //broadcastUpdate(Const.INTENT_BLE_READCOMPLETED, "");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                try {
                                    Thread.sleep(100);
                                    //mBluetoothGatt.disconnect();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    Thread.sleep(100);
                                    //mBluetoothGatt.writeCharacteristic(characteristic);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case RESPONSE_OP_CODE_NOT_SUPPORTED:
                            //broadcastUpdate(Const.INTENT_BLE_OPERATENOTSUPPORTED, "");
                            break;
                        case RESPONSE_PROCEDURE_NOT_COMPLETED:
                        case RESPONSE_ABORT_UNSUCCESSFUL:
                        default:
//                            broadcastUpdate(Const.INTENT_BLE_OPERATEFAILED, "");
                            break;
                    }
                }

            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG,"5");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Log.d(TAG,descriptor.getCharacteristic().getUuid().toString());
                if (Const.BLE_CHAR_GLUCOSE_MEASUREMENT.equals(descriptor.getCharacteristic().getUuid())) { //2A18
                    enableGlucoseContextNotification(gatt);
                }
                if (Const.BLE_CHAR_GLUCOSE_CONTEXT.equals(descriptor.getCharacteristic().getUuid())) { //2A34
                    enableTimeSyncIndication(gatt);
                }
                if (Const.BLE_CHAR_GLUCOSE_RACP.equals(descriptor.getCharacteristic().getUuid())) { //2A52
                    enableGlucoseMeasurementNotification(gatt);
                }
                if (Const.BLE_CHAR_CUSTOM_TIME.equals(descriptor.getCharacteristic().getUuid())) { //FFF1
                    if (getSequenceNumber() == false) {
                        try {
                            Thread.sleep(200);
                            getSequenceNumber();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    Log.d(TAG,"ERR:GATT_INSUFFICIENT_AUTHENTICATION ("+status+")");
                }
            }
        }

        private void readDeviceSoftwareRevision(final BluetoothGatt gatt) {
            gatt.readCharacteristic(deviceSoftwareRevisionCharacteristic);
        }

        private boolean getAllRecords() {
            if (bluetoothGatt == null || RACPCharacteristic == null) {
                Log.d(TAG,"ERROR! cannot get all records!");
                return false;
            }

            //Log.d(TAG, "All Records!");

            setOpCode(RACPCharacteristic, OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS);
            return bluetoothGatt.writeCharacteristic(RACPCharacteristic);

        }

        private boolean getAfterRecords() {
            if (bluetoothGatt == null || RACPCharacteristic == null) {
                return false;
            }

            if (customTimeCharacteristic == null) { //0403
                setOpCode(RACPCharacteristic, OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_GREATER_OR_EQUAL_RECORDS,
                        _number);
            } else {
                if (customTimeCharacteristic.getUuid().equals(Const.BLE_CHAR_CUSTOM_TIME_MC) == true)
                    setOpCode(RACPCharacteristic, OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_GREATER_OR_EQUAL_RECORDS,
                            _number);
                else
                    setOpCode(RACPCharacteristic, OP_CODE_REPORT_STORED_RECORDS, OPERATOR_GREATER_OR_EQUAL_RECORDS,
                            _number);
            }

            return bluetoothGatt.writeCharacteristic(RACPCharacteristic);

        }
    };
    public boolean isRunning(){
        return isOn;
    }

    private void enableGlucoseMeasurementNotification(final BluetoothGatt gatt) {
        if (glucoseMeasurementCharacteristic == null) return;

        gatt.setCharacteristicNotification(glucoseMeasurementCharacteristic, true);
        final BluetoothGattDescriptor descriptor = glucoseMeasurementCharacteristic.getDescriptor(Const.BLE_DESCRIPTOR_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableGlucoseContextNotification(final BluetoothGatt gatt) {
        if (glucoseMeasurementContextCharacteristic == null) return;

        gatt.setCharacteristicNotification(glucoseMeasurementContextCharacteristic, true);
        final BluetoothGattDescriptor descriptor = glucoseMeasurementContextCharacteristic.getDescriptor(Const.BLE_DESCRIPTOR_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableRecordAccessControlPointIndication(final BluetoothGatt gatt) {
        if (RACPCharacteristic == null) return;

        gatt.setCharacteristicNotification(RACPCharacteristic, true);
        final BluetoothGattDescriptor descriptor = RACPCharacteristic.getDescriptor(Const.BLE_DESCRIPTOR_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void enableTimeSyncIndication(final BluetoothGatt gatt) {
        if (customTimeCharacteristic == null) return;

        gatt.setCharacteristicNotification(customTimeCharacteristic, true);
        final BluetoothGattDescriptor descriptor = customTimeCharacteristic.getDescriptor(Const.BLE_DESCRIPTOR_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private void setOpCode(final BluetoothGattCharacteristic characteristic, final int opCode, final int operator, final Integer... params) {
        if (characteristic == null) return;

        final int size = 2 + ((params.length > 0) ? 1 : 0) + params.length * 2; // 1 byte for opCode, 1 for operator, 1 for filter type (if parameters exists) and 2 for each parameter
        characteristic.setValue(new byte[size]);

        int offset = 0;
        characteristic.setValue(opCode, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        offset += 1;

        characteristic.setValue(operator, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        offset += 1;

        if (params.length > 0) {
            characteristic.setValue(FILTER_TYPE_SEQUENCE_NUMBER, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
            offset += 1;

            for (final Integer i : params) {
                characteristic.setValue(i, BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                offset += 2;
            }
        }
    }

    private boolean getSequenceNumber() {
        if (bluetoothGatt == null || RACPCharacteristic == null) {
            //broadcastUpdate(Const.INTENT_BLE_ERROR, getResources().getString(R.string.ERROR_CONNECTION_STATE_CHANGE) + "null");
            Log.d(TAG,"getSequenceNumber() ERROR!");
            return false;
        }

        //clear(); //레코드들 비움
        //broadcastUpdate(Const.INTENT_BLE_OPERATESTARTED, "");

        setOpCode(RACPCharacteristic, OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_ALL_RECORDS);
        return bluetoothGatt.writeCharacteristic(RACPCharacteristic);

    }
    public String getDate(long t) {
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdfNow.format(t * 1000);

        return date;
    }

    private float bytesToFloat(byte b0, byte b1) {
        return (float) unsignedByteToInt(b0) + ((unsignedByteToInt(b1) & 0x0F) << 8);

    }
    private int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }
}
