import os
import urllib2
import email.utils as eut
import json

import process_bgs

from threading import Thread, Lock
from time import sleep

relevantCams = [
    "KA091",
    "KA061",
    "KA041",
    "KA151"
]

printMutex = Lock()

def printLocked(text):
    printMutex.acquire()
    try:
        print(text)
    finally:
        printMutex.release()

def getImageUrl(camera):
    return "http://www.svz-bw.de/kamera/ftpdata/" + camera + "/" + camera + "_gross.jpg"

def downloadImage(camera):
    url = getImageUrl(camera)
    image = {}
    image["url"] = url
    image["valid"] = False

    try:
        request = urllib2.Request(url, headers={"Referer" : "https://www.svz-bw.de"})
        response = urllib2.urlopen(request)

        image["data"] = response.read()

        image["time_raw"] = response.info().getheader('Last-Modified')
        t = eut.parsedate(image["time_raw"])
        image["time"] = t

        image["time_str"] = str(t[0]) + "-" + str(t[1]) + "-" + str(t[2]) + "-" + str(t[3]) + "-" + str(t[4]) + "-" + str(t[5])
        image["valid"] = True

        printLocked("Downloaded image for " + camera)
    except:
        printLocked("Failed to download image for " + camera)
        image["valid"] = False
    
    return image

def createFolder(folder):
    if not os.path.exists(folder):
        os.makedirs(folder)

def saveImage(container, image):
    createFolder("data/" + container["camera"])

    f = open("data/" + container["camera"] + "/" + image["time_str"] + ".jpg", "wb")
    f.write(image["data"])
    f.close()

def processImage(container, image):
    saveImage(container, image)
    result = container["processor"].processRaw(image["data"])

    data = {}
    data["left"] = result[0]
    data["right"] = result[1]
    data["time"] = image["time_raw"]
    data["verified"] = False

    info = json.dumps(data, indent=4)

    f = open("data/" + container["camera"] + "/" + image["time_str"] + ".json", "w")
    f.write(info)
    f.close()

    printMutex.acquire()
    try:
        print("Image parsed for " + container["camera"])
        print("\tL: Jam = " + str(result[0]["jam"]) + " Vehicles = " + str(result[0]["vehicles"]))
        print("\tR: Jam = " + str(result[1]["jam"]) + " Vehicles = " + str(result[1]["vehicles"]))
    finally:
        printMutex.release()

def handleCamera(camera):
    container = {}
    container["camera"] = camera
    container["last"] = None
    container["valid"] = None
    container["processor"] = process_bgs.ImageProcessor(camera)

    while(True):
        printLocked("Downloading image for " + camera)
        image = downloadImage(camera)

        if image["valid"] == True:
            if(container["valid"] is None or container["valid"]["time_str"] != image["time_str"]):
                processImage(container, image) # Only process if it's a new image

            container["valid"] = image

        container["last"] = image
        sleep(60)

def main():
    threads = []

    for cam in relevantCams:
        thread = Thread(target = handleCamera, args = (cam, ))
        thread.start()
        threads.append(thread)

    for thread in threads:
        thread.join()

main()
