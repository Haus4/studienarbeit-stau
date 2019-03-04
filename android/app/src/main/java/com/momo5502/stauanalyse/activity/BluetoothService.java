package com.momo5502.stauanalyse.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.momo5502.stauanalyse.bluetooth.BluetoothDeviceChecker;

public class BluetoothService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDeviceChecker.isConnectedToStoredDevice()) {
            Intent app = new Intent(context, MainActivity.class);
            context.startActivity(app);
        }
    }
}
