import os
import cv2
import numpy
import image_utils

def parseImageWithMask(img, mask, show, name):
    target = img.copy()

    med = cv2.GaussianBlur(target, (5, 5), 1.2)
    image_utils.applyMask(med, mask)
    med = cv2.cvtColor(med, cv2.COLOR_BGR2GRAY)

    edges = cv2.Canny(med,100,200)

    #cv2.imshow("hi1", edges)
    #cv2.waitKey(0)

    fgthres = cv2.threshold(edges.copy(), 200, 255, cv2.THRESH_BINARY)[1]

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))
    closing = cv2.morphologyEx(fgthres, cv2.MORPH_CLOSE, kernel)
    opening = cv2.morphologyEx(closing, cv2.MORPH_OPEN, kernel)
    dilation = cv2.dilate(opening, kernel, iterations=2)

    img_dilation = dilation

    vehicles = image_utils.findVehicles(img_dilation)

    stau = len(vehicles) > 10

    info = {}
    info["jam"] = stau
    info["vehicles"] = 0

    return (info, target)

def parseImage(img, masks, show, camera):
    left = parseImageWithMask(img, masks[0], show, camera + " left")
    right = parseImageWithMask(img, masks[1], show, camera + " right")

    #cv2.imshow("hi1", left[1])
    #cv2.imshow("hi2", right[1])
    #cv2.waitKey(0)

    return (left[0], right[0])

class ImageProcessor:
    def __init__(self, camera):
        self.camera = camera
        self.masks = image_utils.loadMasks(camera)

    def processRaw(self, image, show = False):
        data = numpy.fromstring(image, dtype=numpy.uint8)
        img = cv2.imdecode(data, 1)
        return self.process(img, show)

    def process(self, image, show = False):
        return parseImage(image, self.masks, show, self.camera)
        #cv2.imshow("hi - " + self.camera, img)
        #cv2.waitKey(0)
