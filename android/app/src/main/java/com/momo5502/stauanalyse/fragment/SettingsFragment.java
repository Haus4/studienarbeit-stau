package com.momo5502.stauanalyse.fragment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.momo5502.stauanalyse.R;
import com.momo5502.stauanalyse.bluetooth.BluetoothDeviceChecker;
import com.momo5502.stauanalyse.bluetooth.StoredBluetoothDevice;
import com.momo5502.stauanalyse.view.BluetoothRow;

import java.util.Set;

public class SettingsFragment extends Fragment {

    private Thread thread;
    private Set<BluetoothDevice> devices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        thread = new Thread(() -> loop());
        thread.start();

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    private void loop() {
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            updateDevices();
        }
    }

    private void updateDevices() {
        Set<BluetoothDevice> newDevices = BluetoothDeviceChecker.getDevices();

        if (devices != null && newDevices != null) {
            if (devices.containsAll(newDevices) && newDevices.containsAll(devices)) {
                return;
            }
        }

        devices = newDevices;
        updateUI();
    }

    public void updateUI() {
        Context context = getContext();
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;

            new Handler(Looper.getMainLooper()).post(() -> {
                TableLayout bluetoothTable = activity.findViewById(R.id.bluetoothTable);
                TableLayout storedTable = activity.findViewById(R.id.storedTable);

                bluetoothTable.removeAllViews();
                storedTable.removeAllViews();

                Set<StoredBluetoothDevice> storedDevices = BluetoothDeviceChecker.getStoredDevices();

                if (devices != null) {
                    for (BluetoothDevice device : devices) {
                        bluetoothTable.addView(new BluetoothRow(activity, this, new StoredBluetoothDevice(device), true));
                    }
                }

                for (StoredBluetoothDevice device : storedDevices) {
                    storedTable.addView(new BluetoothRow(activity, this, device, false));
                }
            });
        }
    }
}