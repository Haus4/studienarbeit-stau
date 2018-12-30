import os
import cv2
import numpy

def main():
    face_cascade = cv2.CascadeClassifier('cars.xml')

    frame = cv2.imread("eval/jam/20171108-1054-A.png")
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    cars = face_cascade.detectMultiScale(gray, 1.1, 1)
 
    ncars = 0
    for (x,y,w,h) in cars:
        cv2.rectangle(frame,(x,y),(x+w,y+h),(0,0,255),2)
        ncars = ncars + 1

    cv2.imshow("hi", frame)

    cv2.waitKey(0)
    cv2.destroyAllWindows()

main()
