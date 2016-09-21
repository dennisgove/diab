package com.dennisgove.endo.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dennisgove.endo.R;
import com.dennisgove.endo.comm.BroadcastReceiverCollection;
import com.dennisgove.endo.service.EndoManager;

public class StartActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    // UI objects
    private TextView glucoseValue;

    private BroadcastReceiverCollection broadcastReceivers;
    private EndoManager.Binder endoManagerBinder;
    private ServiceConnection endoManagerServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Get references to all UI objects
        glucoseValue = (TextView)findViewById(R.id.glucoseValue);
        glucoseValue.setText("Glucose");

        // TODO: delete this setup
        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endoManagerBinder.connectToCgm();
            }
        });

        broadcastReceivers = new BroadcastReceiverCollection(this);

        // Bind to the EndoManager
        Intent intent = new Intent(this, EndoManager.class);
        boolean isBound = bindService(intent, getEndoManagerServiceConnection(), BIND_AUTO_CREATE);

        Log.d(TAG, "StartActivity has created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        broadcastReceivers.close();
        unbindService(getEndoManagerServiceConnection());

        Log.d(TAG, "StartActivity has been destroyed");
    }

    @Override
    public void onStart(){

        super.onStart();

        Log.i(TAG, "Starting activity");
        requestPermissions(new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    private ServiceConnection getEndoManagerServiceConnection(){
        if(null == endoManagerServiceConnection){
            endoManagerServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder binder) {
                    Log.d(TAG, "Bound to EndoManager");
                    endoManagerBinder = (EndoManager.Binder)binder;

                    if(!endoManagerBinder.isBluetoothEnabled() && endoManagerBinder.isBluetoothCapable()){
                        Log.i(TAG, "Bluetooth is off - requesting user to turn it on");
                        endoManagerBinder.askUserTurnBluetoothOn();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    Log.d(TAG, "Unbound from EndoManager");
                    endoManagerBinder = null;
                }
            };
        }

        return endoManagerServiceConnection;
    }

}
