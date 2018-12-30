import os
import cv2
import numpy

def main():
    frame = cv2.imread("eval/jam/20171108-1054-A.png")
    frame = cv2.GaussianBlur(frame, (3, 3), 0)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    edges = cv2.Canny(gray,100,200)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))
    closing = cv2.morphologyEx(edges, cv2.MORPH_CLOSE, kernel)
    opening = cv2.morphologyEx(closing, cv2.MORPH_OPEN, kernel)
    dilation = cv2.dilate(opening, kernel, iterations=2)

    cv2.imshow("hi", opening)

    cv2.waitKey(0)
    cv2.destroyAllWindows()

main()
