package com.momo5502.stauanalyse.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.momo5502.stauanalyse.R;
import com.momo5502.stauanalyse.backend.MultiImageLoader;
import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraImage;
import com.momo5502.stauanalyse.camera.CameraImageFetcher;
import com.momo5502.stauanalyse.speech.Speaker;
import com.momo5502.stauanalyse.vision.ContourParser;
import com.momo5502.stauanalyse.vision.EvaluatedImage;
import com.momo5502.stauanalyse.vision.ImageEvaluator;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.bgsegm.BackgroundSubtractorMOG;
import org.opencv.bgsegm.Bgsegm;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;

public class MainActivity extends FragmentActivity {

    private Speaker speaker;
    private ImageEvaluator imageEvaluator;
    private MultiImageLoader multiImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();

        multiImageLoader = new MultiImageLoader();
        speaker = new Speaker(getApplicationContext());
        imageEvaluator = new ImageEvaluator();

        run();
    }

    public void setOriginalImage(final Bitmap bitmap) {
        setImage(bitmap, R.id.og);
    }

    public void setAnalyzedImage(final Bitmap bitmap) {
        setImage(bitmap, R.id.mog);
    }

    private void setImage(final Bitmap bitmap, final int id) {
        new Handler(Looper.getMainLooper()).post(() -> {
            ImageView iv = findViewById(id);
            iv.setImageBitmap(bitmap);
        });
    }

    private void handleImages(List<CameraImage> images, Exception error) {
        if (images == null || images.isEmpty()) return;

        for (int i = 0; i < images.size(); ++i) {
            CameraImage image = images.get(i);

            if (i + 1 == images.size()) {
                EvaluatedImage evaluatedImage = imageEvaluator.evaluate(image);

                int cars = evaluatedImage.getObjects().size();
                speaker.speak(cars + " Autos erkannt.");

                if (cars > 40) {
                    speaker.speak("Es ist Stau.");
                } else {
                    speaker.speak("Es ist kein Stau.");
                }

                setOriginalImage(evaluatedImage.getImage().getBitmap());
                setAnalyzedImage(evaluatedImage.getMask().getBitmap());
            } else {
                imageEvaluator.train(image);
            }
        }
    }

    private void run() {
        new Thread(() -> {
            CameraImageFetcher cameraImageFetcher = new CameraImageFetcher("KA061");
            cameraImageFetcher.setCallback(((value, error) -> handleImages(value, error)));

            while (true) {
                cameraImageFetcher.work();

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }
}
