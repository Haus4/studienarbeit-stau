import os
import cv2
import json
import time
import email.utils as eut
from threading import Lock

import return_thread

import process_null
import process_bgs

# --- CONFIGURE HERE-------

skipImages = 5 # Images to skip from evaluation to train the processor
onlyVerified = True # Evaluate only verified images

imageProcessors = [
    [process_null.ImageProcessor, "No-Jam-Null"],
    [process_bgs.ImageProcessor, "Background-Subtraction"],
]

# -------------------------

printMutex = Lock()

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

    return image

def buildGroups(images):
    group = []
    groups = []

    for image in images:
        if len(group) > 0:
            lastTs = group[len(group) - 1]["info"]["timestamp"]
            thisTs = image["info"]["timestamp"]
            
            if (thisTs - lastTs) > (60 * 10): #if more than 10 minutes appart, split group
                groups.append(group)
                group = []
        
        group.append(image)


    if len(group) > 0:
        groups.append(group)

    return groups

def loadImages(camera):
    path = "data/" + camera
    files = [f.replace(".jpg", "") for f in os.listdir(path) if os.path.isfile(os.path.join(path, f)) and f.endswith(".jpg") and os.path.isfile(os.path.join(path, f.replace(".jpg", ".json")))]
    images = [loadImage(camera, file) for file in files]
    images.sort(key=lambda x: x["info"]["timestamp"])

    return buildGroups(images)

def buildImageList(cameras):
    imageList = []

    for camera in cameras:
        images = {}
        images["camera"] = camera
        images["groups"] = loadImages(camera)
        imageList.append(images)

    return imageList

def evaluateImage(processor, image):
    result = processor.process(image["data"])

    leftResult = result[0]["jam"] == image["info"]["left"]["jam"]
    rightResult = result[1]["jam"] == image["info"]["right"]["jam"]

    accuracy = 0

    if leftResult:
        accuracy += 0.5

    if rightResult:
        accuracy += 0.5

    return accuracy
    

def analyzeGroup(camera, group, proc):
    processor = proc[0](camera)
    result = [[evaluateImage(processor, image), image] for image in group]
    result = [res[0] for i, res in enumerate(result) if i >= skipImages and (not onlyVerified or ("verified" in res[1]["info"] and res[1]["info"]["verified"]))]

    return result

def analyzeCamera(data, proc):
    camera = data["camera"]
    groups = data["groups"]

    result = [return_thread.start(target=analyzeGroup, args=(camera, group, proc,)) for group in groups]
    result = [t.join() for t in result]
    result = reduce(lambda x,y: x + y, result, [])
    accuracy = reduce(lambda x,y: x + y, result, 0)

    return [camera, accuracy, len(result), result]

def buildStatisticSummary(res):
    data = [x for x in res if x[2] > 0]

    if len(data) <= 0:
        print("\tNo result")
        return 0.0

    totalAccuracy = 0
    totalCount = 0

    for x in data:
        accuracy = round((x[1] * 100) / x[2], 2)
        totalAccuracy += x[1]
        totalCount += x[2]
        print("\t" + x[0] + ": " + str(accuracy) + "%")

    totalAccuracy *= 100
    totalAccuracy /= totalCount
    totalperc = round(totalAccuracy, 2)
    print("\tTotal: " + str(totalperc) + "%")

    return totalperc
    

def generateStatisticsForProcessor(proc, imageList):

    data = [return_thread.start(target=analyzeCamera, args=(x, proc,)) for x in imageList]
    data = [t.join() for t in data]

    printMutex.acquire()
    try:
        print("Statistics for " + proc[1])
        res = buildStatisticSummary(data)
        print("")
    finally:
        printMutex.release()

    return res

def generateStatistics(imageList):
    accuracy = 0.0
    proc = None

    result = [[return_thread.start(target=generateStatisticsForProcessor, args=(imageProcessor, imageList,)), imageProcessor] for imageProcessor in imageProcessors]

    for x in result:
        res = x[0].join()

        if accuracy < res:
            accuracy = res
            proc = x[1]

    if proc is not None:
        print("Optimal processor: " + proc[1] + " (" + str(accuracy) + "%)")
    else:
        print("No good processor found")


def main():
    cameras = loadCameras()
    imageList = buildImageList(cameras)
    generateStatistics(imageList)

main()