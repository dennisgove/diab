/*
 * Copyright 2016 Dennis Gove
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dennisgove.endo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dennisgove.endo.EndoApplication;
import com.dennisgove.endo.R;
import com.dennisgove.endo.ble.BleController;
import com.dennisgove.endo.cgm.CgmTransmitter;
import com.dennisgove.endo.cgm.DexcomG5Transmitter;
import com.dennisgove.endo.comm.BroadcastReceiverCollection;
import com.dennisgove.endo.comm.EndoBroadcastReceiver;

import javax.inject.Inject;
import javax.inject.Named;

public class EndoManager extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private final int ALERT_ID_BLE_OFF = 1;

    private Binder binder;
    private BroadcastReceiverCollection broadcastReceivers;
    private CgmTransmitter cgmTransmitter;

    private EndoApplication endoApplication;

    @Inject @Named("applicationContext")
    public Context applicationContext;

    @Inject
    public BleController bleController;

    @Override
    public void onCreate(){
        super.onCreate();

        endoApplication = (EndoApplication)getApplication();
        endoApplication.getEndoManagerComponent().inject(this);

        broadcastReceivers = new BroadcastReceiverCollection(endoApplication);
        cgmTransmitter = new DexcomG5Transmitter(endoApplication);

        if(!bleController.isRunning()){
            bleController.start();
        }

        // Whenever bluetooh state changes, if off then the manager will show an alert to the user
        broadcastReceivers.register(new EndoBroadcastReceiver(BleController.ACTION_CONNECTION_CHANGED) {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast for '" + intent.getAction() + "'");

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // Cancel any existing alert
                notificationManager.cancel(ALERT_ID_BLE_OFF);

                if(!bleController.isBluetoothEnabled()){
                    Notification.Builder builder =
                            new Notification.Builder(context)
                                    .setSmallIcon(R.drawable.and)
                                    .setPriority(Notification.PRIORITY_HIGH)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setContentTitle("Endo requires Bluetooth")
                                    .setContentText("Bluetooth is required for Endo to function.");

                    notificationManager.notify(ALERT_ID_BLE_OFF, builder.build());
                }
            }
        });

        Log.d(TAG, "EndoManager was created");
    }

    @Override
    public void onDestroy() {
        broadcastReceivers.close();
        bleController.stop();
        Log.d(TAG, "EndoManager was destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Service will continue to run even if the app is killed
        Log.d(TAG, "EndoManager has started (sticky)");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(null == binder){
            binder = new Binder();
        }

        return binder;
    }

    public void connectToCgm(){

        Log.i(TAG, "Connecting to transmitter");
        cgmTransmitter.connectToTransmitter("40A90B");

//        Log.i(TAG, "Starting device scan");
//
//        final BluetoothLeScanner scanner = bleAdapter.getBluetoothLeScanner();
//        ScanSettings settings = new ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .build();
//        List<ScanFilter> filters = new ArrayList<>();
//        filters.add(new ScanFilter.Builder().setDeviceName("Dexcom0B").build());
//
//        scanner.startScan(filters, settings, new ScanCallback() {
//            @Override
//            public void onScanResult(int callbackType, ScanResult result) {
//                super.onScanResult(callbackType, result);
//
//                // Get real device from adapter
//                BluetoothDevice remoteDevice = bleAdapter.getRemoteDevice(result.getDevice().getAddress());
//                scanner.stopScan(new ScanCallback() {
//                    @Override
//                    public void onScanFailed(int errorCode) {
//                        Log.e(TAG, "Stop of scanning failed with code " + errorCode);
//                    }
//                });
//
//                cgmTransmitter.connectToRemote(remoteDevice);
//
//            }
//
//            @Override
//            public void onScanFailed(int errorCode) {
////                super.onScanFailed(errorCode);
//
//                Log.e(TAG, "ble Scan failed with code " + errorCode);
//            }
//        });
    }

    public BleController getBleController(){
        return bleController;
    }

    public class Binder extends android.os.Binder {
        private final EndoManager service;

        public Binder(){
            this.service = EndoManager.this;
        }

        public boolean isBluetoothCapable(){
            return service.getBleController().isBluetoothCapable();
        }

        public boolean isBluetoothEnabled(){
            return service.getBleController().isBluetoothEnabled();
        }

        public void askUserTurnBluetoothOn(){
            service.getBleController().askUserTurnBluetoothOn();
        }

        public void connectToCgm(){
            service.connectToCgm();
        }
    }

}
