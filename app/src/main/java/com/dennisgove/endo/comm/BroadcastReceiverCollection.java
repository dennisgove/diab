package com.dennisgove.endo.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dennis on 9/9/16.
 */
public class BroadcastReceiverCollection implements Closeable {
    private final String TAG = this.getClass().getSimpleName();

    private final Context context;
    private final List<BroadcastReceiver> receivers;

    public BroadcastReceiverCollection(Context context){
        this.context = context;
        receivers = new ArrayList<>();
    }

    public void register(EndoBroadcastReceiver receiver){
        context.registerReceiver(receiver, receiver.getIntentFilter());
        receivers.add(receiver);
    }

    public void register(BroadcastReceiver receiver, IntentFilter intentFilter){
        context.registerReceiver(receiver, intentFilter);
        receivers.add(receiver);
    }

    @Override
    public void close(){
        for(BroadcastReceiver receiver : receivers){
            context.unregisterReceiver(receiver);
        }
    }
}
