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

import java.util.UUID;

// Taken from https://github.com/StephenBlackWasAlreadyTaken/xDrip-Experimental/blob/master/app/src/main/java/com/eveningoutpost/dexdrip/G5Model/BluetoothServices.java
public class BleServices {
    //Transmitter Service UUIDs
    public static final UUID DeviceInfo = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");

    public static final UUID Advertisement = UUID.fromString("0000FEBC-0000-1000-8000-00805F9B34FB");
    public static final UUID CGMService = UUID.fromString("F8083532-849E-531C-C594-30F1F86A4EA5");
    public static final UUID ServiceB = UUID.fromString("F8084532-849E-531C-C594-30F1F86A4EA5");

    //DeviceInfoCharacteristicUUID, Read, DexcomUN
    public static final UUID ManufacturerNameString = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");

    //CGMServiceCharacteristicUUID
    public static final UUID Communication = UUID.fromString("F8083533-849E-531C-C594-30F1F86A4EA5");
    public static final UUID Control = UUID.fromString("F8083534-849E-531C-C594-30F1F86A4EA5");
    public static final UUID Authentication = UUID.fromString("F8083535-849E-531C-C594-30F1F86A4EA5");
    public static final UUID ProbablyBackfill = UUID.fromString("F8083536-849E-531C-C594-30F1F86A4EA5");

    //ServiceBCharacteristicUUID
    public static final UUID CharacteristicE = UUID.fromString("F8084533-849E-531C-C594-30F1F86A4EA5");
    public static final UUID CharacteristicF = UUID.fromString("F8084534-849E-531C-C594-30F1F86A4EA5");

    //CharacteristicDescriptorUUID
    public static final UUID CharacteristicUpdateNotification = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

}
