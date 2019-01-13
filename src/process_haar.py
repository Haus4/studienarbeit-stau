import os
import cv2
import numpy
import image_utils

def parseImageWithMask(img, mask, show, name, classifier):
    target = img.copy()

    image_utils.applyMask(target, mask)
    target = cv2.cvtColor(target, cv2.COLOR_BGR2GRAY)
    cars = classifier.detectMultiScale(target, 1.1, 1)
	
    stau = len(cars) > 10

    info = {}
    info["jam"] = stau
    info["vehicles"] = 0

    return (info, target)

def parseImage(img, masks, show, camera, classifier):
    left = parseImageWithMask(img, masks[0], show, camera + " left", classifier)
    right = parseImageWithMask(img, masks[1], show, camera + " right", classifier)

    #cv2.imshow("hi1", left[1])
    #cv2.imshow("hi2", right[1])
    #cv2.waitKey(0)

    return (left[0], right[0])

class ImageProcessor:
    def __init__(self, camera):
        self.camera = camera
        self.classifier = cv2.CascadeClassifier('old/cars.xml')
        self.masks = image_utils.loadMasks(camera)

    def processRaw(self, image, show = False):
        data = numpy.fromstring(image, dtype=numpy.uint8)
        img = cv2.imdecode(data, 1)
        return self.process(img, show)

    def process(self, image, show = False):
        return parseImage(image, self.masks, show, self.camera, self.classifier)
        #cv2.imshow("hi - " + self.camera, img)
        #cv2.waitKey(0)
