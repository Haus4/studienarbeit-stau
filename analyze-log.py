import os
import cv2
import numpy

def main():
    frame = cv2.imread("eval/jam/20171108-1054-A.png")
    frame = cv2.GaussianBlur(frame, (3, 3), 0)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    ddepth = cv2.CV_64F
    kernel_size = 3
    dst = cv2.Laplacian(gray, ddepth, 3)
    #sobelx = cv2.Sobel(gray,cv2.CV_64F,1,0,ksize=3)  # x
    #sobely = cv2.Sobel(gray,cv2.CV_64F,0,1,ksize=3)  # y
 
    #ncars = 0
    #for (x,y,w,h) in cars:
    #    cv2.rectangle(frame,(x,y),(x+w,y+h),(0,0,255),2)
    #    ncars = ncars + 1

    cv2.imshow("hi", dst)

    cv2.waitKey(0)
    cv2.destroyAllWindows()

main()
