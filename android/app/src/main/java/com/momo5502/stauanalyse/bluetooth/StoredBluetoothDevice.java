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
            return device.getName().equals(getName()) && device.getAddress().equals(getAddress());

        }/* else if(obj instanceof BluetoothDevice) {
            this.equals(new StoredBluetoothDevice((BluetoothDevice)obj));
        }*/

        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;

        if (name != null) {
            code += name.hashCode();
        }

        if (address != null) {
            code += address.hashCode();
        }

        return code;
    }
}
