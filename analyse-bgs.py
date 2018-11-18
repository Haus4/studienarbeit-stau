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

def parseImageWithMask(name, img, bgs, i, total, mask):
    target = img.copy()

    med = cv2.GaussianBlur(target, (5, 5), 1.2)
    applyMask(med, mask)
    applyMask(target, mask)

    image = bgs.apply(med)

    # Remove shadows
    fgthres = cv2.threshold(image.copy(), 200, 255, cv2.THRESH_BINARY)[1]

    #kernel = numpy.ones((2, 2), numpy.uint8)
    #img_erosion = cv2.erode(fgthres, kernel, iterations=1)
    #img_dilation = cv2.dilate(img_erosion, kernel, iterations=1)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))
    closing = cv2.morphologyEx(fgthres, cv2.MORPH_CLOSE, kernel)
    opening = cv2.morphologyEx(closing, cv2.MORPH_OPEN, kernel)
    dilation = cv2.dilate(opening, kernel, iterations=2)

    img_dilation = dilation

    vehicles = findVehicles(img_dilation)

    for vehicle in vehicles:
        cv2.rectangle(target, (vehicle[0], vehicle[1]), (vehicle[0] +
                                                         vehicle[2], vehicle[1] + vehicle[3]), (0, 255, 0))

    #oldVehicles = vehicles
    #vehicles = []

    #for vehicle in oldVehicles:
    #    if vehicle[1] >= 150:
    #        vehicles.append(vehicle)

    text = "Kein Stau"
    color = (0, 255, 0)
    stau = False
    if len(vehicles) >= 14:
        text = "Stau"
        color = (0,0,255)
        stau = True
    cv2.putText(target,text,(10,100), cv2.FONT_HERSHEY_SIMPLEX, 2,color,2,cv2.LINE_AA)

    #print(bgs)

    if total - i < 2 or stau:
        cv2.imshow(name + " " + str(i) + " Veh: " + str(len(vehicles)), target)
        #cv2.imshow(name + " bg", bgs.getBackgroundImage())
        #cv2.imshow(name + str(i) + " fg", img_dilation)

    return stau

def parseImage(name, img, bgs, bgs2, i, total, masks):
    left = parseImageWithMask(name, img[1], bgs, i, total, masks[0])
    right = parseImageWithMask(name, img[1], bgs2, i, total, masks[1])
    print("\"" + img[0] + "\": [" + str(left) + ", " + str(right) + "],")

def doBGWork(name, bgs, bgs2, images, masks):
    print(name)
    for i, image in enumerate(images):
        parseImage(name, image, bgs, bgs2, i, len(images), masks)

def doCam(cam):
    mypath = "cam-srv/data/raw/" + cam
    onlyfiles = [f for f in os.listdir(
        mypath) if os.path.isfile(os.path.join(mypath, f))]

    images = []
    for file in onlyfiles:
        path = os.path.join(mypath, file)
        image = cv2.imread(path)
        images.append((file, image))

    masks = [
        cv2.imread("cam-srv/data/mask/" + cam + "/left.png", -1),
        cv2.imread("cam-srv/data/mask/" + cam + "/right.png", -1),
    ]

    bgWorkers = [
        #["GSOC", cv2.bgsegm.createBackgroundSubtractorGSOC()],
        #["MOG2", cv2.createBackgroundSubtractorMOG2()], # good, but not as good as mog
        #["KNN", cv2.createBackgroundSubtractorKNN()],
        #["CNT", cv2.bgsegm.createBackgroundSubtractorCNT()],
        #["GMG", cv2.bgsegm.createBackgroundSubtractorGMG()],
        #["LSBP", cv2.bgsegm.createBackgroundSubtractorLSBP()],
        ["MOG", cv2.bgsegm.createBackgroundSubtractorMOG(), cv2.bgsegm.createBackgroundSubtractorMOG()], # seems to be the best, by far
    ]

    for worker in bgWorkers:
        doBGWork(cam + " " + worker[0], worker[1], worker[2], images, masks)

def main():
    doCam("KA091")
    #doCam("RLP825")
    doCam("KA061")
    doCam("KA041")
    doCam("KA151")

    cv2.waitKey(0)
    cv2.destroyAllWindows()


# if __name__ == "__main__":
main()
