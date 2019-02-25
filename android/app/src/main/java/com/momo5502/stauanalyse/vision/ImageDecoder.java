package com.momo5502.stauanalyse.vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageDecoder {

    Mat decode(byte[] data) {
        return decode(data, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    Mat decode(byte[] data, int codec) {
        return Imgcodecs.imdecode(new MatOfByte(data), codec);
    }
}
