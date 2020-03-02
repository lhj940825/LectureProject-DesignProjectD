package com.example.hojun.healthcareserviceprojectd;

/**
 * Created by HoJun on 2017-11-11.
 */
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
import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class ScaleRecord{
    String weight;
    String fat;
    String water;
    String muscle;
    String BMR;
    String visfat;
    String bone;
}

public class ScaleBLE extends Thread {
    Timer timer1;
    Timer timer2;
    TimerTask timertask1;
    TimerTask timertask2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private static HashMap<String, String> attributes = new HashMap();
    public static String SCALE_MEASUREMENT_SERVICE = "";
    public static String SCALE_MEASUREMENT = "";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "";

    public final static UUID UUID_SCALE_MEASUREMENT =
            UUID.fromString(SCALE_MEASUREMENT);

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    static {
        // Sample Services.
        attributes.put("", "Heart Rate Service");
        attributes.put("", "Device Information Service");
        // Sample Characteristics.
        attributes.put(SCALE_MEASUREMENT, "Scale Measurement");
        attributes.put("", "Manufacturer Name String");
    }

    String weight_Val;
    String fat_Val;
    String water_Val;
    String muscle_Val;
    String BMR_Val;
    String visceralfat_Val;
    String bone_Val;

    private boolean isRunning=false;
    private boolean isOn = false;
    private int count =0;

    private final String TAG = "ScaleBLE";
    //private final String address = "C8:B2:1E:08:71:B6"; //zip
    private final String address = "C8:B2:1E:08:70:7A";
    private Context context;

    ScaleBLE(Context _context){
        context = _context;
    }

    public void Init(){
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
    }

