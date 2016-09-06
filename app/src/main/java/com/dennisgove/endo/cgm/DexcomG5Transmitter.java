package com.dennisgove.endo.cgm;

import com.dennisgove.endo.bluetooth.BleDevice;

/**
 * Created by dennis on 8/29/16.
 */
public class DexcomG5Transmitter extends BleDevice implements Transmitter {

    private final String TAG = this.getClass().getSimpleName();

    public DexcomG5Transmitter() {
    }

    @Override
    public void stop() {

    }
}
