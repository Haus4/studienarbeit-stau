import os
import cv2
import numpy

def isBetween(p1, p2, t):
    return t >= min(p1, p2) and t <= max(p1, p2)

def hasIntersection(c1, c2):
    a_x1 = c1[0]
    a_x2 = c1[0] + c1[2]

    a_y1 = c1[1]
    a_y2 = c1[1] + c1[3]

    b_x1 = c2[0]
    b_x2 = c2[0] + c2[2]

    b_y1 = c2[1]
    b_y2 = c2[1] + c2[3]

    has_x_intersection = (isBetween(a_x1, a_x2, b_x1) or isBetween(a_x1, a_x2, b_x2))
    has_y_intersection = (isBetween(a_y1, a_y2, b_y1) or isBetween(a_y1, a_y2, b_y2))

    return has_x_intersection and has_y_intersection


def mergeOverlappingContours(contours):
    res = contours[:]

    while(True):
        changeMade = False

        for i in range(0, len(res)):
            if(changeMade):
                break

            for j in range(0, len(res)):
                if(changeMade):
                    break

                if(i == j):
                    continue

                c1 = res[i]
                c2 = res[j]

                if(hasIntersection(c1, c2)):
                    x1 = min(c1[0], c2[0])
                    y1 = min(c1[1], c2[1])

                    x2 = max(c1[0] + c1[2], c2[0] + c2[2])
                    y2 = max(c1[1] + c1[3], c2[1] + c2[3])

                    res[i] = (x1, y1, x2 - x1, y2 - y1)
                    del res[j]

                    changeMade = True

        if(not changeMade):
            break

    return res

def getContourSizeForOffset(offset):
    min = 5
    max = 20

    return min + (offset / (480 / (max - min)))

def isValid(contour):
    size = getContourSizeForOffset(contour[1])
    return contour[1] >= size and contour[2] >= size

def findVehicles(img):
    im, contours, hierarchy = cv2.findContours(
        img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_TC89_L1)

    matches = []

    for contour in contours:
        (x, y, w, h) = cv2.boundingRect(contour)

        if not isValid((x, y, w, h)):
            continue

        matches.append((x, y, w, h))

    matches = mergeOverlappingContours(matches)
    return matches

def applyMask(l_img, s_img):
    y_offset = 0
    x_offset = 0

    y1, y2 = y_offset, y_offset + s_img.shape[0]
    x1, x2 = x_offset, x_offset + s_img.shape[1]

    alpha_s = s_img[:, :, 3] / 255.0
    alpha_l = 1.0 - alpha_s

    for c in range(0, 3):
        l_img[y1:y2, x1:x2, c] = (alpha_s * s_img[:, :, c] + alpha_l * l_img[y1:y2, x1:x2, c])

def parseImageWithMask(img, bgs, mask):
    target = img.copy()

    med = cv2.GaussianBlur(target, (5, 5), 1.2)
    applyMask(med, mask)
    applyMask(target, mask)

    image = bgs.apply(med)

    fgthres = cv2.threshold(image.copy(), 200, 255, cv2.THRESH_BINARY)[1]

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))
    closing = cv2.morphologyEx(fgthres, cv2.MORPH_CLOSE, kernel)
    opening = cv2.morphologyEx(closing, cv2.MORPH_OPEN, kernel)
    dilation = cv2.dilate(opening, kernel, iterations=2)

    img_dilation = dilation

    vehicles = findVehicles(img_dilation)

    for vehicle in vehicles:
        cv2.rectangle(target, (vehicle[0], vehicle[1]), (vehicle[0] +
                                                         vehicle[2], vehicle[1] + vehicle[3]), (0, 255, 0))

    text = "Kein Stau"
    color = (0, 255, 0)
    stau = False
    if len(vehicles) >= 10:
        text = "Stau"
        color = (0,0,255)
        stau = True
    cv2.putText(target,text,(10,100), cv2.FONT_HERSHEY_SIMPLEX, 2,color,2,cv2.LINE_AA)

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

        self.masks = [
            cv2.imread("cam-srv/data/mask/" + self.camera + "/left.png", -1),
            cv2.imread("cam-srv/data/mask/" + self.camera + "/right.png", -1),
        ]

    def process(self, image):
        data = numpy.fromstring(image["data"], dtype=numpy.uint8)
        img = cv2.imdecode(data, 1)

        return parseImage(img, self.bgs[0], self.bgs[1], self.masks)
        #cv2.imshow("hi - " + self.camera, img)
        #cv2.waitKey(0)
