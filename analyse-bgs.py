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


def findVehicles(img, min_width, min_height):
    im, contours, hierarchy = cv2.findContours(
        img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_TC89_L1)

    matches = []

    for contour in contours:
        (x, y, w, h) = cv2.boundingRect(contour)
        contour_valid = (w >= min_width) and (
            h >= min_height)

        if not contour_valid:
            continue

        matches.append((x, y, w, h))

    matches = mergeOverlappingContours(matches)
    return matches


def doBGWork(name, bgs, images):
    print(name)
    for image in images[:-1]:
        med = cv2.GaussianBlur(image, (5, 5), 1.2)
        bgs.apply(med)

    target = images[-1].copy()

    med = cv2.GaussianBlur(target, (5, 5), 1.2)
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

    vehicles = findVehicles(img_dilation, 2, 2)

    for vehicle in vehicles:
        cv2.rectangle(target, (vehicle[0], vehicle[1]), (vehicle[0] +
                                                         vehicle[2], vehicle[1] + vehicle[3]), (0, 255, 0))

    if len(vehicles) >= 10:
        print("Stau")
    else:
        print("Kein Stau")

    #print(bgs)

    cv2.imshow(name, target)
    #cv2.imshow(name + " bg", bgs.getBackgroundImage())
    cv2.imshow(name + " fg", img_dilation)


def main():
    mypath = "cam-srv/data/raw/KA061"
    onlyfiles = [f for f in os.listdir(
        mypath) if os.path.isfile(os.path.join(mypath, f))]

    images = []
    for file in onlyfiles:
        path = os.path.join(mypath, file)
        image = cv2.imread(path)
        images.append(image)

    bgWorkers = [
        #["GSOC", cv2.bgsegm.createBackgroundSubtractorGSOC()],
        #["MOG2", cv2.createBackgroundSubtractorMOG2()], # good, but not as good as mog
        #["KNN", cv2.createBackgroundSubtractorKNN()],
        #["CNT", cv2.bgsegm.createBackgroundSubtractorCNT()],
        #["GMG", cv2.bgsegm.createBackgroundSubtractorGMG()],
        #["LSBP", cv2.bgsegm.createBackgroundSubtractorLSBP()],
        ["MOG", cv2.bgsegm.createBackgroundSubtractorMOG()], # seems to be the best, by far
    ]

    for worker in bgWorkers:
        doBGWork(worker[0], worker[1], images)

    cv2.waitKey(0)
    cv2.destroyAllWindows()


# if __name__ == "__main__":
main()
