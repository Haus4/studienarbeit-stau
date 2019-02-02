package com.momo5502.stauanalyse.vision;

import com.momo5502.stauanalyse.util.RectangleIntersector;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ContourParser {

    private int threshold;

    public ContourParser(int threshold) {
        this.threshold = threshold;
    }


    public List<Rect> parse(List<MatOfPoint> contours) {
        List<Rect> rects = convert(contours);
        rects = merge(rects);
        return filter(rects);
    }

    private List<Rect> convert(List<MatOfPoint> contours) {
        return contours.stream().map(Imgproc::boundingRect).collect(Collectors.toList());
    }

    private List<Rect> filter(List<Rect> contours) {
        return contours.stream().filter(c -> c.width >= threshold && c.height >= threshold).collect(Collectors.toList());
    }

    private List<Rect> merge(List<Rect> contours) {
        List<Rect> result = new LinkedList<>(contours);

        boolean hadChange;
        do {
            hadChange = false;
            for (int i = 0; i < result.size() && !hadChange; ++i) {
                for (int j = 0; j < result.size() && !hadChange; ++j) {
                    if (i == j) continue;

                    Rect first = result.get(i);
                    Rect second = result.get(j);

                    if (RectangleIntersector.hasIntersection(first, second)) {
                        int x = Math.min(first.x, second.x);
                        int y = Math.min(first.y, second.y);

                        int x_max = Math.max(first.x + first.width, second.x + second.width);
                        int y_max = Math.max(first.y + first.height, second.y + second.height);

                        Rect newRect = new Rect(x, y, x_max - x, y_max - y);

                        result.remove(first);
                        result.remove(second);
                        result.add(newRect);

                        hadChange = true;
                    }
                }
            }
        } while (hadChange);

        return result;
    }
}
