package com.dennisgove.endo.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dennisgove.endo.bluetooth.BluetoothController;
import com.dennisgove.endo.bluetooth.BluetoothStateChangedHandler;
import com.dennisgove.endo.cgm.Transmitter;

/**
 * Created by dennis on 9/1/16.
 */
public class EndoCommunicationService extends Service {
    private final String TAG = this.getClass().getSimpleName();

    private Transmitter transmitter;
    private BluetoothController bleController;

    public static final String BROADCAST_ACTION_BLUETOOTH_OFF = "com.dennisgove.endo.service.EndoCommunicationService.bluetooth.off";
    public static final String BROADCAST_ACTION_BLUETOOTH_ON = "com.dennisgove.endo.service.EndoCommunicationService.bluetooth.on";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null == bleController){
            bleController = new BluetoothController(this);
            bleController.start();

            final EndoCommunicationService self = this;
            bleController.registerStateChangedHandler(BluetoothAdapter.STATE_ON, new BluetoothStateChangedHandler() {
                @Override
                public void handle(Context context, int oldState, int newState) {
                    self.handleBluetoothOn(context, oldState);
                }
            });
            bleController.registerStateChangedHandler(BluetoothAdapter.STATE_OFF, new BluetoothStateChangedHandler() {
                @Override
                public void handle(Context context, int oldState, int newState) {
                    self.handleBluetoothOff(context, oldState);
                }
            });

            if(bleController.isBluetoothCapable() && !bleController.isBluetoothEnabled()){
                bleController.enableBluetooth();
            }
        }

        // Service will continue to run even if the app is killed
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");

        return null;
    }

    @Override
    public void onDestroy() {
        if(null != bleController){
            bleController.stop();
            bleController = null;
        }
    }

    private void handleBluetoothOn(Context context, int oldState){
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION_BLUETOOTH_ON);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }

    private void handleBluetoothOff(Context context, int oldState){
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION_BLUETOOTH_OFF);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }
}