    @Override
    public void run() {
        isOn=true;
        while(isOn) {
            try {
                while (!isRunning) {
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Init();
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                //Log.d(TAG,"기기연결 안대쬬");
                continue;
            }
            if (bluetoothManager != null && bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED) {
                //Log.d(TAG,"연결 안대쬬!");
                continue;
            }
            bluetoothGatt = device.connectGatt(context, false, mGattCallBack);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void Stop(){
        isOn=false;
    }
    public void Suspend() {isRunning=false;}
    public void Resume(){isRunning=true;}

    private final BluetoothGattCallback mGattCallBack= new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState==BluetoothProfile.STATE_CONNECTING){
                isRunning=false;
            }
            if(isRunning && newState== BluetoothProfile.STATE_CONNECTED){
                isRunning=false;
                Log.d(TAG,"Connected!");
                /*
                timer1=new Timer();
                timertask1 = new TimerTask() {
                    @Override
                    public void run() {
                        if(weight_Val==null || Double.valueOf(weight_Val).equals(0)){
                            bluetoothGatt.disconnect();
                            timer1.cancel();
                        }
                    }
                };
                timer1.schedule(timertask1,3000);
                */
                bluetoothGatt.discoverServices();
            }
            else if(!isRunning && newState== BluetoothProfile.STATE_DISCONNECTED){
                isRunning=true;
                Log.d(TAG,"Disconnected!");
                weight_Val=null;
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG,"onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    List<BluetoothGattService> ser = gatt.getServices();
                    if(!UUID.fromString(SCALE_MEASUREMENT_SERVICE).equals(service.getUuid()))
                        continue;
                    for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                        if(!UUID.fromString(SCALE_MEASUREMENT).equals(characteristic.getUuid()))
                            continue;
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            gatt.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            gatt.setCharacteristicNotification(characteristic, true);
                        }
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                    }
                }

            }else {
                Log.d(TAG,"DOWNLOAD ERROR!");
                return;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(UUID.fromString(SCALE_MEASUREMENT).equals(characteristic.getUuid())){
                    int flag = characteristic.getProperties();
                    int format = -1;
                    if ((flag & 0x01) != 0) {
                        format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    } else {
                        format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    }

                    String mValue = "";
                    try {
                        StringBuilder sb = new StringBuilder();
                        String str = getHexString(characteristic.getValue()).toUpperCase();

                        sb.append(str.substring(6, 10));

                        float mValue1 = (float)getHexToDec(sb.toString());

                        NumberFormat numberformat = new DecimalFormat("###,###.##");
                        // UI상에서 보여줄 값

                        String strVal = numberformat.format(mValue1/100.0D);

                        StringBuilder stringbuilder = new StringBuilder();
                        StringBuilder stringbuilder2 = new StringBuilder();
                        StringBuilder stringbuilder3 = new StringBuilder();
                        StringBuilder stringbuilder4 = new StringBuilder();
                        StringBuilder stringbuilder5 = new StringBuilder();
                        StringBuilder stringbuilder6 = new StringBuilder();
                        StringBuilder stringbuilder7 = new StringBuilder();

                        float weight_Value = (float)getHexToDec(stringbuilder.append(str.substring(10, 14)).toString());
                        float fat_Value = (float)getHexToDec(stringbuilder2.append(str.substring(14, 18)).toString());
                        float water_Value = (float)getHexToDec(stringbuilder3.append(str.substring(18, 22)).toString());
                        float muscle_Value = (float)getHexToDec(stringbuilder4.append(str.substring(22, 26)).toString());
                        float BMR_Value = (float)getHexToDec(stringbuilder5.append(str.substring(26, 30)).toString());
                        float visceralfat_Value = (float)getHexToDec(stringbuilder6.append(str.substring(30, 34)).toString());
                        float bone_Value = (float)getHexToDec(stringbuilder7.append(str.substring(34, 36)).toString());


                        NumberFormat numformat1 = new DecimalFormat("###,###.##");
                        NumberFormat numformat2 = new DecimalFormat("###,###.##");
                        NumberFormat numformat3 = new DecimalFormat("###,###.##");
                        NumberFormat numformat4 = new DecimalFormat("###,###.##");
                        NumberFormat numformat5 = new DecimalFormat("###,###.##");
                        NumberFormat numformat6 = new DecimalFormat("###,###.##");
                        NumberFormat numformat7 = new DecimalFormat("###,###.##");

                        weight_Val = numformat1.format(weight_Value/10.0D);
                        fat_Val = numformat2.format(fat_Value/10.0D);
                        water_Val = numformat3.format(water_Value/10.0D);
                        muscle_Val = numformat4.format(muscle_Value/10.0D);
                        BMR_Val = numformat5.format(BMR_Value/1.0D);
                        visceralfat_Val = numformat6.format(visceralfat_Value/10.0D);
                        bone_Val = numformat7.format(bone_Value/10.0D);

                    } catch (Exception e) {e.printStackTrace();}

                    final int heartRate = characteristic.getIntValue(format, 1);
                }
            }else{

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //Log.d(TAG,"onCharacteristicWrite!");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //super.onCharacteristicChanged(gatt, characteristic);
            if(UUID.fromString(SCALE_MEASUREMENT).equals(characteristic.getUuid())){
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                }

                String mValue = "";
                try {
                    StringBuilder sb = new StringBuilder();
                    String str = getHexString(characteristic.getValue()).toUpperCase();

                    sb.append(str.substring(6, 10));

                    float mValue1 = (float)getHexToDec(sb.toString());

                    NumberFormat numberformat = new DecimalFormat("###,###.##");
                    // UI상에서 보여줄 값

                    String strVal = numberformat.format(mValue1/100.0D);

                    StringBuilder stringbuilder = new StringBuilder();
                    StringBuilder stringbuilder2 = new StringBuilder();
                    StringBuilder stringbuilder3 = new StringBuilder();
                    StringBuilder stringbuilder4 = new StringBuilder();
                    StringBuilder stringbuilder5 = new StringBuilder();
                    StringBuilder stringbuilder6 = new StringBuilder();
                    StringBuilder stringbuilder7 = new StringBuilder();

                    final float weight_Value = (float)getHexToDec(stringbuilder.append(str.substring(10, 14)).toString());
                    final float fat_Value = (float)getHexToDec(stringbuilder2.append(str.substring(14, 18)).toString());
                    final float water_Value = (float)getHexToDec(stringbuilder3.append(str.substring(18, 22)).toString());
                    final float muscle_Value = (float)getHexToDec(stringbuilder4.append(str.substring(22, 26)).toString());
                    final float BMR_Value = (float)getHexToDec(stringbuilder5.append(str.substring(26, 30)).toString());
                    final float visceralfat_Value = (float)getHexToDec(stringbuilder6.append(str.substring(30, 34)).toString());
                    final float bone_Value = (float)getHexToDec(stringbuilder7.append(str.substring(34, 36)).toString());


                    NumberFormat numformat1 = new DecimalFormat("###,###.##");
                    NumberFormat numformat2 = new DecimalFormat("###,###.##");
                    NumberFormat numformat3 = new DecimalFormat("###,###.##");
                    NumberFormat numformat4 = new DecimalFormat("###,###.##");
                    NumberFormat numformat5 = new DecimalFormat("###,###.##");
                    NumberFormat numformat6 = new DecimalFormat("###,###.##");
                    NumberFormat numformat7 = new DecimalFormat("###,###.##");

                    weight_Val = numformat1.format(weight_Value/10.0D);
                    fat_Val = numformat2.format(fat_Value/10.0D);
                    water_Val = numformat3.format(water_Value/10.0D);
                    muscle_Val = numformat4.format(muscle_Value/10.0D);
                    BMR_Val = numformat5.format(BMR_Value/1.0D);
                    visceralfat_Val = numformat6.format(visceralfat_Value/10.0D);
                    bone_Val = numformat7.format(bone_Value/10.0D);

                    /*
                    if(timertask1!=null){
                        timertask1.cancel();
                        timertask1=null;
                    }
                    if(timer1!=null){
                        timer1.cancel();
                        timer1=null;
                    }
*/
                    // if(weight_Value!=0){
                    count=3;

                    if(timertask2!=null) {
                        timertask2.cancel();
                        timertask2=null;
                    }
                    if(timer2!=null){
                        timer2.cancel();
                        timer2=null;
                    }

                    timertask2= new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            if(--count<=0){
                                //bluetoothGatt.disconnect();
                                if(weight_Value==0 || weight_Val==null)
                                    return;
                                ScaleRecord record = new ScaleRecord();
                                record.weight =weight_Val;
                                //record.BMR=String.valueOf(BMR_Value;
                                record.bone=bone_Val;
                                record.fat= fat_Val;
                                record.muscle=muscle_Val;
                                //record.visfat=String.valueOf(visceralfat_Value);
                                //record.water=String.valueOf(water_Value);
                                ((UserHomeActivity)context).scaleRecords.add(record);
                                ((UserHomeActivity)context).updateScaleInfo();
                                Log.d(TAG, "체중 = " + weight_Val + "kg" + "\n"+
                                        "fat = " + fat_Val + "%" + "\n"+
                                        "체수분 = " + water_Val + "%" + "\n"+
                                        "근육량 = " + muscle_Val + "%" + "\n"+
                                        "BMR = " + BMR_Val + "kcal" + "\n"+
                                        "visfat = " + visceralfat_Val + "%" + "\n"+
                                        "골격 = " + bone_Val + "kg" + "\n");
                                timer2.cancel();
                                timertask2.cancel();
                            }
                        }
                    };
                    timer2 = new Timer();
                    timer2.schedule(timertask2,1000,1000);
                    //Log.d(TAG,"SCALING..");
                    // }
                } catch (Exception e) {e.printStackTrace();}

                final int heartRate = characteristic.getIntValue(format, 1);
            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //    super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG,"onDescriptorRead!");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //    super.onDescriptorWrite(gatt, descriptor, status);
            //    Log.d(TAG,"onDescriptorWrite!");
        }

        public void writeCustomCharacteristic(int value) {
            if (bluetoothAdapter == null || bluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
		/*check if the service is available on the device*/
            BluetoothGattService mCustomService = bluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
            if(mCustomService == null){
                Log.w(TAG, "Custom BLE Service not found");
                return;
            }
		/*get the read characteristic from the service*/
            BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
            mWriteCharacteristic.setValue(value,android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0);
            if(bluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false){
                Log.w(TAG, "Failed to write characteristic");
            }
        }
        public void writeCustomCharacteristic(byte[] value) {
            if (bluetoothAdapter == null || bluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
		/*check if the service is available on the device*/
            BluetoothGattService mCustomService = bluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
            if(mCustomService == null){
                //Log.w(TAG, "Custom BLE Service not found");
                return;
            } if(mCustomService !=null) {
                //Log.w(TAG, "BLE가 되는겨!!!!!!!!!!!!!!!!!");
            }
		/*get the read characteristic from the service*/
            BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
            mWriteCharacteristic.setValue(value);
            if(bluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false){
                Log.w(TAG, "Failed to write characteristic");
            }
        }
        public float changeOnePoint(float paramFloat, int paramInt)
        {
            return new BigDecimal(paramFloat).setScale(paramInt, 4).floatValue();
        }

        public int dec(String hex){
            String[] temp = hex.split("0x");
            StringBuffer dec = null;
            for(String strArr : temp){
                dec = new StringBuffer();
                dec.append(strArr);
            }
            return Integer.parseInt(dec.toString(), 16);
        }

        public int hexToTen(String paramString)
        {
            if ((paramString == null) || ((paramString != null) && ("".equals(paramString)))) {}
            for (int i = 0;; i = Integer.valueOf(paramString, 16).intValue()) {
                return i;
            }
        }

        private float getHexToDec(String hex) {
            long v = Long.parseLong(hex, 16);
            return Float.parseFloat(String.valueOf(v));
        }

        public String byteToBinaryString(byte n) {
            StringBuilder sb = new StringBuilder("00000000");
            for (int bit = 0; bit < 8; bit++) {
                if (((n >> bit) & 1) > 0) {
                    sb.setCharAt(7 - bit, '1');
                }
            }
            return sb.toString();
        }

        private String getHexString(byte b) {
            try {
                return Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 );
            } catch (Exception e) {	return null; }
        }

        private String getHexString(byte[] b) {
            String result = "";
            try {
                for (int i=0; i < b.length; i++) {
                    result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
                }
                return result;
            } catch (Exception e) {	return null; }
        }
    };
}
