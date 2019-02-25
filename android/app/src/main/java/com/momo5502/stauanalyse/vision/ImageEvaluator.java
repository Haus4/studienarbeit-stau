package com.momo5502.stauanalyse.vision;

import org.opencv.core.Rect;

import com.momo5502.stauanalyse.camera.CameraImage;

import org.opencv.bgsegm.Bgsegm;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;

public class ImageEvaluator {
    ContourParser contourParser;
    BackgroundSubtractor backgroundSubtractor;
    ImageDecoder imageDecoder;
    Material mask;

    public ImageEvaluator(byte[] mask) {
        imageDecoder = new ImageDecoder();
        contourParser = new ContourParser(4);
        backgroundSubtractor = Bgsegm.createBackgroundSubtractorMOG();
        this.mask = new Material(imageDecoder.decode(mask, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE));
        if (this.mask.getMat().height() <= 0 || this.mask.getMat().width() <= 0) {
            throw new RuntimeException("Invalid mask");
        }
    }

    public EvaluatedImage evaluate(CameraImage image) {
        EvaluatedImage currentImage = train(image);
        Mat newMask = applyMorphology(currentImage.getMask().getMat());

        List<Rect> objects = findObjects(newMask);
        Mat scene = paintObjects(currentImage.getImage().getMat(), objects);

        return new EvaluatedImage(newMask, scene, objects);
    }

    private Mat paintObjects(Mat image, List<Rect> objects) {
        Mat result = image.clone();
        Scalar color = new Scalar(0, 200, 0);

        for (Rect object : objects) {
            Point start = new Point(object.x, object.y);
            Point end = new Point(object.x + object.width, object.y + object.height);
            Imgproc.rectangle(result, start, end, color);
        }

        return result;
    }

    private List<Rect> findObjects(Mat mask) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        return contourParser.parse(contours);
    }

    public EvaluatedImage train(CameraImage image) {
        Mat eval = new Mat();
        Mat scene = parseImage(image);

        Mat blurredScene = blur(scene);

        Mat maskedScene = new Mat();
        blurredScene.copyTo(maskedScene, mask.getMat());

        backgroundSubtractor.apply(maskedScene, eval);

        return new EvaluatedImage(eval, scene);
    }

    private Mat parseImage(CameraImage image) {
        return imageDecoder.decode(image.getData());
    }

    private Mat blur(Mat image) {
        Mat dst = new Mat();
        Imgproc.blur(image, dst, new Size(2, 2));
        return dst;
    }

    private Mat applyMorphology(Mat mask) {
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));

        Mat closed = new Mat();
        Imgproc.morphologyEx(mask, closed, MORPH_CLOSE, kernel);

        Mat opened = new Mat();
        Imgproc.morphologyEx(closed, opened, MORPH_OPEN, kernel);

        Mat dilated = new Mat();
        Imgproc.dilate(opened, dilated, kernel, new Point(-1, -1), 2);

        return dilated;
    }
}
