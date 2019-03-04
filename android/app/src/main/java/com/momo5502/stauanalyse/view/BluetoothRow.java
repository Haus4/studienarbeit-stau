package com.momo5502.stauanalyse.view;

import android.content.Context;

import com.momo5502.stauanalyse.bluetooth.BluetoothDeviceChecker;
import com.momo5502.stauanalyse.bluetooth.StoredBluetoothDevice;
import com.momo5502.stauanalyse.fragment.SettingsFragment;

public class BluetoothRow extends TextRow {
    private SettingsFragment settingsFragment;
    private StoredBluetoothDevice device;
    private boolean add;

    public BluetoothRow(Context context, SettingsFragment settingsFragment, StoredBluetoothDevice device, boolean add) {
        super(context);
        this.settingsFragment = settingsFragment;
        this.device = device;
        this.add = add;
        setText(device.getName());

        setOnClickListener(v -> onClick());
    }

    private void onClick() {
        if (add) {
            BluetoothDeviceChecker.storeDevice(device);
        } else {
            BluetoothDeviceChecker.removeDevice(device);
        }

        settingsFragment.updateUI();
    }
}
