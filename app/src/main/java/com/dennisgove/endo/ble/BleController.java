package com.dennisgove.endo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dennisgove.endo.EndoApplication;
import com.dennisgove.endo.comm.BroadcastReceiverCollection;
import com.dennisgove.endo.comm.EndoBroadcastReceiver;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by dennis on 9/1/16.
 *
 * The BleController is the proxy for all interactions with the bluetooth adapter. It works only with
 * bluetooth low energy (ble) and exposes various functions of the adapter as well as broadcasting
 * actions on various events.
 *
 * Broadcast Actions:
 * Certain events with the bluetooth adapter will result in the BleController broadcasting actions.
 * The broadcast actions are meant to notify listeners that some event occurred but does not include
 * must information (if any) about that event. It is suggested that listeners use the receipt of the
 * action as an opportunity to check all neccessary pieces of their bluetooth connectivity.
 *
 * ACTION_CONNECTION_CHANGED - broadcast when any change in connectivity is found. This includes all
 *                             actions to turn bluetooth on or off, connecting or disconnecting
 *                             devices whether done via user action or any other means.
 *
 */
public class BleController {
    private final String TAG = this.getClass().getSimpleName();

    public static final String ACTION_CONNECTION_CHANGED = "com.dennisgove.endo.ble.BleController.action.CONNECTION_CHANGED";

    // injected
    private EndoApplication endoApplication;
    @Inject @Named("applicationContext") Context applicationContext;
    @Inject BleAdapter bleAdapter;

    // created internally
    private boolean isStarted = false;
    private BroadcastReceiverCollection broadcastReceivers;

    public BleController(EndoApplication endoApplication){
        this.endoApplication = endoApplication;
    }

    public void start() {

        endoApplication.getEndoManagerComponent().inject(this);
        broadcastReceivers = new BroadcastReceiverCollection(endoApplication);

        // Register a state changed receiver (bluetooth on/off changes)
        broadcastReceivers.register(new EndoBroadcastReceiver(
                BluetoothAdapter.ACTION_STATE_CHANGED,
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED
        ) {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch(intent.getAction()){
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                        // we only care about on/off states and can ignore turning on/off states
                        if(BluetoothAdapter.STATE_ON == state || BluetoothAdapter.STATE_OFF == state){
                            Log.i(TAG, "Bluetooth state has changed - broadcasting action '" + ACTION_CONNECTION_CHANGED + "' + state '" + state + "'");
                            applicationContext.sendBroadcast(
                                    new Intent(ACTION_CONNECTION_CHANGED)
                                            .addFlags(Intent.FLAG_FROM_BACKGROUND)
                            );
                        }
                        break;

                    default:
                        Log.d(TAG, "BluetoothAdapter action '" + intent.getAction() + "' occurred but is being unhandled");
                }
            }
        });

        isStarted = true;
    }

    public void stop() {
        broadcastReceivers.close();
        isStarted = false;
    }

    public boolean isRunning(){
        return isStarted;
    }

    public boolean isBluetoothCapable() {
        return null != bleAdapter;
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothCapable() && bleAdapter.isEnabled();
    }

    public void askUserTurnBluetoothOn() {

        if(isBluetoothEnabled()){
            Log.i(TAG, "Bluetooth already enabled on device - nothing to do");
        }else if(isBluetoothCapable()){
            Log.i(TAG, "Requesting that bluetooth be enabled on device");

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if(bleAdapter == null || !bleAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivities(new Intent[]{enableBtIntent});
            }
        }else{
            Log.i(TAG, "Device is not capable of bluetooth communication - nothing can be done");
        }
    }

