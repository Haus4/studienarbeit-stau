import os
import cv2
import json
import time
import image_utils
import email.utils as eut

def loadCameras():
    path = "data/"
    cameras = [f for f in os.listdir(path) if os.path.isdir(os.path.join(path, f))]
    return cameras

def loadImage(camera, name):
    path = "data/" + camera
    image = {}

    image["data"] = cv2.imread(os.path.join(path, name + ".jpg"))

    f = open(os.path.join(path, name + ".json"), "r")
    info = f.read()
    f.close()

    image["info"] = json.loads(info)

    image["info"]["time_parsed"] = eut.parsedate(image["info"]["time"])
    image["info"]["timestamp"] = time.mktime(image["info"]["time_parsed"])

    image["name"] = name

    return image

def loadImages(camera):
    path = "data/" + camera
    files = [f.replace(".jpg", "") for f in os.listdir(path) if os.path.isfile(os.path.join(path, f)) and f.endswith(".jpg") and os.path.isfile(os.path.join(path, f.replace(".jpg", ".json")))]
    images = [loadImage(camera, file) for file in files]
    images.sort(key=lambda x: x["info"]["timestamp"])
    images = [image for image in images if not "verified" in image["info"] or not image["info"]["verified"]]

    return images

def buildImageList(cameras):
    imageList = []

    for camera in cameras:
        images = {}
        images["index"] = 0
        images["camera"] = camera
        images["images"] = loadImages(camera)
        images["masks"] = image_utils.loadMasks(camera)
        imageList.append(images)

    return imageList

def validateSingle(image, mask, info, name):
    img = image.copy()

    image_utils.applyMask(img, mask)

    text = "Kein Stau"
    color = (0, 255, 0)
    if info["jam"]:
        text = "Stau"
        color = (0,0,255)
        stau = True

    cv2.putText(img,text,(10,100), cv2.FONT_HERSHEY_SIMPLEX, 2,color,2,cv2.LINE_AA)

    cv2.imshow(name, img)

    key = cv2.waitKey(0) & 0xFF
    cv2.destroyAllWindows()

    if key == 13: # Enter - Stau
        print("Stau")
        return True

    print("Kein Stau")
    return False


def validatePair(image, masks):
    img = image["data"].copy()

    left = validateSingle(image["data"], masks[0], image["info"]["left"], image["name"])
    right = validateSingle(image["data"], masks[1], image["info"]["right"], image["name"])

    image["info"]["left"]["jam"] = left
    image["info"]["right"]["jam"] = right

    image["info"]["verified"] = True

def saveMetadata(info, name, camera):
    info = json.dumps(info, indent=4)

    f = open("data/" + camera + "/" + name + ".json", "w")
    f.write(info)
    f.close()

def tryValidateForCam(imageList, index):
    camImgList = imageList[index]
    index = camImgList["index"]
    camImgList["index"] = index + 1

    images = camImgList["images"]

    if len(images) <= index:
        return False

    image = images[index]

    print("Processing " + camImgList["camera"] + " " + image["name"])
    validatePair(image, camImgList["masks"])
    saveMetadata(image["info"], image["name"], camImgList["camera"])

    return True

def validateNextImage(imageList, counter):
    initialCounter = counter

    while counter < initialCounter + len(imageList):
        index = counter % len(imageList)
        counter += 1

        if tryValidateForCam(imageList, index):
            return True

    return False


def main():
    cameras = loadCameras()
    imageList = buildImageList(cameras)

    counter = 0

    while True:
        res = validateNextImage(imageList, counter)
        counter += 1
        if not res:
            break
    
    print("Done validating")

main()