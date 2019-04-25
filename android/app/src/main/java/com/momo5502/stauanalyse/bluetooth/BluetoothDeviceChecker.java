package com.momo5502.stauanalyse.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class BluetoothDeviceChecker {
    private static Gson gson = new Gson();
    private static final ReentrantLock lock = new ReentrantLock();

    public static boolean isStoredDevice(BluetoothDevice device) {
        Set<StoredBluetoothDevice> storedDevices = getStoredDevices();
        return storedDevices.contains(new StoredBluetoothDevice(device));
    }

    public static Set<BluetoothDevice> getPairedDevices() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    }

    public static Set<StoredBluetoothDevice> getStoredDevices() {
        lock.lock();
        Set<StoredBluetoothDevice> set = new HashSet<>();
        try {
            String data = readFile();
            if (data != null) {
                List<StoredBluetoothDevice> list = gson.fromJson(data, new TypeToken<ArrayList<StoredBluetoothDevice>>() {
                }.getType());
                set.addAll(list);
            }
        } finally {
            lock.unlock();
        }

        return set;
    }

    public static void storeDevice(BluetoothDevice bluetoothDevice) {
        storeDevice(new StoredBluetoothDevice(bluetoothDevice));
    }

    public static void removeDevice(BluetoothDevice bluetoothDevice) {
        removeDevice(new StoredBluetoothDevice(bluetoothDevice));
    }

    public static void storeDevice(StoredBluetoothDevice bluetoothDevice) {
        lock.lock();
        try {
            Set<StoredBluetoothDevice> storedDevices = getStoredDevices();
            storedDevices.add(bluetoothDevice);

            String data = gson.toJson(storedDevices);
            writeFile(data);
        } finally {
            lock.unlock();
        }
    }

    public static void removeDevice(StoredBluetoothDevice bluetoothDevice) {
        lock.lock();
        try {
            Set<StoredBluetoothDevice> storedDevices = getStoredDevices();
            storedDevices.remove(bluetoothDevice);

            String data = gson.toJson(storedDevices);
            writeFile(data);
        } finally {
            lock.unlock();
        }
    }

    private static String readFile() {
        try {
            File file = new File(getPath());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                return new String(data, "UTF-8");
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }

        return null;
    }

    private static void writeFile(String data) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getPath()))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPath() {
        return new File(Environment.getExternalStorageDirectory(), "stau.json").getAbsolutePath();
    }
}