//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
//                switch (newState) {
//                    case BluetoothProfile.STATE_CONNECTED:
//                        Log.e(TAG, "STATE_CONNECTED");
//                        isConnected = true;
//
//                        if (enforceMainThread()){
//                            Handler iHandler = new Handler(Looper.getMainLooper());
//                            iHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    android.util.Log.i(TAG, "discoverServices On Main Thread? " + isOnMainThread());
//                                    if (mGatt != null)
//                                        mGatt.discoverServices();
//                                }
//                            });
//                        } else {
//                            android.util.Log.i(TAG, "discoverServices On Main Thread? " + isOnMainThread());
//                            if (mGatt != null)
//                                mGatt.discoverServices();
//                        }
//
//
//                        stopScan();
//                        scan_interval_timer.cancel();
//                        keepAlive();
//                        break;
//                    case BluetoothProfile.STATE_DISCONNECTED:
//                        isConnected = false;
//                        if (isScanning) {
//                            stopScan();
//                        }
//                        Log.e(TAG, "STATE_DISCONNECTED: " + status);
//                        if (mGatt != null)
//                            mGatt.close();
//                        mGatt = null;
//                        if (status == 0 && !encountered133) {// || status == 59) {
//                            android.util.Log.i(TAG, "clean disconnect");
//                            max133RetryCounter = 0;
//                            if (scanConstantly())
//                                cycleScan(15000);
//                        } else if (status == 133 || max133RetryCounter >= max133Retries) {
//                            Log.e(TAG, "max133RetryCounter? " + max133RetryCounter);
//                            Log.e(TAG, "Encountered 133: " + encountered133);
//                            max133RetryCounter = 0;
//                            cycleBT();
//                        } else if (encountered133) {
//                            Log.e(TAG, "max133RetryCounter? " + max133RetryCounter);
//                            Log.e(TAG, "Encountered 133: " + encountered133);
//                            if (scanConstantly())
//                                startScan();
//                            else
//                                cycleScan(0);
//                            max133RetryCounter++;
//                        } else if (status == 129) {
//                            forgetDevice();
//                        } else {
//                            if (scanConstantly())
//                                startScan();
//                            else
//                                cycleScan(0);
//                            max133RetryCounter = 0;
//                        }
//
//                        break;
//                    default:
//                        Log.e("gattCallback", "STATE_OTHER");
//                }
//            }
//
//
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
//            if (enforceMainThread()) {
//                Handler iHandler = new Handler(Looper.getMainLooper());
//                iHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        android.util.Log.i(TAG, "onServicesDiscovered On Main Thread? " + isOnMainThread());
//                        Log.e(TAG, "onServicesDiscovered: " + status);
//                        if (status == BluetoothGatt.GATT_SUCCESS) {
//                            cgmService = mGatt.getService(BluetoothServices.CGMService);
//                            authCharacteristic = cgmService.getCharacteristic(BluetoothServices.Authentication);
//                            controlCharacteristic = cgmService.getCharacteristic(BluetoothServices.Control);
//                            commCharacteristic = cgmService.getCharacteristic(BluetoothServices.Communication);
//                            mBluetoothAdapter.cancelDiscovery();
//
//                            //TODO : ADD option in settings!
//                            if (alwaysAuthenticate() || alwaysUnbond()) {
//                                fullAuthenticate();
//                            } else {
//                                authenticate();
//                            }
//
//                        } else {
//                            Log.w(TAG, "onServicesDiscovered received: " + status);
//                        }
//
//                        if (status == 133) {
//                            encountered133 = true;
//                        }
//                    }
//                });
//            } else {
//                android.util.Log.i(TAG, "onServicesDiscovered On Main Thread? " + isOnMainThread());
//                Log.e(TAG, "onServicesDiscovered: " + status);
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    cgmService = mGatt.getService(BluetoothServices.CGMService);
//                    authCharacteristic = cgmService.getCharacteristic(BluetoothServices.Authentication);
//                    controlCharacteristic = cgmService.getCharacteristic(BluetoothServices.Control);
//                    commCharacteristic = cgmService.getCharacteristic(BluetoothServices.Communication);
//                    mBluetoothAdapter.cancelDiscovery();
//
//                    //TODO : ADD option in settings!
//                    if (alwaysAuthenticate() || alwaysUnbond()) {
//                        fullAuthenticate();
//                    } else {
//                        authenticate();
//                    }
//
//                } else {
//                    Log.w(TAG, "onServicesDiscovered received: " + status);
//                }
//
//                if (status == 133) {
//                    encountered133 = true;
//                }
//            }
//
//
//        }
//
//        // DENNIS
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
//            if (enforceMainThread()) {
//                Handler iHandler = new Handler(Looper.getMainLooper());
//                iHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        android.util.Log.i(TAG, "onDescriptorWrite On Main Thread? " + isOnMainThread());
//                        if (status == BluetoothGatt.GATT_SUCCESS) {
//                            mGatt.writeCharacteristic(descriptor.getCharacteristic());
//                            Log.e(TAG, "Writing descriptor: " + status);
//                        } else {
//                            Log.e(TAG, "Unknown error writing descriptor");
//                        }
//
//                        if (status == 133) {
//                            encountered133 = true;
//                        }
//                    }
//                });
//            } else {
//                android.util.Log.i(TAG, "onDescriptorWrite On Main Thread? " + isOnMainThread());
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    mGatt.writeCharacteristic(descriptor.getCharacteristic());
//                    Log.e(TAG, "Writing descriptor: " + status);
//                } else {
//                    Log.e(TAG, "Unknown error writing descriptor");
//                }
//
//                if (status == 133) {
//                    encountered133 = true;
//                }
//            }
//
//        }
//
//        // DENNIS
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
//            if (enforceMainThread()) {
//                Handler iHandler = new Handler(Looper.getMainLooper());
//                iHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e(TAG, "Success Write " + String.valueOf(status));
//                        //Log.e(TAG, "Characteristic " + String.valueOf(characteristic.getUuid()));
//                        android.util.Log.i(TAG, "onCharacteristicWrite On Main Thread? " + isOnMainThread());
//
//                        if (status == BluetoothGatt.GATT_SUCCESS) {
//                            if (String.valueOf(characteristic.getUuid()).equalsIgnoreCase(String.valueOf(authCharacteristic.getUuid()))) {
//                                android.util.Log.i(TAG, "Char Value: " + Arrays.toString(characteristic.getValue()));
//                                android.util.Log.i(TAG, "auth? " + String.valueOf(characteristic.getUuid()));
//                                if (characteristic.getValue() != null && characteristic.getValue()[0] != 0x6) {
//                                    mGatt.readCharacteristic(characteristic);
//                                }
//                            } else {
//                                android.util.Log.i(TAG, "control? " + String.valueOf(characteristic.getUuid()));
//                                android.util.Log.i(TAG, "status? " + status);
//                            }
//                        }
//
//                        if (status == 133) {
//                            encountered133 = true;
//                        }
//                    }
//                });
//
//            } else {
//                Log.e(TAG, "Success Write " + String.valueOf(status));
//                //Log.e(TAG, "Characteristic " + String.valueOf(characteristic.getUuid()));
//                android.util.Log.i(TAG, "onCharacteristicWrite On Main Thread? " + isOnMainThread());
//
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    if (String.valueOf(characteristic.getUuid()).equalsIgnoreCase(String.valueOf(authCharacteristic.getUuid()))) {
//                        android.util.Log.i(TAG, "Char Value: " + Arrays.toString(characteristic.getValue()));
//                        android.util.Log.i(TAG, "auth? " + String.valueOf(characteristic.getUuid()));
//                        if (characteristic.getValue() != null && characteristic.getValue()[0] != 0x6) {
//                            mGatt.readCharacteristic(characteristic);
//                        }
//                    } else {
//                        android.util.Log.i(TAG, "control? " + String.valueOf(characteristic.getUuid()));
//                        android.util.Log.i(TAG, "status? " + status);
//                    }
//                }
//
//                if (status == 133) {
//                    encountered133 = true;
//                }
//            }
//
//
//        }
//
//        // DENNIS
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
//            if (enforceMainThread()) {
//                Handler iHandler = new Handler(Looper.getMainLooper());
//                iHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e(TAG, "ReadStatus: " + String.valueOf(status));
//                        android.util.Log.i(TAG, "onCharacteristicRead On Main Thread? " + isOnMainThread());
//
//                        if (status == BluetoothGatt.GATT_SUCCESS) {
//                            Log.e(TAG, "CharBytes-or " + Arrays.toString(characteristic.getValue()));
//                            android.util.Log.i(TAG, "CharHex-or " + Extensions.bytesToHex(characteristic.getValue()));
//
//                            byte[] buffer = characteristic.getValue();
//                            byte code = buffer[0];
//                            Transmitter defaultTransmitter = new Transmitter(prefs.getString("dex_txid", "ABCDEF"));
//                            mBluetoothAdapter = mBluetoothManager.getAdapter();
//
//                            switch (code) {
//                                case 5:
//                                    authStatus = new AuthStatusRxMessage(characteristic.getValue());
//                                    if (authStatus.authenticated == 1 && authStatus.bonded == 1 && isBondedOrBonding == true) {
//                                        isBondedOrBonding = true;
//                                        getSensorData();
//                                    } else if (authStatus.authenticated == 1 && authStatus.bonded == 2) {
//                                        android.util.Log.i(TAG, "Let's Bond!");
//                                        BondRequestTxMessage bondRequest = new BondRequestTxMessage();
//                                        characteristic.setValue(bondRequest.byteSequence);
//                                        mGatt.writeCharacteristic(characteristic);
//                                        isBondedOrBonding = true;
//                                        device.createBond();
//                                    } else {
//                                        android.util.Log.i(TAG, "Transmitter NOT already authenticated");
//                                        authRequest = new AuthRequestTxMessage();
//                                        characteristic.setValue(authRequest.byteSequence);
//                                        android.util.Log.i(TAG, authRequest.byteSequence.toString());
//                                        mGatt.writeCharacteristic(characteristic);
//                                    }
//                                    break;
//
//                                case 3:
//                                    AuthChallengeRxMessage authChallenge = new AuthChallengeRxMessage(characteristic.getValue());
//                                    if (authRequest == null) {
//                                        authRequest = new AuthRequestTxMessage();
//                                    }
//                                    android.util.Log.i(TAG, "tokenHash " + Arrays.toString(authChallenge.tokenHash));
//                                    android.util.Log.i(TAG, "singleUSe " + Arrays.toString(calculateHash(authRequest.singleUseToken)));
//
//                                    byte[] challengeHash = calculateHash(authChallenge.challenge);
//                                    android.util.Log.d(TAG, "challenge hash" + Arrays.toString(challengeHash));
//                                    if (challengeHash != null) {
//                                        android.util.Log.d(TAG, "Transmitter try auth challenge");
//                                        AuthChallengeTxMessage authChallengeTx = new AuthChallengeTxMessage(challengeHash);
//                                        android.util.Log.i(TAG, "Auth Challenge: " + Arrays.toString(authChallengeTx.byteSequence));
//                                        characteristic.setValue(authChallengeTx.byteSequence);
//                                        mGatt.writeCharacteristic(characteristic);
//                                    }
//                                    break;
//
//                                default:
//                                    android.util.Log.i(TAG, code + " - Transmitter NOT already authenticated");
//                                    authRequest = new AuthRequestTxMessage();
//                                    characteristic.setValue(authRequest.byteSequence);
//                                    android.util.Log.i(TAG, authRequest.byteSequence.toString());
//                                    mGatt.writeCharacteristic(characteristic);
//                                    break;
//                            }
//
//                        }
//
//                        if (status == 133) {
//                            encountered133 = true;
//                        }
//                    }
//                });
//            } else {
//                Log.e(TAG, "ReadStatus: " + String.valueOf(status));
//                android.util.Log.i(TAG, "onCharacteristicRead On Main Thread? " + isOnMainThread());
//
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    Log.e(TAG, "CharBytes-or " + Arrays.toString(characteristic.getValue()));
//                    android.util.Log.i(TAG, "CharHex-or " + Extensions.bytesToHex(characteristic.getValue()));
//
//                    byte[] buffer = characteristic.getValue();
//                    byte code = buffer[0];
//                    Transmitter defaultTransmitter = new Transmitter(prefs.getString("dex_txid", "ABCDEF"));
//                    mBluetoothAdapter = mBluetoothManager.getAdapter();
//
//                    switch (code) {
//                        case 5:
//                            authStatus = new AuthStatusRxMessage(characteristic.getValue());
//                            if (authStatus.authenticated == 1 && authStatus.bonded == 1 && isBondedOrBonding == true) {
//                                isBondedOrBonding = true;
//                                getSensorData();
//                            } else if (authStatus.authenticated == 1 && authStatus.bonded == 2) {
//                                android.util.Log.i(TAG, "Let's Bond!");
//                                BondRequestTxMessage bondRequest = new BondRequestTxMessage();
//                                characteristic.setValue(bondRequest.byteSequence);
//                                mGatt.writeCharacteristic(characteristic);
//                                isBondedOrBonding = true;
//                                device.createBond();
//                            } else {
//                                android.util.Log.i(TAG, "Transmitter NOT already authenticated");
//                                authRequest = new AuthRequestTxMessage();
//                                characteristic.setValue(authRequest.byteSequence);
//                                android.util.Log.i(TAG, authRequest.byteSequence.toString());
//                                mGatt.writeCharacteristic(characteristic);
//                            }
//                            break;
//
//                        case 3:
//                            AuthChallengeRxMessage authChallenge = new AuthChallengeRxMessage(characteristic.getValue());
//                            if (authRequest == null) {
//                                authRequest = new AuthRequestTxMessage();
//                            }
//                            android.util.Log.i(TAG, "tokenHash " + Arrays.toString(authChallenge.tokenHash));
//                            android.util.Log.i(TAG, "singleUSe " + Arrays.toString(calculateHash(authRequest.singleUseToken)));
//
//                            byte[] challengeHash = calculateHash(authChallenge.challenge);
//                            android.util.Log.d(TAG, "challenge hash" + Arrays.toString(challengeHash));
//                            if (challengeHash != null) {
//                                android.util.Log.d(TAG, "Transmitter try auth challenge");
//                                AuthChallengeTxMessage authChallengeTx = new AuthChallengeTxMessage(challengeHash);
//                                android.util.Log.i(TAG, "Auth Challenge: " + Arrays.toString(authChallengeTx.byteSequence));
//                                characteristic.setValue(authChallengeTx.byteSequence);
//                                mGatt.writeCharacteristic(characteristic);
//                            }
//                            break;
//
//                        default:
//                            android.util.Log.i(TAG, code + " - Transmitter NOT already authenticated");
//                            authRequest = new AuthRequestTxMessage();
//                            characteristic.setValue(authRequest.byteSequence);
//                            android.util.Log.i(TAG, authRequest.byteSequence.toString());
//                            mGatt.writeCharacteristic(characteristic);
//                            break;
//                    }
//
//                }
//
//                if (status == 133) {
//                    encountered133 = true;
//                }
//            }
//
//
//        }
//
//        // DENNIS
//        @Override
//        // Characteristic notification
//        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
//            if (enforceMainThread()) {
//                Handler iHandler = new Handler(Looper.getMainLooper());
//                iHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e(TAG, "CharBytes-nfy" + Arrays.toString(characteristic.getValue()));
//                        android.util.Log.i(TAG, "CharHex-nfy" + Extensions.bytesToHex(characteristic.getValue()));
//
//                        android.util.Log.i(TAG, "onCharacteristicChanged On Main Thread? " + isOnMainThread());
//
//                        byte[] buffer = characteristic.getValue();
//                        byte firstByte = buffer[0];
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && gatt != null) {
//                            mGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
//                        }
//                        if (firstByte == 0x2f) {
//                            SensorRxMessage sensorRx = new SensorRxMessage(characteristic.getValue());
//
//                            ByteBuffer sensorData = ByteBuffer.allocate(buffer.length);
//                            sensorData.order(ByteOrder.LITTLE_ENDIAN);
//                            sensorData.put(buffer, 0, buffer.length);
//
//                            int sensor_battery_level = 0;
//                            if (sensorRx.status == TransmitterStatus.BRICKED) {
//                                //TODO Handle this in UI/Notification
//                                sensor_battery_level = 206; //will give message "EMPTY"
//                            } else if (sensorRx.status == TransmitterStatus.LOW) {
//                                sensor_battery_level = 209; //will give message "LOW"
//                            } else {
//                                sensor_battery_level = 216; //no message, just system status "OK"
//                            }
//
//                            //Log.e(TAG, "filtered: " + sensorRx.filtered);
//                            Log.e(TAG, "unfiltered: " + sensorRx.unfiltered);
//                            doDisconnectMessage(gatt, characteristic);
//
//                            // DENNIS
//                            processNewTransmitterData(sensorRx.unfiltered, sensorRx.filtered, sensor_battery_level, new Date().getTime());
//                        }
//                    }
//                });
//            } else {
//                Log.e(TAG, "CharBytes-nfy" + Arrays.toString(characteristic.getValue()));
//                android.util.Log.i(TAG, "CharHex-nfy" + Extensions.bytesToHex(characteristic.getValue()));
//
//                android.util.Log.i(TAG, "onCharacteristicChanged On Main Thread? " + isOnMainThread());
//
//                byte[] buffer = characteristic.getValue();
//                byte firstByte = buffer[0];
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && gatt != null) {
//                    mGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
//                }
//                if (firstByte == 0x2f) {
//                    SensorRxMessage sensorRx = new SensorRxMessage(characteristic.getValue());
//
//                    ByteBuffer sensorData = ByteBuffer.allocate(buffer.length);
//                    sensorData.order(ByteOrder.LITTLE_ENDIAN);
//                    sensorData.put(buffer, 0, buffer.length);
//
//                    int sensor_battery_level = 0;
//                    if (sensorRx.status == TransmitterStatus.BRICKED) {
//                        //TODO Handle this in UI/Notification
//                        sensor_battery_level = 206; //will give message "EMPTY"
//                    } else if (sensorRx.status == TransmitterStatus.LOW) {
//                        sensor_battery_level = 209; //will give message "LOW"
//                    } else {
//                        sensor_battery_level = 216; //no message, just system status "OK"
//                    }
//
//                    //Log.e(TAG, "filtered: " + sensorRx.filtered);
//                    Log.e(TAG, "unfiltered: " + sensorRx.unfiltered);
//                    doDisconnectMessage(gatt, characteristic);
//                    processNewTransmitterData(sensorRx.unfiltered, sensorRx.filtered, sensor_battery_level, new Date().getTime());
//                }
//            }
//
//
//        }
//    };

}
