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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;

import com.dennisgove.endo.EndoApplication;

import javax.inject.Inject;
import javax.inject.Named;

public class BluetoothAdapterWrapper implements BleAdapter {

    // injected
    EndoApplication endoApplication;
    @Inject @Named("applicationContext") Context applicationContext;

    private BluetoothAdapter adapter;

    public BluetoothAdapterWrapper(EndoApplication endoApplication){

        endoApplication.getEndoManagerComponent().inject(this);
        BluetoothManager manager = (BluetoothManager) applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
    }

    @Override
    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    @Override
    public BluetoothLeScanner getBluetoothLeScanner() {
        return adapter.getBluetoothLeScanner();
    }

    @Override
    public BluetoothDevice getRemoteDevice(String address) {
        return adapter.getRemoteDevice(address);
    }

    @Override
    public void cancelDiscovery() {
        adapter.cancelDiscovery();
    }
}
