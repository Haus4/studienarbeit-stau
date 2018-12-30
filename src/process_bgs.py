import os
import cv2
import numpy
import image_utils

def parseImageWithMask(img, bgs, mask):
    target = img.copy()

    med = cv2.GaussianBlur(target, (5, 5), 1.2)
    image_utils.applyMask(med, mask)
    image_utils.applyMask(target, mask)

    image = bgs.apply(med)

    fgthres = cv2.threshold(image.copy(), 200, 255, cv2.THRESH_BINARY)[1]

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))
    closing = cv2.morphologyEx(fgthres, cv2.MORPH_CLOSE, kernel)
    opening = cv2.morphologyEx(closing, cv2.MORPH_OPEN, kernel)
    dilation = cv2.dilate(opening, kernel, iterations=2)

    img_dilation = dilation

    vehicles = image_utils.findVehicles(img_dilation)

    for vehicle in vehicles:
        cv2.rectangle(target, (vehicle[0], vehicle[1]), (vehicle[0] +
                                                         vehicle[2], vehicle[1] + vehicle[3]), (0, 255, 0))

    #text = "Kein Stau"
    #color = (0, 255, 0)
    stau = False
    if len(vehicles) >= 10:
        #text = "Stau"
        #color = (0,0,255)
        stau = True
    #cv2.putText(target,text,(10,100), cv2.FONT_HERSHEY_SIMPLEX, 2,color,2,cv2.LINE_AA)

    info = {}
    info["jam"] = stau
    info["vehicles"] = len(vehicles)

    return (info, target)

def parseImage(img, bgs, bgs2, masks):
    left = parseImageWithMask(img, bgs, masks[0])
    right = parseImageWithMask(img, bgs2, masks[1])

    #cv2.imshow("hi1", left[1])
    #cv2.imshow("hi2", right[1])
    #cv2.waitKey(0)

    return (left[0], right[0])

class ImageProcessor:
    def __init__(self, camera):
        self.camera = camera
        self.bgs = [
            cv2.bgsegm.createBackgroundSubtractorMOG(),
            cv2.bgsegm.createBackgroundSubtractorMOG()
        ]

        self.masks = image_utils.loadMasks(camera)

    def processRaw(self, image):
        data = numpy.fromstring(image, dtype=numpy.uint8)
        img = cv2.imdecode(data, 1)
        return self.process(img)

    def process(self, image):
        return parseImage(image, self.bgs[0], self.bgs[1], self.masks)
        #cv2.imshow("hi - " + self.camera, img)
        #cv2.waitKey(0)
