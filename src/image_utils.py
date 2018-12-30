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

def loadMasks(camera):
    return [
            cv2.imread("cam-srv/data/mask/" + camera + "/left.png", -1),
            cv2.imread("cam-srv/data/mask/" + camera + "/right.png", -1),
        ]