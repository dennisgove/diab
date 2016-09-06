package com.dennisgove.endo.bluetooth;

import android.content.Context;

/**
 * Created by dennis on 9/6/16.
 */

public interface BluetoothStateChangedHandler {
    void handle(Context context, int oldState, int newState);
}
