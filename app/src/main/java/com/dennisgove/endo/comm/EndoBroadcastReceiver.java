package com.dennisgove.endo.comm;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by dennis on 9/9/16.
 */
public abstract class EndoBroadcastReceiver extends BroadcastReceiver {
    private IntentFilter intentFilter;

    public EndoBroadcastReceiver(String action, String... additionalActions) {
        intentFilter = new IntentFilter(action);

        for(String additionalAction : additionalActions){
            intentFilter.addAction(additionalAction);
        }
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }
}
