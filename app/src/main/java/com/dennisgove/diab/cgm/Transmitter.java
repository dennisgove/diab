package com.dennisgove.diab.cgm;

import com.dennisgove.diab.bluetooth.BleMessage;

/**
 * Created by dennis on 8/29/16.
 */
public interface Transmitter {

    void connect(String transmitterId);
    void disconnect();
    void reconnect();
    boolean isConnected();

    void sendMessage(BleMessage message);

}
