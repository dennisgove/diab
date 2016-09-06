package com.dennisgove.endo.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.dennisgove.endo.R;
import com.dennisgove.endo.service.EndoCommunicationService;

import java.util.LinkedList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    // UI objects
    private TextView glucoseValue;

    private List<EndoBroadcastReceiver> registeredReceivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Get references to all UI objects
        glucoseValue = (TextView)findViewById(R.id.glucoseValue);

        Intent intent = new Intent(this, EndoCommunicationService.class);
        startService(intent);

        glucoseValue.setText("Glucose");

        registeredReceivers = new LinkedList<>();
        registerReceiver(new BluetoothOnHandler());
        registerReceiver(new BluetoothOffHandler());

        Log.i(TAG, "After creation");
    }

    private void registerReceiver(EndoBroadcastReceiver receiver){
        registerReceiver(receiver, receiver.getIntentFilter());
        registeredReceivers.add(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy called");

        for(BroadcastReceiver receiver : registeredReceivers){
            unregisterReceiver(receiver);
        }

    }

    private abstract class EndoBroadcastReceiver extends BroadcastReceiver{
        private IntentFilter intentFilter;

        public EndoBroadcastReceiver(String action){
            intentFilter = new IntentFilter(action);
        }

        public IntentFilter getIntentFilter(){
            return intentFilter;
        }
    }

    private class BluetoothOnHandler extends EndoBroadcastReceiver {

        public BluetoothOnHandler(){
            super(EndoCommunicationService.BROADCAST_ACTION_BLUETOOTH_ON);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification.Builder mBuilder =
                    new Notification.Builder(context)
                            .setSmallIcon(R.drawable.and)
                            .setContentTitle("Bluetooth change")
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setSound(sound)
                            .setContentText("Bluetooth is on!");
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(1);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    private class BluetoothOffHandler extends EndoBroadcastReceiver {

        public BluetoothOffHandler(){
            super(EndoCommunicationService.BROADCAST_ACTION_BLUETOOTH_OFF);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification.Builder mBuilder =
                    new Notification.Builder(context)
                            .setSmallIcon(R.drawable.and)
                            .setContentTitle("Bluetooth change")
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setSound(sound)
                            .setContentText("Bluetooth is off!");
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(1);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

}
