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

package com.dennisgove.endo.cgm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.dennisgove.endo.EndoApplication;
import com.dennisgove.endo.ble.BleServices;

import java.security.InvalidParameterException;

public class DexcomG5Transmitter extends CgmTransmitter {

    private final String TAG = this.getClass().getSimpleName();

    public DexcomG5Transmitter(EndoApplication endoApplication) {
        super(endoApplication);
    }

    @Override
    public void connectToTransmitter(final String transmitterId){
        Log.i(TAG, "Beginning connecting process for transmitter '" + transmitterId + "'");

        if(null == transmitterId || 6 != transmitterId.length()){
            Log.w(TAG, "Invalid transmitter id '" + transmitterId + "'");
            throw new InvalidParameterException("Transmitter Id '" + null == transmitterId ? "null" : transmitterId + "' is not valid");
        }

        setState(CgmTransmitter.STATE_SCANNING);
        scanForDevice(generateDeviceName(transmitterId), new ScanCallback() {
            private boolean gotAScanResult = false;

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e(TAG, "scanForDevice failed with code " + errorCode + " - connection state unchanged");
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if(gotAScanResult){
                    Log.i(TAG, "Already connecting - ignoring this can result");
                    return;
                }
                gotAScanResult = true;

                Log.i(TAG, "scanForDevice succeeded");

                setState(CgmTransmitter.STATE_CONNECTING);
                BluetoothDevice remoteDevice = getBleAdapter().getRemoteDevice(result.getDevice().getAddress());
                connectToDevice(remoteDevice, new BluetoothGattCallback() {
                    private BluetoothGattCharacteristic authCharacteristic;
                    private BluetoothGattCharacteristic controlCharacteristic;
                    private BluetoothGattCharacteristic commCharacteristic;

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        Log.i(TAG, "Connection state to trasmitter '" + transmitterId + "' has changed to state " + newState);

                        switch(newState){
                            case BluetoothProfile.STATE_CONNECTED:
                                Log.i(TAG, "Connected to transmitter '" + transmitterId + "'");
                                gatt.discoverServices();
                                break;

                            case BluetoothProfile.STATE_DISCONNECTED:
                                Log.i(TAG, "Disconnected from transmitter '" + transmitterId + "'");
                                break;

                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        Log.i(TAG, "onServicesDiscovered: " + status);

                        if(status == BluetoothGatt.GATT_SUCCESS){
                            Log.i(TAG, "Sending authentication request");
                            BluetoothGattService service = gatt.getService(BleServices.CGMService);
                            getBleAdapter().cancelDiscovery();
                            authCharacteristic = service.getCharacteristic(BleServices.Authentication);
                            controlCharacteristic = service.getCharacteristic(BleServices.Control);
                            commCharacteristic = service.getCharacteristic(BleServices.Communication);
                            getBleAdapter().cancelDiscovery();

                            authCharacteristic.setValue(DexcomG5MessageFactory.generateAuthenticationRequest());
                            gatt.writeCharacteristic(authCharacteristic);
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
                        Log.i(TAG, "WriteStatus: " + String.valueOf(status));
                    }

                        @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
                        Log.i(TAG, "ReadStatus: " + String.valueOf(status));

                        if (status == BluetoothGatt.GATT_SUCCESS) {
//                            Log.i(TAG, "CharBytes-or " + Arrays.toString(characteristic.getValue()));

                            byte[] buffer = characteristic.getValue();
                            byte code = buffer[0];

                            switch (code) {
                                case 5:
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
                                    break;

                                case 3:
                                    Log.i(TAG, "Sending authentication challenge request");
                                    characteristic.setValue(DexcomG5MessageFactory.generateAuthenticationChallenge(characteristic.getValue(), transmitterId));
                                    gatt.writeCharacteristic(characteristic);
                                    break;

                                default:
                                    break;
                            }

                        }
                    }
                });
            }
        });
    }

    private String generateDeviceName(String transmitterId){
        return "Dexcom" + transmitterId.substring(4); // last 2 characters of a 6 character id
    }

    private class TransmitterGattCallback extends BluetoothGattCallback{
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    }
}
