import os
import cv2
import numpy
import image_utils

def parseImageWithMask(img, mask, show, name):
    target = img.copy()

    image_utils.applyMask(target, mask)
    target = cv2.cvtColor(target, cv2.COLOR_BGR2GRAY)
	
    colorThreshold = 40
	
    notBlack = numpy.sum(target > 0)
    countAbove = numpy.sum(target > colorThreshold)
    countBelow = notBlack - countAbove

    #for rows in target:
    #    for pixel in rows:
    #        if pixel != 0:
    #            if pixel > colorThreshold:
    #                countAbove += 1
    #            else:
    #                countBelow += 1
	
    stau = countBelow > countAbove

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
