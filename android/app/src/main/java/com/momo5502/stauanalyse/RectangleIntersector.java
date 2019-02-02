package com.momo5502.stauanalyse;

import org.opencv.core.Rect;

public class RectangleIntersector {
    private static boolean isBetween(int p1, int p2, int t) {
        return t >= Math.min(p1, p2) && t <= Math.max(p1, p2);
    }

    public static boolean hasIntersection(Rect c1, Rect c2) {

        int a_x1 = c1.x;
        int a_x2 = c1.x + c1.width;

        int a_y1 = c1.y;
        int a_y2 = c1.y + c1.height;

        int b_x1 = c2.x;
        int b_x2 = c2.x + c2.width;

        int b_y1 = c2.y;
        int b_y2 = c2.y + c2.height;

        boolean has_x_intersection = (isBetween(a_x1, a_x2, b_x1) || isBetween(a_x1, a_x2, b_x2));
        boolean has_y_intersection = (isBetween(a_y1, a_y2, b_y1) || isBetween(a_y1, a_y2, b_y2));

        return has_x_intersection && has_y_intersection;
    }
}
