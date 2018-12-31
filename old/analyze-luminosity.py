import os
import cv2
import numpy


def evaluateImage(img):
    mat = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY);
    res = cv2.calcHist([img], [0], None, [256], [0, 256])

    medium = 0.0
    total = 0.0
    for i, val in enumerate(res):
        medium += i * val[0]
        total += val[0]

    medium /= total
    print(medium)

def main():
    mypath="data_abend/raw/KA061"
    onlyfiles=[f for f in os.listdir(
        mypath) if os.path.isfile(os.path.join(mypath, f))]

    images=[]
    for file in onlyfiles:
        path=os.path.join(mypath, file)
        image=cv2.imread(path)
        images.append(image)

    for image in images[:-1]:
        evaluateImage(image)

    # cv2.waitKey(0)
    # cv2.destroyAllWindows()


# if __name__ == "__main__":
main()
