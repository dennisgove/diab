package com.dennisgove.endo.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by dennis on 9/1/16.
 */
public class BluetoothController {
    private final String TAG = this.getClass().getSimpleName();

    private Service parentService;

    private final BluetoothManager bleManager;
    private final BluetoothAdapter bleAdapter;
    private BluetoothStateChangedReceiver stateChangedReceiver;

    public BluetoothController(Service parentService) {
        this.parentService = parentService;

        bleManager = (BluetoothManager) parentService.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();
    }

    public void start() {
        registerStateChangedReceiver();
    }

    public void stop() {
        unregisterStateChangedReceiver();
    }

    private void registerStateChangedReceiver() {
        stateChangedReceiver = new BluetoothStateChangedReceiver(bleAdapter.getState());
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        parentService.registerReceiver(stateChangedReceiver, filter);
    }

    private void unregisterStateChangedReceiver() {
        if(null != stateChangedReceiver){
            parentService.unregisterReceiver(stateChangedReceiver);
        }
    }

    public boolean isBluetoothCapable() {
        return null != bleAdapter;
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothCapable() && bleAdapter.isEnabled();
    }

    public void enableBluetooth() {

        if(isBluetoothEnabled()){
            Log.i(TAG, "Bluetooth already enabled on device - nothing to do");
        }else if(isBluetoothCapable()){
            Log.i(TAG, "Requesting that bluetooth be enabled on device");

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if(bleAdapter == null || !bleAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                parentService.startActivities(new Intent[]{enableBtIntent});
            }
        }else{
            Log.i(TAG, "Device is not capable of bluetooth communication - nothing can be done");
        }

    }

    public void registerStateChangedHandler(int onState, BluetoothStateChangedHandler handler){
        stateChangedReceiver.registerHandler(onState, handler);
    }

    /**
     * Provides receiver for any bluetooth state changes which occur and executes
     * registered handlers
     */
    private class BluetoothStateChangedReceiver extends BroadcastReceiver {
        private final String TAG = this.getClass().getSimpleName();

        private HashMap<Integer, BluetoothStateChangedHandler> handlers;
        private int currentState;

        public BluetoothStateChangedReceiver(int initialState) {
            currentState = initialState;
            handlers = new HashMap<>();

            Log.i(TAG, String.format(Locale.ROOT, "Created new instance with currentState=%d", currentState));
        }

        public void registerHandler(int onState, BluetoothStateChangedHandler handler) {
            Log.i(TAG, String.format(Locale.ROOT, "Registering handler for newState=%d", onState));

            Object existingHandler = handlers.put(onState, handler);
            if(null != existingHandler){
                Log.d(TAG, String.format(Locale.ROOT, "Existing handler for newState=%d has been replaced", onState));
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // atm, we only require state changes, though there's something to be said
            // for possibly caring about other actions. Right now we don't care.
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                Log.d(TAG, String.format(Locale.ROOT, "Handling action '%s'", action));

                int oldState = currentState;
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                currentState = newState;

                if(handlers.containsKey(newState)){
                    Log.i(TAG, String.format(Locale.ROOT, "Handling newState=%d (oldState=%d)", newState, oldState));
                    handlers.get(newState).handle(context, oldState, newState);
                }else{
                    Log.i(TAG, String.format(Locale.ROOT, "newState=%d does not have a registered handler - being ignored (oldState=%d)", newState, oldState));
                }
            }else{
                Log.d(TAG, String.format(Locale.ROOT, "Action '%s' is not being watched and is being ignored", action));
            }
        }
    }
}
