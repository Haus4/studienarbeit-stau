package com.momo5502.stauanalyse.camera;

import java.util.Date;

public class CameraImage {
    private Date time;
    private byte[] data;

    public CameraImage(Date time, byte[] data) {
        this.time = time;
        this.data = data;
    }

    public Date getTime() {
        return time;
    }

    public byte[] getData() {
        return data;
    }
}
