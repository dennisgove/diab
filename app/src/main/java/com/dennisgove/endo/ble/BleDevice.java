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
package com.dennisgove.endo.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.dennisgove.endo.EndoApplication;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BleDevice {
    private final String TAG = this.getClass().getSimpleName();

    private final EndoApplication endoApplication;
    @Inject @Named("applicationContext") Context applicationContext;
    @Inject BleAdapter bleAdapter;

    private BluetoothDevice remoteDevice;


    public BleDevice(EndoApplication endoApplication){
        this.endoApplication = endoApplication;
        endoApplication.getEndoManagerComponent().inject(this);
    }

    protected BleAdapter getBleAdapter(){
        return bleAdapter;
    }

    /**
     * Begins a BLE scan for the device with the provided name. provided callback
     * will be called when a device with that name is called (or an error occurs).
     * This is a non-blocking call in that the scan will be started and caller will
     * get control back while scan is occurring.
     * @param deviceName BLE name of device, used as a ScanFilter
     * @param callback called when device is found or an error occurs
     */
    protected void scanForDevice(final String deviceName, final ScanCallback callback) {
        Log.i(TAG, "Scanning for deviceName = '" + deviceName + "'");

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setDeviceName(deviceName).build());
        final BluetoothLeScanner bleScanner = bleAdapter.getBluetoothLeScanner();

        bleScanner.startScan(filters, settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.i(TAG, "Scan for device '" + deviceName + "' got a result");

                // Stop ongoing scan
                bleScanner.stopScan(new ScanCallback() {
                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.e(TAG, "Failed to stop scan for device '" + deviceName + "'");
                    }
                });

                // Call user's handler
                callback.onScanResult(callbackType, result);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e(TAG, "Scan for device '" + deviceName + "' failed with errorCode=" + errorCode);

                // Stop ongoing scan
                bleScanner.stopScan(new ScanCallback() {
                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.e(TAG, "Failed to stop scan for device '" + deviceName + "'");
                    }
                });

                // Call user's handler
                callback.onScanFailed(errorCode);
            }
        });
    }

    protected void connectToDevice(BluetoothDevice device, BluetoothGattCallback callback){
        Log.i(TAG, "Connecting to device '" + device.getName() + "'");

        device.connectGatt(applicationContext, false, callback);
    }
}
