package com.momo5502.stauanalyse.activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.momo5502.stauanalyse.bluetooth.BluetoothDeviceChecker;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;

public class BluetoothService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDeviceChecker.isStoredDevice(device)) {
            Intent app = new Intent(context, MainActivity.class);
            app.putExtra("kill", ACTION_ACL_DISCONNECTED.equals(action));
            context.startActivity(app);
        }
    }
}
