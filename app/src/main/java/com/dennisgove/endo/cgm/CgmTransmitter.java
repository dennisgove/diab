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

import com.dennisgove.endo.EndoApplication;
import com.dennisgove.endo.ble.BleDevice;

public abstract class CgmTransmitter extends BleDevice {

    private final String TAG = this.getClass().getSimpleName();

    public static final String STATE_SCANNING = "com.dennisgove.endo.cgm.CgmTransmitter.SCANNING";
    public static final String STATE_CONNECTING = "com.dennisgove.endo.cgm.CgmTransmitter.CONNECTING";
    public static final String STATE_CONNECTED = "com.dennisgove.endo.cgm.CgmTransmitter.CONNECTED";
    public static final String STATE_DISCONNECTED = "com.dennisgove.endo.cgm.CgmTransmitter.DISCONNECTED";

    private String state;
    private String transmitterId;

    public CgmTransmitter(EndoApplication endoApplication){
        super(endoApplication);

        state = STATE_DISCONNECTED;
    }

    abstract public void connectToTransmitter(String transmitterId);

    public final String getState(){
        return state;
    }

    public final boolean isConnected(){
        return STATE_CONNECTED == state;
    }

    protected final void setState(String state){
        this.state = state;
    }

}
