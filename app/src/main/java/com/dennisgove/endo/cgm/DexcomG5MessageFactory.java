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

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DexcomG5MessageFactory {
    private static String TAG = DexcomG5MessageFactory.class.getSimpleName();

    private static byte[] generateSingleUseToken(){
        byte[] uuidBytes = new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        UUID uuid = UUID.nameUUIDFromBytes(uuidBytes);

        try {
            uuidBytes = uuid.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to generate a single-use token from an empty byte array");
        }

        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.put(uuidBytes, 0, 8);
        return bb.array();
    }

    public static byte[] generateAuthenticationRequest(){
        byte opCode = 0x1;
        byte endByte = 0x2;

        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(opCode);
        buffer.put(generateSingleUseToken());
        buffer.put(endByte);

        return buffer.array();
    }

    public static byte[] generateAuthenticationChallenge(byte[] data, String transmitterId){
        byte opCode = 0x4;

        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put(opCode);
        buffer.put(calculateHash(data, transmitterId));

        return buffer.array();
    }

    private static byte[] calculateHash(byte[] data, String transmitterId){
        if (data.length != 8) {
            android.util.Log.e("Decrypt", "Data length should be exactly 8.");
            return null;
        }

        try{
            byte[] key = ("00" + transmitterId + "00" + transmitterId).getBytes("UTF-8");

            byte[] doubleData;
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.put(data);
            bb.put(data);

            doubleData = bb.array();

            Cipher aesCipher;

            aesCipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] aesBytes = aesCipher.doFinal(doubleData, 0, doubleData.length);

            bb = ByteBuffer.allocate(8);
            bb.put(aesBytes, 0, 8);

            return bb.array();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            Log.e(TAG, "Failed to calculate hash", e);
        }

        return null;
    }
}
