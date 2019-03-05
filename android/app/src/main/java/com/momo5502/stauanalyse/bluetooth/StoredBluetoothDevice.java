package com.momo5502.stauanalyse.bluetooth;

import android.bluetooth.BluetoothDevice;

public class StoredBluetoothDevice {
    private String name;
    private String address;

    public StoredBluetoothDevice() {

    }

    public StoredBluetoothDevice(BluetoothDevice bluetoothDevice) {
        setName(bluetoothDevice.getName());
        setAddress(bluetoothDevice.getAddress());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StoredBluetoothDevice) {
            StoredBluetoothDevice device = (StoredBluetoothDevice) obj;
            return (device.getAddress() == null && getAddress() == null) || device.getAddress().equals(getAddress());
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (address != null) {
            return address.hashCode();
        }

        return 0;
    }
}
